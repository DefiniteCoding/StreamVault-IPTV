package com.streamvault.core.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.streamvault.core.ui.R

// Sync persists "Category <id>" when a provider omits a category name, so the placeholder is recognised by shape.
private val PlaceholderName = Regex("^Category (\\d+)$")

// A real category literally named "Category 5" matches too, so it is shown as the localized placeholder.
fun categoryPlaceholderId(name: String): String? =
    PlaceholderName.matchEntire(name)?.groupValues?.get(1)

/** Provider category name for display, with the sync placeholder "Category <id>" localized. */
@Composable
fun providerCategoryName(name: String): String {
    val placeholderId = categoryPlaceholderId(name) ?: return name
    return stringResource(R.string.category_name_placeholder, placeholderId)
}

@Composable
fun favoritesCategoryName(): String = stringResource(R.string.category_name_favorites)

@Composable
fun recentCategoryName(): String = stringResource(R.string.category_name_recent)

@Composable
fun allChannelsCategoryName(): String = stringResource(R.string.category_name_all_channels)
