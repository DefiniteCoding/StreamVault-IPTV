package com.streamvault.feature.playback.ui

import androidx.compose.runtime.Composable
import com.streamvault.core.ui.localization.allChannelsCategoryName
import com.streamvault.core.ui.localization.favoritesCategoryName
import com.streamvault.core.ui.localization.providerCategoryName
import com.streamvault.core.ui.localization.recentCategoryName
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.VirtualCategoryIds
import com.streamvault.domain.repository.ChannelRepository

// Virtual names are matched by id, never by text: a user group may legitimately be called "Favorites".
@Composable
fun Category.localizedName(): String = when (id) {
    VirtualCategoryIds.FAVORITES -> favoritesCategoryName()
    VirtualCategoryIds.RECENT -> recentCategoryName()
    ChannelRepository.ALL_CHANNELS_ID -> allChannelsCategoryName()
    else -> providerCategoryName(name)
}
