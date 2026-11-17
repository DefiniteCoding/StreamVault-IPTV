package com.streamvault.feature.live.home

import androidx.compose.runtime.Composable
import com.streamvault.core.ui.components.dialogs.PremiumDialog
import com.streamvault.core.ui.components.dialogs.PremiumDialogFooterButton
import com.streamvault.feature.live.presentation.components.LiveQuickFiltersContent

@Composable
fun LiveQuickFiltersDialog(
    title: String,
    subtitle: String,
    closeLabel: String,
    savedFilters: List<String>,
    categorySearchQuery: String,
    activeFilter: String?,
    hiddenCategoriesButtonLabel: String?,
    hiddenChannelsButtonLabel: String?,
    chipTitle: String,
    allLabel: String,
    emptyLabel: String,
    addChipLabel: String,
    onShowHiddenCategories: () -> Unit,
    onShowHiddenChannels: () -> Unit,
    onAddFilter: () -> Unit,
    onAllSelected: () -> Unit,
    onSavedFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    PremiumDialog(
        title = title,
        subtitle = subtitle,
        onDismissRequest = onDismiss,
        widthFraction = 0.55f,
        heightFraction = 0.9f,
        bodyHeightFraction = 0.8f,
        content = {
            LiveQuickFiltersContent(
                savedFilters = savedFilters,
                categorySearchQuery = categorySearchQuery,
                activeFilter = activeFilter,
                hiddenCategoriesButtonLabel = hiddenCategoriesButtonLabel,
                hiddenChannelsButtonLabel = hiddenChannelsButtonLabel,
                chipTitle = chipTitle,
                allLabel = allLabel,
                emptyLabel = emptyLabel,
                addChipLabel = addChipLabel,
                onShowHiddenCategories = onShowHiddenCategories,
                onShowHiddenChannels = onShowHiddenChannels,
                onAddFilter = onAddFilter,
                onAllSelected = onAllSelected,
                onSavedFilterSelected = onSavedFilterSelected
            )
        },
        footer = {
            PremiumDialogFooterButton(label = closeLabel, onClick = onDismiss)
        }
    )
}
