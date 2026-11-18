package com.streamvault.core.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.core.ui.design.AppColors
import com.streamvault.core.ui.design.AppPalette
import com.streamvault.core.ui.design.BrowseColors
import com.streamvault.core.ui.design.BrowseFocusSpec
import com.streamvault.core.ui.design.BrowseShapes
import com.streamvault.core.ui.design.LocalBrowseVisuals
import com.streamvault.core.ui.design.contentOnColor
import com.streamvault.core.ui.device.rememberIsTelevisionDevice
import com.streamvault.core.ui.interaction.mouseClickable
import com.streamvault.core.ui.theme.FocusBorder
import com.streamvault.core.ui.theme.SurfaceElevated
import com.streamvault.core.ui.theme.SurfaceHighlight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A TV-optimized search input field.
 * The shell is focusable for D-pad navigation and only enters edit mode on click/select.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    imeAction: ImeAction = ImeAction.Search,
    onSearch: () -> Unit = {},
    enabled: Boolean = true
) {
    val isTelevisionDevice = rememberIsTelevisionDevice()
    var hasContainerFocus by remember { mutableStateOf(false) }
    var hasInputFocus by remember { mutableStateOf(false) }
    var acceptsInput by remember { mutableStateOf(false) }
    var pendingInputActivation by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val inputFocusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val isFocused = enabled && (hasContainerFocus || hasInputFocus)

    fun requestBringIntoView(delayMillis: Long = 0L) {
        coroutineScope.launch {
            if (delayMillis > 0) {
                delay(delayMillis)
            }
            runCatching { bringIntoViewRequester.bringIntoView() }
        }
    }

    fun activateInput() {
        if (!enabled) {
            return
        }
        if (!isTelevisionDevice) {
            acceptsInput = true
            inputFocusRequester.requestFocus()
            keyboardController?.show()
            requestBringIntoView()
            requestBringIntoView(180)
            return
        }
        acceptsInput = true
        pendingInputActivation = true
        requestBringIntoView()
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            val coercedSelectionStart = textFieldValue.selection.start.coerceIn(0, value.length)
            val coercedSelectionEnd = textFieldValue.selection.end.coerceIn(0, value.length)
            val coercedComposition = textFieldValue.composition?.let { composition ->
                val compositionStart = composition.start.coerceIn(0, value.length)
                val compositionEnd = composition.end.coerceIn(0, value.length)
                if (compositionStart <= compositionEnd) {
                    TextRange(compositionStart, compositionEnd)
                } else {
                    null
                }
            }
            textFieldValue = textFieldValue.copy(
                text = value,
                selection = TextRange(coercedSelectionStart, coercedSelectionEnd),
                composition = coercedComposition
            )
        }
    }

    LaunchedEffect(acceptsInput, pendingInputActivation) {
        if (!isTelevisionDevice || !acceptsInput || !pendingInputActivation) {
            return@LaunchedEffect
        }
        inputFocusRequester.requestFocus()
        keyboardController?.show()
        requestBringIntoView(120)
        pendingInputActivation = false
    }

    BackHandler(enabled = shouldConsumeSearchInputBack(acceptsInput)) {
        pendingInputActivation = false
        acceptsInput = false
        keyboardController?.hide()
        focusRequester.requestFocus()
    }

    val modern = LocalBrowseVisuals.current
    val palette = if (modern) BrowseColors.current else AppColors.current
    val searchShape = if (modern) BrowseShapes.Control else RoundedCornerShape(12.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val colors = remember(palette, isFocused) { inputFieldColors(palette, isFocused) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .focusRequester(focusRequester)
            .bringIntoViewRequester(bringIntoViewRequester)
            .background(colors.container, searchShape)
            // Rest state is a filled container with no stroke; focus adds the ring on top of the focus fill.
            .then(
                if (isFocused) Modifier.border(BrowseFocusSpec.SelectableRingWidth, palette.focus, searchShape)
                else Modifier
            )
            .semantics(mergeDescendants = true) {
                contentDescription = placeholder
                stateDescription = value.ifBlank { placeholder }
            }
            .onFocusChanged {
                hasContainerFocus = enabled && it.isFocused
            }
            .focusProperties {
                canFocus = enabled
            }
            .mouseClickable(enabled = enabled, focusRequester = focusRequester) {
                activateInput()
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) {
                activateInput()
            }
            .focusable(enabled = enabled)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.placeholder
                    )
                }

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { updatedValue ->
                        textFieldValue = updatedValue
                        if (updatedValue.text != value) {
                            onValueChange(updatedValue.text)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(inputFocusRequester)
                        .focusProperties {
                            canFocus = enabled && acceptsInput
                        }
                        .onFocusChanged {
                            hasInputFocus = enabled && it.hasFocus
                            if (it.hasFocus) {
                                requestBringIntoView(120)
                            } else {
                                acceptsInput = false
                                keyboardController?.hide()
                            }
                        }
                        .onPreviewKeyEvent { event ->
                            if (!isTelevisionDevice || !acceptsInput) {
                                return@onPreviewKeyEvent false
                            }
                            handleTvSearchKey(event, textFieldValue, { textFieldValue = it },
                                focusManager::moveFocus) {
                                pendingInputActivation = false
                                acceptsInput = false
                                keyboardController?.hide()
                            }
                        },
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.text),
                    singleLine = true,
                    cursorBrush = SolidColor(colors.cursor),
                    enabled = enabled,
                    readOnly = !acceptsInput,
                    keyboardOptions = KeyboardOptions(imeAction = imeAction),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch()
                            acceptsInput = false
                            keyboardController?.hide()
                            val movedFocus = focusManager.moveFocus(FocusDirection.Down)
                            if (!movedFocus) {
                                focusRequester.requestFocus()
                            }
                        }
                    )
                )
            }

            Icon(
                imageVector = if (value.isBlank()) Icons.Default.Search else Icons.Default.Close,
                contentDescription = null,
                tint = colors.icon,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .clickable(enabled = enabled && value.isNotBlank()) {
                        textFieldValue = TextFieldValue(text = "", selection = TextRange(0))
                        onValueChange("")
                        activateInput()
                    }
            )
        }
    }
}

