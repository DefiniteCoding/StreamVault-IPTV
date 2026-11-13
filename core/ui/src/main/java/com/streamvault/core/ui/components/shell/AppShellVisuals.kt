package com.streamvault.core.ui.components.shell

import com.streamvault.core.ui.layout.AdaptiveRow

import androidx.compose.foundation.layout.FlowRow

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import com.streamvault.core.ui.accessibility.LocalReducedEffects
import com.streamvault.core.ui.design.AppPalette
import com.streamvault.core.ui.design.GlassTier
import com.streamvault.core.ui.design.glassSurface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.core.ui.design.AppColors
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.BrowseShapes
import com.streamvault.core.ui.design.LocalBrowseVisuals
import com.streamvault.core.ui.design.browseBackdrop
import com.streamvault.core.ui.design.selectableFocusBorder
import com.streamvault.core.ui.design.selectableSurfaceColors
import com.streamvault.core.ui.design.browsePanel
import com.streamvault.core.ui.design.AppMotion
import com.streamvault.core.ui.design.BrowseFocusSpec as FocusSpec
import com.streamvault.core.ui.design.LocalAppShapes
import com.streamvault.core.ui.design.LocalAppSpacing
import com.streamvault.core.ui.interaction.TvIconButton
import com.streamvault.core.ui.interaction.mouseClickable
import com.streamvault.core.ui.interaction.rememberTvInteractionSounds
import com.streamvault.core.ui.layout.isPortraitLayout

enum class NavigationChrome {
    Rail,
    TopBar
}

