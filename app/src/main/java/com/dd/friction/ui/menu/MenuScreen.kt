package com.dd.friction.ui.menu

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dd.friction.R
import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.ui.components.PlainTextButton

private const val GRID_COLUMNS = 3
private val IconSize = 40.dp
private val GrayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun MenuScreen(
    onOpenGate: (InstalledApp) -> Unit,
    onOpenSettings: () -> Unit,
    onLaunched: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // Distractions always go through the gate; everything else opens directly.
    val onAppClick: (MenuItem) -> Unit = { item ->
        if (item.isDistraction) {
            onOpenGate(item.app)
        } else if (viewModel.launch(item.app)) {
            onLaunched()
        }
    }

    MenuContent(
        state = state,
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        onAppClick = onAppClick,
        onAppLongClick = { item ->
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.markAsDistraction(item.app)
            Toast.makeText(
                context,
                context.getString(R.string.menu_marked_distraction, item.app.label),
                Toast.LENGTH_SHORT,
            ).show()
        },
        onOpenSettings = onOpenSettings,
        loadIcon = viewModel::loadIcon,
    )
}

@Composable
private fun MenuContent(
    state: MenuUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onAppClick: (MenuItem) -> Unit,
    onAppLongClick: (MenuItem) -> Unit,
    onOpenSettings: () -> Unit,
    loadIcon: suspend (InstalledApp, Int) -> Bitmap?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            SearchField(
                value = query,
                onValueChange = onQueryChange,
                onDone = { state.exactMatch?.let(onAppClick) },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(20.dp))
            PlainTextButton(
                text = stringResource(R.string.menu_settings),
                onClick = onOpenSettings,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        state.exactMatch?.let { match ->
            PlainTextButton(
                text = stringResource(R.string.menu_open_match, match.app.label),
                onClick = { onAppClick(match) },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            items(items = state.apps, key = { it.app.key }) { item ->
                AppCell(
                    item = item,
                    showIcon = state.showIcons,
                    onClick = { onAppClick(item) },
                    onLongClick = { onAppLongClick(item) },
                    loadIcon = loadIcon,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textStyle = MaterialTheme.typography.bodyLarge
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onBackground),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Column {
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.menu_search_hint),
                            style = textStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(
    item: MenuItem,
    showIcon: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    loadIcon: suspend (InstalledApp, Int) -> Bitmap?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
    ) {
        if (showIcon) {
            AppIcon(app = item.app, loadIcon = loadIcon)
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = item.app.label,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        // Fixed-height slot so marked and unmarked cells line up in the grid.
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(DistractionDotSize)
                .background(
                    color = if (item.isDistraction) MaterialTheme.colorScheme.onSurfaceVariant else Color.Transparent,
                    shape = CircleShape,
                ),
        )
    }
}

private val DistractionDotSize = 5.dp

@Composable
private fun AppIcon(
    app: InstalledApp,
    loadIcon: suspend (InstalledApp, Int) -> Bitmap?,
) {
    val sizePx = with(LocalDensity.current) { IconSize.roundToPx() }
    val bitmap by produceState<Bitmap?>(initialValue = null, app.key, sizePx) {
        value = loadIcon(app, sizePx)
    }
    val imageBitmap = remember(bitmap) { bitmap?.asImageBitmap() }

    Box(Modifier.size(IconSize)) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                colorFilter = GrayscaleFilter,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
