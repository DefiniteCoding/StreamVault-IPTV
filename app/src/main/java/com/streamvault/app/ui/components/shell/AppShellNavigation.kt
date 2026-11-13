package com.streamvault.app.ui.components.shell

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.LocalAppSpacing
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.streamvault.app.R
import com.streamvault.app.navigation.Routes
import com.streamvault.core.ui.components.shell.CoreAppScreenScaffold
import com.streamvault.core.ui.components.shell.NavigationChrome
import com.streamvault.core.ui.components.shell.TopBarSearch
import com.streamvault.core.ui.components.shell.UiDestination
import com.streamvault.domain.model.AppTopLevelDestination
import com.streamvault.domain.model.CatalogLayout

enum class AppNavigationChrome {
    Rail,
    TopBar
}

internal val LocalAppDestinationItems = staticCompositionLocalOf<List<UiDestination>?> { null }

@Composable
fun AppScreenScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationChrome: AppNavigationChrome = AppNavigationChrome.Rail,
    topBarVisible: Boolean = true,
    compactHeader: Boolean = false,
    showScreenHeader: Boolean = true,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    topBarActions: (@Composable RowScope.() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable ColumnScope.() -> Unit
) {
    val navigationDestinations = LocalAppDestinationItems.current
        ?: rememberAppDestinationItems(
            configuredDestinations = AppTopLevelDestination.defaultOrder,
            catalogLayout = CatalogLayout.SPLIT
        )
    val searchLabel = stringResource(R.string.search_title)
    // Always on the TV top bar, even when Search is not a configured destination (user decision).
    val topBarSearch = remember(searchLabel) {
        TopBarSearch(destinationId = Routes.SEARCH, contentDescription = searchLabel)
    }

    CoreAppScreenScaffold(
        currentDestinationId = selectedNavigationDestinationId(currentRoute, navigationDestinations),
        destinations = navigationDestinations,
        onDestinationSelected = onNavigate,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        navigationChrome = when (navigationChrome) {
            AppNavigationChrome.Rail -> NavigationChrome.Rail
            AppNavigationChrome.TopBar -> NavigationChrome.TopBar
        },
        topBarVisible = topBarVisible,
        compactHeader = compactHeader,
        showScreenHeader = showScreenHeader,
        header = header,
        topBarActions = topBarActions,
        brand = { StreamVaultBrandMark() },
        topBarSearch = topBarSearch,
        contentPadding = contentPadding,
        content = content
    )
}

internal fun selectedNavigationDestinationId(
    currentRoute: String,
    destinations: List<UiDestination>
): String = if (
    currentRoute.substringBefore('?') == Routes.ADDONS &&
    destinations.any { destination -> destination.id == Routes.PLUGINS }
) {
    Routes.PLUGINS
} else {
    currentRoute
}

internal data class AppDestinationItem(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

internal fun buildDestinationItems(
    configured: List<AppTopLevelDestination>,
    layout: CatalogLayout
): List<AppDestinationItem> {
    if (layout == CatalogLayout.SPLIT) return configured.map { it.toDestinationItem() }

    var insertedVod = false
    return buildList {
        configured.forEach { destination ->
            when (destination) {
                AppTopLevelDestination.MOVIES,
                AppTopLevelDestination.SERIES -> if (!insertedVod) {
                    add(AppDestinationItem(Routes.VOD, R.string.nav_vod, Icons.Outlined.StarBorder, Icons.Filled.Star))
                    insertedVod = true
                }
                else -> add(destination.toDestinationItem())
            }
        }
    }
}

@Composable
internal fun rememberAppDestinationItems(
    configuredDestinations: List<AppTopLevelDestination>,
    catalogLayout: CatalogLayout
): List<UiDestination> {
    val destinationItems = remember(configuredDestinations, catalogLayout) {
        buildDestinationItems(configuredDestinations, catalogLayout)
    }
    return destinationItems.map { item ->
        UiDestination(
            id = item.route,
            label = stringResource(item.labelRes),
            icon = item.icon,
            selectedIcon = item.selectedIcon
        )
    }
}

private fun AppTopLevelDestination.toDestinationItem(): AppDestinationItem = when (this) {
    AppTopLevelDestination.HOME -> AppDestinationItem(Routes.HOME, R.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home)
    AppTopLevelDestination.LIVE_TV -> AppDestinationItem(Routes.LIVE_TV, R.string.nav_live_tv, Icons.Outlined.PlayArrow, Icons.Filled.PlayArrow)
    AppTopLevelDestination.SPORTS -> AppDestinationItem(Routes.SPORTS, R.string.nav_sports, Icons.Outlined.SportsSoccer, Icons.Filled.SportsSoccer)
    AppTopLevelDestination.MOVIES -> AppDestinationItem(Routes.MOVIES, R.string.nav_movies, Icons.Outlined.StarBorder, Icons.Filled.Star)
    AppTopLevelDestination.SERIES -> AppDestinationItem(Routes.SERIES, R.string.nav_series, Icons.Default.Menu)
    AppTopLevelDestination.DOWNLOADS -> AppDestinationItem(Routes.DOWNLOADS, R.string.nav_downloads, Icons.Outlined.Download, Icons.Filled.Download)
    AppTopLevelDestination.GUIDE -> AppDestinationItem(Routes.EPG, R.string.nav_epg, Icons.Outlined.Info, Icons.Filled.Info)
    AppTopLevelDestination.SEARCH -> AppDestinationItem(Routes.SEARCH, R.string.search_title, Icons.Default.Search)
    AppTopLevelDestination.EXTENSIONS -> AppDestinationItem(Routes.PLUGINS, R.string.nav_extensions, Icons.Outlined.Extension, Icons.Filled.Extension)
    AppTopLevelDestination.SETTINGS -> AppDestinationItem(Routes.SETTINGS, R.string.nav_settings, Icons.Outlined.Settings, Icons.Filled.Settings)
}

// The launcher foreground carries adaptive-icon padding around its tile, so it is drawn larger and clipped to the tile.
@Composable
private fun StreamVaultBrandMark() {
    val spacing = LocalAppSpacing.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(BrandTileSize)
                .clip(BrandTileShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.requiredSize(BrandTileSize * LauncherForegroundToTile)
            )
        }
        Text(
            text = stringResource(R.string.brand_wordmark),
            style = MaterialTheme.typography.titleMedium,
            color = BrowseColors.TextPrimary,
            maxLines = 1
        )
    }
}

private val BrandTileSize = 28.dp
private val BrandTileShape = RoundedCornerShape(7.dp)
// The tile spans about 76% of the 108dp launcher foreground.
private const val LauncherForegroundToTile = 1.32f
