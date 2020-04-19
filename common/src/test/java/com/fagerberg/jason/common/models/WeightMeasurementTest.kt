package com.fagerberg.jason.common.models

import org.assertj.core.api.Assertions
import org.junit.Test

class WeightMeasurementTest {

    @Test
    fun `from lowercase string factory`() {
        Assertions.assertThat(WeightMeasurement.fromLowercaseString("kg")).isEqualTo(WeightMeasurement.KG)
    }
}
