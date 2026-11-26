package com.streamvault.core.ui.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 40.dp,
    val screenGutter: Dp = 56.dp,
    val railWidth: Dp = 124.dp,
    val sectionGap: Dp = 32.dp,
    val cardGap: Dp = 16.dp,
    val chipGap: Dp = 10.dp,
    val channelListOverlayWidth: Dp = 420.dp,
    val channelListPanelWidth: Dp = 760.dp,
    val channelListMinTouchTarget: Dp = 48.dp,
    val safeTop: Dp = 32.dp,
    val safeBottom: Dp = 32.dp,
    val safeHoriz: Dp = 56.dp
)

/**
 * Horizontal padding the screen shell puts around every screen's content. Rows of focusable items pass it as their
 * edge gutter so cards slide off the screen edge instead of being cut at this padding.
 */
val AppSpacing.shellHorizontalInset: Dp get() = md

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
