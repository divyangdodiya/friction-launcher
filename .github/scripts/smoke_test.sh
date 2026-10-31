#!/usr/bin/env bash
# Installs the release APK on a running emulator and walks the main screens,
# failing if the app crashes. Catches R8/minification problems that only appear at runtime.
set -euo pipefail

APK="$1"
PKG="com.dd.friction"
OUT="smoke-screens"
mkdir -p "$OUT"

log() { echo "::group::$1"; }
endlog() { echo "::endgroup::"; }

screen_size() { adb shell wm size | awk -F'[ x]' '/Physical/ {print $3, $4}'; }
read -r WIDTH HEIGHT < <(screen_size)

assert_alive() {
    if ! adb shell pidof "$PKG" >/dev/null; then
        echo "::error::Friction is not running after: $1"
        adb logcat -d -s AndroidRuntime:E | tail -n 60
        exit 1
    fi
    if adb logcat -d -s AndroidRuntime:E | grep -q "Process: $PKG"; then
        echo "::error::Friction crashed during: $1"
        adb logcat -d -s AndroidRuntime:E | tail -n 60
        exit 1
    fi
    echo "ok: $1"
}

shot() { adb exec-out screencap -p > "$OUT/$1.png"; }

# Taps the centre of the first on-screen element whose text matches exactly.
tap_text() {
    adb shell uiautomator dump /sdcard/ui.xml >/dev/null
    adb pull /sdcard/ui.xml "$OUT/ui.xml" >/dev/null
    local point
    point=$(python3 - "$OUT/ui.xml" "$1" <<'EOF'
import re, sys, xml.etree.ElementTree as ET
for node in ET.parse(sys.argv[1]).iter("node"):
    if node.get("text") == sys.argv[2]:
        x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
        print((x1 + x2) // 2, (y1 + y2) // 2)
        break
EOF
)
    if [ -z "$point" ]; then
        echo "::error::Could not find '$1' on screen"
        exit 1
    fi
    adb shell input tap $point
    sleep 2
}

screen_has() {
    adb shell uiautomator dump /sdcard/ui.xml >/dev/null
    adb pull /sdcard/ui.xml "$OUT/ui.xml" >/dev/null
    grep -q "text=\"$1\"" "$OUT/ui.xml"
}

log "Install and launch"
adb install -r "$APK"
adb logcat -c
adb shell am start -W -n "$PKG/.MainActivity"
sleep 5
assert_alive "launch"
shot 01-home
endlog

log "Swipe up opens the menu"
adb shell input swipe $((WIDTH / 2)) $((HEIGHT * 8 / 10)) $((WIDTH / 2)) $((HEIGHT * 2 / 10)) 200
sleep 2
assert_alive "open menu"
screen_has "Type the full app name" || { echo "::error::Menu did not open"; exit 1; }
shot 02-menu
endlog

log "Settings and home app picker"
tap_text "settings"
assert_alive "open settings"
shot 03-settings
tap_text "+ add app"
assert_alive "open home app picker"
shot 04-picker
adb shell input keyevent KEYCODE_BACK
sleep 1
adb shell input keyevent KEYCODE_BACK
sleep 1
endlog

log "Gate for a distraction app"
if adb shell pm list packages | grep -q "package:com.android.chrome$"; then
    tap_text "Type the full app name"
    adb shell input text "Chrome"
    sleep 1
    tap_text "Open Chrome"
    assert_alive "open gate"
    screen_has "You are about to open" || { echo "::error::Gate did not open for Chrome"; exit 1; }
    shot 05-gate
    tap_text "Never mind"
    assert_alive "leave gate"
else
    echo "::notice::Chrome is not installed on this image; gate check skipped."
fi
endlog

log "Home button returns home"
adb shell input keyevent KEYCODE_HOME
sleep 2
shot 06-home-again
assert_alive "return home"
endlog

echo "Smoke test passed."
