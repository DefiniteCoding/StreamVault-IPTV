package com.streamvault.core.ui.design

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.util.Locale

class AppTypeTokensTest {

    @Test
    fun `every app locale is classified as cased or caseless`() {
        val tags = appLocaleTags()

        assertThat(tags).hasSize(CASED_TAGS.size + CASELESS_TAGS.size)
        assertThat(tags.filterNot { it in CASED_TAGS || it in CASELESS_TAGS }).isEmpty()
    }

    @Test
    fun `cased locales uppercase and track the kicker`() {
        CASED_TAGS.forEach { tag ->
            val tokens = tokensFor(tag)

            assertThat(tokens.kicker("abc")).isEqualTo("ABC")
            assertThat(tokens.labelKicker.letterSpacing).isNotEqualTo(TextUnit.Unspecified)
        }
    }

    @Test
    fun `caseless locales keep the text as written and add no tracking`() {
        CASELESS_TAGS.forEach { tag ->
            val tokens = tokensFor(tag)

            assertThat(tokens.kicker("Mixed text")).isEqualTo("Mixed text")
            assertThat(tokens.labelKicker.letterSpacing).isEqualTo(TextUnit.Unspecified)
        }
    }

    @Test
    fun `turkish kicker uppercases with the dotted capital i`() {
        assertThat(tokensFor("tr").kicker("title")).isEqualTo("TİTLE")
    }

    @Test
    fun `cyrillic kicker is uppercase`() {
        assertThat(tokensFor("ru").kicker("Новости")).isEqualTo("НОВОСТИ")
        assertThat(tokensFor("uk").kicker("Фільми")).isEqualTo("ФІЛЬМИ")
    }

    @Test
    fun `greek kicker drops the tonos and keeps the dialytika`() {
        val greek = tokensFor("el")

        assertThat(greek.kicker("Ταινίες")).isEqualTo("ΤΑΙΝΙΕΣ")
        assertThat(greek.kicker("Ευρώπη")).isEqualTo("ΕΥΡΩΠΗ")
        assertThat(greek.kicker("Προϊόν")).isEqualTo("ΠΡΟΪΟΝ")
    }

    @Test
    fun `greek kicker adds a dialytika where the dropped tonos split a diphthong`() {
        val greek = tokensFor("el")

        assertThat(greek.kicker("Μάιος")).isEqualTo("ΜΑΪΟΣ")
        assertThat(greek.kicker("ρολόι")).isEqualTo("ΡΟΛΟΪ")
    }

    @Test
    fun `explicit script subtag decides the treatment`() {
        assertThat(tokensFor("sr-Latn").kicker("vesti")).isEqualTo("VESTI")
        assertThat(tokensFor("sr-Cyrl").kicker("вести")).isEqualTo("ВЕСТИ")
    }

    @Test
    fun `indonesian is cased under both the legacy and the current code`() {
        // Android reports "in"; JDK 17+ normalises the same locale to "id".
        assertThat(appTypeTokensFor(Locale("in")).kicker("baru")).isEqualTo("BARU")
        assertThat(tokensFor("id").kicker("baru")).isEqualTo("BARU")
    }

    @Test
    fun `unknown languages fall back to plain text without tracking`() {
        val tokens = tokensFor("ur")

        assertThat(tokens.kicker("Mixed text")).isEqualTo("Mixed text")
        assertThat(tokens.labelKicker.letterSpacing).isEqualTo(TextUnit.Unspecified)
    }

    @Test
    fun `numeric label uses tabular figures at the tv minimum size`() {
        val numeric = tokensFor("en").labelNumeric

        assertThat(numeric.fontFeatureSettings).isEqualTo("tnum")
        assertThat(numeric.fontSize).isEqualTo(14.sp)
    }

    @Test
    fun `kicker is semibold at the tv minimum size`() {
        val kicker = tokensFor("en").labelKicker

        assertThat(kicker.fontWeight).isEqualTo(FontWeight.SemiBold)
        assertThat(kicker.fontSize).isEqualTo(14.sp)
    }

    private fun tokensFor(tag: String) = appTypeTokensFor(Locale.forLanguageTag(tag))

    private fun appLocaleTags(): List<String> {
        val config = generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "app/src/main/res/xml/locales_config.xml") }
            .first { it.exists() }
        return Regex("""android:name="([^"]+)"""").findAll(config.readText()).map { it.groupValues[1] }.toList()
    }

    private companion object {
        val CASED_TAGS = setOf(
            "en", "bg", "cs", "da", "de", "el", "es", "fi", "fil", "fr", "hr", "hu", "in", "it", "mk", "ms",
            "nb", "nl", "pl", "pt", "ro", "ru", "sk", "sq", "sr", "sv", "tr", "uk", "vi"
        )
        val CASELESS_TAGS = setOf("ar", "fa", "iw", "hi", "th", "ja", "ko", "zh", "zh-TW")
    }
}
