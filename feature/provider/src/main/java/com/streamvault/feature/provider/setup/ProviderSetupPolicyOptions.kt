package com.streamvault.feature.provider.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface as TvSurface
import androidx.tv.material3.Text
import com.streamvault.feature.provider.R
import com.streamvault.core.ui.interaction.mouseClickable
import com.streamvault.core.ui.design.browse.*
import com.streamvault.domain.model.ChannelLogoSourcePolicy
import com.streamvault.domain.model.GuideSourcePolicy
import com.streamvault.domain.model.ProviderEpgSyncMode
import com.streamvault.domain.model.ProviderXtreamLiveSyncMode
import com.streamvault.domain.model.StalkerAuthMode

@Composable
internal fun XtreamLiveSyncModeOptionRow(
    mode: ProviderXtreamLiveSyncMode,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val titleRes = when (mode) {
        ProviderXtreamLiveSyncMode.AUTO -> R.string.setup_xtream_live_sync_mode_auto_title
        ProviderXtreamLiveSyncMode.CATEGORY_BY_CATEGORY -> R.string.setup_xtream_live_sync_mode_category_title
        ProviderXtreamLiveSyncMode.STREAM_ALL -> R.string.setup_xtream_live_sync_mode_stream_all_title
    }
    val descriptionRes = when (mode) {
        ProviderXtreamLiveSyncMode.AUTO -> R.string.setup_xtream_live_sync_mode_auto_description
        ProviderXtreamLiveSyncMode.CATEGORY_BY_CATEGORY -> R.string.setup_xtream_live_sync_mode_category_description
        ProviderXtreamLiveSyncMode.STREAM_ALL -> R.string.setup_xtream_live_sync_mode_stream_all_description
    }
    TvSurface(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .mouseClickable(onClick = onSelect),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Primary.copy(alpha = 0.12f) else Color.Transparent,
            focusedContainerColor = if (selected) Primary.copy(alpha = 0.26f) else SurfaceHighlight.copy(alpha = 0.9f)
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                BorderStroke(
                    1.dp,
                    if (selected) Primary.copy(alpha = 0.45f) else Color.Transparent
                )
            ),
            focusedBorder = Border(BorderStroke(3.dp, PrimaryLight))
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(titleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
        }
    }
}

@Composable
internal fun StalkerAuthModeOptionRow(
    mode: StalkerAuthMode,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val title = when (mode) {
        StalkerAuthMode.AUTO -> stringResource(R.string.setup_stalker_auth_auto_title)
        StalkerAuthMode.MAC_ONLY -> stringResource(R.string.setup_stalker_auth_mac_only_title)
        StalkerAuthMode.MAC_PLUS_CREDENTIALS -> stringResource(R.string.setup_stalker_auth_mac_credentials_title)
        StalkerAuthMode.CREDENTIALS_ONLY -> stringResource(R.string.setup_stalker_auth_credentials_only_title)
    }
    val description = when (mode) {
        StalkerAuthMode.AUTO -> stringResource(R.string.setup_stalker_auth_auto_description)
        StalkerAuthMode.MAC_ONLY -> stringResource(R.string.setup_stalker_auth_mac_only_description)
        StalkerAuthMode.MAC_PLUS_CREDENTIALS -> stringResource(R.string.setup_stalker_auth_mac_credentials_description)
        StalkerAuthMode.CREDENTIALS_ONLY -> stringResource(R.string.setup_stalker_auth_credentials_only_description)
    }
    TvSurface(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .mouseClickable(onClick = onSelect),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Primary.copy(alpha = 0.12f) else Color.Transparent,
            focusedContainerColor = if (selected) Primary.copy(alpha = 0.26f) else SurfaceHighlight.copy(alpha = 0.9f)
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                BorderStroke(
                    1.dp,
                    if (selected) Primary.copy(alpha = 0.45f) else Color.Transparent
                )
            ),
            focusedBorder = Border(BorderStroke(3.dp, PrimaryLight))
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
        }
    }
}

