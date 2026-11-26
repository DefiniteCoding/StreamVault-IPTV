package com.streamvault.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.feature.settings.R
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.FocusSpec
import com.streamvault.core.ui.design.contentOnColor
import com.streamvault.core.ui.design.selectableFocusBorder
import com.streamvault.core.ui.interaction.TvClickableSurface
import com.streamvault.core.ui.theme.OnBackground
import com.streamvault.core.ui.theme.OnSurfaceDim
import com.streamvault.core.ui.theme.Primary
import com.streamvault.core.ui.theme.Secondary
import com.streamvault.core.ui.theme.Surface
import com.streamvault.core.ui.theme.SurfaceHighlight
import com.streamvault.domain.model.ProviderType

@Composable
public fun ProviderM3uOptionsPanel(
    m3uVodClassificationEnabled: Boolean,
    isSyncing: Boolean,
    onToggleM3uVodClassification: (Boolean) -> Unit,
    onRefreshM3uClassification: () -> Unit,
    classificationModifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .settingsDetailPanel()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_m3u_vod_classification_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = OnBackground
                )
                Text(
                    text = stringResource(R.string.settings_m3u_vod_classification_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
            Switch(
                checked = m3uVodClassificationEnabled,
                onCheckedChange = onToggleM3uVodClassification,
                modifier = classificationModifier,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TvClickableSurface(
                onClick = onRefreshM3uClassification,
                enabled = !isSyncing,
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Primary.copy(alpha = 0.15f),
                    focusedContainerColor = Primary.copy(alpha = 0.3f)
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
            ) {
                Text(
                    text = stringResource(R.string.settings_m3u_vod_classification_refresh),
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
public fun ProviderSyncWarningsPanel(
    providerType: ProviderType,
    syncWarnings: List<String>,
    isSyncing: Boolean,
    onRetryWarningAction: (ProviderWarningAction) -> Unit
) {
    val hasEpgWarning = syncWarnings.any { it.contains("EPG", ignoreCase = true) }
    val hasMoviesWarning = syncWarnings.any { it.contains("Movies", ignoreCase = true) }
    val hasSeriesWarning = syncWarnings.any { it.contains("Series", ignoreCase = true) }

    Text(
        text = stringResource(R.string.settings_provider_warnings, syncWarnings.take(3).joinToString(", ")),
        style = MaterialTheme.typography.bodySmall,
        color = Secondary
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hasEpgWarning) {
            ProviderWarningRetryButton(
                label = stringResource(R.string.settings_retry_epg),
                isSyncing = isSyncing,
                onClick = { onRetryWarningAction(ProviderWarningAction.EPG) }
            )
        }
        if (hasMoviesWarning) {
            ProviderWarningRetryButton(
                label = stringResource(R.string.settings_retry_movies),
                isSyncing = isSyncing,
                onClick = { onRetryWarningAction(ProviderWarningAction.MOVIES) }
            )
        }
        if (hasSeriesWarning && providerType == ProviderType.XTREAM_CODES) {
            ProviderWarningRetryButton(
                label = stringResource(R.string.settings_retry_series),
                isSyncing = isSyncing,
                onClick = { onRetryWarningAction(ProviderWarningAction.SERIES) }
            )
        }
    }
}

@Composable
private fun ProviderWarningRetryButton(
    label: String,
    isSyncing: Boolean,
    onClick: () -> Unit
) {
    TvClickableSurface(
        onClick = onClick,
        enabled = !isSyncing,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Secondary.copy(alpha = 0.16f),
            focusedContainerColor = Secondary.copy(alpha = 0.35f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Secondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@Composable
public fun ProviderActionButtons(
    isActive: Boolean,
    isSyncing: Boolean,
    liveOnboardingIncomplete: Boolean,
    onConnect: () -> Unit,
    onRefresh: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onParentalControl: () -> Unit,
    targetItemId: String? = null,
    targetFocusModifier: Modifier = Modifier,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!isActive) {
            ProviderActionButton(
                label = when {
                    liveOnboardingIncomplete && isSyncing -> stringResource(R.string.settings_syncing_btn)
                    liveOnboardingIncomplete -> stringResource(R.string.settings_sync_btn)
                    else -> stringResource(R.string.settings_connect)
                },
                primary = true,
                onClick = if (liveOnboardingIncomplete) onRefresh else onConnect,
                modifier = if (targetItemId == "sources.sync") targetFocusModifier else Modifier,
            )
        } else {
            ProviderActionButton(
                label = if (isSyncing) stringResource(R.string.settings_syncing_btn) else stringResource(R.string.settings_sync_btn),
                primary = true,
                onClick = onRefresh,
                modifier = if (targetItemId == "sources.sync") targetFocusModifier else Modifier,
            )
        }

        ProviderActionButton(
            label = stringResource(R.string.settings_edit),
            onClick = onEdit,
            modifier = if (targetItemId == "sources.edit") targetFocusModifier else Modifier,
        )

        if (isActive) {
            ProviderActionButton(
                label = stringResource(R.string.settings_provider_category_controls_action),
                onClick = onParentalControl
            )
        }

        ProviderActionButton(
            label = stringResource(R.string.settings_delete),
            destructive = true,
            onClick = onDelete,
            modifier = if (targetItemId == "sources.delete") targetFocusModifier else Modifier,
        )
    }
}

/**
 * The first action (Connect, or Sync once active) is the card's single primary. Delete is a red-label
 * destructive secondary that fills red on focus; the rest are accent-tinted secondaries (rules 12 to 15).
 */
@Composable
private fun ProviderActionButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false,
    destructive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(SettingsDetailShapes.Control),
        colors = when {
            primary -> ClickableSurfaceDefaults.colors(
                containerColor = BrowseColors.Brand,
                contentColor = BrowseColors.OnPrimary,
                focusedContainerColor = BrowseColors.BrandStrong,
                focusedContentColor = contentOnColor(BrowseColors.BrandStrong)
            )
            destructive -> ClickableSurfaceDefaults.colors(
                containerColor = BrowseColors.SurfaceElevated,
                contentColor = BrowseColors.Live,
                focusedContainerColor = BrowseColors.Live,
                focusedContentColor = contentOnColor(BrowseColors.Live)
            )
            else -> ClickableSurfaceDefaults.colors(
                containerColor = BrowseColors.SurfaceAccent,
                contentColor = BrowseColors.TextPrimary,
                focusedContainerColor = BrowseColors.SurfaceAccent,
                focusedContentColor = BrowseColors.TextPrimary
            )
        },
        border = selectableFocusBorder(SettingsDetailShapes.Control),
        scale = ClickableSurfaceDefaults.scale(focusedScale = FocusSpec.FocusedScale)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
