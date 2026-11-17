package com.streamvault.feature.provider.setup

import com.streamvault.core.ui.design.BrowseShapes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface as TvSurface
import androidx.tv.material3.Text
import com.streamvault.feature.provider.R
import com.streamvault.core.ui.interaction.TvClickableSurface
import com.streamvault.core.ui.interaction.mouseClickable
import com.streamvault.core.ui.design.browse.*
import com.streamvault.domain.model.ChannelLogoSourcePolicy
import com.streamvault.domain.model.GuideSourcePolicy
import com.streamvault.domain.model.ProviderEpgSyncMode
import com.streamvault.domain.model.ProviderXtreamLiveSyncMode
import com.streamvault.domain.model.StalkerAuthMode
import com.streamvault.domain.model.StalkerCatalogMode
import com.streamvault.domain.model.StalkerCatalogLoadOrder

@Composable
internal fun AdvancedProviderOptionsSection(options: ProviderAdvancedOptions) {
    AdvancedProviderOptionsSection(
        sourceType = options.sourceType,
        uiState = options.uiState,
        httpUserAgent = options.httpUserAgent,
        onHttpUserAgentChange = options.onHttpUserAgentChange,
        httpHeaders = options.httpHeaders,
        onHttpHeadersChange = options.onHttpHeadersChange,
        onToggleM3uVodClassification = options.onToggleM3uVodClassification,
        onSelectEpgSyncMode = options.onSelectEpgSyncMode,
        onSelectStalkerCatalogMode = options.onSelectStalkerCatalogMode,
        onSelectStalkerCatalogLoadOrder = options.onSelectStalkerCatalogLoadOrder,
        onSelectXtreamLiveSyncMode = options.onSelectXtreamLiveSyncMode,
        onSelectGuideSourcePolicy = options.onSelectGuideSourcePolicy,
        onSelectChannelLogoSourcePolicy = options.onSelectChannelLogoSourcePolicy,
        username = options.username,
        onUsernameChange = options.onUsernameChange,
        password = options.password,
        onPasswordChange = options.onPasswordChange,
        stalkerAuthMode = options.stalkerAuthMode,
        onStalkerAuthModeChange = options.onStalkerAuthModeChange,
        stalkerMacAddress = options.stalkerMacAddress,
        stalkerDeviceProfile = options.stalkerDeviceProfile,
        onStalkerDeviceProfileChange = options.onStalkerDeviceProfileChange,
        stalkerDeviceTimezone = options.stalkerDeviceTimezone,
        onStalkerDeviceTimezoneChange = options.onStalkerDeviceTimezoneChange,
        stalkerDeviceLocale = options.stalkerDeviceLocale,
        onStalkerDeviceLocaleChange = options.onStalkerDeviceLocaleChange,
        stalkerSerialNumber = options.stalkerSerialNumber,
        onStalkerSerialNumberChange = options.onStalkerSerialNumberChange,
        stalkerDeviceId = options.stalkerDeviceId,
        onStalkerDeviceIdChange = options.onStalkerDeviceIdChange,
        stalkerDeviceId2 = options.stalkerDeviceId2,
        onStalkerDeviceId2Change = options.onStalkerDeviceId2Change,
        stalkerSignature = options.stalkerSignature,
        onStalkerSignatureChange = options.onStalkerSignatureChange,
        stalkerHwVersion = options.stalkerHwVersion,
        onStalkerHwVersionChange = options.onStalkerHwVersionChange,
        stalkerApiUserAgent = options.stalkerApiUserAgent,
        onStalkerApiUserAgentChange = options.onStalkerApiUserAgentChange,
        stalkerPlayerUserAgent = options.stalkerPlayerUserAgent,
        onStalkerPlayerUserAgentChange = options.onStalkerPlayerUserAgentChange,
        stalkerPlayerHeaders = options.stalkerPlayerHeaders,
        onStalkerPlayerHeadersChange = options.onStalkerPlayerHeadersChange,
        stalkerXUserAgentLink = options.stalkerXUserAgentLink,
        onStalkerXUserAgentLinkChange = options.onStalkerXUserAgentLinkChange,
        stalkerProxyEnabled = options.stalkerProxyEnabled,
        onStalkerProxyEnabledChange = options.onStalkerProxyEnabledChange,
        stalkerProxyHost = options.stalkerProxyHost,
        onStalkerProxyHostChange = options.onStalkerProxyHostChange,
        stalkerProxyPort = options.stalkerProxyPort,
        onStalkerProxyPortChange = options.onStalkerProxyPortChange,
        stalkerRequestRules = options.stalkerRequestRules,
        onAddStalkerRequestRule = options.onAddStalkerRequestRule,
        onUpdateStalkerRequestRule = options.onUpdateStalkerRequestRule,
        onRemoveStalkerRequestRule = options.onRemoveStalkerRequestRule
    )
}

