package com.streamvault.feature.live.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import com.streamvault.core.ui.components.shell.AppMessageState
import com.streamvault.core.ui.design.AppColors
import com.streamvault.core.ui.design.LocalAppSpacing

@Composable
fun LiveChannelContentHost(
    isLoading: Boolean,
    errorMessage: String?,
    hasChannels: Boolean,
    isBlockedCategorySearch: Boolean,
    loadingLabel: String,
    lockedLabel: String,
    noChannelsLabel: String,
    noChannelsSubtitle: String,
    additionalEmptyHint: String? = null,
    modifier: Modifier = Modifier,
    channelContent: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> HomeLoadingPane(message = loadingLabel)

            // The view model already supplies the full sentence, so it is the card title.
            errorMessage != null -> StateCard(title = errorMessage, subtitle = null)

            !hasChannels && isBlockedCategorySearch -> StateCard(
                title = lockedLabel,
                subtitle = noChannelsSubtitle,
                showLockIcon = true
            )

            !hasChannels -> StateCard(
                title = noChannelsLabel,
                subtitle = listOfNotNull(noChannelsSubtitle, additionalEmptyHint).joinToString("\n")
            )

            else -> channelContent()
        }
    }
}

@Composable
private fun BoxScope.StateCard(
    title: String,
    subtitle: String?,
    showLockIcon: Boolean = false
) {
    Column(
        modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalAppSpacing.current.sm)
    ) {
        if (showLockIcon) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(34.dp)
            )
        }
        AppMessageState(title = title, subtitle = subtitle)
    }
}
