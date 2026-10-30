package com.dd.friction.ui.home

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dd.friction.R
import com.dd.friction.domain.model.EssentialApp
import com.dd.friction.ui.components.PlainTextButton
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenMenu: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshEssentials() }
    // A home screen has nothing behind it.
    BackHandler {}

    HomeContent(
        state = state,
        onEssentialClick = viewModel::onEssentialClick,
        onOpenMenu = onOpenMenu,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onEssentialClick: (EssentialApp) -> Unit,
    onOpenMenu: () -> Unit,
) {
    val context = LocalContext.current
    val timePattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
    val timeFormatter = DateTimeFormatter.ofPattern(timePattern, Locale.getDefault())
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
    val opensText = if (state.opensToday == 0) {
        stringResource(R.string.home_no_distractions)
    } else {
        context.resources.getQuantityString(R.plurals.home_opens_today, state.opensToday, state.opensToday)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        Spacer(Modifier.weight(1f))

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

        state.essentials.forEach { app ->
            PlainTextButton(
                text = stringResource(app.labelRes()),
                onClick = { onEssentialClick(app) },
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Spacer(Modifier.height(32.dp))

        PlainTextButton(
            text = stringResource(R.string.home_menu),
            onClick = onOpenMenu,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@StringRes
private fun EssentialApp.labelRes(): Int = when (this) {
    EssentialApp.Phone -> R.string.essential_phone
    EssentialApp.Messages -> R.string.essential_messages
    EssentialApp.WhatsApp -> R.string.essential_whatsapp
}
