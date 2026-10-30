package com.dd.friction.ui.gate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dd.friction.R

@Composable
fun GateScreen(
    onExit: () -> Unit,
    viewModel: GateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.startCountdown() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.stopCountdown() }

    GateContent(
        state = state,
        intention = viewModel.intention,
        onIntentionChange = viewModel::onIntentionChange,
        onOpen = { if (viewModel.open()) onExit() },
        onCancel = onExit,
    )
}

@Composable
private fun GateContent(
    state: GateUiState,
    intention: String,
    onIntentionChange: (String) -> Unit,
    onOpen: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(R.string.gate_opening),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = state.appLabel, style = MaterialTheme.typography.titleLarge)

        if (state.requireIntention) {
            Spacer(Modifier.height(40.dp))
            Text(text = stringResource(R.string.gate_question), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = intention,
                onValueChange = onIntentionChange,
                placeholder = { Text(stringResource(R.string.gate_hint)) },
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.weight(1f))

        // The safe choice is the big, obvious one.
        Button(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(text = stringResource(R.string.gate_cancel), style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onOpen,
            enabled = state.canOpen,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val label = if (state.secondsRemaining > 0) {
                stringResource(R.string.gate_open_in, state.secondsRemaining)
            } else {
                stringResource(R.string.gate_open)
            }
            Text(text = label)
        }
    }
}
