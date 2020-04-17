package com.fagerberg.jason.common.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CountryUtilsTest {

    @Test
    fun `current local`() {
        assertThat(getLocal().country).isEqualTo("US")
    }

    @Test
    fun `is 12 hour countries returns correct result`() {
        assertThat(isCountryThatUses12HourTime()).isTrue()
        arrayOf("US", "UK", "PH", "CA", "AU", "NZ", "IN", "EG", "SA", "CO", "PK", "MY").forEach {
            assertThat(isCountryThatUses12HourTime(it)).isTrue()
        }

        arrayOf("FR", "GR", "JA").forEach {
            assertThat(isCountryThatUses12HourTime(it)).isFalse()
        }
    }
}
