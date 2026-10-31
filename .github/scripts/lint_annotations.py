"""Turns Android lint XML results into GitHub annotations so issues show up on the run and PR."""

import os
import sys
import xml.etree.ElementTree as ET

LEVELS = {"Fatal": "error", "Error": "error", "Warning": "warning"}


def escape(text: str) -> str:
    return text.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


def main(report: str) -> None:
    if not os.path.exists(report):
        print(f"No lint report at {report}")
        return
    workspace = os.environ.get("GITHUB_WORKSPACE", os.getcwd()).rstrip("/") + "/"
    issues = ET.parse(report).getroot().iter("issue")
    # Errors first so the per-step annotation limit never hides them behind warnings.
    ordered = sorted(issues, key=lambda issue: issue.get("severity") not in ("Fatal", "Error"))
    for issue in ordered:
        level = LEVELS.get(issue.get("severity", ""))
        if level is None:
            continue
        location = issue.find("location")
        props = ""
        if location is not None:
            path = location.get("file", "").replace(workspace, "")
            props = f" file={path}"
            if location.get("line"):
                props += f",line={location.get('line')}"
        message = f"{issue.get('id')}: {issue.get('message', '')}"
        print(f"::{level}{props}::{escape(message)}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "app/build/reports/lint-results-release.xml")
