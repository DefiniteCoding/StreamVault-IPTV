package com.streamvault.feature.provider.setup

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.Text
import com.streamvault.core.ui.components.dialogs.PremiumDialog
import com.streamvault.core.ui.components.dialogs.PremiumDialogFooterButton
import com.streamvault.feature.provider.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
internal fun ProviderSetupCompletionLayer(
    uiState: ProviderSetupState,
    knownLocalM3uUrls: Set<String>,
    selectedM3uUrl: String,
    filesDir: java.io.File,
    onProviderAdded: () -> Unit,
    onAttachCreatedProvider: () -> Unit,
    onSkipCreatedProviderCombinedAttach: () -> Unit,
    cleanupImportedFiles: suspend (java.io.File, Set<String>, Int) -> Unit = ::cleanupOldImportedM3uFilesAsync
) {
    val context = LocalContext.current
    val savedSyncPendingMessage = stringResource(R.string.setup_completion_saved_sync_pending)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(uiState.onboardingCompletion, uiState.completionWarning, uiState.pendingCombinedAttachProfileId) {
        if (
            uiState.onboardingCompletion != ProviderSetupViewModel.OnboardingCompletion.NONE &&
            uiState.pendingCombinedAttachProfileId == null
        ) {
            if (uiState.completionWarning != null) {
                Toast.makeText(context, savedSyncPendingMessage, Toast.LENGTH_LONG).show()
            }
            val previousLocal = uiState.m3uUrl.takeIf { it.startsWith("file://") }
            val selectedLocal = selectedM3uUrl.takeIf { it.startsWith("file://") }
            val protectedUris = buildSet {
                knownLocalM3uUrls.forEach { knownUri ->
                    if (knownUri != previousLocal || previousLocal == selectedLocal) add(knownUri)
                }
                selectedLocal?.let(::add)
            }
            cleanupImportedFiles(filesDir, protectedUris, 20)
            lifecycle.awaitResumedForProviderSetup()
            onProviderAdded()
        }
    }

    if (uiState.pendingCombinedAttachProfileId != null) {
        PremiumDialog(
            title = stringResource(R.string.setup_combined_attach_title),
            subtitle = stringResource(
                R.string.setup_combined_attach_message,
                uiState.createdProviderName ?: stringResource(R.string.setup_combined_attach_fallback_playlist),
                uiState.pendingCombinedAttachProfileName
                    ?: stringResource(R.string.setup_combined_attach_fallback_source)
            ),
            onDismissRequest = onSkipCreatedProviderCombinedAttach,
            content = {},
            footer = {
                PremiumDialogFooterButton(
                    label = stringResource(R.string.setup_combined_attach_skip),
                    onClick = onSkipCreatedProviderCombinedAttach
                )
                PremiumDialogFooterButton(
                    label = stringResource(R.string.setup_combined_attach_confirm),
                    onClick = onAttachCreatedProvider,
                    emphasized = true
                )
            }
        )
    }
}

private suspend fun Lifecycle.awaitResumedForProviderSetup() {
    if (currentState.isAtLeast(Lifecycle.State.RESUMED)) return

    suspendCancellableCoroutine { continuation ->
        lateinit var observer: LifecycleEventObserver
        observer = LifecycleEventObserver { source, _ ->
            if (source.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                source.lifecycle.removeObserver(observer)
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
        addObserver(observer)
        continuation.invokeOnCancellation { removeObserver(observer) }
    }
}
