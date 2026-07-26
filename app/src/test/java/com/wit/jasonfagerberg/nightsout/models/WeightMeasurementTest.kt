package com.wit.jasonfagerberg.nightsout.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WeightMeasurementTest {

    @Test
    fun `from lowercase string factory`() {
        assertThat(WeightMeasurement.fromLowercaseString("kg")).isEqualTo(WeightMeasurement.KG)
    }
}
