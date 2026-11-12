package com.streamvault.core.ui.design

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Typography
import com.streamvault.core.ui.R
import java.text.Normalizer
import java.util.Locale

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val LocaleSafeSansFamily = FontFamily.SansSerif

// Of the app's scripts only Latin, Cyrillic and Greek have letter case. Arabic, Hebrew, Devanagari, Thai and CJK
// keep the kicker weight only: tracking would break Arabic joining and Indic and Thai clusters. Unknown languages
// fall on that safe side. Android keeps "in" while JDK 17+ reports "id", so both are listed.
private val CasedScriptLanguages = setOf(
    "bg", "cs", "da", "de", "el", "en", "es", "fi", "fil", "fr", "hr", "hu", "id", "in", "it", "mk",
    "ms", "nb", "nl", "pl", "pt", "ro", "ru", "sk", "sq", "sr", "sv", "tr", "uk", "vi"
)
private val CasedScripts = setOf("Latn", "Cyrl", "Grek")

private const val COMBINING_TONOS = '\u0301'
private const val COMBINING_DIALYTIKA = '\u0308'
private const val GREEK_DIPHTHONG_FIRST = "αεουΑΕΟΥ"
private const val GREEK_DIPHTHONG_SECOND = "ιυΙΥ"

private val KickerTracking = 0.08.em

/** Styles that tv-material3's fixed [Typography] slots cannot hold. Read through [LocalAppTypeTokens]. */
@Immutable
data class AppTypeTokens(
    val labelKicker: TextStyle,
    val labelNumeric: TextStyle,
    private val kickerUppercaseLocale: Locale?,
) {
    /** Uppercases with the app locale (so Turkish gets a dotted capital I); caseless scripts pass through. */
    fun kicker(text: String): String {
        val locale = kickerUppercaseLocale ?: return text
        return if (isGreek(locale)) uppercaseGreek(text, locale) else text.uppercase(locale)
    }
}

/**
 * Greek capitals drop the tonos. When the dropped tonos sat on the first of two vowels that would otherwise read as
 * a diphthong, the second vowel takes a dialytika instead: "Μάιος" becomes "ΜΑΪΟΣ".
 */
internal fun uppercaseGreek(text: String, locale: Locale): String {
    val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
    val out = StringBuilder(decomposed.length + 2)
    var index = 0
    while (index < decomposed.length) {
        val char = decomposed[index]
        if (char != COMBINING_TONOS) {
            out.append(char)
            index++
            continue
        }
        val accentedVowel = out.lastOrNull()
        val next = decomposed.getOrNull(index + 1)
        val nextHasDialytika = decomposed.getOrNull(index + 2) == COMBINING_DIALYTIKA
        if (accentedVowel != null && accentedVowel in GREEK_DIPHTHONG_FIRST &&
            next != null && next in GREEK_DIPHTHONG_SECOND && !nextHasDialytika
        ) {
            out.append(next).append(COMBINING_DIALYTIKA)
            index += 2
        } else {
            index++
        }
    }
    return Normalizer.normalize(out.toString().uppercase(locale), Normalizer.Form.NFC)
}

val LocalAppTypeTokens = staticCompositionLocalOf { appTypeTokensFor(Locale.ENGLISH) }

@Composable
fun rememberAppLocale(): Locale {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0] ?: Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale ?: Locale.getDefault()
        }
    }
}

@Composable
fun rememberAppTypography(locale: Locale = rememberAppLocale()): Typography =
    remember(locale.toLanguageTag()) {
        createTypography(fontFamilyFor(locale))
    }

@Composable
fun rememberAppTypeTokens(locale: Locale = rememberAppLocale()): AppTypeTokens =
    remember(locale.toLanguageTag()) {
        appTypeTokensFor(locale)
    }

internal fun appTypeTokensFor(locale: Locale): AppTypeTokens {
    val fontFamily = fontFamilyFor(locale)
    val cased = usesCasedScript(locale)
    return AppTypeTokens(
        labelKicker = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = if (cased) KickerTracking else TextUnit.Unspecified
        ),
        labelNumeric = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontFeatureSettings = "tnum"
        ),
        kickerUppercaseLocale = locale.takeIf { cased }
    )
}

private fun usesCasedScript(locale: Locale): Boolean =
    if (locale.script.isNotEmpty()) {
        locale.script in CasedScripts
    } else {
        locale.language in CasedScriptLanguages
    }

private fun isGreek(locale: Locale): Boolean =
    if (locale.script.isNotEmpty()) locale.script == "Grek" else locale.language == "el"

private fun fontFamilyFor(locale: Locale): FontFamily =
    if (locale.language.equals("en", ignoreCase = true)) {
        InterFamily
    } else {
        LocaleSafeSansFamily
    }

private fun createTypography(fontFamily: FontFamily) = Typography(
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 13.sp
    )
)
