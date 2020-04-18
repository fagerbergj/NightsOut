package com.fagerberg.jason.common.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VolumeMeasurementTest {

    @Test
    fun `from lowercase string factory`() {
        assertThat(VolumeMeasurement.fromLowercaseString("ml")).isEqualTo(VolumeMeasurement.ML)
        assertThat(VolumeMeasurement.fromLowercaseString("wine glasses")).isEqualTo(VolumeMeasurement.WINE_GLASSES)
    }
}