/** Ink and fill of a text input (search and setup forms). Focus takes the tonal selected fill (the Focus ring is drawn on top), so every ink flips to [AppPalette.onSelectedContainer]. */
data class InputFieldColors(
    val container: Color,
    val text: Color,
    val placeholder: Color,
    val icon: Color,
    val cursor: Color
)

fun inputFieldColors(palette: AppPalette, focused: Boolean): InputFieldColors =
    if (focused) {
        InputFieldColors(
            container = palette.selectedContainer,
            text = palette.onSelectedContainer,
            placeholder = palette.onSelectedContainer,
            icon = palette.onSelectedContainer,
            cursor = palette.onSelectedContainer
        )
    } else {
        restInputFieldColors(palette)
    }

private const val INPUT_FILL_CONTRAST = 3f
private const val INPUT_VALUE_CONTRAST = 5f
private const val INPUT_PLACEHOLDER_CONTRAST = 4.5f
private const val SHADE_STEP = 0.01f

// An input has no stroke at rest (rule 14), so its fill alone marks it and holds 3:1 (rule 7) against the canvas and
// the surface it sits on. controlContainer stays the lighter fill of buttons and chips, which carry a label.
// Ink is then pulled toward the strongest ink the fill allows, because textSecondary falls under 4.5:1 on it.
internal fun restInputFieldColors(palette: AppPalette): InputFieldColors {
    val container = shadeUntil(palette.controlContainer, palette.textPrimary) {
        contrastRatio(it, palette.canvas) >= INPUT_FILL_CONTRAST && contrastRatio(it, palette.surface) >= INPUT_FILL_CONTRAST
    }
    val strongest = contentOnColor(container)
    val value = shadeUntil(palette.textPrimary, strongest) { contrastRatio(it, container) >= INPUT_VALUE_CONTRAST }
    val placeholder = shadeUntil(palette.textSecondary, value) { contrastRatio(it, container) >= INPUT_PLACEHOLDER_CONTRAST }
    return InputFieldColors(
        container = container,
        text = value,
        placeholder = placeholder,
        icon = placeholder,
        cursor = if (contrastRatio(palette.brand, container) >= INPUT_FILL_CONTRAST) palette.brand else value
    )
}

// First color on the way from [from] to [to] that satisfies [accepts]; [to] when none does.
private inline fun shadeUntil(from: Color, to: Color, accepts: (Color) -> Boolean): Color {
    var step = 0f
    var shade = from
    while (!accepts(shade) && step < 1f) {
        step = minOf(1f, step + SHADE_STEP)
        shade = lerp(from, to, step)
    }
    return shade
}

private fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = first.luminance()
    val secondLuminance = second.luminance()
    return (maxOf(firstLuminance, secondLuminance) + 0.05f) / (minOf(firstLuminance, secondLuminance) + 0.05f)
}

internal fun shouldConsumeSearchInputBack(acceptsInput: Boolean): Boolean = acceptsInput
