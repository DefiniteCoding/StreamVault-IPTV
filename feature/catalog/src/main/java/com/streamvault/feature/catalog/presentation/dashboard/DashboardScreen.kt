package com.streamvault.feature.catalog.presentation.dashboard

import com.streamvault.core.ui.layout.isPortraitLayout

import com.streamvault.core.ui.design.BrowseShapes
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.flow.first
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.feature.catalog.R
import com.streamvault.core.ui.device.rememberIsTelevisionDevice
import com.streamvault.core.ui.image.ChannelLogoBadge
import com.streamvault.feature.catalog.presentation.components.CatalogChannelProgressTicker
import com.streamvault.feature.catalog.presentation.components.CategoryRow
import com.streamvault.feature.catalog.presentation.components.ChannelCard
import com.streamvault.feature.catalog.presentation.components.ContinueWatchingMenuActions
import com.streamvault.feature.catalog.presentation.components.ContinueWatchingRow
import com.streamvault.feature.catalog.presentation.components.dialogs.ActionMenuDialog
import com.streamvault.feature.catalog.presentation.components.dialogs.ActionMenuEntry
import com.streamvault.feature.catalog.presentation.components.MovieCard
import com.streamvault.core.ui.image.rememberCrossfadeImageModel
import com.streamvault.feature.catalog.presentation.components.SeriesCard
import com.streamvault.core.ui.components.AppDialogButton
import com.streamvault.core.ui.components.shell.AppMessageState
import com.streamvault.core.ui.components.shell.LocalTopBarContentInset
import com.streamvault.core.ui.components.shell.LocalTopBarDownTarget
import com.streamvault.core.ui.design.requestFocusSafely
import com.streamvault.core.ui.design.BrowseColors as AppColors
import com.streamvault.feature.catalog.presentation.time.LocalCatalogTimeFormat
import com.streamvault.feature.catalog.presentation.time.createCatalogDateTimeFormat
import com.streamvault.feature.catalog.api.CatalogDashboardShelfCustomizationContent
import com.streamvault.feature.catalog.api.CatalogNavigationChrome
import com.streamvault.feature.catalog.api.CatalogScaffoldContent
import com.streamvault.core.navigation.AppDestination
import com.streamvault.core.ui.design.BrowseColors.Brand as Primary
import com.streamvault.core.ui.design.BrowseColors.Focus as FocusBorder
import com.streamvault.core.ui.design.BrowseColors.SurfaceElevated as SurfaceElevated
import com.streamvault.core.ui.design.BrowseColors.SurfaceEmphasis as SurfaceHighlight
import com.streamvault.core.ui.design.BrowseColors.TextPrimary as OnBackground
import com.streamvault.core.ui.design.BrowseColors.TextPrimary as TextPrimary
import com.streamvault.core.ui.design.BrowseColors.TextTertiary as OnSurfaceDim
import com.streamvault.core.ui.design.BrowseColors.TextTertiary as TextTertiary
import com.streamvault.domain.model.AppHomeDashboardShelf
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.streamvault.core.ui.interaction.TvClickableSurface
import com.streamvault.core.ui.interaction.TvButton
import com.streamvault.core.ui.interaction.TvIconButton

private val ContinueWatchingShelves = setOf(
    AppHomeDashboardShelf.CONTINUE_WATCHING,
    AppHomeDashboardShelf.CONTINUE_WATCHING_MOVIES,
    AppHomeDashboardShelf.CONTINUE_WATCHING_SERIES
)

