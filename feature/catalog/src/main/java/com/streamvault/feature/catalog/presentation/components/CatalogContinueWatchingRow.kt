package com.streamvault.feature.catalog.presentation.components

import com.streamvault.core.ui.design.shellHorizontalInset
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.streamvault.core.ui.design.FocusSafeLazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.core.ui.components.AppDialogButton
import com.streamvault.core.ui.components.dialogs.AppAlertDialog
import com.streamvault.core.ui.components.shell.BadgeScrimAlpha
import com.streamvault.core.ui.design.LocalAppSpacing
import com.streamvault.core.ui.design.requestFocusSafely
import com.streamvault.core.ui.design.browse.SurfaceElevated
import com.streamvault.core.ui.design.browse.TextPrimary
import com.streamvault.core.ui.design.browse.TextSecondary
import com.streamvault.core.ui.image.rememberCrossfadeImageModel
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.feature.catalog.R
import com.streamvault.feature.catalog.presentation.components.dialogs.ActionMenuDialog
import com.streamvault.feature.catalog.presentation.components.dialogs.ActionMenuEntry

private const val MillisPerMinute = 60_000L
private const val MinutesPerHour = 60
private val CardWidth = 208.dp
private val ArtHeight = 117.dp
// Two caption lines (13sp title, 12sp meta) plus padding.
private val CaptionHeight = 44.dp

/**
 * Whole minutes left in a title, rounded up so a nearly finished item reads "1m left", not "0m left".
 * Null when there is no honest figure: live streams, unknown duration, or nothing left.
 */
internal fun continueWatchingMinutesLeft(history: PlaybackHistory): Int? {
    if (history.contentType == ContentType.LIVE || history.totalDurationMs <= 0L) return null
    val remainingMs = history.totalDurationMs - history.resumePositionMs
    if (remainingMs <= 0L) return null
    return ((remainingMs + MillisPerMinute - 1) / MillisPerMinute).toInt()
}

/** Which card takes focus once [removedId] has left the list, so the D-pad is not dropped to the top bar. */
private data class RemovalHandoff(val removedId: Long, val focusId: Long?)

/**
 * Actions of the long-press menu. [onOpenDetails] and [onMarkWatched] are optional: a screen that cannot offer
 * one leaves it null and the entry is not shown.
 */
class ContinueWatchingMenuActions(
    val onResume: (PlaybackHistory) -> Unit,
    val onRemove: (PlaybackHistory) -> Unit,
    val onOpenDetails: ((PlaybackHistory) -> Unit)? = null,
    val onMarkWatched: ((PlaybackHistory) -> Unit)? = null
)

/**
 * Shelf of unfinished titles. When [menu] is set, a long press on a card opens the actions menu; the row never
 * edits [items] itself. [initialFocusModifier] goes on the first card so the screen can pick it as its initial focus.
 */
