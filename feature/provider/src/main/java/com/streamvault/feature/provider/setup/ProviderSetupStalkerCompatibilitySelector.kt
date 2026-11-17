package com.streamvault.feature.provider.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.core.ui.design.browse.AccentAmber
import com.streamvault.core.ui.design.browse.OnBackground
import com.streamvault.domain.model.StalkerCompatibilityRegistry
import com.streamvault.domain.model.StalkerCompatibilityProfileIds
import com.streamvault.domain.model.StalkerProfileVerification
import com.streamvault.domain.model.StalkerProtocolPreference
import com.streamvault.feature.provider.R

@Composable
internal fun StalkerCompatibilitySelector(
    protocol: StalkerProtocolPreference,
    profileId: String,
    onProtocolSelected: (StalkerProtocolPreference) -> Unit,
    onProfileSelected: (String) -> Unit
) {
    var showProtocols by rememberSaveable { mutableStateOf(false) }
    var showProfiles by rememberSaveable { mutableStateOf(false) }
    val selectedProfile = StalkerCompatibilityRegistry.find(profileId)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.setup_stalker_selector_title), style = MaterialTheme.typography.titleSmall, color = OnBackground)
        SmallActionButton(
            text = stringResource(R.string.setup_stalker_protocol_button, protocol.label()),
            onClick = { showProtocols = !showProtocols }
        )
        AnimatedVisibility(showProtocols) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StalkerProtocolPreference.entries
                    .filterNot { it == StalkerProtocolPreference.MINISTRA_API_V3 }
                    .forEach { option ->
                        SmallActionButton(
                            text = if (option == protocol) "✓ ${option.label()}" else option.label(),
                            onClick = {
                                onProtocolSelected(option)
                                onProfileSelected(StalkerCompatibilityProfileIds.AUTO)
                                showProtocols = false
                            }
                        )
                    }
            }
        }
        SmallActionButton(
            text = stringResource(
                R.string.setup_stalker_model_button,
                selectedProfile?.displayName ?: stringResource(R.string.setup_stalker_model_automatic)
            ),
            onClick = { showProfiles = !showProfiles }
        )
        AnimatedVisibility(showProfiles) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val automaticLabel = stringResource(R.string.setup_stalker_model_automatic)
                SmallActionButton(
                    text = if (profileId == StalkerCompatibilityProfileIds.AUTO) "✓ $automaticLabel" else automaticLabel,
                    onClick = {
                        onProfileSelected(StalkerCompatibilityProfileIds.AUTO)
                        showProfiles = false
                    }
                )
                val family = if (protocol == StalkerProtocolPreference.MINISTRA_API_V3) {
                    com.streamvault.domain.model.StalkerProtocolFamily.MINISTRA_API_V3
                } else {
                    com.streamvault.domain.model.StalkerProtocolFamily.CLASSIC_MAG
                }
                StalkerCompatibilityRegistry.profiles
                    .filter { it.protocolFamily == family }
                    .groupBy { it.generation }
                    .forEach { (generation, profiles) ->
                        Text(generation, style = MaterialTheme.typography.labelMedium, color = OnBackground.copy(alpha = 0.72f))
                        profiles.forEach { option ->
                            val experimental = option.verification == StalkerProfileVerification.EXPERIMENTAL
                            SmallActionButton(
                                text = buildString {
                                    if (option.id == profileId) append("✓ ")
                                    append(
                                        if (experimental) {
                                            stringResource(R.string.setup_stalker_profile_experimental, option.displayName)
                                        } else {
                                            option.displayName
                                        }
                                    )
                                },
                                onClick = {
                                    onProfileSelected(option.id)
                                    showProfiles = false
                                }
                            )
                        }
                    }
                if (family == com.streamvault.domain.model.StalkerProtocolFamily.CLASSIC_MAG) {
                    Text(
                        stringResource(R.string.setup_stalker_experimental_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentAmber
                    )
                    val customLabel = stringResource(R.string.setup_stalker_profile_custom)
                    SmallActionButton(
                        text = if (profileId == StalkerCompatibilityProfileIds.CUSTOM) "✓ $customLabel" else customLabel,
                        onClick = {
                            onProfileSelected(StalkerCompatibilityProfileIds.CUSTOM)
                            showProfiles = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StalkerProtocolPreference.label(): String = stringResource(
    when (this) {
        StalkerProtocolPreference.AUTO -> R.string.setup_stalker_protocol_auto
        StalkerProtocolPreference.CLASSIC_MAG -> R.string.setup_stalker_protocol_classic_mag
        StalkerProtocolPreference.MINISTRA_API_V3 -> R.string.setup_stalker_protocol_ministra
    }
)
