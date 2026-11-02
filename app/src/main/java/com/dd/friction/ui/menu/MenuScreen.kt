package com.dd.friction.ui.menu

import android.graphics.Bitmap
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dd.friction.R
import com.dd.friction.domain.model.InstalledApp
import com.dd.friction.domain.model.SearchMode
import com.dd.friction.ui.components.PlainTextButton
import com.dd.friction.ui.model.AppItem

private const val GRID_COLUMNS = 3
private val IconSize = 40.dp
private val ResultIconSize = 32.dp

/** Stable ids exposed to UI Automator for the emulator smoke test. */
internal object MenuTestTags {
    const val SEARCH_FIELD = "search_field"
    const val SEARCH_RESULT = "search_result"
}
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
    val resources = LocalResources.current
    val haptics = LocalHapticFeedback.current

    // Distractions always go through the gate; everything else opens directly.
    val onAppClick: (AppItem) -> Unit = { item ->
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
                resources.getString(R.string.menu_marked_distraction, item.app.label),
                Toast.LENGTH_SHORT,
            ).show()
        },
        onOpenSettings = onOpenSettings,
        loadIcon = viewModel::loadIcon,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MenuContent(
    state: MenuUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onOpenSettings: () -> Unit,
    loadIcon: suspend (InstalledApp, Int) -> Bitmap?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { testTagsAsResourceId = true }
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
                hint = stringResource(state.searchMode.hintRes()),
                onValueChange = onQueryChange,
                // Enter opens the result only when there is no doubt which app is meant.
                onDone = { state.results.singleOrNull()?.let { onAppClick(it.item) } },
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

        if (state.isSearching) {
            SearchResults(
                results = state.results,
                searchMode = state.searchMode,
                onAppClick = onAppClick,
                loadIcon = loadIcon,
            )
        } else {
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
}

@Composable
private fun SearchField(
    value: String,
    hint: String,
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
        modifier = modifier.testTag(MenuTestTags.SEARCH_FIELD),
        decorationBox = { innerTextField ->
            Column {
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
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

/** Search hits as a list with grayscale icons, so look-alike names are easy to tell apart. */
@Composable
private fun SearchResults(
    results: List<SearchResult>,
    searchMode: SearchMode,
    onAppClick: (AppItem) -> Unit,
    loadIcon: suspend (InstalledApp, Int) -> Bitmap?,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        if (results.isEmpty()) {
            Text(
                text = stringResource(searchMode.noResultsRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
        results.forEach { result ->
            SearchResultRow(result = result, onClick = { onAppClick(result.item) }, loadIcon = loadIcon)
        }
    }
}

@Composable
private fun SearchResultRow(
    result: SearchResult,
    onClick: () -> Unit,
    loadIcon: suspend (InstalledApp, Int) -> Bitmap?,
) {
    val app = result.item.app
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MenuTestTags.SEARCH_RESULT)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        AppIcon(app = app, loadIcon = loadIcon, size = ResultIconSize)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (result.showPackage) {
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (result.item.isDistraction) {
            Spacer(Modifier.width(12.dp))
            DistractionDot(visible = true)
        }
    }
}

@StringRes
private fun SearchMode.hintRes(): Int =
    if (this == SearchMode.Exact) R.string.menu_search_hint_exact else R.string.menu_search_hint

@StringRes
private fun SearchMode.noResultsRes(): Int = when (this) {
    SearchMode.Exact -> R.string.menu_no_results_exact
    SearchMode.Smart -> R.string.menu_no_results_smart
    SearchMode.Free -> R.string.menu_no_results_free
}

@Composable
private fun DistractionDot(visible: Boolean) {
    Box(
        modifier = Modifier
            .size(DistractionDotSize)
            .background(
                color = if (visible) MaterialTheme.colorScheme.onSurfaceVariant else Color.Transparent,
                shape = CircleShape,
            ),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(
    item: AppItem,
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
        DistractionDot(visible = item.isDistraction)
    }
}

private val DistractionDotSize = 5.dp

@Composable
private fun AppIcon(
    app: InstalledApp,
    loadIcon: suspend (InstalledApp, Int) -> Bitmap?,
    size: Dp = IconSize,
) {
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    var imageBitmap by remember(app.key, sizePx) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(app.key, sizePx) {
        imageBitmap = loadIcon(app, sizePx)?.asImageBitmap()
    }

    Box(Modifier.size(size)) {
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
