package com.fagerberg.jason.common.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Locale

class LogHeaderTest {

    @Test
    fun `date string returns correct value for US locale`() {
        Locale.setDefault(Locale.US)
        assertThat(LogHeader(date = 20190005, bac = 15.0, duration = 112.0).dateString).isEqualTo("January 5th")
        assertThat(LogHeader(date = 20190105, bac = 15.0, duration = 112.0).dateString).isEqualTo("February 5th")
        assertThat(LogHeader(date = 20190205, bac = 15.0, duration = 112.0).dateString).isEqualTo("March 5th")
        assertThat(LogHeader(date = 20190305, bac = 15.0, duration = 112.0).dateString).isEqualTo("April 5th")
        assertThat(LogHeader(date = 20190405, bac = 15.0, duration = 112.0).dateString).isEqualTo("May 5th")
        assertThat(LogHeader(date = 20190505, bac = 15.0, duration = 112.0).dateString).isEqualTo("June 5th")
        assertThat(LogHeader(date = 20190605, bac = 15.0, duration = 112.0).dateString).isEqualTo("July 5th")
        assertThat(LogHeader(date = 20190705, bac = 15.0, duration = 112.0).dateString).isEqualTo("August 5th")
        assertThat(LogHeader(date = 20190802, bac = 15.0, duration = 112.0).dateString).isEqualTo("September 2nd")
        assertThat(LogHeader(date = 20190901, bac = 15.0, duration = 112.0).dateString).isEqualTo("October 1st")
        assertThat(LogHeader(date = 20191012, bac = 15.0, duration = 112.0).dateString).isEqualTo("November 12th")
        assertThat(LogHeader(date = 20191131, bac = 15.0, duration = 112.0).dateString).isEqualTo("December 31st")
    }

    @Test
    fun `date string returns correct value for other locale`() {
        Locale.setDefault(Locale.UK)
        assertThat(LogHeader(date = 20190005, bac = 15.0, duration = 112.0).dateString).isEqualTo("5th of January")
        assertThat(LogHeader(date = 20190105, bac = 15.0, duration = 112.0).dateString).isEqualTo("5th of February")
        assertThat(LogHeader(date = 20190205, bac = 15.0, duration = 112.0).dateString).isEqualTo("5th of March")
        assertThat(LogHeader(date = 20190305, bac = 15.0, duration = 112.0).dateString).isEqualTo("5th of April")
        assertThat(LogHeader(date = 20190405, bac = 15.0, duration = 112.0).dateString).isEqualTo("5th of May")
        assertThat(LogHeader(date = 20190505, bac = 15.0, duration = 112.0).dateString).isEqualTo("5th of June")
        assertThat(LogHeader(date = 20190605, bac = 15.0, duration = 112.0).dateString).isEqualTo("5th of July")
        assertThat(LogHeader(date = 20190705, bac = 15.0, duration = 112.0).dateString).isEqualTo("5th of August")
        assertThat(LogHeader(date = 20190802, bac = 15.0, duration = 112.0).dateString).isEqualTo("2nd of September")
        assertThat(LogHeader(date = 20190901, bac = 15.0, duration = 112.0).dateString).isEqualTo("1st of October")
        assertThat(LogHeader(date = 20191012, bac = 15.0, duration = 112.0).dateString).isEqualTo("12th of November")
        assertThat(LogHeader(date = 20191131, bac = 15.0, duration = 112.0).dateString).isEqualTo("31st of December")
    }

    @Test
    fun `duration string`() {
        assertThat(LogHeader(date = 20190005, bac = 15.0, duration = 10.25).durationString).isEqualTo("10:15")
    }

    @Test
    fun `duration string with padded 0s`() {
        assertThat(LogHeader(date = 20190005, bac = 15.0, duration = 1.05).durationString).isEqualTo("1:03")
    }
}
