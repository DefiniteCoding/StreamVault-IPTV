@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.streamvault.feature.catalog.presentation.search

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.core.ui.components.dialogs.AppDialog
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.BrowseShapes
import com.streamvault.core.ui.design.FocusOverflowPadding
import com.streamvault.core.ui.design.LocalAppSpacing
import com.streamvault.core.ui.design.ProvideClearOfFadeBringIntoView
import com.streamvault.core.ui.design.browsePrimaryButtonColors
import com.streamvault.core.ui.design.fadeWhenMoreContentVertically
import com.streamvault.core.ui.design.focusRingButtonBorder
import com.streamvault.core.ui.design.requestFocusSafely
import com.streamvault.core.ui.design.secondaryButtonColors
import com.streamvault.core.ui.design.secondarySurfaceColors
import com.streamvault.core.ui.design.selectableFocusBorder
import com.streamvault.core.ui.interaction.TvClickableSurface
import com.streamvault.feature.catalog.R

private val SheetWidth = 320.dp
private val SheetScrollFadeHeight = 16.dp
private const val FirstRowTolerancePx = 4f
private val ScrimColor = Color.Black.copy(alpha = 0.6f)
private const val SelectedChipFocusScale = 1.03f

@get:StringRes
private val SearchSort.labelRes: Int
    get() = when (this) {
        SearchSort.RELEVANCE -> R.string.search_sort_relevance
        SearchSort.NAME_ASC -> R.string.favorites_sort_title_alpha
        SearchSort.NEWEST -> R.string.search_sort_newest
        SearchSort.RATING -> R.string.search_sort_rating
    }

/** Live TV channels carry no date or rating, so only Relevance and A-Z apply there. */
private fun sortOptionsFor(tab: SearchTab): List<SearchSort> =
    if (tab == SearchTab.LIVE) {
        listOf(SearchSort.RELEVANCE, SearchSort.NAME_ASC)
    } else {
        SearchSort.values().toList()
    }