@Composable
fun ContinueWatchingRow(
    title: String? = null,
    items: List<PlaybackHistory>,
    onItemClick: (PlaybackHistory) -> Unit,
    modifier: Modifier = Modifier,
    menu: ContinueWatchingMenuActions? = null,
    initialFocusModifier: Modifier = Modifier,
    // Horizontal padding the shell puts around the screen; the row grows into it so cards slide off the screen edge.
    edgeGutter: Dp = LocalAppSpacing.current.shellHorizontalInset
) {
    if (items.isEmpty()) return

    var menuTarget by remember { mutableStateOf<PlaybackHistory?>(null) }
    var pendingRemoval by remember { mutableStateOf<PlaybackHistory?>(null) }
    var handoff by remember { mutableStateOf<RemovalHandoff?>(null) }
    val cardRequesters = remember { HashMap<Long, FocusRequester>() }

    LaunchedEffect(items) {
        cardRequesters.keys.retainAll(items.mapTo(HashSet()) { it.id })
        val pending = handoff ?: return@LaunchedEffect
        if (items.any { it.id == pending.removedId }) return@LaunchedEffect
        handoff = null
        // The dialog window is still leaving; wait a frame so focus is not taken back from the card.
        withFrameNanos { }
        // The neighbour may have scrolled out of the lazy row, leaving a requester with no attached node.
        pending.focusId?.let {
            cardRequesters[it]?.requestFocusSafely(tag = "ContinueWatchingRow", target = "Neighbour card")
        }
    }

    Column(modifier = modifier.fillMaxWidth().suppressParentVerticalScroll()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title ?: stringResource(R.string.continue_watching_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        FocusSafeLazyRow(
            modifier = Modifier.focusRestorer(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            edgeGutterStart = edgeGutter,
            edgeGutterEnd = edgeGutter
        ) {
            items(
                items = items,
                key = { it.id },
                contentType = { it.contentType }
            ) { history ->
                ContinueWatchingCard(
                    history = history,
                    modifier = if (history.id == items.first().id) initialFocusModifier else Modifier,
                    onClick = { onItemClick(history) },
                    onLongClick = menu?.let { { menuTarget = history } },
                    focusRequester = remember(history.id) {
                        cardRequesters.getOrPut(history.id) { FocusRequester() }
                    }
                )
            }
        }
    }

    fun planFocusHandoff(removed: PlaybackHistory) {
        val index = items.indexOfFirst { it.id == removed.id }
        val neighbor = items.getOrNull(index + 1) ?: items.getOrNull(index - 1)
        handoff = RemovalHandoff(removedId = removed.id, focusId = neighbor?.id)
    }

    val target = menuTarget
    if (target != null && menu != null) {
        ActionMenuDialog(
            title = target.title,
            entries = continueWatchingMenuEntries(
                history = target,
                menu = menu,
                onBeforeLeaveShelf = { planFocusHandoff(target) },
                onRequestRemoval = { pendingRemoval = target }
            ),
            onDismissRequest = { menuTarget = null }
        )
    }

    // Removal clears saved progress, so it keeps the confirm step even though the menu already named the action.
    val removal = pendingRemoval
    if (removal != null && menu != null) {
        ContinueWatchingRemovalDialog(
            history = removal,
            onDismiss = { pendingRemoval = null },
            onConfirm = {
                planFocusHandoff(removal)
                pendingRemoval = null
                menu.onRemove(removal)
            }
        )
    }
}

/** Confirms taking [history] off Continue Watching. Shared by the shelves and the Classic in-progress views. */
@Composable
fun ContinueWatchingRemovalDialog(
    history: PlaybackHistory,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val confirmFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { confirmFocusRequester.requestFocus() }
    AppAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppDialogButton(
                text = stringResource(R.string.add_group_remove_btn),
                onClick = onConfirm,
                primary = true,
                focusRequester = confirmFocusRequester
            )
        },
        dismissButton = {
            AppDialogButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                primary = false
            )
        },
        title = {
            Text(
                text = stringResource(R.string.continue_watching_remove_title),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Text(
                text = stringResource(R.string.continue_watching_remove_message, history.title),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        },
        containerColor = SurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@Composable
private fun continueWatchingMenuEntries(
    history: PlaybackHistory,
    menu: ContinueWatchingMenuActions,
    onBeforeLeaveShelf: () -> Unit,
    onRequestRemoval: () -> Unit
): List<ActionMenuEntry> {
    val isSeries = history.contentType == ContentType.SERIES || history.contentType == ContentType.SERIES_EPISODE
    val resumeLabel = stringResource(R.string.continue_watching_menu_resume)
    val detailsLabel = stringResource(R.string.continue_watching_menu_open_details)
    val watchedLabel = stringResource(
        if (isSeries) R.string.continue_watching_menu_mark_episode_watched else R.string.continue_watching_menu_mark_watched
    )
    val removeLabel = stringResource(R.string.continue_watching_menu_remove)
    return buildList {
        add(ActionMenuEntry(resumeLabel) { menu.onResume(history) })
        menu.onOpenDetails?.let { openDetails -> add(ActionMenuEntry(detailsLabel) { openDetails(history) }) }
        // Marking watched takes the card off the shelf, so it hands focus to a neighbour first; removal does the same after its confirm.
        menu.onMarkWatched?.let { markWatched ->
            add(ActionMenuEntry(watchedLabel) {
                onBeforeLeaveShelf()
                markWatched(history)
            })
        }
        add(ActionMenuEntry(removeLabel) { onRequestRemoval() })
    }
}

@Composable
private fun ContinueWatchingCard(
    history: PlaybackHistory,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    focusRequester: FocusRequester
) {
    val spacing = LocalAppSpacing.current
    val progress = if (history.totalDurationMs > 0) {
        (history.resumePositionMs.toFloat() / history.totalDurationMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    val minutesLeft = continueWatchingMinutesLeft(history)

    FocusableCard(
        onClick = onClick,
        modifier = modifier,
        onLongClick = onLongClick,
        width = CardWidth,
        height = ArtHeight + CaptionHeight,
        focusRequester = focusRequester
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ArtHeight)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                // Fallback always visible; covered by AsyncImage on successful load
                Text(
                    text = kindLabel(history),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary
                )
                if (!history.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = rememberCrossfadeImageModel(history.posterUrl),
                        contentDescription = history.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Rules 2 and 9: white ink on a black scrim of at least 55%, limited to the chip.
                if (minutesLeft != null) {
                    Text(
                        text = timeLeftLabel(minutesLeft),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = spacing.xs, bottom = spacing.sm)
                            .background(Color.Black.copy(alpha = BadgeScrimAlpha), RoundedCornerShape(4.dp))
                            .padding(horizontal = spacing.xs, vertical = spacing.xxs)
                    )
                }
                if (progress > 0f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(4.dp),
                        color = Color.White,
                        trackColor = Color.Black.copy(alpha = BadgeScrimAlpha),
                        // The default stop dot would draw a stray white mark at the end of the track.
                        drawStopIndicator = {}
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xs, vertical = spacing.xs)
            ) {
                Text(
                    text = history.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = metaLabel(history),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun kindLabel(history: PlaybackHistory): String = when (history.contentType) {
    ContentType.LIVE -> stringResource(R.string.card_live_badge)
    ContentType.MOVIE,
    ContentType.VOD -> stringResource(R.string.nav_vod)
    ContentType.SERIES, ContentType.SERIES_EPISODE -> stringResource(R.string.nav_series)
}

@Composable
private fun metaLabel(history: PlaybackHistory): String {
    val season = history.seasonNumber
    val episode = history.episodeNumber
    return when {
        history.contentType == ContentType.SERIES_EPISODE && season != null && episode != null ->
            stringResource(R.string.continue_watching_season_episode, season, episode)
        history.contentType == ContentType.MOVIE || history.contentType == ContentType.VOD ->
            stringResource(R.string.continue_watching_kind_movie)
        history.contentType == ContentType.LIVE -> stringResource(R.string.card_live_badge)
        else -> stringResource(R.string.nav_series)
    }
}

@Composable
private fun timeLeftLabel(minutesLeft: Int): String {
    val hours = minutesLeft / MinutesPerHour
    val minutes = minutesLeft % MinutesPerHour
    return when {
        hours == 0 -> stringResource(R.string.continue_watching_time_left_m, minutes)
        minutes == 0 -> stringResource(R.string.continue_watching_time_left_h, hours)
        else -> stringResource(R.string.continue_watching_time_left_hm, hours, minutes)
    }
}