@Composable
fun DashboardScreen(
    onDestinationRequested: (AppDestination) -> Unit,
    onAddProvider: () -> Unit,
    onRecentChannelClick: (Channel, Long?) -> Unit,
    onFavoriteChannelClick: (Channel, Long?) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onPlaybackHistoryClick: (PlaybackHistory) -> Unit,
    onOpenHistoryDetails: (PlaybackHistory) -> Unit,
    scaffold: CatalogScaffoldContent,
    dashboardShelfCustomizationContent: CatalogDashboardShelfCustomizationContent,
    stremioContent: @Composable () -> Boolean = { false },
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recordingChannelIds by viewModel.recordingChannelIds.collectAsStateWithLifecycle()
    val scheduledChannelIds by viewModel.scheduledChannelIds.collectAsStateWithLifecycle()
    val nowMs by CatalogChannelProgressTicker.nowMs.collectAsStateWithLifecycle()
    val provider = uiState.provider
    val snackbarHostState = remember { SnackbarHostState() }
    var showHomeCustomizationDialog by remember { mutableStateOf(false) }
    var channelMenuTarget by remember { mutableStateOf<Channel?>(null) }
    val recentChannelRequesters = remember { HashMap<Long, FocusRequester>() }
    var recentRemovalHandoff by remember { mutableStateOf<Pair<Long, Long?>?>(null) }
    val initialFocusRequester = remember { FocusRequester() }
    var initialTargetPlaced by remember { mutableStateOf(false) }
    val initialFocusModifier = remember {
        Modifier
            .focusRequester(initialFocusRequester)
            .onGloballyPositioned { initialTargetPlaced = true }
    }

    // One shot per screen entry: wait until the target card is placed, or the list scrolls to an unfinished rect.
    LaunchedEffect(Unit) {
        snapshotFlow { initialTargetPlaced }.first { it }
        initialFocusRequester.requestFocusSafely(tag = "DashboardScreen", target = "Home initial card")
    }

    LaunchedEffect(uiState.recentChannels) {
        recentChannelRequesters.keys.retainAll(uiState.recentChannels.mapTo(HashSet()) { it.id })
        val (removedId, neighborId) = recentRemovalHandoff ?: return@LaunchedEffect
        if (uiState.recentChannels.any { it.id == removedId }) return@LaunchedEffect
        recentRemovalHandoff = null
        // The menu window is still leaving; wait a frame so focus is not taken back from the card.
        withFrameNanos { }
        neighborId?.let { recentChannelRequesters[it]?.requestFocus() }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        scaffold(
            AppDestination.Home,
            stringResource(R.string.nav_home),
            provider?.name,
            CatalogNavigationChrome.TopBarOverlay,
            true,
            true,
            false
        ) {
            val downTarget = LocalTopBarDownTarget.current
            val hasProvider = provider != null
            DisposableEffect(downTarget, hasProvider) {
                // The empty state has nothing under the bar, so default focus search already works there.
                downTarget.requester = initialFocusRequester.takeIf { hasProvider }
                onDispose { downTarget.requester = null }
            }
            if (provider == null) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(top = LocalTopBarContentInset.current)
                        .verticalScroll(rememberScrollState())
                ) {
                    val hasStremio = stremioContent()
                    if (!hasStremio) Box(Modifier.fillMaxWidth().height(380.dp)) {
                        EmptyDashboard(onAddProvider = onAddProvider,
                            onOpenSettings = { onDestinationRequested(AppDestination.Settings()) })
                    }
                }
                return@scaffold
            }
            val orderedSections = rememberDashboardSections(uiState)
            // Rule 17: the first Continue Watching card, else the first Recent Channels card.
            val initialFocusShelf = orderedSections.firstOrNull { it in ContinueWatchingShelves }
                ?: AppHomeDashboardShelf.RECENT_CHANNELS.takeIf { it in orderedSections }
            val presentedSeriesFor: (PlaybackHistory) -> Series? = { history ->
                val rawSeriesId = history.seriesId ?: history.contentId
                if (
                    history.contentType == com.streamvault.domain.model.ContentType.SERIES ||
                    history.contentType == com.streamvault.domain.model.ContentType.SERIES_EPISODE
                ) {
                    uiState.continueWatchingSeries.firstOrNull { series ->
                        series.rawSeriesIdsForNavigation().contains(rawSeriesId)
                    }
                } else {
                    null
                }
            }
            val onContinueWatchingItemClick: (PlaybackHistory) -> Unit = { history ->
                presentedSeriesFor(history)?.let(onSeriesClick) ?: onPlaybackHistoryClick(history)
            }
            val continueWatchingMenu = ContinueWatchingMenuActions(
                onResume = onPlaybackHistoryClick,
                onRemove = viewModel::onRemoveContinueWatching,
                onOpenDetails = { history ->
                    presentedSeriesFor(history)?.let(onSeriesClick) ?: onOpenHistoryDetails(history)
                },
                onMarkWatched = viewModel::onMarkContinueWatchingWatched
            )

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = LocalTopBarContentInset.current, bottom = 28.dp)
            ) {
                item(key = "stremio_viewing") { stremioContent() }
                if (uiState.isLoading && orderedSections.isEmpty()) {
                    item(key = "dashboard_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp, bottom = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                }
                if (uiState.providerWarnings.isNotEmpty()) {
                    item(key = "provider_warnings") {
                        DashboardProviderWarningCard(
                            warnings = uiState.providerWarnings,
                            onOpenSettings = { onDestinationRequested(com.streamvault.core.navigation.AppDestination.Settings()) }
                        )
                    }
                }
                uiState.updateNotice?.let { updateNotice ->
                    item(key = "update_notice") {
                        DashboardUpdateCard(
                            notice = updateNotice,
                            onOpenSettings = { onDestinationRequested(AppDestination.Settings()) },
                            onInstallUpdate = viewModel::installDownloadedUpdate
                        )
                    }
                }
                items(
                    items = orderedSections,
                    key = { it.storageValue },
                    contentType = { it.storageValue }
                ) { section ->
                    when (section) {
                    AppHomeDashboardShelf.LIVE_SHORTCUTS -> DashboardShortcutRow(
                        title = stringResource(R.string.dashboard_live_shortcuts),
                        subtitle = stringResource(R.string.dashboard_live_shortcuts_subtitle),
                        shortcuts = uiState.liveShortcuts,
                        onShortcutClick = { shortcut ->
                            shortcut.categoryId?.let { categoryId ->
                                onDestinationRequested(AppDestination.LiveTv(categoryId))
                            } ?: onDestinationRequested(AppDestination.LiveTv())
                        }
                    )

                    AppHomeDashboardShelf.FAVORITE_CHANNELS -> FavoriteChannelsRow(
                        title = stringResource(R.string.dashboard_favorite_channels),
                        channels = uiState.favoriteChannels,
                        onSeeAll = { onDestinationRequested(AppDestination.LiveTv(com.streamvault.domain.model.VirtualCategoryIds.FAVORITES)) },
                        onChannelClick = { channel ->
                            onFavoriteChannelClick(channel, uiState.currentCombinedProfileId)
                        }
                    )

                    AppHomeDashboardShelf.RECENT_CHANNELS -> CategoryRow(
                        title = stringResource(R.string.dashboard_recent_channels),
                        items = uiState.recentChannels,
                        keySelector = { it.id },
                        onSeeAll = { onDestinationRequested(AppDestination.LiveTv(com.streamvault.domain.model.VirtualCategoryIds.RECENT)) }
                    ) { channel ->
                        ChannelCard(
                            channel = channel,
                            nowMs = nowMs,
                            isRecording = channel.id in recordingChannelIds,
                            isScheduledRecording = channel.id in scheduledChannelIds,
                            onClick = { onRecentChannelClick(channel, uiState.currentCombinedProfileId) },
                            onLongClick = { channelMenuTarget = channel },
                            modifier = if (
                                initialFocusShelf == AppHomeDashboardShelf.RECENT_CHANNELS &&
                                channel.id == uiState.recentChannels.firstOrNull()?.id
                            ) initialFocusModifier else Modifier,
                            focusRequester = remember(channel.id) {
                                recentChannelRequesters.getOrPut(channel.id) { FocusRequester() }
                            }
                        )
                    }

                    AppHomeDashboardShelf.CONTINUE_WATCHING -> ContinueWatchingRow(
                        items = uiState.continueWatching,
                        onItemClick = onContinueWatchingItemClick,
                        menu = continueWatchingMenu,
                        initialFocusModifier = initialFocusModifier.takeIf { initialFocusShelf == section } ?: Modifier
                    )

                    AppHomeDashboardShelf.PINNED_CATEGORIES -> PinnedCategoriesSection(
                        pinnedMovieCategories = uiState.pinnedMovieCategories,
                        pinnedSeriesCategories = uiState.pinnedSeriesCategories,
                        onMovieClick = onMovieClick,
                        onSeriesClick = onSeriesClick
                    )

                    AppHomeDashboardShelf.RECENT_MOVIES -> CategoryRow(
                        title = stringResource(R.string.dashboard_recent_movies),
                        items = uiState.recentMovies,
                        keySelector = { it.id },
                        onSeeAll = { onDestinationRequested(AppDestination.Movies) }
                    ) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }

                    AppHomeDashboardShelf.RECENT_SERIES -> CategoryRow(
                        title = stringResource(R.string.dashboard_recent_series),
                        items = uiState.recentSeries,
                        keySelector = { it.id },
                        onSeeAll = { onDestinationRequested(AppDestination.Series) }
                    ) { series ->
                        SeriesCard(
                            series = series,
                            subtitle = series.releaseDate ?: stringResource(R.string.dashboard_updated_series_badge),
                            onClick = { onSeriesClick(series) }
                        )
                    }

                    AppHomeDashboardShelf.FAVORITE_MOVIES -> CategoryRow(
                        title = stringResource(R.string.dashboard_favorite_movies),
                        items = uiState.favoriteMovies,
                        keySelector = { it.id },
                        onSeeAll = { onDestinationRequested(AppDestination.Movies) }
                    ) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }

                    AppHomeDashboardShelf.FAVORITE_SERIES -> CategoryRow(
                        title = stringResource(R.string.dashboard_favorite_series),
                        items = uiState.favoriteSeries,
                        keySelector = { it.id },
                        onSeeAll = { onDestinationRequested(AppDestination.Series) }
                    ) { series ->
                        SeriesCard(
                            series = series,
                            subtitle = series.releaseDate ?: stringResource(R.string.dashboard_updated_series_badge),
                            onClick = { onSeriesClick(series) }
                        )
                    }

                    AppHomeDashboardShelf.TRAKT_WATCHLIST_MOVIES -> CategoryRow(
                        title = stringResource(R.string.dashboard_trakt_watchlist_movies),
                        items = uiState.traktWatchlistMovies,
                        keySelector = { it.id },
                        onSeeAll = { onDestinationRequested(AppDestination.Movies) }
                    ) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }

                    AppHomeDashboardShelf.TRAKT_WATCHLIST_SERIES -> CategoryRow(
                        title = stringResource(R.string.dashboard_trakt_watchlist_series),
                        items = uiState.traktWatchlistSeries,
                        keySelector = { it.id },
                        onSeeAll = { onDestinationRequested(AppDestination.Series) }
                    ) { series ->
                        SeriesCard(
                            series = series,
                            subtitle = series.releaseDate ?: stringResource(R.string.dashboard_updated_series_badge),
                            onClick = { onSeriesClick(series) }
                        )
                    }

                    AppHomeDashboardShelf.CONTINUE_WATCHING_MOVIES -> ContinueWatchingRow(
                        title = stringResource(R.string.dashboard_continue_watching_movies),
                        items = uiState.continueWatchingMovies,
                        onItemClick = onPlaybackHistoryClick,
                        menu = continueWatchingMenu,
                        initialFocusModifier = initialFocusModifier.takeIf { initialFocusShelf == section } ?: Modifier
                    )

                    AppHomeDashboardShelf.CONTINUE_WATCHING_SERIES -> ContinueWatchingRow(
                        title = stringResource(R.string.dashboard_continue_watching_series),
                        items = uiState.continueWatchingSeriesItems,
                        onItemClick = onContinueWatchingItemClick,
                        menu = continueWatchingMenu,
                        initialFocusModifier = initialFocusModifier.takeIf { initialFocusShelf == section } ?: Modifier
                    )

                    AppHomeDashboardShelf.TOP_RATED_MOVIES -> CategoryRow(
                        title = stringResource(R.string.dashboard_top_rated_movies),
                        items = uiState.topRatedMovies,
                        keySelector = { it.id },
                        onSeeAll = { onDestinationRequested(AppDestination.Movies) }
                    ) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }

                    AppHomeDashboardShelf.RECOMMENDED_MOVIES -> CategoryRow(
                        title = stringResource(R.string.dashboard_recommended_movies),
                        items = uiState.recommendedMovies,
                        keySelector = { it.id },
                        onSeeAll = { onDestinationRequested(AppDestination.Movies) }
                    ) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }
            }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    channelMenuTarget?.let { channel ->
        val openLabel = stringResource(R.string.channel_menu_open)
        // The Recent shelf's channel rows do not re-emit when a favorite changes, so read the favorites shelf.
        val isFavorite = uiState.favoriteChannels.any { it.id == channel.id && it.providerId == channel.providerId }
        val favoriteLabel = stringResource(
            if (isFavorite) R.string.add_group_remove_favorites else R.string.add_group_add_favorites
        )
        val removeLabel = stringResource(R.string.channel_options_remove_from_recent)
        ActionMenuDialog(
            title = channel.name,
            entries = listOf(
                ActionMenuEntry(openLabel) { onRecentChannelClick(channel, uiState.currentCombinedProfileId) },
                ActionMenuEntry(favoriteLabel) { viewModel.onToggleChannelFavorite(channel, isFavorite) },
                ActionMenuEntry(removeLabel) {
                    val recent = uiState.recentChannels
                    val index = recent.indexOfFirst { it.id == channel.id }
                    val neighbor = recent.getOrNull(index + 1) ?: recent.getOrNull(index - 1)
                    recentRemovalHandoff = channel.id to neighbor?.id
                    viewModel.onRemoveRecentChannel(channel)
                }
            ),
            onDismissRequest = { channelMenuTarget = null }
        )
    }

    if (showHomeCustomizationDialog) {
        dashboardShelfCustomizationContent(
            uiState.homeDashboardShelves,
            { showHomeCustomizationDialog = false },
            { shelves ->
                viewModel.setHomeDashboardShelves(shelves)
                showHomeCustomizationDialog = false
            }
        )
    }
}

