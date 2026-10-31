package com.dd.friction.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dd.friction.R
import com.dd.friction.domain.model.FrictionSettings
import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.domain.model.SettingChange
import com.dd.friction.domain.model.UnlockPhrase
import com.dd.friction.ui.components.PlainTextButton
import com.dd.friction.ui.model.AppItem

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SettingsContent(
        state = state,
        onBack = onBack,
        onRequest = viewModel::request,
        onAddHomeApp = viewModel::openHomeAppPicker,
        onRemoveHomeApp = viewModel::removeHomeApp,
        onOpenHomeSettings = {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        },
    )

    if (viewModel.isPickingHomeApp) {
        HomeAppPicker(
            candidates = state.homeCandidates,
            onPick = viewModel::addHomeApp,
            onDismiss = viewModel::closeHomeAppPicker,
        )
    }

    viewModel.pendingChange?.let { change ->
        ConfirmDialog(
            change = change,
            text = viewModel.confirmationText,
            isValid = viewModel.isConfirmationValid,
            onTextChange = viewModel::onConfirmationChange,
            onConfirm = viewModel::confirm,
            onDismiss = viewModel::dismiss,
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onRequest: (SettingChange) -> Unit,
    onAddHomeApp: () -> Unit,
    onRemoveHomeApp: (InstalledApp) -> Unit,
    onOpenHomeSettings: () -> Unit,
) {
    val settings = state.settings
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                PlainTextButton(
                    text = stringResource(R.string.settings_done),
                    onClick = onBack,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SectionTitle(stringResource(R.string.settings_home_apps_title))
            Text(
                text = stringResource(
                    R.string.settings_home_apps_hint,
                    state.homeApps.size,
                    FrictionSettings.MAX_HOME_APPS,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            if (state.homeApps.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_home_apps_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }

        items(items = state.homeApps, key = { "home:${it.app.packageName}" }) { item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                AppLabel(item = item, modifier = Modifier.weight(1f))
                PlainTextButton(
                    text = stringResource(R.string.settings_remove),
                    onClick = { onRemoveHomeApp(item.app) },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            if (state.canAddHomeApp) {
                PlainTextButton(
                    text = stringResource(R.string.settings_home_apps_add),
                    onClick = onAddHomeApp,
                )
            } else {
                Text(
                    text = stringResource(R.string.settings_home_apps_full),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }

        item {
            SectionTitle(stringResource(R.string.settings_delay_title))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                FrictionSettings.DELAY_OPTIONS.forEach { seconds ->
                    val selected = seconds == settings.delaySeconds
                    Text(
                        text = stringResource(R.string.settings_delay_option, seconds),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .clickable(role = Role.RadioButton) { onRequest(SettingChange.Delay(seconds)) }
                            .padding(vertical = 8.dp),
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            ToggleRow(
                label = stringResource(R.string.settings_intention),
                checked = settings.requireIntention,
                onCheckedChange = { onRequest(SettingChange.RequireIntention(it)) },
            )
            ToggleRow(
                label = stringResource(R.string.settings_icons),
                checked = settings.showIcons,
                onCheckedChange = { onRequest(SettingChange.ShowIcons(it)) },
            )
        }

        item {
            SectionTitle(stringResource(R.string.settings_distractions_title))
            Text(
                text = stringResource(R.string.settings_distractions_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            if (state.distractions.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_distractions_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }

        items(items = state.distractions, key = { "distraction:${it.packageName}" }) { app ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                PlainTextButton(
                    text = stringResource(R.string.settings_remove),
                    onClick = { onRequest(SettingChange.RemoveDistraction(app.packageName, app.label)) },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SectionTitle(stringResource(R.string.settings_home_title))
            PlainTextButton(
                text = stringResource(R.string.settings_home_action),
                onClick = onOpenHomeSettings,
            )
        }
    }
}

/** App name, with a muted tag when it is a distraction. */
@Composable
private fun AppLabel(item: AppItem, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(
            text = item.app.label,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (item.isDistraction) {
            Text(
                text = stringResource(R.string.settings_distraction_tag),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun HomeAppPicker(
    candidates: List<AppItem>,
    onPick: (InstalledApp) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_home_apps_pick)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = PickerMaxHeight)) {
                items(items = candidates, key = { it.app.packageName }) { item ->
                    AppLabel(
                        item = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { onPick(item.app) }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

private val PickerMaxHeight = 420.dp

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 32.dp, bottom = 8.dp),
    )
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 12.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun ConfirmDialog(
    change: SettingChange,
    text: String,
    isValid: Boolean,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_title)) },
        text = {
            Column {
                Text(change.description(), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.confirm_instruction),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = UnlockPhrase.TEXT,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = isValid) {
                Text(stringResource(R.string.confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.confirm_keep))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Composable
private fun SettingChange.description(): String = when (this) {
    is SettingChange.Delay -> stringResource(R.string.confirm_delay, seconds)
    is SettingChange.RequireIntention -> stringResource(R.string.confirm_intention_off)
    is SettingChange.ShowIcons -> stringResource(R.string.confirm_icons_on)
    is SettingChange.RemoveDistraction -> stringResource(R.string.confirm_remove, label)
    is SettingChange.AddHomeApp -> stringResource(R.string.confirm_home_add, label)
    is SettingChange.AddDistraction, is SettingChange.RemoveHomeApp -> ""
}
