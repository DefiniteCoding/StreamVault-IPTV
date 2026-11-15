package com.streamvault.feature.catalog.presentation.components

import com.streamvault.core.ui.design.shellHorizontalInset
import com.streamvault.core.ui.design.LocalAppSpacing
import androidx.compose.ui.unit.Dp
import com.streamvault.core.ui.design.BrowseShapes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series
import com.streamvault.core.ui.design.LocalAppTypeTokens
import com.streamvault.core.ui.design.browsePrimaryButtonColors
import com.streamvault.core.ui.design.selectableFocusBorder
import com.streamvault.core.ui.design.selectableSurfaceColors
import com.streamvault.core.ui.design.secondarySurfaceColors
import com.streamvault.core.ui.image.rememberCrossfadeImageModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import com.streamvault.core.ui.design.FocusSafeLazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.streamvault.core.ui.components.AppChipGroup
import com.streamvault.core.ui.components.AppDropdownPill
import com.streamvault.core.ui.components.AppFilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.streamvault.core.ui.device.rememberIsTelevisionDevice
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.tv.material3.Icon
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.streamvault.core.ui.design.BrowseColors as AppColors
import com.streamvault.feature.catalog.presentation.components.ChipRowItem
import com.streamvault.feature.catalog.presentation.components.ChipRowSection
import com.streamvault.core.ui.design.BrowseColors.TextPrimary as TextPrimary
import com.streamvault.core.ui.design.BrowseColors.TextTertiary as OnSurfaceDim
import com.streamvault.feature.catalog.R
import com.streamvault.core.ui.components.SearchInput
import com.streamvault.domain.util.SearchTextNormalizer
import com.streamvault.feature.catalog.presentation.components.SelectionChip
import com.streamvault.feature.catalog.presentation.components.SelectionChipRow
import com.streamvault.core.ui.components.dialogs.PremiumDialog
import com.streamvault.core.ui.components.dialogs.PremiumDialogFooterButton
import com.streamvault.core.ui.interaction.TvClickableSurface
import com.streamvault.core.ui.interaction.TvButton
import com.streamvault.core.ui.interaction.TvIconButton
import com.streamvault.core.ui.components.shell.AppSectionHeader

