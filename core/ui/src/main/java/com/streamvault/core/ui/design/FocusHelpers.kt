package com.streamvault.core.ui.design

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private const val FOCUS_TAG = "FocusHelpers"

fun FocusRequester.requestFocusSafely(tag: String = FOCUS_TAG, target: String = "Focus target"): Boolean {
    return try {
        requestFocus()
        true
    } catch (e: IllegalStateException) {
        Log.d(tag, "$target focus request failed: ${e.message}")
        false
    }
}

/**
 * Modifier for an initial-focus target inside a lazy list. Focus is requested only after the node's first
 * placement: lazy items attach during layout, and a request before placement makes the enclosing list
 * scroll to a rect that is not final yet (the page jumped down on screen entry).
 */
@Composable
fun rememberInitialFocusModifier(
    key: Any?,
    enabled: Boolean = true,
    tag: String = FOCUS_TAG,
    target: String = "Initial focus target"
): Modifier {
    val focusRequester = remember { FocusRequester() }
    var placed by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key, placed, enabled) {
        if (placed && enabled) {
            focusRequester.requestFocusSafely(tag = tag, target = target)
        }
    }
    return Modifier
        .focusRequester(focusRequester)
        .onGloballyPositioned { placed = true }
}

/**
 * Leaves the list alone while the focused item sits inside a comfort zone (the middle of the viewport),
 * and scrolls just far enough to bring it back to the zone edge otherwise. Compose's default rule pulls
 * the focused item toward a pivot even when it is fully visible, which made the detail pages jump when
 * Play took initial focus. A strict "only if off screen" rule fixed that but left rows hugging the screen
 * edge while moving down, so the zone keeps a margin on both sides.
 */
