package com.wit.jasonfagerberg.nightsout.constants

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.assertj.core.data.Percentage.withPercentage
import org.junit.Test
import java.util.Calendar

class ConstantsTest {

    @Test
    fun `get long time now returns current time in milliseconds`() {
        assertThat(Constants.getLongTimeNow()).isCloseTo(Calendar.getInstance().timeInMillis, withPercentage(0.00001))
    }

    @Test
    fun `get current time in minuets returns 24-hour time`() {
        val actual = Constants.getCurrentTimeInMinuets()
        val now = Calendar.getInstance()
        val expected = now[Calendar.HOUR_OF_DAY] * 60 + now[Calendar.MINUTE]
        assertThat(actual).isBetween(0, 1439)
        assertThat(actual).isCloseTo(expected, within(1))
    }
}
