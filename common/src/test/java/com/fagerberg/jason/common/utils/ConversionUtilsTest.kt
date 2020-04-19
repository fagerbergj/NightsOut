package com.fagerberg.jason.common.utils

import com.fagerberg.jason.common.models.VolumeMeasurement
import com.fagerberg.jason.common.models.WeightMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConversionUtilsTest {

    @Test
    fun `convert weights to lbs`() {
        assertThat(WeightMeasurement.LBS.toLbs(1.0)).isEqualTo(1.0)
        assertThat(WeightMeasurement.KG.toLbs(5.0)).isEqualTo(11.025)
        assertThat(WeightMeasurement.KG.toLbs(20.0)).isEqualTo(44.1)
    }

    @Test
    fun `convert volume to fluid oz`() {
        assertThat(VolumeMeasurement.OZ.toFluidOz(1.0)).isEqualTo(1.0)
        assertThat(VolumeMeasurement.OZ.toFluidOz(5.0)).isEqualTo(5.0)
        assertThat(VolumeMeasurement.ML.toFluidOz(20.0)).isEqualTo(0.67628)
        assertThat(VolumeMeasurement.BEERS.toFluidOz(12.0)).isEqualTo(144.0)
        assertThat(VolumeMeasurement.SHOTS.toFluidOz(6.0)).isEqualTo(9.0)
        assertThat(VolumeMeasurement.WINE_GLASSES.toFluidOz(17.0)).isEqualTo(85.0)
        assertThat(VolumeMeasurement.PINTS.toFluidOz(8.0)).isEqualTo(128.0)
    }

    @Test
    fun `convert fl oz to grams of water`() {
        assertThat(fluidOzToGramsOfAlcohol(1.0)).isEqualTo(23.3333333)
        assertThat(fluidOzToGramsOfAlcohol(6.0)).isEqualTo(139.9999998)
    }

    @Test
    fun `convert military hours and min to raw minuets`() {
        assertThat(militaryHoursAndMinutesToMinutes(1, 0)).isEqualTo(60)
        assertThat(militaryHoursAndMinutesToMinutes(0, 15)).isEqualTo(15)
        assertThat(militaryHoursAndMinutesToMinutes(14, 10)).isEqualTo(850)
    }

    @Test
    fun `convert decimal time to hours and minuets`() {
        assertThat(decimalTimeToHoursAndMinuets(1.0)).isEqualTo(Pair(1, 0))
        assertThat(decimalTimeToHoursAndMinuets(0.25)).isEqualTo(Pair(0, 15))
        assertThat(decimalTimeToHoursAndMinuets(6.75)).isEqualTo(Pair(6, 45))
    }
}
