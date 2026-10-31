package com.dd.friction.ui.home

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dd.friction.R
import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.ui.components.PlainTextButton
import com.dd.friction.ui.model.AppItem
import java.time.format.DateTimeFormatter

private val SwipeThreshold = 64.dp

@Composable
fun HomeScreen(
    onOpenMenu: () -> Unit,
    onOpenGate: (InstalledApp) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // A home screen has nothing behind it.
    BackHandler {}

    HomeContent(
        state = state,
        // Distractions pinned to home still go through the gate.
        onAppClick = { item ->
            if (item.isDistraction) onOpenGate(item.app) else viewModel.launch(item.app)
        },
        onOpenMenu = onOpenMenu,
        onSwipeDown = viewModel::onSwipeDown,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onAppClick: (AppItem) -> Unit,
    onOpenMenu: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    val context = LocalContext.current
    // Locale comes from the configuration so a language change recomposes the clock.
    // The 24-hour setting is re-read on every minute tick.
    val locale = LocalConfiguration.current.locales[0]
    val is24Hour = DateFormat.is24HourFormat(context)
    val timeFormatter = remember(locale, is24Hour) {
        DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm", locale)
    }
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEEE, d MMMM", locale) }
    val opensText = if (state.opensToday == 0) {
        stringResource(R.string.home_no_distractions)
    } else {
        pluralStringResource(R.plurals.home_opens_today, state.opensToday, state.opensToday)
    }
    val currentOnOpenMenu by rememberUpdatedState(onOpenMenu)
    val currentOnSwipeDown by rememberUpdatedState(onSwipeDown)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalSwipes(
                onSwipeUp = { currentOnOpenMenu() },
                onSwipeDown = { currentOnSwipeDown() },
            )
            .safeDrawingPadding()
            .padding(horizontal = 32.dp, vertical = 16.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        Text(text = state.now.format(timeFormatter), style = MaterialTheme.typography.displayLarge)
        Text(
            text = state.now.format(dateFormatter),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = opensText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.weight(1f))
        Reminders()
        Spacer(Modifier.weight(1f))

        state.apps.forEach { item ->
            Text(
                text = item.app.label,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { onAppClick(item) }
                    .padding(vertical = 8.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        PlainTextButton(
            text = stringResource(R.string.home_menu),
            onClick = onOpenMenu,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Hard reminders in a scratched, distressed face, each line knocked slightly off angle
 * so they read like something carved into the screen, not a polite to-do list.
 */
@Composable
private fun Reminders() {
    Column {
        stringArrayResource(R.array.home_reminders).forEachIndexed { index, reminder ->
            Text(
                text = reminder,
                style = ReminderStyle,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .rotate(ReminderTilts[index % ReminderTilts.size]),
            )
        }
    }
}

private val ReminderStyle = TextStyle(
    fontFamily = FontFamily(Font(R.font.rubik_distressed)),
    fontSize = 32.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.5.sp,
)

private val ReminderTilts = listOf(-2.5f, 1.5f, -1f, 2.5f)

/**
 * Swipe up anywhere opens the menu; swipe down pulls the notification shade.
 * The detector is installed once, so callers pass lambdas that read current state.
 */
private fun Modifier.verticalSwipes(onSwipeUp: () -> Unit, onSwipeDown: () -> Unit): Modifier =
    pointerInput(Unit) {
        val threshold = SwipeThreshold.toPx()
        var totalDrag = 0f
        detectVerticalDragGestures(
            onDragStart = { totalDrag = 0f },
            onDragCancel = { totalDrag = 0f },
            onDragEnd = {
                when {
                    totalDrag < -threshold -> onSwipeUp()
                    totalDrag > threshold -> onSwipeDown()
                }
                totalDrag = 0f
            },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                totalDrag += dragAmount
            },
        )
    }