@Composable
internal fun EpgSyncModeOptionRow(
    mode: ProviderEpgSyncMode,
    sourceType: SourceType,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val titleRes = when (mode) {
        ProviderEpgSyncMode.UPFRONT -> R.string.setup_epg_sync_mode_upfront_title
        ProviderEpgSyncMode.BACKGROUND -> R.string.setup_epg_sync_mode_background_title
        ProviderEpgSyncMode.SKIP -> R.string.setup_epg_sync_mode_skip_title
    }
    val descriptionRes = when (mode) {
        ProviderEpgSyncMode.UPFRONT -> if (sourceType == SourceType.STALKER) {
            R.string.setup_stalker_epg_sync_mode_upfront_description
        } else {
            R.string.setup_epg_sync_mode_upfront_description
        }
        ProviderEpgSyncMode.BACKGROUND -> if (sourceType == SourceType.STALKER) {
            R.string.setup_stalker_epg_sync_mode_background_description
        } else {
            R.string.setup_epg_sync_mode_background_description
        }
        ProviderEpgSyncMode.SKIP -> if (sourceType == SourceType.STALKER) {
            R.string.setup_stalker_epg_sync_mode_skip_description
        } else {
            R.string.setup_epg_sync_mode_skip_description
        }
    }
    TvSurface(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .mouseClickable(onClick = onSelect),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Primary.copy(alpha = 0.12f) else Color.Transparent,
            focusedContainerColor = if (selected) Primary.copy(alpha = 0.26f) else SurfaceHighlight.copy(alpha = 0.9f)
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                BorderStroke(
                    1.dp,
                    if (selected) Primary.copy(alpha = 0.45f) else Color.Transparent
                )
            ),
            focusedBorder = Border(BorderStroke(3.dp, PrimaryLight))
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(titleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
        }
    }
}

internal fun supportsGuideAndLogoPolicy(sourceType: SourceType): Boolean = when (sourceType) {
    SourceType.XTREAM,
    SourceType.STALKER,
    SourceType.M3U_URL,
    SourceType.M3U_FILE -> true
    SourceType.JELLYFIN -> false
}

@Composable
internal fun GuideSourcePolicyOptionRow(
    policy: GuideSourcePolicy,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val title = when (policy) {
        GuideSourcePolicy.AUTO -> stringResource(R.string.setup_guide_policy_auto_title)
        GuideSourcePolicy.EXTERNAL_ONLY -> stringResource(R.string.setup_guide_policy_external_title)
        GuideSourcePolicy.PROVIDER_ONLY -> stringResource(R.string.setup_guide_policy_provider_title)
        GuideSourcePolicy.DISABLED -> stringResource(R.string.setup_guide_policy_disabled_title)
    }
    val description = when (policy) {
        GuideSourcePolicy.AUTO -> stringResource(R.string.setup_guide_policy_auto_description)
        GuideSourcePolicy.EXTERNAL_ONLY -> stringResource(R.string.setup_guide_policy_external_description)
        GuideSourcePolicy.PROVIDER_ONLY -> stringResource(R.string.setup_guide_policy_provider_description)
        GuideSourcePolicy.DISABLED -> stringResource(R.string.setup_guide_policy_disabled_description)
    }
    PolicyOptionRow(title = title, description = description, selected = selected, onSelect = onSelect)
}

@Composable
internal fun ChannelLogoSourcePolicyOptionRow(
    policy: ChannelLogoSourcePolicy,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val title = when (policy) {
        ChannelLogoSourcePolicy.SUPPLIER_PREFERRED -> stringResource(R.string.setup_logo_policy_supplier_preferred_title)
        ChannelLogoSourcePolicy.EPG_PREFERRED -> stringResource(R.string.setup_logo_policy_epg_preferred_title)
        ChannelLogoSourcePolicy.SUPPLIER_ONLY -> stringResource(R.string.setup_logo_policy_supplier_only_title)
        ChannelLogoSourcePolicy.EPG_ONLY -> stringResource(R.string.setup_logo_policy_epg_only_title)
    }
    val description = when (policy) {
        ChannelLogoSourcePolicy.SUPPLIER_PREFERRED -> stringResource(R.string.setup_logo_policy_supplier_preferred_description)
        ChannelLogoSourcePolicy.EPG_PREFERRED -> stringResource(R.string.setup_logo_policy_epg_preferred_description)
        ChannelLogoSourcePolicy.SUPPLIER_ONLY -> stringResource(R.string.setup_logo_policy_supplier_only_description)
        ChannelLogoSourcePolicy.EPG_ONLY -> stringResource(R.string.setup_logo_policy_epg_only_description)
    }
    PolicyOptionRow(title = title, description = description, selected = selected, onSelect = onSelect)
}

@Composable
internal fun PolicyOptionRow(
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    TvSurface(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .mouseClickable(onClick = onSelect),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Primary.copy(alpha = 0.12f) else Color.Transparent,
            focusedContainerColor = if (selected) Primary.copy(alpha = 0.26f) else SurfaceHighlight.copy(alpha = 0.9f)
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                BorderStroke(
                    1.dp,
                    if (selected) Primary.copy(alpha = 0.45f) else Color.Transparent
                )
            ),
            focusedBorder = Border(BorderStroke(3.dp, PrimaryLight))
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
            }
        }
    }
}
