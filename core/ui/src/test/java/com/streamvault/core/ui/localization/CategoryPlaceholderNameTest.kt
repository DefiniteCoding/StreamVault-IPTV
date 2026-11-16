package com.streamvault.core.ui.localization

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CategoryPlaceholderNameTest {

    @Test
    fun `sync placeholder yields the provider category id`() {
        assertThat(categoryPlaceholderId("Category 226")).isEqualTo("226")
    }

    @Test
    fun `provider supplied names are not placeholders`() {
        assertThat(categoryPlaceholderId("Sports HD")).isNull()
        assertThat(categoryPlaceholderId("Category Kids")).isNull()
        assertThat(categoryPlaceholderId("Category 12 Extra")).isNull()
        assertThat(categoryPlaceholderId("category 12")).isNull()
        assertThat(categoryPlaceholderId("")).isNull()
    }
}