private fun Series.rawSeriesIdsForNavigation(): List<Long> =
    variants.map { it.rawSeriesId }.ifEmpty { listOf(selectedVariantId ?: id) }

@Composable
private fun DashboardShortcutRow(
    title: String,
    subtitle: String,
    shortcuts: List<DashboardLiveShortcut>,
    onShortcutClick: (DashboardLiveShortcut) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shortcuts, key = { "${it.type}:${it.categoryId}:${it.label}" }) { shortcut ->
                DashboardShortcutCard(
                    shortcut = shortcut,
                    onClick = { onShortcutClick(shortcut) }
                )
            }
        }
    }
}

@Composable
private fun DashboardShortcutCard(
    shortcut: DashboardLiveShortcut,
    onClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val isTelevisionDevice = rememberIsTelevisionDevice()
    val cardWidth = when {
        screenWidth < 700.dp -> 148.dp
        !isTelevisionDevice && screenWidth < 1280.dp -> 160.dp
        else -> 170.dp
    }
    val accentColor = when (shortcut.type) {
        DashboardShortcutType.FAVORITES -> Color(0xFFFFC857)
        DashboardShortcutType.RECENT -> Color(0xFF4FD1C5)
        DashboardShortcutType.LAST_GROUP -> Color(0xFF60A5FA)
        DashboardShortcutType.CUSTOM_GROUP -> Primary
    }

    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier
            .width(cardWidth)
            .height(76.dp),
        shape = ClickableSurfaceDefaults.shape(BrowseShapes.Control),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = SurfaceElevated,
            focusedContainerColor = SurfaceHighlight
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
                shape = BrowseShapes.Control
            ),
            focusedBorder = Border(
                border = BorderStroke(2.dp, FocusBorder),
                shape = BrowseShapes.Control
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(BrowseShapes.Control)
                        .background(accentColor)
                )
                Text(
                    text = shortcut.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = shortcut.detail,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DashboardProviderWarningCard(
    warnings: List<String>,
    onOpenSettings: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 6.dp),
        shape = BrowseShapes.Panel,
        colors = SurfaceDefaults.colors(containerColor = SurfaceElevated)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_warning_title),
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
            Text(
                text = warnings.take(3).joinToString(" | "),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceDim
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppDialogButton(
                    text = stringResource(R.string.dashboard_warning_review),
                    onClick = onOpenSettings,
                    primary = false
                )
            }
        }
    }
}

