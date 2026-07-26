package com.wit.jasonfagerberg.nightsout.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Locale

class CountryUtilsTest {

    @Test
    fun `current local`() {
        Locale.setDefault(Locale.US)
        assertThat(getLocal().country).isEqualTo("US")
    }

    @Test
    fun `is 12 hour countries returns correct result`() {
        Locale.setDefault(Locale.US)
        assertThat(isCountryThatUses12HourTime()).isTrue()
        arrayOf("US", "UK", "PH", "CA", "AU", "NZ", "IN", "EG", "SA", "CO", "PK", "MY").forEach {
            assertThat(isCountryThatUses12HourTime(it)).isTrue()
        }

        arrayOf("FR", "GR", "JA").forEach {
            assertThat(isCountryThatUses12HourTime(it)).isFalse()
        }
    }
}
