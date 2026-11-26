@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.streamvault.feature.catalog.presentation.search

import com.streamvault.core.ui.design.FocusSafeLazyRow
import com.streamvault.core.ui.design.focusRingButtonBorder
import com.streamvault.core.ui.design.secondaryButtonColors
import com.streamvault.feature.catalog.presentation.components.DetailCloseButton

import androidx.compose.foundation.layout.FlowRow

import com.streamvault.core.ui.layout.isPortraitLayout

import android.widget.Toast
import com.streamvault.core.ui.design.BrowseShapes
import com.streamvault.core.ui.design.selectableFocusBorder
import com.streamvault.core.ui.design.selectableSurfaceColors
import androidx.annotation.StringRes
import com.streamvault.core.ui.interaction.TvClickableSurface

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import com.streamvault.core.ui.design.FocusSafeLazyColumn
import com.streamvault.core.ui.design.FocusOverflowPadding
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.streamvault.core.ui.design.LocalAppSpacing
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.streamvault.core.ui.components.dialogs.AppDialog as Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.tv.material3.*
import com.streamvault.feature.catalog.R
import com.streamvault.feature.catalog.presentation.components.CategoryRow
import com.streamvault.core.ui.components.SearchInput
import com.streamvault.feature.catalog.presentation.components.ChannelCard
import com.streamvault.feature.catalog.presentation.components.CatalogChannelProgressTicker
import com.streamvault.feature.catalog.presentation.components.MovieCard
import com.streamvault.feature.catalog.presentation.components.SeriesCard
import com.streamvault.core.ui.components.TvEmptyState
import com.streamvault.feature.catalog.api.CatalogNavigationChrome
import com.streamvault.feature.catalog.api.CatalogScaffoldContent
import com.streamvault.feature.catalog.api.CatalogMessage
import com.streamvault.feature.catalog.api.CatalogUiEvent
import com.streamvault.core.ui.design.BrowseColors as AppColors
import com.streamvault.core.ui.design.requestFocusSafely
import com.streamvault.feature.catalog.presentation.catalogMessageText
import com.streamvault.core.ui.interaction.mouseClickable
import com.streamvault.core.ui.design.browse.*
import com.streamvault.domain.manager.ParentalControlManager
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.CatalogCompleteness
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.SearchHistoryScope
import com.streamvault.domain.model.Series
import com.streamvault.domain.repository.CategoryRepository
import com.streamvault.domain.repository.FavoriteRepository
import com.streamvault.domain.repository.ProviderRepository
import com.streamvault.domain.usecase.SearchContent
import com.streamvault.domain.usecase.SearchContentScope
import com.streamvault.domain.manager.RecordingManager
import com.streamvault.domain.model.RecordingStatus
import com.streamvault.domain.trakt.TraktFavorites
import com.streamvault.domain.trakt.TraktFavoriteActionResult
import com.streamvault.domain.trakt.TraktFavoriteFailure
import com.streamvault.domain.trakt.TraktFavoriteIdentity
import com.streamvault.domain.trakt.TraktFavoriteType
import com.streamvault.domain.util.AdultContentVisibilityPolicy
import com.streamvault.core.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val searchContent: SearchContent,
    private val preferencesRepository: com.streamvault.data.preferences.PreferencesRepository,
    private val parentalControlManager: ParentalControlManager,
    private val favoriteRepository: FavoriteRepository,
    private val categoryRepository: CategoryRepository,
    private val recordingManager: RecordingManager,
    private val traktFavorites: TraktFavorites,
) : ViewModel() {
    private companion object {
        const val MAX_RESULTS_PER_SECTION = 120
        const val MAX_RECENT_QUERIES = 6
    }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedTab = MutableStateFlow(SearchTab.ALL)
    val selectedTab: StateFlow<SearchTab> = _selectedTab.asStateFlow()

    // Each scope keeps its own sort and filters. They survive new queries and tab switches, not leaving the screen.
    private val _refinementsByTab = MutableStateFlow<Map<SearchTab, SearchRefinements>>(emptyMap())
    val refinements: StateFlow<SearchRefinements> = combine(_selectedTab, _refinementsByTab) { tab, byTab ->
        byTab[tab] ?: SearchRefinements()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchRefinements())

    private val _parentalControlLevel = MutableStateFlow(0)
    private val _activeProviderId = MutableStateFlow<Long?>(null)

    private val _recordingChannelIds = MutableStateFlow<Set<Long>>(emptySet())
    val recordingChannelIds: StateFlow<Set<Long>> = _recordingChannelIds.asStateFlow()

    private val _scheduledChannelIds = MutableStateFlow<Set<Long>>(emptySet())
    val scheduledChannelIds: StateFlow<Set<Long>> = _scheduledChannelIds.asStateFlow()

    private val _uiEvents = MutableSharedFlow<CatalogUiEvent>()
    val uiEvents: SharedFlow<CatalogUiEvent> = _uiEvents.asSharedFlow()

    private val unlockedCategoryIds = providerRepository.getActiveProvider()
        .onEach { provider -> _activeProviderId.value = provider?.id }
        .flatMapLatest { provider ->
            provider?.let { parentalControlManager.unlockedCategoriesForProvider(it.id) } ?: flowOf(emptySet())
        }

    private val searchProviderWithHiddenCategoryIds = providerRepository.getActiveProvider()
        .flatMapLatest { provider ->
            if (provider == null) {
                flowOf(SearchProviderVisibility())
            } else {
                combine(
                    preferencesRepository.getHiddenCategoryIds(provider.id, ContentType.MOVIE),
                    preferencesRepository.getHiddenCategoryIds(provider.id, ContentType.SERIES)
                ) { movieIds, seriesIds ->
                    SearchProviderVisibility(provider, movieIds, seriesIds)
                }
            }
        }

    init {
        viewModelScope.launch {
            preferencesRepository.parentalControlLevel.collect { level ->
                _parentalControlLevel.value = level
            }
        }
        viewModelScope.launch {
            recordingManager.observeRecordingItems().collect { items ->
                _recordingChannelIds.value = items
                    .filter { it.status == RecordingStatus.RECORDING }
                    .map { it.channelId }.toSet()
                _scheduledChannelIds.value = items
                    .filter { it.status == RecordingStatus.SCHEDULED }
                    .map { it.channelId }.toSet()
            }
        }
    }

    val recentQueries: StateFlow<List<String>> = combine(
        _selectedTab,
        _activeProviderId
    ) { tab, providerId ->
        tab.toSearchHistoryScope() to providerId
    }.flatMapLatest { (scope, providerId) ->
        preferencesRepository.getRecentSearchQueries(
            scope = scope,
            providerId = providerId,
            limit = MAX_RECENT_QUERIES
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Search results as returned by the use case, before the user's sort and filters. */
    @OptIn(FlowPreview::class)
    private val searchFilterParams = combine(
        searchProviderWithHiddenCategoryIds,
        _query.debounce(300),
        _selectedTab,
        _parentalControlLevel,
        unlockedCategoryIds
    ) { providerVisibility, query, tab, level, unlockedIds ->
        SearchFilterParams(
            provider = providerVisibility.provider,
            query = query,
            tab = tab,
            level = level,
            unlockedCategoryIds = unlockedIds,
            hiddenMovieCategoryIds = providerVisibility.hiddenMovieCategoryIds,
            hiddenSeriesCategoryIds = providerVisibility.hiddenSeriesCategoryIds
        )
    }

    val unrefinedState: StateFlow<SearchUiState> = searchFilterParams
        .distinctUntilChanged().flatMapLatest { params ->
        val provider = params.provider
        val query = params.query
        val tab = params.tab
        val level = params.level
        val unlockedIds = params.unlockedCategoryIds

        val trimmedQueryLength = query.trim().length
        if (provider == null || trimmedQueryLength < 2) {
            flowOf(
                SearchUiState(
                    parentalControlLevel = level,
                    hasActiveProvider = provider != null,
                    queryLength = trimmedQueryLength,
                    unlockedCategoryIds = unlockedIds
                )
            )
        } else {
            searchContent(
                providerId = provider.id,
                query = query,
                scope = tab.toSearchScope(),
                maxResultsPerSection = MAX_RESULTS_PER_SECTION
            ).map { results ->
                val filterAdult = !AdultContentVisibilityPolicy.showInAggregatedSurfaces(level)
                SearchUiState(
                    channels = if (filterAdult)
                        results.channels.filterNot { it.isAdult || it.isUserProtected }
                    else results.channels,
                    movies = results.movies.filter { movie ->
                        movie.categoryId?.let(params.hiddenMovieCategoryIds::contains) != true &&
                            (!filterAdult || (!movie.isAdult && !movie.isUserProtected))
                    },
                    series = results.series.filter { series ->
                        series.categoryId?.let(params.hiddenSeriesCategoryIds::contains) != true &&
                            (!filterAdult || (!series.isAdult && !series.isUserProtected))
                    },
                    isLoading = false,
                    hasSearched = true,
                    hasSearchError = results.isPartialResult &&
                        results.catalogCompleteness == CatalogCompleteness.COMPLETE,
                    catalogCompleteness = results.catalogCompleteness,
                    parentalControlLevel = level,
                    hasActiveProvider = true,
                    queryLength = trimmedQueryLength,
                    unlockedCategoryIds = unlockedIds
                )
            }.onStart {
                emit(
                    SearchUiState(
                        isLoading = true,
                        hasSearched = true,
                        parentalControlLevel = level,
                        hasActiveProvider = true,
                        queryLength = trimmedQueryLength,
                        unlockedCategoryIds = unlockedIds
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    val uiState: StateFlow<SearchUiState> = combine(unrefinedState, refinements) { state, refinements ->
        state.withRefinements(refinements)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun setSort(sort: SearchSort) = updateRefinements { it.copy(sort = sort) }

    fun applyFilters(filters: SearchFilters) = updateRefinements { it.copy(filters = filters) }

    private fun updateRefinements(transform: (SearchRefinements) -> SearchRefinements) {
        val tab = _selectedTab.value
        _refinementsByTab.update { byTab -> byTab + (tab to transform(byTab[tab] ?: SearchRefinements())) }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onSearchSubmitted() {
        val normalizedQuery = _query.value.trim()
        if (normalizedQuery.length < 2) return

        _query.value = normalizedQuery
        viewModelScope.launch {
            preferencesRepository.recordRecentSearchQuery(
                query = normalizedQuery,
                scope = _selectedTab.value.toSearchHistoryScope(),
                providerId = _activeProviderId.value
            )
        }
    }

    fun onRecentQuerySelected(query: String) {
        _query.value = query
        onSearchSubmitted()
    }

    fun submitExternalQuery(query: String) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.length < 2) return

        _query.value = normalizedQuery
        onSearchSubmitted()
    }

    fun buildCompleteStalkerSearchIndex() {
        val providerId = _activeProviderId.value ?: return
        viewModelScope.launch {
            providerRepository.buildStalkerSearchIndexOnce(providerId)
        }
    }

    fun clearRecentQueries() {
        viewModelScope.launch {
            preferencesRepository.clearRecentSearchQueries(
                scope = _selectedTab.value.toSearchHistoryScope(),
                providerId = _activeProviderId.value
            )
        }
    }

    fun onTabSelected(tab: SearchTab) {
        _selectedTab.value = tab
    }

    suspend fun verifyPin(pin: String): Boolean {
        return preferencesRepository.verifyParentalPin(pin)
    }

    fun unlockCategory(categoryId: Long?) {
        val providerId = _activeProviderId.value ?: return
        val resolvedCategoryId = categoryId ?: return
        parentalControlManager.unlockCategory(providerId, resolvedCategoryId)
    }

    // ג”€ג”€ Search item long-press actions ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€

    fun toggleFavorite(
        contentId: Long,
        contentType: ContentType,
        currentlyFavorite: Boolean,
        tmdbId: Long? = null,
    ) {
        viewModelScope.launch {
            val providerId = _activeProviderId.value ?: return@launch
            val favorited = !currentlyFavorite
            if (!favorited) {
                favoriteRepository.removeFavorite(providerId, contentId, contentType)
            } else {
                favoriteRepository.addFavorite(providerId, contentId, contentType)
            }
            val traktType = when (contentType) {
                ContentType.MOVIE -> TraktFavoriteType.MOVIE
                ContentType.SERIES -> TraktFavoriteType.SHOW
                else -> null
            }
            tmdbId?.takeIf { it > 0L }?.let { id ->
                traktType?.let { type ->
                    when (val result = traktFavorites.setFavorited(TraktFavoriteIdentity(type, id), favorited)) {
                        TraktFavoriteActionResult.Success -> Unit
                        is TraktFavoriteActionResult.Failure -> if (
                            result.reason != TraktFavoriteFailure.DISCONNECTED &&
                            result.reason != TraktFavoriteFailure.INCOGNITO
                        ) {
                            _uiEvents.emit(CatalogUiEvent.ShowMessage(CatalogMessage.TraktFavoriteSyncFailed))
                        }
                    }
                }
            }
        }
    }

    fun hideItemCategory(categoryId: Long, contentType: ContentType) {
        val providerId = _activeProviderId.value ?: return
        viewModelScope.launch {
            preferencesRepository.setCategoryHidden(
                providerId = providerId,
                type = contentType,
                categoryId = categoryId,
                hidden = true
            )
        }
    }

    fun toggleCategoryProtection(categoryId: Long, contentType: ContentType, currentlyProtected: Boolean) {
        val providerId = _activeProviderId.value ?: return
        viewModelScope.launch {
            categoryRepository.setCategoryProtection(
                providerId = providerId,
                categoryId = categoryId,
                type = contentType,
                isProtected = !currentlyProtected
            )
        }
    }
}

private data class SearchFilterParams(
    val provider: com.streamvault.domain.model.LegacyProvider?,
    val query: String,
    val tab: SearchTab,
    val level: Int,
    val unlockedCategoryIds: Set<Long>,
    val hiddenMovieCategoryIds: Set<Long> = emptySet(),
    val hiddenSeriesCategoryIds: Set<Long> = emptySet()
)

private data class SearchProviderVisibility(
    val provider: com.streamvault.domain.model.LegacyProvider? = null,
    val hiddenMovieCategoryIds: Set<Long> = emptySet(),
    val hiddenSeriesCategoryIds: Set<Long> = emptySet()
)

private fun SearchUiState.withRefinements(refinements: SearchRefinements): SearchUiState {
    val refined = SearchResultSet(channels, movies, series).refined(refinements)
    return copy(
        channels = refined.channels,
        movies = refined.movies,
        series = refined.series,
        unfilteredTotal = totalResults
    )
}

enum class SearchTab(@get:StringRes val titleRes: Int) {
    ALL(R.string.search_all),
    LIVE(R.string.search_live_tv),
    MOVIES(R.string.search_movies),
    SERIES(R.string.search_series)
}

private fun SearchTab.toSearchScope(): SearchContentScope = when (this) {
    SearchTab.ALL -> SearchContentScope.ALL
    SearchTab.LIVE -> SearchContentScope.LIVE
    SearchTab.MOVIES -> SearchContentScope.MOVIES
    SearchTab.SERIES -> SearchContentScope.SERIES
}

private fun SearchTab.toSearchHistoryScope(): SearchHistoryScope = when (this) {
    SearchTab.ALL -> SearchHistoryScope.ALL
    SearchTab.LIVE -> SearchHistoryScope.LIVE
    SearchTab.MOVIES -> SearchHistoryScope.MOVIE
    SearchTab.SERIES -> SearchHistoryScope.SERIES
}

data class SearchUiState(
    val channels: List<Channel> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val series: List<Series> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val hasSearchError: Boolean = false,
    val parentalControlLevel: Int = 0,
    val hasActiveProvider: Boolean = false,
    val queryLength: Int = 0,
    val unlockedCategoryIds: Set<Long> = emptySet(),
    val catalogCompleteness: CatalogCompleteness = CatalogCompleteness.COMPLETE,
    // Result count before the user's filters. Above zero while isEmpty means the filters hid everything.
    val unfilteredTotal: Int = 0
) {
    val isEmpty: Boolean get() = hasSearched && channels.isEmpty() && movies.isEmpty() && series.isEmpty()
    val totalResults: Int get() = channels.size + movies.size + series.size
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String = "",
    onChannelClick: (Channel) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit,
    scaffold: CatalogScaffoldContent,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val recentQueries by viewModel.recentQueries.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recordingChannelIds by viewModel.recordingChannelIds.collectAsStateWithLifecycle()
    val scheduledChannelIds by viewModel.scheduledChannelIds.collectAsStateWithLifecycle()
    val nowMs by CatalogChannelProgressTicker.nowMs.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }
    var pendingChannel by remember { mutableStateOf<Channel?>(null) }
    var pendingMovie by remember { mutableStateOf<Movie?>(null) }
    var pendingSeries by remember { mutableStateOf<Series?>(null) }
    val scope = rememberCoroutineScope()
    val selectedStateLabel = stringResource(R.string.a11y_selected)
    val incorrectPinMessage = stringResource(R.string.search_incorrect_pin)

    LaunchedEffect(viewModel, context) {
        viewModel.uiEvents.collect { event ->
            if (event is CatalogUiEvent.ShowMessage) {
                Toast.makeText(context, context.catalogMessageText(event.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ג”€ג”€ Long-press actions dialog state ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€
    var showActionsDialog by remember { mutableStateOf(false) }
    var actionsChannel by remember { mutableStateOf<Channel?>(null) }
    var actionsMovie by remember { mutableStateOf<Movie?>(null) }
    var actionsSeries by remember { mutableStateOf<Series?>(null) }
    var actionsIsFavorite by remember { mutableStateOf(false) }
    // Separate PIN purpose: unlock-to-play vs toggle-parental-protection
    var pinIsForProtectionToggle by remember { mutableStateOf(false) }
    var pendingProtectionCategoryId by remember { mutableStateOf<Long?>(null) }
    var pendingProtectionContentType by remember { mutableStateOf<ContentType?>(null) }
    var pendingProtectionCurrentlyProtected by remember { mutableStateOf(false) }

    fun showChannelActions(channel: Channel) {
        actionsChannel = channel; actionsMovie = null; actionsSeries = null
        actionsIsFavorite = channel.isFavorite
        showActionsDialog = true
    }
    fun showMovieActions(movie: Movie) {
        actionsChannel = null; actionsMovie = movie; actionsSeries = null
        actionsIsFavorite = movie.isFavorite
        showActionsDialog = true
    }
    fun showSeriesActions(series: Series) {
        actionsChannel = null; actionsMovie = null; actionsSeries = series
        actionsIsFavorite = series.isFavorite
        showActionsDialog = true
    }
    val portrait = isPortraitLayout()
    val inputPaneWidth = if (portrait) 0 else (SearchInputPaneWidth + SearchPaneGap).value.toInt()
    val availableWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp - 64 - inputPaneWidth
    val channelColumns = (availableWidth / 240).coerceIn(1, 4)
    val posterColumns = (availableWidth / 148).coerceIn(1, 6)
    val channelRows = remember(uiState.channels, channelColumns) { uiState.channels.chunked(channelColumns) }
    val movieRows = remember(uiState.movies, posterColumns) { uiState.movies.chunked(posterColumns) }
    val seriesRows = remember(uiState.series, posterColumns) { uiState.series.chunked(posterColumns) }

    fun isLocked(categoryId: Long?, isAdult: Boolean, isUserProtected: Boolean): Boolean {
        if (uiState.parentalControlLevel != 1) {
            return false
        }
        if (!isAdult && !isUserProtected) {
            return false
        }
        return categoryId == null || categoryId !in uiState.unlockedCategoryIds
    }

    LaunchedEffect(Unit) {
        searchFocusRequester.requestFocusSafely(tag = "SearchScreen", target = "Search field")
    }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            viewModel.submitExternalQuery(initialQuery)
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(showPinDialog) {
        if (!showPinDialog) {
            searchFocusRequester.requestFocusSafely(tag = "SearchScreen", target = "Search field")
        }
    }

    if (showPinDialog) {
        com.streamvault.core.ui.components.dialogs.PinDialog(
            title = stringResource(R.string.pin_dialog_title),
            cancelLabel = stringResource(R.string.pin_dialog_cancel),
            onDismissRequest = {
                showPinDialog = false
                pinError = null
                pendingChannel = null
                pendingMovie = null
                pendingSeries = null
                pinIsForProtectionToggle = false
                pendingProtectionCategoryId = null
                pendingProtectionContentType = null
            },
            onPinEntered = { pin ->
                scope.launch {
                    if (viewModel.verifyPin(pin)) {
                        if (pinIsForProtectionToggle) {
                            val catId = pendingProtectionCategoryId
                            val ct = pendingProtectionContentType
                            if (catId != null && ct != null) {
                                viewModel.toggleCategoryProtection(catId, ct, pendingProtectionCurrentlyProtected)
                            }
                            showPinDialog = false
                            pinError = null
                            pinIsForProtectionToggle = false
                            pendingProtectionCategoryId = null
                            pendingProtectionContentType = null
                        } else {
                            pendingChannel?.categoryId?.let(viewModel::unlockCategory)
                            pendingMovie?.categoryId?.let(viewModel::unlockCategory)
                            pendingSeries?.categoryId?.let(viewModel::unlockCategory)
                            showPinDialog = false
                            pinError = null
                            pendingChannel?.let { onChannelClick(it) }
                            pendingMovie?.let { onMovieClick(it) }
                            pendingSeries?.let { onSeriesClick(it) }
                            pendingChannel = null
                            pendingMovie = null
                            pendingSeries = null
                        }
                    } else {
                        pinError = incorrectPinMessage
                    }
                }
            },
            error = pinError
        )
    }

    val resultsFocusRequester = remember { FocusRequester() }
    val hasResults = uiState.hasActiveProvider && uiState.queryLength >= 2 && !uiState.isLoading && !uiState.isEmpty
    var searchFieldFocused by remember { mutableStateOf(true) }
    var resultsFocused by remember { mutableStateOf(false) }
    var focusedResultRow by remember { mutableIntStateOf(0) }
    fun onResultRowFocused(row: Int) { focusedResultRow = row }
    // Left leaves for the input pane only from the first card of a row; the legend says so.
    var resultAtRowStart by remember { mutableStateOf(true) }
    fun onResultColumnFocused(atStart: Boolean) { resultAtRowStart = atStart }
    var leftPaneFocused by remember { mutableStateOf(true) }
    var tabsFocused by remember { mutableStateOf(false) }
    var refineFocused by remember { mutableStateOf(false) }
    val hintContext = when {
        searchFieldFocused -> SearchHintContext.FIELD
        leftPaneFocused -> SearchHintContext.LEFT_PANE
        resultsFocused -> if (focusedResultRow <= 0) SearchHintContext.TOP_RESULT else SearchHintContext.RESULTS
        refineFocused -> SearchHintContext.REFINE
        tabsFocused -> SearchHintContext.TABS
        else -> null
    }
    val filteredEmpty = uiState.isEmpty && uiState.unfilteredTotal > 0 && !uiState.hasSearchError
    // Filters stay reachable while they hide everything, otherwise the user could not undo them.
    val showRefinementBar = uiState.hasActiveProvider && uiState.queryLength >= 2 && !uiState.isLoading &&
        (hasResults || filteredEmpty)

    val refinements by viewModel.refinements.collectAsStateWithLifecycle()
    var refinementSheet by remember { mutableStateOf<SearchRefinementSheet?>(null) }
    var lastRefinementSheet by remember { mutableStateOf<SearchRefinementSheet?>(null) }
    var focusRemoveFiltersOnClose by remember { mutableStateOf(false) }
    val filteredEmptyNow by rememberUpdatedState(filteredEmpty)
    val sortFocusRequester = remember { FocusRequester() }
    val filtersFocusRequester = remember { FocusRequester() }
    val removeFiltersFocusRequester = remember { FocusRequester() }

    // Back out of a sheet lands on the button that opened it. If the applied filters hid every result, the
    // "Remove filters" action is the way forward, so focus goes there once it is composed.
    LaunchedEffect(refinementSheet) {
        val sheet = refinementSheet
        if (sheet != null) {
            lastRefinementSheet = sheet
            return@LaunchedEffect
        }
        val closed = lastRefinementSheet ?: return@LaunchedEffect
        lastRefinementSheet = null
        if (focusRemoveFiltersOnClose) {
            focusRemoveFiltersOnClose = false
            withTimeoutOrNull(REMOVE_FILTERS_FOCUS_TIMEOUT_MS) { snapshotFlow { filteredEmptyNow }.first { it } }
            withFrameNanos { }
            removeFiltersFocusRequester.requestFocusSafely(tag = "SearchScreen", target = "Remove filters")
        } else {
            val target = if (closed == SearchRefinementSheet.SORT) sortFocusRequester else filtersFocusRequester
            target.requestFocusSafely(tag = "SearchScreen", target = "Refinement button")
        }
    }

    when (refinementSheet) {
        SearchRefinementSheet.SORT -> SearchSortSheet(
            tab = selectedTab,
            current = refinements.sort,
            onSelect = { sort ->
                viewModel.setSort(sort)
                refinementSheet = null
            },
            onDismiss = { refinementSheet = null }
        )

        SearchRefinementSheet.FILTERS -> {
            val unrefined by viewModel.unrefinedState.collectAsStateWithLifecycle()
            val unrefinedResults = remember(unrefined.channels, unrefined.movies, unrefined.series) {
                SearchResultSet(unrefined.channels, unrefined.movies, unrefined.series)
            }
            val options = remember(unrefinedResults, refinements.filters) {
                unrefinedResults.filterOptions(refinements.filters)
            }
            SearchFiltersSheet(
                initial = refinements.filters,
                options = options,
                unrefined = unrefinedResults,
                    onApply = { filters, matchCount ->
                    if (filters != refinements.filters) viewModel.applyFilters(filters)
                    focusRemoveFiltersOnClose = matchCount == 0 && filters.isActive
                    refinementSheet = null
                },
                onDismiss = { refinementSheet = null }
            )
        }

        null -> Unit
    }

    // ג”€ג”€ Long-press actions dialog ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€
    if (showActionsDialog) {
        val actionsTitle = actionsChannel?.name ?: actionsMovie?.name ?: actionsSeries?.name ?: ""
        val actionsCategoryId = actionsChannel?.categoryId ?: actionsMovie?.categoryId ?: actionsSeries?.categoryId
        val actionsIsProtected = actionsChannel?.isUserProtected ?: actionsMovie?.isUserProtected ?: actionsSeries?.isUserProtected ?: false
        val actionsContentType = when {
            actionsChannel != null -> ContentType.LIVE
            actionsMovie != null -> ContentType.MOVIE
            else -> ContentType.SERIES
        }
        val actionsItemId = actionsChannel?.id ?: actionsMovie?.id ?: actionsSeries?.id ?: 0L
        SearchItemActionsDialog(
            title = actionsTitle,
            isFavorite = actionsIsFavorite,
            isProtected = actionsIsProtected,
            hasCategoryId = actionsCategoryId != null,
            onDismiss = { showActionsDialog = false },
            onToggleFavorite = {
                viewModel.toggleFavorite(
                    actionsItemId,
                    actionsContentType,
                    actionsIsFavorite,
                    actionsMovie?.tmdbId ?: actionsSeries?.tmdbId,
                )
                actionsIsFavorite = !actionsIsFavorite
            },
            onHide = if (actionsCategoryId != null) {
                {
                    viewModel.hideItemCategory(actionsCategoryId, actionsContentType)
                    showActionsDialog = false
                }
            } else null,
            onToggleLock = if (actionsCategoryId != null) {
                {
                    showActionsDialog = false
                    pendingProtectionCategoryId = actionsCategoryId
                    pendingProtectionContentType = actionsContentType
                    pendingProtectionCurrentlyProtected = actionsIsProtected
                    pinIsForProtectionToggle = true
                    showPinDialog = true
                }
            } else null
        )
    }

    scaffold(
        AppDestination.Search(),
        stringResource(R.string.search_title),
        stringResource(R.string.search_screen_subtitle),
        CatalogNavigationChrome.TopBar,
        true,
        true,
        false
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchPanes(
                modifier = Modifier.weight(1f),
                inputPane = {
                    Box(modifier = Modifier.onFocusChanged { leftPaneFocused = it.hasFocus }) {
                        SearchInputPane(
                            query = query,
                            hasResults = hasResults,
                            recentQueries = recentQueries,
                            onQueryChange = viewModel::onQueryChange,
                            onSearch = viewModel::onSearchSubmitted,
                            onRecentQuerySelected = viewModel::onRecentQuerySelected,
                            onClearRecentQueries = viewModel::clearRecentQueries,
                            focusRequester = searchFocusRequester,
                            resultsFocusRequester = resultsFocusRequester,
                            selectedStateLabel = selectedStateLabel,
                            onFieldFocusChanged = { searchFieldFocused = it }
                        )
                    }
                },
                resultsPane = {
                    // Sort and Filters sit at the end of the tab row as one group; the weighted tabs keep them apart.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LocalAppSpacing.current.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SearchScopeTabs(
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { tabsFocused = it.hasFocus },
                            selectedTab = selectedTab,
                            resultCount = if (hasResults) uiState.totalResults else null,
                            onTabSelected = viewModel::onTabSelected,
                            selectedStateLabel = selectedStateLabel
                        )
                        if (showRefinementBar) {
                            SearchRefinementBar(
                                modifier = Modifier.onFocusChanged { refineFocused = it.hasFocus },
                                refinements = refinements,
                                onSortClick = { refinementSheet = SearchRefinementSheet.SORT },
                                onFiltersClick = { refinementSheet = SearchRefinementSheet.FILTERS },
                                sortFocusRequester = sortFocusRequester,
                                filtersFocusRequester = filtersFocusRequester
                            )
                        }
                    }
                    // Fades the rows at the edges they scroll under. The list is widened by the focus overflow and padded
                    // back, so the offscreen layer of the fade does not clip the focus ring of a card at the left edge.
                    FocusSafeLazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .bleedHorizontally(FocusOverflowPadding)
                            .focusRequester(resultsFocusRequester)
                            .onFocusChanged { resultsFocused = it.hasFocus }
                            .focusGroup(),
                        verticalArrangement = Arrangement.spacedBy(LocalAppSpacing.current.xs),
                        contentPadding = PaddingValues(
                            start = FocusOverflowPadding,
                            end = FocusOverflowPadding,
                            bottom = LocalAppSpacing.current.xs
                        ),
                        fadeHeight = LocalAppSpacing.current.md
                    ) {
                        when {
                            !uiState.hasActiveProvider -> {
                                item {
                                    SearchMessageState(
                                        title = stringResource(R.string.search_no_provider_title),
                                        subtitle = stringResource(R.string.search_no_provider_subtitle)
                                    )
                                }
                            }

                            uiState.queryLength < 2 -> {
                                item {
                                    SearchMessageState(
                                        title = stringResource(R.string.search_type_to_search),
                                        subtitle = stringResource(R.string.search_command_subtitle)
                                    )
                                }
                            }

                            uiState.isLoading -> {
                                item {
                                    SearchMessageState(
                                        title = stringResource(R.string.search_loading_title),
                                        subtitle = stringResource(R.string.search_loading_subtitle)
                                    )
                                }
                            }

                            uiState.isEmpty && uiState.hasSearchError -> {
                                item {
                                    SearchMessageState(
                                        title = stringResource(R.string.search_error_title),
                                        subtitle = stringResource(R.string.search_error_subtitle)
                                    )
                                }
                            }

                            filteredEmpty -> {
                                item {
                                    Column {
                                        SearchMessageState(
                                            title = stringResource(R.string.search_no_results_title),
                                            subtitle = stringResource(R.string.search_no_results_filtered)
                                        )
                                        SearchFilteredEmptyAction(
                                            activeFilters = refinements.filters.activeCount,
                                            focusRequester = removeFiltersFocusRequester,
                                            onRemoveFilters = {
                                                // The button disappears with the empty state, so hand focus to a button that stays.
                                                filtersFocusRequester.requestFocusSafely(tag = "SearchScreen", target = "Filters button")
                                                viewModel.applyFilters(SearchFilters())
                                            }
                                        )
                                    }
                                }
                            }

                            uiState.isEmpty -> {
                                item {
                                    SearchMessageState(
                                        title = stringResource(R.string.search_no_results_title),
                                        subtitle = stringResource(R.string.search_no_results, query)
                                    )
                                }
                            }

                            else -> {
                                if (uiState.catalogCompleteness != CatalogCompleteness.COMPLETE) {
                                    item {
                                        SearchCatalogNotice(
                                            completeness = uiState.catalogCompleteness,
                                            onBuildCompleteIndex = viewModel::buildCompleteStalkerSearchIndex,
                                            modifier = Modifier.trackResultRow(0, ::onResultRowFocused)
                                        )
                                    }
                                }

                                if (selectedTab == SearchTab.ALL) {
                                    if (uiState.channels.isNotEmpty()) {
                                        item {
                                            SearchResultRail(
                                                title = stringResource(R.string.search_live_tv),
                                                items = uiState.channels.subList(0, minOf(uiState.channels.size, 18)),
                                                keySelector = { it.id },
                                                onColumnFocused = ::onResultColumnFocused,
                                                modifier = Modifier.trackResultRow(0, ::onResultRowFocused)
                                            ) { channel ->
                                                val channelLocked = isLocked(
                                                    categoryId = channel.categoryId,
                                                    isAdult = channel.isAdult,
                                                    isUserProtected = channel.isUserProtected
                                                )
                                                ChannelCard(
                                                    channel = channel,
                                                    nowMs = nowMs,
                                                    isLocked = channelLocked,
                                                    isRecording = channel.id in recordingChannelIds,
                                                    isScheduledRecording = channel.id in scheduledChannelIds,
                                                    onClick = {
                                                        if (channelLocked) {
                                                            pendingChannel = channel
                                                            showPinDialog = true
                                                        } else {
                                                            onChannelClick(channel)
                                                        }
                                                    },
                                                    onLongClick = { showChannelActions(channel) }
                                                )
                                            }
                                        }
                                    }

                                    if (uiState.movies.isNotEmpty()) {
                                        item {
                                            SearchResultRail(
                                                title = stringResource(R.string.search_movies),
                                                items = uiState.movies.subList(0, minOf(uiState.movies.size, 18)),
                                                keySelector = { it.id },
                                                onColumnFocused = ::onResultColumnFocused,
                                                modifier = Modifier.trackResultRow(
                                                    if (uiState.channels.isNotEmpty()) 1 else 0,
                                                    ::onResultRowFocused
                                                )
                                            ) { movie ->
                                                val movieLocked = isLocked(
                                                    categoryId = movie.categoryId,
                                                    isAdult = movie.isAdult,
                                                    isUserProtected = movie.isUserProtected
                                                )
                                                MovieCard(
                                                    movie = movie,
                                                    isLocked = movieLocked,
                                                    onClick = {
                                                        if (movieLocked) {
                                                            pendingMovie = movie
                                                            showPinDialog = true
                                                        } else {
                                                            onMovieClick(movie)
                                                        }
                                                    },
                                                    onLongClick = { showMovieActions(movie) }
                                                )
                                            }
                                        }
                                    }

                                    if (uiState.series.isNotEmpty()) {
                                        item {
                                            SearchResultRail(
                                                title = stringResource(R.string.search_series),
                                                items = uiState.series.subList(0, minOf(uiState.series.size, 18)),
                                                keySelector = { it.id },
                                                onColumnFocused = ::onResultColumnFocused,
                                                modifier = Modifier.trackResultRow(
                                                    (if (uiState.channels.isNotEmpty()) 1 else 0) +
                                                        (if (uiState.movies.isNotEmpty()) 1 else 0),
                                                    ::onResultRowFocused
                                                )
                                            ) { seriesItem ->
                                                val seriesLocked = isLocked(
                                                    categoryId = seriesItem.categoryId,
                                                    isAdult = seriesItem.isAdult,
                                                    isUserProtected = seriesItem.isUserProtected
                                                )
                                                SeriesCard(
                                                    series = seriesItem,
                                                    isLocked = seriesLocked,
                                                    onClick = {
                                                        if (seriesLocked) {
                                                            pendingSeries = seriesItem
                                                            showPinDialog = true
                                                        } else {
                                                            onSeriesClick(seriesItem)
                                                        }
                                                    },
                                                    onLongClick = { showSeriesActions(seriesItem) }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    item {
                                        SectionHeader(
                                            title = when (selectedTab) {
                                                SearchTab.ALL -> stringResource(R.string.search_all)
                                                SearchTab.LIVE -> stringResource(R.string.search_live_tv)
                                                SearchTab.MOVIES -> stringResource(R.string.search_movies)
                                                SearchTab.SERIES -> stringResource(R.string.search_series)
                                            }
                                        )
                                    }

                                    when (selectedTab) {
                                        SearchTab.ALL -> Unit
                                        SearchTab.LIVE -> itemsIndexed(
                                            items = channelRows,
                                            key = { _, row -> row.joinToString("-") { it.id.toString() } },
                                            contentType = { _, _ -> "search_live_row" }
                                        ) { rowIndex, row ->
                                            SearchChannelGridRow(
                                                modifier = Modifier.trackResultRow(rowIndex, ::onResultRowFocused),
                                                onColumnFocused = ::onResultColumnFocused,
                                                channels = row,
                                                nowMs = nowMs,
                                                recordingChannelIds = recordingChannelIds,
                                                scheduledChannelIds = scheduledChannelIds,
                                                isLocked = { channel ->
                                                    isLocked(
                                                        categoryId = channel.categoryId,
                                                        isAdult = channel.isAdult,
                                                        isUserProtected = channel.isUserProtected
                                                    )
                                                },
                                                onChannelClick = { channel, locked ->
                                                    if (locked) {
                                                        pendingChannel = channel
                                                        showPinDialog = true
                                                    } else {
                                                        onChannelClick(channel)
                                                    }
                                                },
                                                onChannelLongClick = { channel -> showChannelActions(channel) }
                                            )
                                        }

                                        SearchTab.MOVIES -> itemsIndexed(
                                            items = movieRows,
                                            key = { _, row -> row.joinToString("-") { it.id.toString() } },
                                            contentType = { _, _ -> "search_movie_row" }
                                        ) { rowIndex, row ->
                                            SearchMovieGridRow(
                                                modifier = Modifier.trackResultRow(rowIndex, ::onResultRowFocused),
                                                onColumnFocused = ::onResultColumnFocused,
                                                movies = row,
                                                isLocked = { movie ->
                                                    isLocked(
                                                        categoryId = movie.categoryId,
                                                        isAdult = movie.isAdult,
                                                        isUserProtected = movie.isUserProtected
                                                    )
                                                },
                                                onMovieClick = { movie, locked ->
                                                    if (locked) {
                                                        pendingMovie = movie
                                                        showPinDialog = true
                                                    } else {
                                                        onMovieClick(movie)
                                                    }
                                                },
                                                onMovieLongClick = { movie -> showMovieActions(movie) }
                                            )
                                        }

                                        SearchTab.SERIES -> itemsIndexed(
                                            items = seriesRows,
                                            key = { _, row -> row.joinToString("-") { it.id.toString() } },
                                            contentType = { _, _ -> "search_series_row" }
                                        ) { rowIndex, row ->
                                            SearchSeriesGridRow(
                                                modifier = Modifier.trackResultRow(rowIndex, ::onResultRowFocused),
                                                onColumnFocused = ::onResultColumnFocused,
                                                seriesItems = row,
                                                isLocked = { seriesItem ->
                                                    isLocked(
                                                        categoryId = seriesItem.categoryId,
                                                        isAdult = seriesItem.isAdult,
                                                        isUserProtected = seriesItem.isUserProtected
                                                    )
                                                },
                                                onSeriesClick = { seriesItem, locked ->
                                                    if (locked) {
                                                        pendingSeries = seriesItem
                                                        showPinDialog = true
                                                    } else {
                                                        onSeriesClick(seriesItem)
                                                    }
                                                },
                                                onSeriesLongClick = { seriesItem -> showSeriesActions(seriesItem) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
            // A band of its own under the panes: the legend never covers the rows being moved through, and the band
            // is always reserved so the panes do not jump when results appear or clear.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LocalAppSpacing.current.let { it.lg + it.xxs }),
                contentAlignment = Alignment.CenterStart
            ) {
                // The result-side hints need results; the field and input-pane hints are useful before the first search too.
                val relevant = hintContext != null &&
                    (hasResults || hintContext == SearchHintContext.FIELD || hintContext == SearchHintContext.LEFT_PANE)
                if (relevant && hintContext != null) {
                    SearchRemoteHints(
                        context = hintContext,
                        atRowStart = resultAtRowStart,
                        hasResults = hasResults,
                        keyboardVisible = WindowInsets.isImeVisible
                    )
                }
            }
        }
    }
}

private enum class SearchHintContext { FIELD, LEFT_PANE, TABS, REFINE, TOP_RESULT, RESULTS }

// A key is its label, or an icon when the remote prints a symbol (Back); the label then doubles as the content description.
private class SearchHintKey(
    @StringRes val labelRes: Int,
    val icon: ImageVector? = null,
    val onClick: (() -> Unit)? = null
)

/** Widens the node by [extra] on both sides while keeping its place; pair it with equal horizontal content padding. */
private fun Modifier.bleedHorizontally(extra: Dp): Modifier = layout { measurable, constraints ->
    val extraPx = extra.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extraPx * 2))
    layout(constraints.maxWidth, placeable.height) { placeable.placeRelative(-extraPx, 0) }
}

private fun Modifier.trackResultColumn(column: Int, onFocused: (atStart: Boolean) -> Unit): Modifier =
    onFocusChanged { if (it.hasFocus) onFocused(column == 0) }

private fun Modifier.trackResultRow(row: Int, onFocused: (Int) -> Unit): Modifier =
    onFocusChanged { if (it.hasFocus) onFocused(row) }

/**
 * Remote key legend in the band under the panes. What it lists follows the focus: on the top result row Up leads to
 * Sort and filters and Down moves on, lower down Up and Down just browse. Each key is clickable with a pointer and does
 * what the D-pad key does. It is not a focus stop, so D-pad order and Back are untouched. Left becomes Right in RTL
 * because the input pane sits at the start edge.
 */
@Composable
private fun SearchRemoteHints(
    context: SearchHintContext,
    hasResults: Boolean,
    keyboardVisible: Boolean,
    atRowStart: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAppSpacing.current
    val focusManager = LocalFocusManager.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val ok = SearchHintKey(R.string.search_hint_key_ok)
    val back = SearchHintKey(R.string.search_hint_key_back, icon = Icons.AutoMirrored.Filled.ArrowBack)
    val up = SearchHintKey(R.string.search_hint_key_up) { focusManager.moveFocus(FocusDirection.Up) }
    val down = SearchHintKey(R.string.search_hint_key_down) { focusManager.moveFocus(FocusDirection.Down) }
    val side = SearchHintKey(if (rtl) R.string.search_hint_key_right else R.string.search_hint_key_left) {
        focusManager.moveFocus(if (rtl) FocusDirection.Right else FocusDirection.Left)
    }
    // From the input pane the results are the other way round: Right in LTR, Left in RTL.
    val toResults = SearchHintKey(if (rtl) R.string.search_hint_key_left else R.string.search_hint_key_right) {
        focusManager.moveFocus(if (rtl) FocusDirection.Left else FocusDirection.Right)
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (context) {
            SearchHintContext.TOP_RESULT -> {
                SearchRemoteHint(listOf(ok), R.string.search_hint_action_open)
                SearchRemoteHint(listOf(up), R.string.search_hint_action_up)
                SearchRemoteHint(listOf(down), R.string.search_hint_action_more)
                SearchRowHints(side, toResults, atRowStart)
            }
            SearchHintContext.RESULTS -> {
                SearchRemoteHint(listOf(ok), R.string.search_hint_action_open)
                SearchRemoteHint(listOf(up, down), R.string.search_hint_action_browse)
                SearchRowHints(side, toResults, atRowStart)
            }
            SearchHintContext.FIELD -> if (keyboardVisible) {
                SearchRemoteHint(listOf(back), R.string.search_hint_action_keyboard_close)
            } else {
                SearchRemoteHint(listOf(ok), R.string.search_hint_action_type)
                // Without results Down leads to the recents, not to a result.
                if (hasResults) SearchRemoteHint(listOf(down), R.string.search_hint_action_first)
            }
            SearchHintContext.LEFT_PANE -> {
                SearchRemoteHint(listOf(ok), R.string.search_hint_action_select)
                if (hasResults) SearchRemoteHint(listOf(toResults), R.string.search_hint_action_results)
            }
            SearchHintContext.TABS -> {
                SearchRemoteHint(listOf(ok), R.string.search_hint_action_select)
                SearchRemoteHint(listOf(down), R.string.search_hint_action_results)
            }
            SearchHintContext.REFINE -> {
                SearchRemoteHint(listOf(ok), R.string.search_hint_action_open)
                SearchRemoteHint(listOf(down), R.string.search_hint_action_results)
            }
        }
    }
}

/** First card: Left leaves for the input pane and Right moves on. Further along both keys just browse the row. */
@Composable
private fun SearchRowHints(side: SearchHintKey, toResults: SearchHintKey, atRowStart: Boolean) {
    if (atRowStart) {
        SearchRemoteHint(listOf(side), R.string.search_hint_action_side)
        SearchRemoteHint(listOf(toResults), R.string.search_hint_action_row)
    } else {
        SearchRemoteHint(listOf(side, toResults), R.string.search_hint_action_row)
    }
}

@Composable
private fun SearchRemoteHint(keys: List<SearchHintKey>, @StringRes actionRes: Int, labelMaxLines: Int = 1) {
    val spacing = LocalAppSpacing.current
    val labelClick = keys.singleOrNull()?.onClick
    Row(verticalAlignment = Alignment.CenterVertically) {
        keys.forEach { key -> SearchHintKeyCap(key) }
        Text(
            text = stringResource(actionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = labelMaxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(start = spacing.xs)
                .then(if (labelClick != null) Modifier.mouseClickable(onClick = labelClick) else Modifier)
        )
    }
}

@Composable
private fun SearchHintKeyCap(key: SearchHintKey) {
    val spacing = LocalAppSpacing.current
    val capModifier = Modifier
        .padding(end = spacing.xxs)
        .background(AppColors.ControlContainer, BrowseShapes.Control)
        .then(
            if (key.onClick != null) {
                Modifier
                    .semantics { role = Role.Button }
                    .mouseClickable(onClick = key.onClick)
            } else {
                Modifier
            }
        )
        .padding(horizontal = spacing.xs, vertical = spacing.xxs)
    if (key.icon != null) {
        Icon(
            imageVector = key.icon,
            contentDescription = stringResource(key.labelRes),
            tint = AppColors.TextPrimary,
            modifier = capModifier.size(18.dp)
        )
    } else {
        Text(
            text = stringResource(key.labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary,
            maxLines = 1,
            modifier = capModifier
        )
    }
}

// Width of the input and recents column. Results get the rest of the row, so the grids size from it.
private val SearchInputPaneWidth = 240.dp
private val SearchPaneGap = 24.dp
private const val SearchRecentLimit = 8
private const val REMOVE_FILTERS_FOCUS_TIMEOUT_MS = 1_000L

private enum class SearchRefinementSheet { SORT, FILTERS }

@Composable
private fun SearchPanes(
    inputPane: @Composable () -> Unit,
    resultsPane: @Composable () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    if (isPortraitLayout()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            inputPane()
            Column(modifier = Modifier.weight(1f)) { resultsPane() }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(SearchPaneGap)
        ) {
            Box(modifier = Modifier.width(SearchInputPaneWidth)) { inputPane() }
            Column(modifier = Modifier.weight(1f)) { resultsPane() }
        }
    }
}

@Composable
private fun SearchInputPane(
    query: String,
    hasResults: Boolean,
    recentQueries: List<String>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRecentQuerySelected: (String) -> Unit,
    onClearRecentQueries: () -> Unit,
    focusRequester: FocusRequester,
    resultsFocusRequester: FocusRequester,
    selectedStateLabel: String,
    onFieldFocusChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SearchInput(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.search_field_hint),
            focusRequester = focusRequester,
            onSearch = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .onFocusChanged { onFieldFocusChanged(it.hasFocus) }
                // With results on screen, Down skips the recents and lands on the first result. A focusProperties
                // redirect does not reach the field's innermost focus target, so the key is handled here.
                .onPreviewKeyEvent { event ->
                    val jumpToResults = hasResults && event.key == Key.DirectionDown && event.type == KeyEventType.KeyDown
                    if (jumpToResults) resultsFocusRequester.requestFocus()
                    jumpToResults
                }
        )
        if (recentQueries.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.search_recent_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                SearchPill(
                    text = stringResource(R.string.search_clear_history),
                    selected = false,
                    compact = true,
                    onClick = onClearRecentQueries
                )
            }
            recentQueries.take(SearchRecentLimit).forEach { recentQuery ->
                key(recentQuery) {
                    val recentQueryDescription = stringResource(R.string.a11y_recent_search, recentQuery)
                    val isCurrent = recentQuery.equals(query, ignoreCase = true)
                    SearchRecentRow(
                        text = recentQuery,
                        onClick = { onRecentQuerySelected(recentQuery) },
                        modifier = Modifier.semantics {
                            contentDescription = recentQueryDescription
                            if (isCurrent) {
                                selected = true
                                stateDescription = selectedStateLabel
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SearchRecentRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    TvClickableSurface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        colors = selectableSurfaceColors(selected = false),
        border = selectableFocusBorder(shape)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SearchScopeTabs(
    selectedTab: SearchTab,
    resultCount: Int?,
    onTabSelected: (SearchTab) -> Unit,
    selectedStateLabel: String,
    modifier: Modifier = Modifier
) {
    FocusSafeLazyRow(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SearchTab.values().toList(), key = { it.name }) { tab ->
            val name = stringResource(tab.titleRes)
            // Only the selected scope was searched, so only its count is known.
            val label = if (tab == selectedTab && resultCount != null) {
                stringResource(R.string.search_results_count, name, resultCount)
            } else {
                name
            }
            SearchPill(
                text = label,
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.semantics {
                    selected = tab == selectedTab
                    if (tab == selectedTab) {
                        stateDescription = selectedStateLabel
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SearchPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    TvClickableSurface(
        modifier = modifier,
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        colors = selectableSurfaceColors(selected),
        border = selectableFocusBorder(CircleShape)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 16.dp,
                vertical = if (compact) 8.dp else 10.dp
            ),
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun <T : Any> SearchResultRail(
    title: String,
    items: List<T>,
    keySelector: (T) -> Any,
    onColumnFocused: (atStart: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    Box(modifier = modifier) {
        // No inset, so the rail starts under the scope pills; the pane gap above is kept small for the same reason.
        CategoryRow(
            title = title,
            items = items,
            keySelector = keySelector,
            contentInset = 0.dp,
            headerTopPadding = LocalAppSpacing.current.xs,
            onItemFocused = { item -> onColumnFocused(item == items.firstOrNull()) }
        ) { item ->
            itemContent(item)
        }
    }
}

@Composable
private fun SearchChannelGridRow(
    channels: List<Channel>,
    nowMs: Long,
    recordingChannelIds: Set<Long> = emptySet(),
    scheduledChannelIds: Set<Long> = emptySet(),
    isLocked: (Channel) -> Boolean,
    onChannelClick: (Channel, Boolean) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    onColumnFocused: (atStart: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        channels.forEachIndexed { column, channel ->
            val locked = isLocked(channel)
            Box(modifier = Modifier.trackResultColumn(column, onColumnFocused)) {
                ChannelCard(
                    channel = channel,
                    nowMs = nowMs,
                    isLocked = locked,
                    isRecording = channel.id in recordingChannelIds,
                    isScheduledRecording = channel.id in scheduledChannelIds,
                    onClick = { onChannelClick(channel, locked) },
                    onLongClick = { onChannelLongClick(channel) }
                )
            }
        }
    }
}

@Composable
private fun SearchMovieGridRow(
    movies: List<Movie>,
    isLocked: (Movie) -> Boolean,
    onMovieClick: (Movie, Boolean) -> Unit,
    onMovieLongClick: (Movie) -> Unit,
    onColumnFocused: (atStart: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        movies.forEachIndexed { column, movie ->
            val locked = isLocked(movie)
            Box(modifier = Modifier.trackResultColumn(column, onColumnFocused)) {
                MovieCard(
                    movie = movie,
                    isLocked = locked,
                    onClick = { onMovieClick(movie, locked) },
                    onLongClick = { onMovieLongClick(movie) }
                )
            }
        }
    }
}

@Composable
private fun SearchSeriesGridRow(
    seriesItems: List<Series>,
    isLocked: (Series) -> Boolean,
    onSeriesClick: (Series, Boolean) -> Unit,
    onSeriesLongClick: (Series) -> Unit,
    onColumnFocused: (atStart: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        seriesItems.forEachIndexed { column, seriesItem ->
            val locked = isLocked(seriesItem)
            Box(modifier = Modifier.trackResultColumn(column, onColumnFocused)) {
                SeriesCard(
                    series = seriesItem,
                    isLocked = locked,
                    onClick = { onSeriesClick(seriesItem, locked) },
                    onLongClick = { onSeriesLongClick(seriesItem) }
                )
            }
        }
    }
}


@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = Primary,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .semantics { heading() }
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SearchCatalogNotice(
    completeness: CatalogCompleteness,
    onBuildCompleteIndex: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(
                when (completeness) {
                    CatalogCompleteness.INDEXING -> R.string.search_catalog_indexing
                    CatalogCompleteness.TRUNCATED -> R.string.search_catalog_truncated
                    CatalogCompleteness.PARTIAL -> R.string.search_catalog_downloaded_only
                    CatalogCompleteness.COMPLETE -> R.string.search_catalog_complete
                }
            ),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Warning
        )
        if (completeness == CatalogCompleteness.PARTIAL || completeness == CatalogCompleteness.TRUNCATED) {
            // Secondary (rule 13): the results below are the screen's focus, so this stays below them in weight.
            Button(
                onClick = onBuildCompleteIndex,
                colors = secondaryButtonColors(),
                shape = ButtonDefaults.shape(shape = BrowseShapes.Control),
                border = focusRingButtonBorder(BrowseShapes.Control),
                scale = ButtonDefaults.scale(focusedScale = 1.03f)
            ) {
                Text(
                    text = stringResource(R.string.search_build_complete_index),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SearchMessageState(
    title: String,
    subtitle: String
) {
    TvEmptyState(
        title = title,
        subtitle = subtitle,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
    )
}

// ג”€ג”€ Long-press actions dialog ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SearchItemActionsDialog(
    title: String,
    isFavorite: Boolean,
    isProtected: Boolean,
    hasCategoryId: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHide: (() -> Unit)?,
    onToggleLock: (() -> Unit)?
) {
    // Ghost-click debounce: ignore select key-up inherited from the long-press gesture
    var canInteract by remember { mutableStateOf(false) }
    val firstFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        runCatching { firstFocusRequester.requestFocus() }
        delay(500)
        canInteract = true
    }

    val safeDismiss = { if (canInteract) onDismiss() }

    Dialog(
        onDismissRequest = safeDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = BrowseShapes.Panel,
            colors = SurfaceDefaults.colors(containerColor = AppColors.SurfaceElevated),
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    DetailCloseButton(
                        contentDescription = stringResource(R.string.search_actions_dismiss),
                        onClick = safeDismiss,
                        modifier = Modifier.mouseClickable(onClick = safeDismiss)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ג”€ג”€ Favorite toggle ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€
                    SearchActionButton(
                        icon = Icons.Default.Star,
                        label = if (isFavorite) stringResource(R.string.search_actions_remove_favorite)
                                else stringResource(R.string.search_actions_add_favorite),
                        focusRequester = firstFocusRequester,
                        onClick = { if (canInteract) onToggleFavorite() }
                    )

                    // ג”€ג”€ Hide category ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€
                    if (onHide != null) {
                        SearchActionButton(
                            icon = Icons.Default.Close,
                            label = stringResource(R.string.search_actions_hide_category),
                            onClick = { if (canInteract) onHide() }
                        )
                    } else {
                        SearchActionButton(
                            icon = Icons.Default.Close,
                            label = stringResource(R.string.search_actions_hide_no_category),
                            enabled = false,
                            onClick = {}
                        )
                    }

                    // ג”€ג”€ Parental lock toggle ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€ג”€
                    if (onToggleLock != null) {
                        SearchActionButton(
                            icon = Icons.Default.Lock,
                            label = if (isProtected) stringResource(R.string.search_actions_unlock)
                                    else stringResource(R.string.search_actions_lock),
                            onClick = { if (canInteract) onToggleLock() }
                        )
                    } else {
                        SearchActionButton(
                            icon = Icons.Default.Lock,
                            label = stringResource(R.string.search_actions_lock_no_category),
                            enabled = false,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SearchActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    enabled: Boolean = true
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .then(
            if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
        )
        .mouseClickable(onClick = onClick)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = baseModifier,
        colors = secondaryButtonColors(),
        shape = ButtonDefaults.shape(shape = BrowseShapes.Control),
        border = focusRingButtonBorder(BrowseShapes.Control),
        scale = ButtonDefaults.scale(focusedScale = 1f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