@OptIn(ExperimentalFoundationApi::class)
private object ComfortZoneBringIntoViewSpec : BringIntoViewSpec {
    private const val MARGIN_FRACTION = 0.2f

    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
        val trailing = offset + size
        val zoneStart = containerSize * MARGIN_FRACTION
        val zoneEnd = containerSize - zoneStart
        val fitsInZone = size <= zoneEnd - zoneStart
        return when {
            // Taller than the zone: show its leading edge unless it already fills the viewport.
            !fitsInZone -> if (offset < 0f && trailing > containerSize) 0f else offset - zoneStart
            offset < zoneStart -> offset - zoneStart
            trailing > zoneEnd -> trailing - zoneEnd
            else -> 0f
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComfortZoneScrollOnFocus(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBringIntoViewSpec provides ComfortZoneBringIntoViewSpec, content = content)
}

@Composable
fun TvInitialFocus(
    focusRequester: FocusRequester,
    enabled: Boolean = true
) {
    LaunchedEffect(focusRequester, enabled) {
        if (enabled) {
            focusRequester.requestFocusSafely(target = "Initial TV focus")
        }
    }
}

@Composable
fun FocusRestoreHost(
    enabled: Boolean = true,
    onRestore: suspend () -> Unit = {},
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnRestore by rememberUpdatedState(onRestore)
    var restoreTick by remember { mutableIntStateOf(0) }
    var hasComposed by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, enabled) {
        if (!enabled) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && hasComposed) {
                    restoreTick++
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    LaunchedEffect(enabled) {
        if (enabled) {
            hasComposed = true
            restoreTick++
        }
    }

    LaunchedEffect(enabled, restoreTick) {
        if (enabled) {
            latestOnRestore()
        }
    }

    content()
}

/**
 * Room a focused item needs outside its own bounds: the 3dp focus ring is drawn outside the item and the focus
 * scale (1.035) grows it further. A scrolling row clips along its scroll axis, so the first and last item lose
 * that overflow unless the row has padding for it.
 */
val FocusOverflowPadding = 8.dp

/** Width of the fade that tells the viewer a row continues past the screen edge. */
val MoreContentFadeWidth = 56.dp

/**
 * A [LazyRow] whose first and last item can show their focus ring and scale without being clipped, with no visible
 * change in layout: the overflow room is added inside the row and the row bleeds out by the same amount, so items
 * still line up with [contentPadding] as if the row had no extra room. Use it for every row of focusable items.
 *
 * A row inside a padded screen would clip its cards at the padding, a visible cut well short of the screen edge.
 * Pass the screen's padding as [edgeGutterStart] and [edgeGutterEnd] and the row grows into it: cards slide off the
 * screen edge, the first card still lines up with the padding. Leave a side at 0 when the row starts inside a
 * column (for example next to a poster) and must not bleed on that side.
 *
 * While more items exist past a side, the row fades out over [fadeWidth] at that edge, so a cut-off card reads as
 * "keep scrolling" and not as a layout bug. The fade is an offscreen mask, used only while the row can scroll.
 * Pass a [fadeWidth] of 0 to skip it entirely, with no modifier and no offscreen layer: use that for rows drawn over
 * video, where a new offscreen layer needs a measured TV GPU frame time first.
 */
@Composable
fun FocusSafeLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    edgeGutterStart: Dp = 0.dp,
    edgeGutterEnd: Dp = 0.dp,
    fadeWidth: Dp = MoreContentFadeWidth,
    content: LazyListScope.() -> Unit
) {
    val direction = LocalLayoutDirection.current
    val bleedStart = FocusOverflowPadding + edgeGutterStart
    val bleedEnd = FocusOverflowPadding + edgeGutterEnd
    LazyRow(
        modifier = modifier
            .bleed(start = bleedStart, end = bleedEnd, vertical = FocusOverflowPadding)
            .then(
                if (fadeWidth > 0.dp) Modifier.fadeWhenMoreContent(state, edgeInset = FocusOverflowPadding, fadeWidth = fadeWidth)
                else Modifier
            ),
        state = state,
        contentPadding = PaddingValues(
            start = contentPadding.calculateStart(direction) + bleedStart,
            top = contentPadding.calculateTopPadding() + FocusOverflowPadding,
            end = contentPadding.calculateEnd(direction) + bleedEnd,
            bottom = contentPadding.calculateBottomPadding() + FocusOverflowPadding
        ),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * Grows the node by [start], [end] and [vertical] on each side while reporting its original size, so the room
 * is only used for drawing. The vertical room also keeps the focus ring inside the offscreen layer of
 * [fadeWhenMoreContent], which clips to its bounds.
 */
private fun Modifier.bleed(start: Dp, end: Dp, vertical: Dp): Modifier = layout { measurable, constraints ->
    if (constraints.maxWidth == Constraints.Infinity) {
        val placeable = measurable.measure(constraints)
        return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    val extraStart = start.roundToPx()
    val extraEnd = end.roundToPx()
    val extraVertical = vertical.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extraStart + extraEnd))
    val height = (placeable.height - extraVertical * 2).coerceAtLeast(0)
    layout(constraints.maxWidth, height) { placeable.placeRelative(-extraStart, -extraVertical) }
}

/**
 * Masks the row to transparent over [fadeWidth] at each edge that has more items behind it. [edgeInset] is the
 * part of the row outside the screen (the bleed for ring overflow). State is read in the draw phase, so scrolling
 * never recomposes. Without more content nothing is masked and the layer is not offscreen.
 */
private fun Modifier.fadeWhenMoreContent(state: LazyListState, edgeInset: Dp, fadeWidth: Dp): Modifier = this
    .graphicsLayer {
        compositingStrategy =
            if (state.canScrollBackward || state.canScrollForward) CompositingStrategy.Offscreen
            else CompositingStrategy.Auto
    }
    .drawWithContent {
        drawContent()
        val isLtr = layoutDirection == LayoutDirection.Ltr
        val fadeLeft = if (isLtr) state.canScrollBackward else state.canScrollForward
        val fadeRight = if (isLtr) state.canScrollForward else state.canScrollBackward
        val inset = edgeInset.toPx()
        val width = fadeWidth.toPx()
        if (fadeLeft) {
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Transparent, 1f to Color.Black,
                    startX = inset, endX = inset + width
                ),
                size = Size(inset + width, size.height),
                blendMode = BlendMode.DstIn
            )
        }
        if (fadeRight) {
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Black, 1f to Color.Transparent,
                    startX = size.width - inset - width, endX = size.width - inset
                ),
                topLeft = Offset(size.width - inset - width, 0f),
                size = Size(inset + width, size.height),
                blendMode = BlendMode.DstIn
            )
        }
    }

private fun PaddingValues.calculateStart(direction: LayoutDirection): Dp =
    if (direction == LayoutDirection.Ltr) calculateLeftPadding(direction) else calculateRightPadding(direction)

private fun PaddingValues.calculateEnd(direction: LayoutDirection): Dp =
    if (direction == LayoutDirection.Ltr) calculateRightPadding(direction) else calculateLeftPadding(direction)