data class UiDestination(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

/** Search drawn as an icon-only glass pill before the top bar. Its destination never appears as a tab there. */
@Immutable
data class TopBarSearch(
    val destinationId: String,
    val contentDescription: String
)

@Composable
fun CoreAppScreenScaffold(
    currentDestinationId: String,
    destinations: List<UiDestination>,
    onDestinationSelected: (String) -> Unit,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    navigationChrome: NavigationChrome = NavigationChrome.Rail,
    topBarVisible: Boolean = true,
    compactHeader: Boolean = false,
    showScreenHeader: Boolean = true,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    topBarActions: (@Composable RowScope.() -> Unit)? = null,
    topBarOverlaysContent: Boolean = false,
    topBarTier: GlassTier = GlassTier.Bar,
    brand: (@Composable () -> Unit)? = null,
    topBarSearch: TopBarSearch? = null,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalAppSpacing.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "streamvault.destination:$currentDestinationId"
            }
            .browseBackdrop()
    ) {
        if (isPortraitLayout()) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (showScreenHeader) {
                        AppScreenHeader(title = title, subtitle = subtitle, compact = true)
                        Spacer(Modifier.height(8.dp))
                    }
                    header?.invoke(this)
                    Column(Modifier.weight(1f).fillMaxWidth().padding(contentPadding), content = content)
                }
                PortraitNavigation(currentDestinationId, destinations, onDestinationSelected, topBarActions)
            }
        } else if (navigationChrome == NavigationChrome.Rail) {
            Row(modifier = Modifier.fillMaxSize()) {
                DestinationRail(
                    currentDestinationId = currentDestinationId,
                    destinations = destinations,
                    onDestinationSelected = onDestinationSelected,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(spacing.railWidth)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = spacing.lg,
                            end = spacing.screenGutter,
                            top = spacing.safeTop,
                            bottom = spacing.safeBottom
                        )
                ) {
                    if (showScreenHeader) {
                        AppScreenHeader(
                            title = title,
                            subtitle = subtitle,
                            modifier = Modifier.fillMaxWidth(),
                            compact = compactHeader
                        )
                        if (header != null) {
                            Spacer(modifier = Modifier.height(spacing.lg))
                            header()
                        }
                        Spacer(modifier = Modifier.height(spacing.lg))
                    } else if (header != null) {
                        header()
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                    ) {
                        content()
                    }
                }
            }
        } else {
            val barTop = spacing.md
            val contentInset = if (topBarVisible) barTop + TopBarHeight + spacing.sm else barTop
            CompositionLocalProvider(LocalTopBarContentInset provides contentInset) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Composed before the content so default focus order is unchanged; zIndex only lifts its drawing.
                    if (topBarVisible) {
                        TopNavigationBar(
                            currentDestinationId = currentDestinationId,
                            destinations = destinations,
                            onDestinationSelected = onDestinationSelected,
                            tier = topBarTier,
                            brand = brand,
                            search = topBarSearch,
                            actions = topBarActions,
                            modifier = Modifier
                                .zIndex(1f)
                                .fillMaxWidth()
                                .padding(start = spacing.md, end = spacing.md, top = barTop)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = spacing.md,
                                end = spacing.md,
                                top = if (topBarOverlaysContent) 0.dp else contentInset,
                                bottom = spacing.sm
                            )
                    ) {
                        if (showScreenHeader) {
                            AppScreenHeader(
                                title = title,
                                subtitle = subtitle,
                                modifier = Modifier.fillMaxWidth(),
                                compact = true
                            )
                            if (header != null) {
                                Spacer(modifier = Modifier.height(spacing.xs))
                                header()
                            }
                            Spacer(modifier = Modifier.height(spacing.xs))
                        } else if (header != null) {
                            header()
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Distance from the top of a top-bar screen to the first row that must stay clear of the floating bar. Screens that
 * pass `topBarOverlaysContent = true` apply it as list content padding so rows scroll under the glass.
 */
val LocalTopBarContentInset = compositionLocalOf { 0.dp }

private val TopBarHeight = 48.dp
private val TopNavIconSize = 18.dp
// Same height as a tab: titleSmall line height (20) plus xs padding above and below.
private val TopNavSearchPillSize = 36.dp

@Composable
fun AppScreenHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    compact: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!eyebrow.isNullOrBlank()) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelMedium,
                color = BrowseColors.Brand
            )
        }
        Text(
            text = title,
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.displaySmall,
            color = BrowseColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
                color = BrowseColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun TopNavigationBar(
    currentDestinationId: String,
    destinations: List<UiDestination>,
    onDestinationSelected: (String) -> Unit,
    tier: GlassTier,
    brand: (@Composable () -> Unit)?,
    search: TopBarSearch?,
    actions: (@Composable RowScope.() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAppSpacing.current
    val scrollState = rememberScrollState()
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    val pillColors = topNavPillColors(BrowseColors.current, tier)
    val tabs = remember(destinations, search) { topBarTabs(destinations, search) }

    Row(
        modifier = modifier.height(TopBarHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        brand?.invoke()
        // One focus group for the search pill and the tabs, so entering from anywhere lands on the active item.
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .focusProperties {
                    // onEnter returns Unit: a returned requester is discarded, so focus is redirected explicitly.
                    onEnter = {
                        val targetId = topBarEntryTargetId(currentDestinationId, tabs, search)
                        focusRequesters[targetId]?.requestFocus()
                    }
                }
                .focusGroup(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            if (search != null) {
                TopNavigationSearchPill(
                    search = search,
                    selected = currentDestinationId.startsWith(search.destinationId),
                    tier = tier,
                    colors = pillColors,
                    focusRequester = focusRequesters.getOrPut(search.destinationId) { FocusRequester() },
                    onDestinationSelected = onDestinationSelected
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .glassSurface(tier, CircleShape)
                    .horizontalScroll(scrollState)
                    // Inside the scroll so the first and last pills keep room for their focus scale and ring.
                    .padding(horizontal = spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                tabs.forEach { destination ->
                    val requester = focusRequesters.getOrPut(destination.id) { FocusRequester() }
                    TopNavigationButton(
                        destination = destination,
                        selected = currentDestinationId.startsWith(destination.id),
                        colors = pillColors,
                        focusRequester = requester,
                        onClick = {
                            if (!currentDestinationId.startsWith(destination.id)) {
                                onDestinationSelected(destination.id)
                            }
                        }
                    )
                }
            }
        }
        if (actions != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

/** Colors of one [FocusPill] use: nav tabs on a glass tier, or chips on the page. */
@Immutable
internal data class PillColors(
    val restContainer: Color,
    val restContent: Color,
    val selectedContainer: Color,
    val selectedContent: Color,
    val focusedContainer: Color,
    val focusedContent: Color,
    val ring: Color
)

/** Nav pill colors for one glass tier. Pairs are covered by GlassPaletteContrastTest in every theme. */
internal fun topNavPillColors(palette: AppPalette, tier: GlassTier): PillColors =
    if (tier == GlassTier.OverVideo) {
        PillColors(
            restContainer = Color.Transparent,
            restContent = palette.onGlassOverVideo,
            selectedContainer = palette.navSelected,
            selectedContent = palette.onNavSelected,
            focusedContainer = palette.focusOverVideo,
            focusedContent = palette.onFocusOverVideo,
            ring = palette.focusOverVideo
        )
    } else {
        PillColors(
            restContainer = Color.Transparent,
            restContent = palette.textSecondary,
            selectedContainer = palette.navSelected,
            selectedContent = palette.onNavSelected,
            focusedContainer = palette.focus,
            focusedContent = palette.onFocus,
            ring = palette.focus
        )
    }

// Drawn outside the pill: tv-material's Border paints inside the shape and cannot stand off it.
// The corner radius assumes a full pill (CircleShape).
private fun Modifier.pillFocusRing(color: Color, isFocused: () -> Boolean): Modifier = drawWithCache {
    val ringWidth = FocusSpec.NavRingWidth.toPx()
    val outset = FocusSpec.NavRingGap.toPx() + ringWidth / 2f
    val ringSize = Size(size.width + outset * 2f, size.height + outset * 2f)
    val cornerRadius = CornerRadius(ringSize.height / 2f)
    val stroke = Stroke(width = ringWidth)
    onDrawBehind {
        if (isFocused()) {
            drawRoundRect(
                color = color,
                topLeft = Offset(-outset, -outset),
                size = ringSize,
                cornerRadius = cornerRadius,
                style = stroke
            )
        }
    }
}

@Composable
fun AppTopBarCloseAction(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    TvIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = androidx.tv.material3.IconButtonDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = BrowseColors.Brand.copy(alpha = 0.18f),
            contentColor = BrowseColors.TextSecondary,
            focusedContentColor = BrowseColors.TextPrimary
        ),
        border = androidx.tv.material3.IconButtonDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(FocusSpec.BorderWidth, BrowseColors.Focus),
                shape = BrowseShapes.Control
            )
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TopNavigationButton(
    destination: UiDestination,
    selected: Boolean,
    colors: PillColors,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val spacing = LocalAppSpacing.current
    FocusPill(
        selected = selected,
        colors = colors,
        role = Role.Tab,
        onClick = onClick,
        modifier = modifier,
        focusRequester = focusRequester
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) destination.selectedIcon else destination.icon,
                // The visible label already names the tab.
                contentDescription = null,
                modifier = Modifier.size(TopNavIconSize)
            )
            Text(
                text = destination.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.SemiBold else null,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TopNavigationSearchPill(
    search: TopBarSearch,
    selected: Boolean,
    tier: GlassTier,
    colors: PillColors,
    focusRequester: FocusRequester,
    onDestinationSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(TopBarHeight)
            .glassSurface(tier, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        FocusPill(
            selected = selected,
            colors = colors,
            role = Role.Tab,
            onClick = { if (!selected) onDestinationSelected(search.destinationId) },
            focusRequester = focusRequester
        ) {
            Box(modifier = Modifier.size(TopNavSearchPillSize), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = search.contentDescription,
                    modifier = Modifier.size(TopNavIconSize)
                )
            }
        }
    }
}

/**
 * Shared pill body for nav tabs and chips: rest, selected and focused fills, the outset focus ring, focus scale and
 * semantics. [selected] is null for a control that has no selected state, so none is announced.
 */
@Composable
internal fun FocusPill(
    selected: Boolean?,
    colors: PillColors,
    role: Role,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    content: @Composable (focused: Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val sounds = rememberTvInteractionSounds()
    val reducedEffects = LocalReducedEffects.current
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> FocusSpec.PressedScale
            isFocused -> FocusSpec.NavFocusedScale
            else -> 1f
        },
        animationSpec = if (reducedEffects) snap() else AppMotion.FocusSpec,
        label = "pillScale"
    )

    Surface(
        onClick = {
            sounds.playSelect()
            onClick()
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .mouseClickable(
                focusRequester = focusRequester,
                onClick = {
                    sounds.playSelect()
                    onClick()
                }
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .zIndex(if (isFocused) 1f else 0f)
            .pillFocusRing(colors.ring) { isFocused }
            .onFocusChanged {
                if (it.isFocused && !isFocused) sounds.playNavigate()
                isFocused = it.isFocused
            }
            .semantics {
                this.role = role
                if (selected != null) this.selected = selected
            },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        // Scale is applied above so the ring, drawn outside the Surface, scales with the pill.
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected == true) colors.selectedContainer else colors.restContainer,
            contentColor = if (selected == true) colors.selectedContent else colors.restContent,
            focusedContainerColor = colors.focusedContainer,
            focusedContentColor = colors.focusedContent,
            pressedContainerColor = colors.focusedContainer,
            pressedContentColor = colors.focusedContent
        ),
        interactionSource = interactionSource
    ) {
        content(isFocused)
    }
}

@Composable
fun AppHeroHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = BrowseShapes.Hero,
        colors = SurfaceDefaults.colors(containerColor = BrowseColors.SurfaceElevated),
        border = Border(BorderStroke(1.dp, BrowseColors.Outline), shape = BrowseShapes.Hero)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BrowseShapes.Hero)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(BrowseColors.Surface, BrowseColors.SurfaceEmphasis, BrowseColors.Brand.copy(alpha = 0.16f))
                    )
                )
                .padding(if (isPortraitLayout()) 18.dp else 32.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                AppScreenHeader(title = title, subtitle = subtitle, eyebrow = eyebrow, compact = isPortraitLayout())
                if (actions != null) {
                    Row(
                        modifier = if (isPortraitLayout()) Modifier.horizontalScroll(rememberScrollState()) else Modifier,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
                if (footer != null) footer()
            }
        }
    }
}

@Composable
fun AppSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionContentColor: Color = BrowseColors.TextTertiary
) {
    val shapes = LocalAppShapes.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = if (onActionClick != null && !actionLabel.isNullOrBlank()) Modifier.weight(1f) else Modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = BrowseColors.TextPrimary,
                modifier = Modifier.semantics { heading() }
            )
            if (!subtitle.isNullOrBlank()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = BrowseColors.TextTertiary)
            }
        }
        if (onActionClick != null && !actionLabel.isNullOrBlank()) {
            val actionFocusRequester = remember { FocusRequester() }
            Surface(
                onClick = onActionClick,
                modifier = Modifier
                    .focusRequester(actionFocusRequester)
                    .mouseClickable(focusRequester = actionFocusRequester, onClick = onActionClick),
                shape = ClickableSurfaceDefaults.shape(shapes.pill),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = BrowseColors.Brand.copy(alpha = 0.12f),
                    focusedContainerColor = BrowseColors.Brand.copy(alpha = 0.22f),
                    contentColor = actionContentColor
                )
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.SurfaceEmphasis,
    contentColor: Color = AppColors.TextPrimary,
    cornerRadius: Dp = 999.dp,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 4.dp
) {
    val modern = LocalBrowseVisuals.current
    val isStatusColor = containerColor in listOf(AppColors.Live, AppColors.Brand, AppColors.Warning, AppColors.Success, AppColors.Info)
    val tinted = modern && isStatusColor
    val shape = RoundedCornerShape(if (modern && cornerRadius == 999.dp) 6.dp else cornerRadius)
    val resolvedContentColor = when {
        tinted -> containerColor
        modern && containerColor.copy(alpha = 1f) == Color.Black -> Color.White
        else -> contentColor
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (tinted) containerColor.copy(alpha = 0.16f) else containerColor)
            .then(if (modern) Modifier.border(1.dp, (if (tinted) containerColor else AppColors.TextPrimary).copy(alpha = 0.14f), shape) else Modifier)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = resolvedContentColor)
    }
}