@Composable
internal fun SearchRefinementBar(
    refinements: SearchRefinements,
    onSortClick: () -> Unit,
    onFiltersClick: () -> Unit,
    sortFocusRequester: FocusRequester,
    filtersFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAppSpacing.current
    val activeFilters = refinements.filters.activeCount
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        SearchRefinementButton(
            text = stringResource(R.string.search_sort_button, stringResource(refinements.sort.labelRes)),
            trailingIcon = Icons.Default.ArrowDropDown,
            onClick = onSortClick,
            modifier = Modifier.focusRequester(sortFocusRequester)
        )
        SearchRefinementButton(
            text = if (activeFilters > 0) {
                stringResource(R.string.search_filters_button_active, activeFilters)
            } else {
                stringResource(R.string.search_filters_button)
            },
            onClick = onFiltersClick,
            modifier = Modifier.focusRequester(filtersFocusRequester)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SearchRefinementButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null
) {
    val spacing = LocalAppSpacing.current
    TvClickableSurface(
        modifier = modifier,
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = SelectedChipFocusScale),
        colors = secondarySurfaceColors(),
        border = selectableFocusBorder(CircleShape)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    // Matches the 18sp text line, so Sort and Filters are the same height.
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Right-anchored sheet. The platform dialog dim is turned off and one flat black scrim (60%, above the 55% floor
 * of rule 9) covers the whole window, so it has no edge that could cut through a focused pill.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SearchSheetHost(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(0f) }
        Box(modifier = Modifier.fillMaxSize().background(ScrimColor)) {
            Surface(
                shape = BrowseShapes.Panel,
                colors = SurfaceDefaults.colors(containerColor = BrowseColors.SurfaceElevated),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .widthIn(max = SheetWidth)
                    .fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun SearchSortSheet(
    tab: SearchTab,
    current: SearchSort,
    onSelect: (SearchSort) -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = LocalAppSpacing.current
    val initialFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { initialFocus.requestFocusSafely(tag = "SearchSortSheet", target = "Selected sort") }

    SearchSheetHost(onDismiss = onDismiss) {
        SheetScrollArea(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = spacing.lg - FocusOverflowPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Text(
                text = stringResource(R.string.search_sort_title),
                style = MaterialTheme.typography.titleMedium,
                color = BrowseColors.TextPrimary,
                modifier = Modifier.padding(bottom = spacing.xs)
            )
            sortOptionsFor(tab).forEach { sort ->
                SearchSortOptionRow(
                    label = stringResource(sort.labelRes),
                    selected = sort == current,
                    onClick = { onSelect(sort) },
                    modifier = if (sort == current) Modifier.focusRequester(initialFocus) else Modifier
                )
            }
            if (tab == SearchTab.ALL) {
                Text(
                    text = stringResource(R.string.search_sort_live_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrowseColors.TextSecondary,
                    modifier = Modifier.padding(top = spacing.xs)
                )
            }
        }
    }
}

/**
 * Scrolling body of a sheet. The edges fade while more content lies past them, and the focused row is scrolled
 * clear of the fade. [FocusOverflowPadding] keeps the focus ring and scale inside the offscreen fade layer, and is
 * taken out of the horizontal margin so rows still line up with the sheet title.
 */
@Composable
private fun SheetScrollArea(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalAppSpacing.current
    val scrollState = rememberScrollState()
    ProvideClearOfFadeBringIntoView(SheetScrollFadeHeight) {
        Box(
            modifier = modifier
                .padding(horizontal = spacing.lg - FocusOverflowPadding)
                .fadeWhenMoreContentVertically(scrollState, SheetScrollFadeHeight)
                .verticalScroll(scrollState)
                .focusGroup()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FocusOverflowPadding),
                verticalArrangement = verticalArrangement,
                content = content
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SearchSortOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAppSpacing.current
    val selectedLabel = stringResource(R.string.a11y_selected)
    TvClickableSurface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.selected = selected
                if (selected) stateDescription = selectedLabel
            },
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(BrowseShapes.Control),
        scale = ClickableSurfaceDefaults.scale(focusedScale = SelectedChipFocusScale),
        colors = refinementControlColors(selected),
        border = selectableFocusBorder(BrowseShapes.Control)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            RadioGlyph(selected = selected)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Drawn indicator inside the row; the row itself carries the focus ring, so this ring is not an interactive stroke. */
@Composable
private fun RadioGlyph(selected: Boolean) {
    val ringColor = if (selected) BrowseColors.OnSelectedContainer else BrowseColors.TextSecondary
    Box(
        modifier = Modifier
            .size(20.dp)
            .border(2.dp, ringColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(ringColor)
            )
        }
    }
}

@Composable
internal fun SearchFiltersSheet(
    initial: SearchFilters,
    options: SearchFilterOptions,
    unrefined: SearchResultSet,
    onApply: (filters: SearchFilters, matchCount: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = LocalAppSpacing.current
    var draft by remember { mutableStateOf(initial) }
    val matchCount = remember(draft, unrefined) {
        unrefined.refined(SearchRefinements(filters = draft)).total
    }
    // Opens on the first row, not Show results: the chips are what the user came to pick (overrides rule 17 here).
    val firstRowFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstRowFocus.requestFocusSafely(tag = "SearchFiltersSheet", target = "First filter row") }

    // Back applies the draft too, so leaving the sheet never silently throws away a chip the user picked.
    val applyAndClose = { onApply(draft, matchCount) }

    SearchSheetHost(onDismiss = applyAndClose) {
        // The scroll area carries FocusOverflowPadding itself, so the gaps here are smaller by that much.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs)
        ) {
            Text(
                text = stringResource(R.string.search_filters_button),
                style = MaterialTheme.typography.titleMedium,
                color = BrowseColors.TextPrimary,
                modifier = Modifier.padding(horizontal = spacing.lg)
            )
            SheetScrollArea(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(firstRowFocus),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                if (options.decades.isNotEmpty()) {
                    FilterGroup(
                        title = stringResource(R.string.search_filter_year),
                        anySelected = draft.decades.isEmpty(),
                        onAny = { draft = draft.copy(decades = emptySet()) }
                    ) {
                        options.decades.forEach { decade ->
                            RefinementChip(
                                text = decadeLabel(decade),
                                selected = decade in draft.decades,
                                onClick = { draft = draft.copy(decades = draft.decades.toggled(decade)) }
                            )
                        }
                    }
                }
                if (options.genres.isNotEmpty()) {
                    FilterGroup(
                        title = stringResource(R.string.search_filter_genre),
                        anySelected = draft.genres.isEmpty(),
                        onAny = { draft = draft.copy(genres = emptySet()) }
                    ) {
                        options.genres.forEach { genre ->
                            RefinementChip(
                                text = genre,
                                selected = draft.genres.any { it.equals(genre, ignoreCase = true) },
                                onClick = { draft = draft.copy(genres = draft.genres.toggledIgnoringCase(genre)) }
                            )
                        }
                    }
                }
                if (options.hasRatings) {
                    FilterGroup(
                        title = stringResource(R.string.search_filter_rating),
                        anySelected = draft.minRating == 0,
                        onAny = { draft = draft.copy(minRating = 0) }
                    ) {
                        SearchRatingThresholds.forEach { threshold ->
                            RefinementChip(
                                text = stringResource(R.string.search_filter_rating_min, threshold),
                                selected = draft.minRating == threshold,
                                onClick = { draft = draft.copy(minRating = threshold) }
                            )
                        }
                    }
                }
                if (options.categories.isNotEmpty()) {
                    FilterGroup(
                        title = stringResource(R.string.vod_filter_category_label),
                        anySelected = draft.categories.isEmpty(),
                        onAny = { draft = draft.copy(categories = emptySet()) }
                    ) {
                        options.categories.forEach { category ->
                            RefinementChip(
                                text = category,
                                selected = draft.categories.any { it.equals(category, ignoreCase = true) },
                                onClick = {
                                    draft = draft.copy(categories = draft.categories.toggledIgnoringCase(category))
                                }
                            )
                        }
                    }
                }
                if (options.hasGuide) {
                    GuideOnlyRow(
                        checked = draft.guideOnly,
                        onClick = { draft = draft.copy(guideOnly = !draft.guideOnly) }
                    )
                }
            }
            FiltersFooter(
                matchCount = matchCount,
                modifier = Modifier.padding(horizontal = spacing.lg),
                onShowResults = applyAndClose,
                onReset = { draft = SearchFilters() }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FiltersFooter(
    matchCount: Int,
    onShowResults: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAppSpacing.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        // The one primary action of the sheet (rule 12).
        Button(
            onClick = onShowResults,
            modifier = Modifier.fillMaxWidth(),
            colors = browsePrimaryButtonColors(),
            shape = ButtonDefaults.shape(shape = BrowseShapes.Control),
            border = focusRingButtonBorder(BrowseShapes.Control),
            scale = ButtonDefaults.scale(focusedScale = SelectedChipFocusScale)
        ) {
            Text(
                text = stringResource(R.string.search_filters_show_results, matchCount),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Button(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = secondaryButtonColors(),
            shape = ButtonDefaults.shape(shape = BrowseShapes.Control),
            border = focusRingButtonBorder(BrowseShapes.Control),
            scale = ButtonDefaults.scale(focusedScale = SelectedChipFocusScale)
        ) {
            Text(
                text = stringResource(R.string.search_filters_reset),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FilterGroup(
    title: String,
    anySelected: Boolean,
    onAny: () -> Unit,
    chips: @Composable () -> Unit
) {
    val spacing = LocalAppSpacing.current
    val titleSlotPx = with(LocalDensity.current) { spacing.xs.toPx() }
    var firstRowTop by remember { mutableFloatStateOf(0f) }
    // Focus on the first row of chips also brings the group title into view, so scrolling up never leaves a bare
    // row of chips with no label above it.
    val titleResponder = remember {
        object : BringIntoViewResponder {
            override fun calculateRectForParent(localRect: Rect): Rect =
                if (localRect.top <= firstRowTop + FirstRowTolerancePx) localRect.copy(top = 0f) else localRect

            override suspend fun bringChildIntoView(localRect: () -> Rect?) = Unit
        }
    }
    Column(
        modifier = Modifier.bringIntoViewResponder(titleResponder),
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = BrowseColors.TextSecondary,
            modifier = Modifier.onSizeChanged { firstRowTop = it.height + titleSlotPx }
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            RefinementChip(
                text = stringResource(R.string.search_filter_any),
                selected = anySelected,
                onClick = onAny
            )
            chips()
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RefinementChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val spacing = LocalAppSpacing.current
    val selectedLabel = stringResource(R.string.a11y_selected)
    TvClickableSurface(
        modifier = Modifier.semantics {
            this.selected = selected
            if (selected) stateDescription = selectedLabel
        },
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = SelectedChipFocusScale),
        colors = refinementControlColors(selected),
        border = selectableFocusBorder(CircleShape)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selected is also marked by a check and bold text, never by the fill color alone (rule 10).
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = spacing.xxs)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GuideOnlyRow(
    checked: Boolean,
    onClick: () -> Unit
) {
    val spacing = LocalAppSpacing.current
    val selectedLabel = stringResource(R.string.a11y_selected)
    TvClickableSurface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                selected = checked
                if (checked) stateDescription = selectedLabel
            },
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(BrowseShapes.Control),
        scale = ClickableSurfaceDefaults.scale(focusedScale = SelectedChipFocusScale),
        colors = secondarySurfaceColors(),
        border = selectableFocusBorder(BrowseShapes.Control)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Text(
                text = stringResource(R.string.search_filter_guide_only),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            CheckGlyph(checked = checked)
        }
    }
}

/** Checkbox drawn inside the row. Checked: filled brand with a check. Unchecked: empty box, so the state shows by shape. */
@Composable
private fun CheckGlyph(checked: Boolean) {
    val glyphShape = BrowseShapes.Control
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(glyphShape)
            .then(
                if (checked) {
                    Modifier.background(BrowseColors.Brand)
                } else {
                    Modifier.border(2.dp, BrowseColors.TextSecondary, glyphShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = BrowseColors.OnPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
internal fun SearchFilteredEmptyAction(
    activeFilters: Int,
    focusRequester: FocusRequester,
    onRemoveFilters: () -> Unit
) {
    val spacing = LocalAppSpacing.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.md),
        contentAlignment = Alignment.Center
    ) {
        RemoveFiltersButton(activeFilters, focusRequester, onRemoveFilters)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RemoveFiltersButton(
    activeFilters: Int,
    focusRequester: FocusRequester,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.focusRequester(focusRequester),
        colors = browsePrimaryButtonColors(),
        shape = ButtonDefaults.shape(shape = BrowseShapes.Control),
        border = focusRingButtonBorder(BrowseShapes.Control),
        scale = ButtonDefaults.scale(focusedScale = SelectedChipFocusScale)
    ) {
        Text(
            text = stringResource(R.string.search_filters_remove, activeFilters),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun decadeLabel(decade: Int): String =
    if (decade == OLDER_DECADE) {
        stringResource(R.string.search_filter_older)
    } else {
        stringResource(R.string.search_filter_decade, decade, decade + 9)
    }

private fun Set<Int>.toggled(value: Int): Set<Int> = if (value in this) this - value else this + value

private fun Set<String>.toggledIgnoringCase(value: String): Set<String> =
    if (any { it.equals(value, ignoreCase = true) }) {
        filterNot { it.equals(value, ignoreCase = true) }.toSet()
    } else {
        this + value
    }

/**
 * Chip, option and row fill: controlContainer at rest, selectedContainer when selected. Focus never changes the
 * fill (a selected item's label margin is thin in some themes); the ring and scale carry it.
 */
@Composable
private fun refinementControlColors(selected: Boolean) = ClickableSurfaceDefaults.colors(
    containerColor = if (selected) BrowseColors.SelectedContainer else BrowseColors.ControlContainer,
    contentColor = if (selected) BrowseColors.OnSelectedContainer else BrowseColors.TextPrimary,
    focusedContainerColor = if (selected) BrowseColors.SelectedContainer else BrowseColors.ControlContainer,
    focusedContentColor = if (selected) BrowseColors.OnSelectedContainer else BrowseColors.TextPrimary
)