@Composable
fun VodSectionHeader(
    title: String,
    onSeeAll: (() -> Unit)? = null,
    seeAllLabel: String = stringResource(R.string.action_see_all)
) {
    AppSectionHeader(
        title = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
        actionLabel = onSeeAll?.let { seeAllLabel },
        onActionClick = onSeeAll
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VodActionChipRow(
    actions: List<VodActionChip>,
    selectedKey: String? = null,
    modifier: Modifier = Modifier,
    firstActionFocusRequester: FocusRequester? = null
) {
    ChipRowSection(
        chips = actions.map { action ->
            ChipRowItem(
                key = action.key,
                label = action.label,
                supportingText = action.detail,
                onClick = action.onClick
            )
        },
        selectedKey = selectedKey,
        modifier = modifier.focusRestorer(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        chipHorizontalPadding = 14,
        supportingTextStyle = MaterialTheme.typography.labelSmall,
        supportingTextMaxLines = 1,
        firstItemFocusRequester = firstActionFocusRequester
    )
}

private val CategoryPillMaxWidth = 280.dp

/**
 * Filter row of a VOD grid: the screen's own actions, a Sort group (single choice), the category pill and a pill that
 * opens the full filter dialog. Sort values not offered in [sortOptions] stay reachable through the dialog, so no
 * selected chip is shown for them. The row scrolls inside the grid, so its padding leaves room for the focus ring.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VodFilterRow(
    leadingActions: List<VodActionChip>,
    sortLabel: String,
    sortOptions: List<SelectionChip>,
    selectedSortKey: String,
    onSortSelected: (String) -> Unit,
    categoryLabel: String,
    categoryName: String,
    onCategoryClick: () -> Unit,
    moreLabel: String,
    moreValue: String,
    onMoreClick: () -> Unit,
    trailingActions: List<VodActionChip>,
    modifier: Modifier = Modifier,
    firstActionFocusRequester: FocusRequester? = null,
    // Horizontal padding the shell puts around the screen; the row grows into it so cards slide off the screen edge.
    edgeGutter: Dp = LocalAppSpacing.current.shellHorizontalInset
) {
    FocusSafeLazyRow(
        modifier = modifier.fillMaxWidth().focusRestorer().suppressParentVerticalScroll(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        edgeGutterStart = edgeGutter,
        edgeGutterEnd = edgeGutter
    ) {
        itemsIndexed(leadingActions, key = { _, action -> action.key }) { index, action ->
            AppFilterChip(
                label = action.label,
                selected = false,
                onClick = action.onClick,
                modifier = if (index == 0 && firstActionFocusRequester != null) {
                    Modifier.focusRequester(firstActionFocusRequester)
                } else {
                    Modifier
                }
            )
        }
        item(key = "sort") {
            AppChipGroup(label = sortLabel) {
                sortOptions.forEach { option ->
                    AppFilterChip(
                        label = option.label,
                        selected = option.key == selectedSortKey,
                        onClick = { onSortSelected(option.key) }
                    )
                }
            }
        }
        item(key = "category") {
            // Provider category names can be long; the pill ellipsizes instead of pushing the other controls away.
            AppDropdownPill(
                label = categoryLabel,
                value = categoryName,
                onClick = onCategoryClick,
                modifier = Modifier.widthIn(max = CategoryPillMaxWidth)
            )
        }
        item(key = "more") {
            AppDropdownPill(label = moreLabel, value = moreValue, onClick = onMoreClick)
        }
        items(trailingActions, key = { it.key }) { action ->
            AppFilterChip(label = action.label, selected = false, onClick = action.onClick)
        }
    }
}

/** What the hero shows for one title. [metadata] is already formatted for the locale. */
data class VodHeroModel(
    val title: String,
    val kicker: String,
    val metadata: List<String>,
    val plot: String,
    val posterUrl: String?
)

// Provider genre fields are comma or slash separated lists; the hero shows the first.
private fun firstGenre(genre: String?): String? =
    genre?.split(',', '/')?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }

private fun releaseYear(releaseDate: String?): String? =
    releaseDate?.let { Regex("""\d{4}""").find(it)?.value }

private fun heroMetadata(rating: Float, year: String?, genre: String?): List<String> = listOfNotNull(
    formatVodRatingLabel(rating).takeIf { rating > 0f },
    year?.takeIf { it.isNotBlank() },
    firstGenre(genre)
)

// The shelf rows load a slim projection with no backdrop and no plot, and the provider list API carries no backdrop
// for movies, so the poster is the only artwork every title has.
fun Movie.toVodHeroModel() = VodHeroModel(
    title = name,
    kicker = categoryName.orEmpty(),
    metadata = heroMetadata(rating, year ?: releaseYear(releaseDate), genre),
    plot = plot.orEmpty().trim(),
    posterUrl = posterUrl?.takeIf { it.isNotBlank() }
)

fun Series.toVodHeroModel() = VodHeroModel(
    title = name,
    kicker = categoryName.orEmpty(),
    metadata = heroMetadata(rating, releaseYear(releaseDate), genre),
    plot = plot.orEmpty().trim(),
    posterUrl = posterUrl?.takeIf { it.isNotBlank() }
)

private const val HeroFollowDebounceMs = 250L
private val HeroHeight = 240.dp
private val HeroPosterHeight = 208.dp
private val HeroPosterWidth = 139.dp

/**
 * The item the hero shows: the last focused one, once focus has rested for [HeroFollowDebounceMs], so a fast D-pad
 * run does not reload artwork per card. [fallback] shows until something has been focused. [focused] is read only
 * here, so a focus move recomposes the hero and nothing above it.
 */
@OptIn(FlowPreview::class)
@Composable
fun <T : Any> rememberHeroItem(focused: State<T?>, fallback: T?): T? {
    val shown = remember { mutableStateOf<T?>(null) }
    LaunchedEffect(focused) {
        snapshotFlow { focused.value }
            .filterNotNull()
            .debounce(HeroFollowDebounceMs)
            .collect { shown.value = it }
    }
    return shown.value ?: fallback
}

/**
 * [item] enriched by [resolve] (a local row lookup, possibly a provider call). Shows [item] at once and swaps in the
 * resolved copy when it arrives; a new [item] cancels the pending resolve, which is what keeps fast browsing free of
 * provider traffic.
 */
@Composable
fun <T : Any> rememberHeroDetails(item: T?, resolve: suspend (T) -> T): T? =
    produceState(initialValue = item, item) {
        value = item
        if (item != null) value = resolve(item)
    }.value

/** Opaque theme surface with the title on the start side and its poster on the end side: no scrim, no overdraw. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VodFocusHero(
    model: VodHeroModel,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionFocusRequester: FocusRequester? = null
) {
    val type = LocalAppTypeTokens.current
    // The hero is taller than its button, so the default focus scroll reveals only the button and leaves the top of
    // the panel clipped. Asking for the whole hero (padding included) keeps it fully visible while its button is focused.
    val heroInView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.bringIntoViewRequester(heroInView)) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(HeroHeight)
                .clip(BrowseShapes.Hero)
                .background(AppColors.SurfaceElevated)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (model.kicker.isNotBlank()) {
                    Text(
                        text = type.kicker(model.kicker),
                        style = type.labelKicker,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.displaySmall,
                    color = AppColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (model.metadata.isNotEmpty()) {
                    Text(
                        text = model.metadata.joinToString("  \u00B7  "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Two lines are always reserved so the plot arriving later does not move the title or the button.
                Text(
                    text = model.plot,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                TvButton(
                    onClick = onActionClick,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .then(if (actionFocusRequester != null) Modifier.focusRequester(actionFocusRequester) else Modifier)
                        .onFocusChanged { if (it.hasFocus) scope.launch { heroInView.bringIntoView() } },
                    colors = browsePrimaryButtonColors()
                ) {
                    Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
            if (model.posterUrl != null) {
                AsyncImage(
                    model = rememberCrossfadeImageModel(model.posterUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = HeroPosterWidth, height = HeroPosterHeight)
                        .clip(BrowseShapes.Artwork),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

data class VodActionChip(
    val key: String,
    val label: String,
    val detail: String? = null,
    val onClick: () -> Unit
)

data class VodCategoryOption(
    val name: String,
    val count: Int,
    val onClick: () -> Unit,
    val onLongClick: (() -> Unit)? = null,
    val isLocked: Boolean = false
)

@Composable
fun VodCategoryPickerDialog(
    title: String,
    subtitle: String,
    categories: List<VodCategoryOption>,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    val filteredCategories = remember(categories, query) {
        val normalized = query.trim()
        if (normalized.isBlank()) categories
        else {
            val foldedQuery = SearchTextNormalizer.normalize(normalized)
            categories.filter { SearchTextNormalizer.normalize(it.name).contains(foldedQuery) }
        }
    }

    PremiumDialog(
        title = title,
        subtitle = subtitle,
        onDismissRequest = onDismiss,
        widthFraction = 0.52f,
        content = {
            SearchInput(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(R.string.vod_category_picker_search_placeholder),
                focusRequester = searchFocusRequester,
                modifier = Modifier.fillMaxWidth()
            )

            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.vod_category_picker_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isTelevisionDevice = rememberIsTelevisionDevice()
                    val listHeight = when {
                        maxWidth < 700.dp -> 300.dp
                        !isTelevisionDevice && maxWidth < 1280.dp -> 360.dp
                        else -> 420.dp
                    }

                    LazyColumn(
                        modifier = Modifier.height(listHeight),
                        verticalArrangement = Arrangement.spacedBy(LocalAppSpacing.current.xs)
                    ) {
                        items(filteredCategories, key = { it.name }) { category ->
                            TvClickableSurface(
                                onClick = {
                                    category.onClick()
                                    onDismiss()
                                },
                                onLongClick = category.onLongClick?.let { action ->
                                    {
                                        action()
                                        onDismiss()
                                    }
                                },
                                shape = ClickableSurfaceDefaults.shape(BrowseShapes.Control),
                                colors = secondarySurfaceColors(),
                                border = selectableFocusBorder(BrowseShapes.Control),
                                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
                            ) {
                                val spacing = LocalAppSpacing.current
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (category.isLocked) {
                                        Spacer(modifier = Modifier.width(spacing.xs))
                                        Text(
                                            text = stringResource(R.string.home_locked_short),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                    if (category.count > 0) {
                                        Spacer(modifier = Modifier.width(spacing.xs))
                                        Text(
                                            text = category.count.toString(),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        footer = {
            PremiumDialogFooterButton(
                label = stringResource(R.string.category_options_cancel),
                onClick = onDismiss
            )
        }
    )
}

@Composable
fun VodBrowseOptionsDialog(
    title: String,
    filterTitle: String,
    filterChips: List<SelectionChip>,
    selectedFilterKey: String,
    onFilterSelected: (String) -> Unit,
    sortTitle: String,
    sortChips: List<SelectionChip>,
    selectedSortKey: String,
    onSortSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    PremiumDialog(
        title = title,
        onDismissRequest = onDismiss,
        widthFraction = 0.6f,
        heightFraction = null,
        bodyHeightFraction = 0.6f,
        content = {
            VodBrowseOptionsSection(
                title = filterTitle,
                chips = filterChips,
                selectedKey = selectedFilterKey,
                onChipSelected = onFilterSelected
            )
            VodBrowseOptionsSection(
                title = sortTitle,
                chips = sortChips,
                selectedKey = selectedSortKey,
                onChipSelected = onSortSelected
            )
        },
        footer = {
            PremiumDialogFooterButton(
                label = stringResource(R.string.category_options_cancel),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun VodBrowseOptionsSection(
    title: String,
    chips: List<SelectionChip>,
    selectedKey: String,
    onChipSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.TextTertiary
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            chips.forEach { chip ->
                VodBrowseOptionChip(
                    label = chip.label,
                    supportingText = chip.supportingText,
                    selected = chip.key == selectedKey,
                    onClick = { onChipSelected(chip.key) }
                )
            }
        }
    }
}

private val OptionChipCheckSize = 18.dp

@Composable
private fun VodBrowseOptionChip(
    label: String,
    supportingText: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        colors = if (selected) selectableSurfaceColors(selected = true) else secondarySurfaceColors(),
        shape = ClickableSurfaceDefaults.shape(BrowseShapes.Control),
        border = selectableFocusBorder(BrowseShapes.Control),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LocalAppSpacing.current.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selected) {
                    // The check backs up the tonal fill so selection never rests on color alone.
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(OptionChipCheckSize)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            supportingText?.takeIf { it.isNotBlank() }?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