@Composable
fun AppMessageState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    shape: RoundedCornerShape? = null,
    containerBrush: Brush? = null,
    borderColor: Color? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodySmall,
    titleColor: Color = AppColors.TextPrimary,
    subtitleColor: Color = AppColors.TextSecondary,
    titleTextAlign: TextAlign = TextAlign.Start,
    subtitleTextAlign: TextAlign = TextAlign.Start
) {
    val resolvedShape = shape ?: LocalAppShapes.current.large
    Surface(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        shape = resolvedShape,
        border = Border(
            border = BorderStroke(if (borderColor != null) 1.dp else 0.dp, borderColor ?: Color.Transparent),
            shape = resolvedShape
        ),
        colors = SurfaceDefaults.colors(containerColor = AppColors.SurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .then(if (containerBrush != null) Modifier.background(containerBrush) else Modifier)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = titleStyle, color = titleColor, textAlign = titleTextAlign, modifier = Modifier.fillMaxWidth())
            Text(text = subtitle, style = subtitleStyle, color = subtitleColor, textAlign = subtitleTextAlign, modifier = Modifier.fillMaxWidth())
            if (action != null) {
                Spacer(modifier = Modifier.height(8.dp))
                action()
            }
        }
    }
}

@Composable
fun LoadMoreCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shapes = LocalAppShapes.current
    val focusRequester = remember { FocusRequester() }
    Surface(
        onClick = onClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .mouseClickable(focusRequester = focusRequester, onClick = onClick),
        shape = ClickableSurfaceDefaults.shape(shapes.medium),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = BrowseColors.SurfaceElevated,
            focusedContainerColor = BrowseColors.Brand.copy(alpha = 0.18f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(FocusSpec.BorderWidth, BrowseColors.Focus),
                shape = shapes.medium
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = label, tint = BrowseColors.Brand, modifier = Modifier.size(18.dp))
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = BrowseColors.TextPrimary)
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ContentMetadataStrip(
    values: List<String>,
    modifier: Modifier = Modifier
) {
    val filteredValues = values.filter { it.isNotBlank() }
    AdaptiveRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        filteredValues.forEachIndexed { index, value ->
            Text(text = value, style = MaterialTheme.typography.labelMedium, color = BrowseColors.TextSecondary)
            if (index < filteredValues.lastIndex) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(BrowseColors.TextTertiary)
                )
            }
        }
    }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun DestinationRail(
    currentDestinationId: String,
    destinations: List<UiDestination>,
    onDestinationSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAppSpacing.current
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    Box(
        modifier = modifier
            .padding(start = spacing.lg, top = spacing.safeTop, bottom = spacing.safeBottom)
            .browsePanel(BrowseShapes.Panel)
            .focusProperties {
                onEnter = {
                    val activeItem = findActiveDestination(destinations, currentDestinationId)
                    focusRequesters[activeItem?.id] ?: FocusRequester.Default
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "StreamVault", style = MaterialTheme.typography.titleMedium, color = BrowseColors.TextPrimary)
            Text(text = "TV", style = MaterialTheme.typography.labelSmall, color = BrowseColors.TextTertiary)
            Spacer(modifier = Modifier.height(10.dp))
            destinations.forEach { destination ->
                val requester = focusRequesters.getOrPut(destination.id) { FocusRequester() }
                RailButton(
                    destination = destination,
                    selected = currentDestinationId.startsWith(destination.id),
                    modifier = Modifier.focusRequester(requester),
                    onClick = {
                        if (!currentDestinationId.startsWith(destination.id)) {
                            onDestinationSelected(destination.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RailButton(
    destination: UiDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) FocusSpec.FocusedScale else 1f,
        animationSpec = AppMotion.FocusSpec,
        label = "railButtonScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .semantics {
                contentDescription = destination.label
                onClick(label = destination.label) {
                    onClick()
                    true
                }
            }
            .focusRequester(focusRequester)
            .mouseClickable(focusRequester = focusRequester, onClick = onClick)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(BrowseShapes.Panel),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        colors = selectableSurfaceColors(selected),
        border = selectableFocusBorder(BrowseShapes.Panel)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) destination.selectedIcon else destination.icon,
                contentDescription = destination.label,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = destination.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.SemiBold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal fun topBarTabs(destinations: List<UiDestination>, search: TopBarSearch?): List<UiDestination> =
    if (search == null) destinations else destinations.filterNot { it.id == search.destinationId }

/** The item that takes focus when D-pad focus enters the bar: the search pill on Search, else the active tab. */
internal fun topBarEntryTargetId(
    currentDestinationId: String,
    tabs: List<UiDestination>,
    search: TopBarSearch?
): String? =
    if (search != null && currentDestinationId.startsWith(search.destinationId)) {
        search.destinationId
    } else {
        findActiveDestination(tabs, currentDestinationId)?.id
    }

private fun findActiveDestination(
    destinations: List<UiDestination>,
    currentDestinationId: String
): UiDestination? = destinations
    .filter { currentDestinationId.startsWith(it.id) }
    .maxByOrNull { it.id.length }
    ?: destinations.firstOrNull { it.id == currentDestinationId }