@Composable
private fun DashboardUpdateCard(
    notice: DashboardUpdateNotice,
    onOpenSettings: () -> Unit,
    onInstallUpdate: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 6.dp),
        shape = BrowseShapes.Panel,
        colors = SurfaceDefaults.colors(containerColor = SurfaceElevated)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_update_title, notice.latestVersionName),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = stringResource(
                    if (notice.installPermissionRequired) {
                        R.string.dashboard_update_install_permission_required
                    } else if (notice.installReady) {
                        R.string.dashboard_update_install_ready
                    } else {
                        R.string.dashboard_update_available
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceDim
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // The single primary on Home: an update is the one action that needs a decision.
                AppDialogButton(
                    primary = true,
                    text = stringResource(
                        if (notice.installPermissionRequired) {
                            R.string.dashboard_update_allow_installs
                        } else if (notice.installReady) {
                            R.string.dashboard_update_open_installer
                        } else {
                            R.string.dashboard_update_open_settings
                        }
                    ),
                    onClick = {
                        if (notice.installReady || notice.installPermissionRequired) {
                            onInstallUpdate()
                        } else {
                            onOpenSettings()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyDashboard(
    onAddProvider: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val addProviderFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // The button is composed in this same pass, so one frame is enough for it to be attached.
        withFrameNanos { }
        addProviderFocus.requestFocusSafely(tag = "DashboardScreen", target = "Add provider")
    }
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val isTelevisionDevice = rememberIsTelevisionDevice()
        val contentModifier = if (maxWidth < 900.dp) {
            Modifier.fillMaxWidth(0.9f)
        } else if (!isTelevisionDevice && maxWidth < 1280.dp) {
            Modifier.fillMaxWidth(0.76f)
        } else {
            Modifier.width(720.dp)
        }

        AppMessageState(
            title = stringResource(R.string.dashboard_empty_title),
            subtitle = stringResource(R.string.dashboard_empty_body),
            modifier = contentModifier,
            titleStyle = MaterialTheme.typography.headlineMedium,
            subtitleStyle = MaterialTheme.typography.titleMedium,
            action = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppDialogButton(
                        text = stringResource(R.string.settings_add_provider),
                        onClick = onAddProvider,
                        primary = true,
                        focusRequester = addProviderFocus
                    )
                    AppDialogButton(
                        text = stringResource(R.string.nav_settings),
                        onClick = onOpenSettings,
                        primary = false
                    )
                }
            }
        )
    }
}

@Composable
private fun PinnedCategoriesSection(
    pinnedMovieCategories: Map<String, List<Movie>>,
    pinnedSeriesCategories: Map<String, List<Series>>,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit
) {
    Column {
        pinnedMovieCategories.forEach { (categoryName, movies) ->
            if (movies.isNotEmpty()) {
                CategoryRow(
                    title = categoryName,
                    items = movies,
                    keySelector = { it.id }
                ) { movie ->
                    MovieCard(
                        movie = movie,
                        isLocked = false,
                        onClick = { onMovieClick(movie) },
                        onLongClick = {}
                    )
                }
            }
        }
        pinnedSeriesCategories.forEach { (categoryName, seriesList) ->
            if (seriesList.isNotEmpty()) {
                CategoryRow(
                    title = categoryName,
                    items = seriesList,
                    keySelector = { it.id }
                ) { series ->
                    SeriesCard(
                        series = series,
                        isLocked = false,
                        onClick = { onSeriesClick(series) },
                        onLongClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberDashboardSections(
    uiState: DashboardUiState
): List<AppHomeDashboardShelf> {
    return remember(
        uiState.homeDashboardShelves,
        uiState.liveShortcuts,
        uiState.favoriteChannels,
        uiState.recentChannels,
        uiState.continueWatching,
        uiState.continueWatchingMovies,
        uiState.continueWatchingSeriesItems,
        uiState.favoriteMovies,
        uiState.favoriteSeries,
        uiState.traktWatchlistMovies,
        uiState.traktWatchlistSeries,
        uiState.recentMovies,
        uiState.recentSeries,
        uiState.topRatedMovies,
        uiState.recommendedMovies,
        uiState.pinnedMovieCategories,
        uiState.pinnedSeriesCategories
    ) {
        resolveVisibleDashboardShelves(uiState)
    }
}

@Composable
private fun FavoriteChannelsRow(
    title: String,
    channels: List<Channel>,
    onSeeAll: () -> Unit,
    onChannelClick: (Channel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            TvClickableSurface(
                onClick = onSeeAll,
                shape = ClickableSurfaceDefaults.shape(BrowseShapes.Control),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Primary.copy(alpha = 0.12f),
                    focusedContainerColor = Primary.copy(alpha = 0.22f),
                    contentColor = TextTertiary
                )
            ) {
                Text(
                    text = stringResource(R.string.category_see_all),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(channels, key = { it.id }) { channel ->
                FavoriteChannelLogoCard(
                    channel = channel,
                    onClick = { onChannelClick(channel) }
                )
            }
        }
    }
}

@Composable
private fun FavoriteChannelLogoCard(
    channel: Channel,
    onClick: () -> Unit
) {
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.width(86.dp),
        shape = ClickableSurfaceDefaults.shape(BrowseShapes.Panel),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = SurfaceElevated,
            focusedContainerColor = SurfaceHighlight
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, FocusBorder),
                shape = BrowseShapes.Panel
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(BrowseShapes.Control)
            ) {
                ChannelLogoBadge(
                    channelName = channel.name,
                    logoUrl = channel.logoUrl,
                    shape = BrowseShapes.Control,
                    backgroundColor = AppColors.SurfaceEmphasis,
                    contentPadding = PaddingValues(8.dp),
                    textStyle = MaterialTheme.typography.labelLarge,
                    textColor = TextPrimary,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