@Composable
internal fun AdvancedProviderOptionsSection(
    sourceType: SourceType,
    uiState: ProviderSetupState,
    httpUserAgent: String,
    onHttpUserAgentChange: (String) -> Unit,
    httpHeaders: String,
    onHttpHeadersChange: (String) -> Unit,
    onToggleM3uVodClassification: () -> Unit,
    onSelectEpgSyncMode: (ProviderEpgSyncMode) -> Unit,
    onSelectStalkerCatalogMode: (StalkerCatalogMode) -> Unit,
    onSelectStalkerCatalogLoadOrder: (StalkerCatalogLoadOrder) -> Unit = {},
    onSelectXtreamLiveSyncMode: (ProviderXtreamLiveSyncMode) -> Unit,
    onSelectGuideSourcePolicy: (GuideSourcePolicy) -> Unit,
    onSelectChannelLogoSourcePolicy: (ChannelLogoSourcePolicy) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    stalkerAuthMode: StalkerAuthMode,
    onStalkerAuthModeChange: (StalkerAuthMode) -> Unit,
    stalkerMacAddress: String,
    stalkerDeviceProfile: String,
    onStalkerDeviceProfileChange: (String) -> Unit,
    stalkerDeviceTimezone: String,
    onStalkerDeviceTimezoneChange: (String) -> Unit,
    stalkerDeviceLocale: String,
    onStalkerDeviceLocaleChange: (String) -> Unit,
    stalkerSerialNumber: String,
    onStalkerSerialNumberChange: (String) -> Unit,
    stalkerDeviceId: String,
    onStalkerDeviceIdChange: (String) -> Unit,
    stalkerDeviceId2: String,
    onStalkerDeviceId2Change: (String) -> Unit,
    stalkerSignature: String,
    onStalkerSignatureChange: (String) -> Unit,
    stalkerHwVersion: String = "",
    onStalkerHwVersionChange: (String) -> Unit = {},
    stalkerApiUserAgent: String = "",
    onStalkerApiUserAgentChange: (String) -> Unit = {},
    stalkerPlayerUserAgent: String = "",
    onStalkerPlayerUserAgentChange: (String) -> Unit = {},
    stalkerPlayerHeaders: String = "",
    onStalkerPlayerHeadersChange: (String) -> Unit = {},
    stalkerXUserAgentLink: String = ProviderStalkerAdvancedOptions.LINK_ETHERNET,
    onStalkerXUserAgentLinkChange: (String) -> Unit = {},
    stalkerProxyEnabled: Boolean = false,
    onStalkerProxyEnabledChange: (Boolean) -> Unit = {},
    stalkerProxyHost: String = "",
    onStalkerProxyHostChange: (String) -> Unit = {},
    stalkerProxyPort: String = "",
    onStalkerProxyPortChange: (String) -> Unit = {},
    stalkerRequestRules: List<StalkerRequestRuleUiState> = emptyList(),
    onAddStalkerRequestRule: () -> Unit = {},
    onUpdateStalkerRequestRule: (Int, StalkerRequestRuleUiState) -> Unit = { _, _ -> },
    onRemoveStalkerRequestRule: (Int) -> Unit = {}
) {
    var showAdvancedOptions by rememberSaveable(sourceType) { mutableStateOf(false) }
    val defaultEpgSyncMode = when (sourceType) {
        SourceType.STALKER -> ProviderEpgSyncMode.BACKGROUND
        SourceType.XTREAM,
        SourceType.M3U_URL,
        SourceType.M3U_FILE,
        SourceType.JELLYFIN -> ProviderEpgSyncMode.UPFRONT
    }

    LaunchedEffect(uiState.isEditing, uiState.epgSyncMode, uiState.stalkerCatalogMode, uiState.xtreamLiveSyncMode, uiState.guideSourcePolicy, uiState.channelLogoSourcePolicy, sourceType) {
        val hasNonDefaultSelection = ((sourceType == SourceType.XTREAM || sourceType == SourceType.STALKER) && uiState.epgSyncMode != defaultEpgSyncMode) ||
            (sourceType == SourceType.STALKER && uiState.stalkerCatalogMode != StalkerCatalogMode.ON_DEMAND) ||
            (sourceType == SourceType.XTREAM && uiState.xtreamLiveSyncMode != ProviderXtreamLiveSyncMode.AUTO) ||
            (supportsGuideAndLogoPolicy(sourceType) && uiState.guideSourcePolicy != GuideSourcePolicy.AUTO) ||
            (supportsGuideAndLogoPolicy(sourceType) && uiState.channelLogoSourcePolicy != ChannelLogoSourcePolicy.SUPPLIER_PREFERRED) ||
            ((sourceType == SourceType.M3U_URL || sourceType == SourceType.M3U_FILE) && !uiState.m3uVodClassificationEnabled) ||
            ((sourceType == SourceType.XTREAM || sourceType == SourceType.M3U_URL || sourceType == SourceType.M3U_FILE) &&
                httpUserAgent.isNotBlank()) ||
            ((sourceType == SourceType.XTREAM || sourceType == SourceType.STALKER || sourceType == SourceType.M3U_URL || sourceType == SourceType.M3U_FILE) &&
                httpHeaders.isNotBlank()) ||
            (sourceType == SourceType.STALKER && (
                stalkerAuthMode != StalkerAuthMode.AUTO ||
                    username.isNotBlank() ||
                    password.isNotBlank() ||
                    stalkerMacAddress.isBlank() ||
                    stalkerDeviceProfile.isNotBlank() ||
                    stalkerDeviceTimezone.isNotBlank() ||
                    stalkerDeviceLocale.isNotBlank() ||
                    stalkerSerialNumber.isNotBlank() ||
                    stalkerDeviceId.isNotBlank() ||
                    stalkerDeviceId2.isNotBlank() ||
                    stalkerSignature.isNotBlank() ||
                    stalkerHwVersion.isNotBlank() ||
                    stalkerApiUserAgent.isNotBlank() ||
                    stalkerPlayerUserAgent.isNotBlank() ||
                    stalkerPlayerHeaders.isNotBlank() ||
                    stalkerXUserAgentLink != ProviderStalkerAdvancedOptions.LINK_ETHERNET ||
                    stalkerProxyEnabled ||
                    stalkerProxyHost.isNotBlank() ||
                    stalkerProxyPort.isNotBlank() ||
                    stalkerRequestRules.isNotEmpty()
                ))
        if (uiState.isEditing && hasNonDefaultSelection) {
            showAdvancedOptions = true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TvSurface(
            onClick = { showAdvancedOptions = !showAdvancedOptions },
            modifier = Modifier
                .fillMaxWidth()
                .mouseClickable(onClick = { showAdvancedOptions = !showAdvancedOptions }),
            shape = ClickableSurfaceDefaults.shape(BrowseShapes.Artwork),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (showAdvancedOptions) Primary.copy(alpha = 0.12f) else Surface,
                focusedContainerColor = Primary.copy(alpha = 0.24f)
            ),
            border = ClickableSurfaceDefaults.border(
                border = Border(
                    BorderStroke(
                        1.dp,
                        if (showAdvancedOptions) Primary.copy(alpha = 0.45f) else SurfaceHighlight
                    )
                ),
                focusedBorder = Border(BorderStroke(3.dp, PrimaryLight))
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.setup_advanced_options_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.setup_advanced_options_helper),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }
                Text(
                    text = if (showAdvancedOptions) {
                        androidx.compose.ui.res.stringResource(R.string.setup_advanced_options_hide)
                    } else {
                        androidx.compose.ui.res.stringResource(R.string.setup_advanced_options_show)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryLight
                )
            }
        }

        AnimatedVisibility(visible = showAdvancedOptions) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (sourceType == SourceType.M3U_URL || sourceType == SourceType.M3U_FILE) {
                    TvSurface(
                        onClick = onToggleM3uVodClassification,
                        modifier = Modifier
                            .fillMaxWidth()
                            .mouseClickable(onClick = onToggleM3uVodClassification),
                        shape = ClickableSurfaceDefaults.shape(BrowseShapes.Artwork),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (uiState.m3uVodClassificationEnabled) Primary.copy(alpha = 0.1f) else Surface,
                            focusedContainerColor = Primary.copy(alpha = 0.22f)
                        ),
                        border = ClickableSurfaceDefaults.border(
                            border = Border(
                                BorderStroke(
                                    1.dp,
                                    if (uiState.m3uVodClassificationEnabled) Primary.copy(alpha = 0.4f) else SurfaceHighlight
                                )
                            ),
                            focusedBorder = Border(BorderStroke(3.dp, PrimaryLight))
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(R.string.setup_m3u_vod_classification_label),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = androidx.compose.ui.res.stringResource(R.string.setup_m3u_vod_classification_helper),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceDim
                                )
                            }
                            Switch(
                                checked = uiState.m3uVodClassificationEnabled,
                                onCheckedChange = { onToggleM3uVodClassification() }
                            )
                        }
                    }
                }

                if (sourceType == SourceType.XTREAM || sourceType == SourceType.M3U_URL || sourceType == SourceType.M3U_FILE) {
                    ProviderTextField(
                        value = httpUserAgent,
                        onValueChange = onHttpUserAgentChange,
                        placeholder = stringResource(R.string.setup_adv_user_agent_override_placeholder),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                if (sourceType == SourceType.XTREAM || sourceType == SourceType.STALKER || sourceType == SourceType.M3U_URL || sourceType == SourceType.M3U_FILE) {
                    ProviderTextField(
                        value = httpHeaders,
                        onValueChange = onHttpHeadersChange,
                        placeholder = if (sourceType == SourceType.STALKER) {
                            stringResource(R.string.setup_adv_headers_stalker_placeholder)
                        } else {
                            stringResource(R.string.setup_adv_headers_custom_placeholder)
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                if (supportsGuideAndLogoPolicy(sourceType)) {
                    if (sourceType == SourceType.XTREAM) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface, BrowseShapes.Artwork)
                                .border(1.dp, SurfaceHighlight, BrowseShapes.Artwork)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.setup_xtream_live_sync_mode_label),
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.setup_xtream_live_sync_mode_helper),
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceDim
                            )
                            ProviderXtreamLiveSyncMode.entries.forEach { mode ->
                                XtreamLiveSyncModeOptionRow(
                                    mode = mode,
                                    selected = uiState.xtreamLiveSyncMode == mode,
                                    onSelect = { onSelectXtreamLiveSyncMode(mode) }
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, BrowseShapes.Artwork)
                            .border(1.dp, SurfaceHighlight, BrowseShapes.Artwork)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.setup_guide_source_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.setup_guide_source_helper),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDim
                        )
                        GuideSourcePolicy.entries.forEach { policy ->
                            GuideSourcePolicyOptionRow(
                                policy = policy,
                                selected = uiState.guideSourcePolicy == policy,
                                onSelect = { onSelectGuideSourcePolicy(policy) }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, BrowseShapes.Artwork)
                            .border(1.dp, SurfaceHighlight, BrowseShapes.Artwork)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.setup_logo_source_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.setup_logo_source_helper),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDim
                        )
                        ChannelLogoSourcePolicy.entries.forEach { policy ->
                            ChannelLogoSourcePolicyOptionRow(
                                policy = policy,
                                selected = uiState.channelLogoSourcePolicy == policy,
                                onSelect = { onSelectChannelLogoSourcePolicy(policy) }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, BrowseShapes.Artwork)
                            .border(1.dp, SurfaceHighlight, BrowseShapes.Artwork)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.setup_epg_sync_mode_label),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(
                                if (sourceType == SourceType.STALKER) {
                                    R.string.setup_stalker_epg_sync_mode_helper
                                } else {
                                    R.string.setup_epg_sync_mode_helper
                                }
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDim
                        )
                        ProviderEpgSyncMode.entries.forEach { mode ->
                            EpgSyncModeOptionRow(
                                mode = mode,
                                sourceType = sourceType,
                                selected = uiState.epgSyncMode == mode,
                                onSelect = { onSelectEpgSyncMode(mode) }
                            )
                        }
                    }
                }

                if (sourceType == SourceType.STALKER) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, BrowseShapes.Artwork)
                            .border(1.dp, SurfaceHighlight, BrowseShapes.Artwork)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.setup_stalker_catalog_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.setup_stalker_catalog_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDim
                        )
                        PolicyOptionRow(
                            title = stringResource(R.string.setup_stalker_catalog_on_demand_title),
                            description = stringResource(R.string.setup_stalker_catalog_on_demand_description),
                            selected = uiState.stalkerCatalogMode == StalkerCatalogMode.ON_DEMAND,
                            onSelect = { onSelectStalkerCatalogMode(StalkerCatalogMode.ON_DEMAND) }
                        )
                        PolicyOptionRow(
                            title = stringResource(R.string.setup_stalker_catalog_background_title),
                            description = stringResource(R.string.setup_stalker_catalog_background_description),
                            selected = uiState.stalkerCatalogMode == StalkerCatalogMode.BACKGROUND_INDEX,
                            onSelect = { onSelectStalkerCatalogMode(StalkerCatalogMode.BACKGROUND_INDEX) }
                        )
                        Text(
                            text = stringResource(R.string.setup_stalker_load_order_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        PolicyOptionRow(
                            title = stringResource(R.string.setup_stalker_load_parallel_title),
                            description = stringResource(R.string.setup_stalker_load_parallel_description),
                            selected = uiState.stalkerCatalogLoadOrder == StalkerCatalogLoadOrder.PARALLEL,
                            onSelect = { onSelectStalkerCatalogLoadOrder(StalkerCatalogLoadOrder.PARALLEL) }
                        )
                        PolicyOptionRow(
                            title = stringResource(R.string.setup_stalker_load_movies_first_title),
                            description = stringResource(R.string.setup_stalker_load_movies_first_description),
                            selected = uiState.stalkerCatalogLoadOrder == StalkerCatalogLoadOrder.MOVIES_FIRST,
                            onSelect = { onSelectStalkerCatalogLoadOrder(StalkerCatalogLoadOrder.MOVIES_FIRST) }
                        )
                        PolicyOptionRow(
                            title = stringResource(R.string.setup_stalker_load_series_first_title),
                            description = stringResource(R.string.setup_stalker_load_series_first_description),
                            selected = uiState.stalkerCatalogLoadOrder == StalkerCatalogLoadOrder.SERIES_FIRST,
                            onSelect = { onSelectStalkerCatalogLoadOrder(StalkerCatalogLoadOrder.SERIES_FIRST) }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, BrowseShapes.Artwork)
                            .border(1.dp, SurfaceHighlight, BrowseShapes.Artwork)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.setup_stalker_auth_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.setup_stalker_auth_helper),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDim
                        )
                        StalkerAuthMode.entries.forEach { mode ->
                            StalkerAuthModeOptionRow(
                                mode = mode,
                                selected = stalkerAuthMode == mode,
                                onSelect = { onStalkerAuthModeChange(mode) }
                            )
                        }
                    }
                    if (stalkerAuthMode != StalkerAuthMode.MAC_ONLY) {
                        ProviderTextField(
                            value = username,
                            onValueChange = onUsernameChange,
                            placeholder = if (stalkerAuthMode == StalkerAuthMode.AUTO) {
                                stringResource(R.string.setup_stalker_username_optional_placeholder)
                            } else {
                                stringResource(R.string.setup_stalker_username_placeholder)
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Next
                            )
                        )
                        ProviderTextField(
                            value = password,
                            onValueChange = onPasswordChange,
                            placeholder = if (stalkerAuthMode == StalkerAuthMode.AUTO) {
                                stringResource(R.string.setup_stalker_password_optional_placeholder)
                            } else {
                                stringResource(R.string.setup_stalker_password_placeholder)
                            },
                            isPassword = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            )
                        )
                    }
                    ProviderTextField(
                        value = stalkerDeviceProfile,
                        onValueChange = onStalkerDeviceProfileChange,
                        placeholder = stringResource(R.string.setup_stalker_mag_type_placeholder)
                    )
                    ProviderTextField(
                        value = stalkerDeviceTimezone,
                        onValueChange = onStalkerDeviceTimezoneChange,
                        placeholder = stringResource(R.string.setup_stalker_timezone_placeholder)
                    )
                    ProviderTextField(
                        value = stalkerDeviceLocale,
                        onValueChange = onStalkerDeviceLocaleChange,
                        placeholder = stringResource(R.string.setup_stalker_locale_placeholder)
                    )
                    ProviderTextField(
                        value = stalkerSerialNumber,
                        onValueChange = onStalkerSerialNumberChange,
                        placeholder = stringResource(R.string.setup_stalker_serial_placeholder)
                    )
                    ProviderTextField(
                        value = stalkerDeviceId,
                        onValueChange = onStalkerDeviceIdChange,
                        placeholder = stringResource(R.string.setup_stalker_device_id_placeholder)
                    )
                    ProviderTextField(
                        value = stalkerDeviceId2,
                        onValueChange = onStalkerDeviceId2Change,
                        placeholder = stringResource(R.string.setup_stalker_device_id2_placeholder)
                    )
                    ProviderTextField(
                        value = stalkerSignature,
                        onValueChange = onStalkerSignatureChange,
                        placeholder = stringResource(R.string.setup_stalker_signature_placeholder)
                    )
                    HorizontalDivider(color = SurfaceHighlight.copy(alpha = 0.45f))
                    Text(
                        text = stringResource(R.string.setup_stalker_compat_section_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.setup_stalker_compat_section_helper),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                    ProviderTextField(
                        value = stalkerHwVersion,
                        onValueChange = onStalkerHwVersionChange,
                        placeholder = stringResource(R.string.setup_stalker_hw_version_placeholder)
                    )
                    ProviderTextField(
                        value = stalkerApiUserAgent,
                        onValueChange = onStalkerApiUserAgentChange,
                        placeholder = stringResource(R.string.setup_stalker_api_user_agent_placeholder)
                    )
                    ProviderTextField(
                        value = stalkerPlayerUserAgent,
                        onValueChange = onStalkerPlayerUserAgentChange,
                        placeholder = stringResource(R.string.setup_stalker_player_user_agent_placeholder)
                    )
                    ProviderTextField(
                        value = stalkerPlayerHeaders,
                        onValueChange = onStalkerPlayerHeadersChange,
                        placeholder = stringResource(R.string.setup_stalker_player_headers_placeholder)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.setup_stalker_x_user_agent_link),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StalkerLinkOptionButton(
                                text = stringResource(R.string.setup_stalker_link_ethernet),
                                selected = stalkerXUserAgentLink == ProviderStalkerAdvancedOptions.LINK_ETHERNET,
                                onClick = { onStalkerXUserAgentLinkChange(ProviderStalkerAdvancedOptions.LINK_ETHERNET) }
                            )
                            StalkerLinkOptionButton(
                                text = stringResource(R.string.setup_stalker_link_wifi),
                                selected = stalkerXUserAgentLink == ProviderStalkerAdvancedOptions.LINK_WIFI,
                                onClick = { onStalkerXUserAgentLinkChange(ProviderStalkerAdvancedOptions.LINK_WIFI) }
                            )
                        }
                    }
                    val proxyTitle = stringResource(R.string.setup_stalker_proxy_title)
                    TvClickableSurface(
                        onClick = { onStalkerProxyEnabledChange(!stalkerProxyEnabled) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = proxyTitle },
                        shape = ClickableSurfaceDefaults.shape(BrowseShapes.Artwork),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (stalkerProxyEnabled) Primary.copy(alpha = 0.1f) else Surface,
                            focusedContainerColor = Primary.copy(alpha = 0.22f)
                        ),
                        border = ClickableSurfaceDefaults.border(
                            border = Border(
                                BorderStroke(
                                    1.dp,
                                    if (stalkerProxyEnabled) Primary.copy(alpha = 0.4f) else SurfaceHighlight
                                )
                            ),
                            focusedBorder = Border(BorderStroke(3.dp, PrimaryLight))
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = proxyTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.setup_stalker_proxy_helper),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceDim
                                )
                            }
                            Switch(
                                checked = stalkerProxyEnabled,
                                onCheckedChange = onStalkerProxyEnabledChange
                            )
                        }
                    }
                    AnimatedVisibility(stalkerProxyEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProviderTextField(
                                value = stalkerProxyHost,
                                onValueChange = onStalkerProxyHostChange,
                                placeholder = stringResource(R.string.setup_stalker_proxy_host_placeholder)
                            )
                            ProviderTextField(
                                value = stalkerProxyPort,
                                onValueChange = onStalkerProxyPortChange,
                                placeholder = stringResource(R.string.setup_stalker_proxy_port_placeholder),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                    StalkerRequestRulesEditor(
                        rules = stalkerRequestRules,
                        onAddRule = onAddStalkerRequestRule,
                        onUpdateRule = onUpdateStalkerRequestRule,
                        onRemoveRule = onRemoveStalkerRequestRule
                    )
                }
            }
        }
    }
}
