package com.fagerberg.jason.common

import com.fagerberg.jason.common.models.VolumeMeasurement
import com.fagerberg.jason.common.models.WeightMeasurement
import com.fagerberg.jason.common.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConversionTest {

    @Test
    fun `convert weights to lbs`() {
        assertThat(weightToLbs(1.0, WeightMeasurement.LBS)).isEqualTo(1.0)
        assertThat(weightToLbs(5.0, WeightMeasurement.LBS)).isEqualTo(5.0)
        assertThat(weightToLbs(20.0, WeightMeasurement.KG)).isEqualTo(44.1)
    }

    @Test
    fun `convert volume to fluid oz`() {
        assertThat(volumeToFluidOz(1.0, VolumeMeasurement.OZ)).isEqualTo(1.0)
        assertThat(volumeToFluidOz(5.0, VolumeMeasurement.OZ)).isEqualTo(5.0)
        assertThat(volumeToFluidOz(20.0, VolumeMeasurement.ML)).isEqualTo(0.67628)
        assertThat(volumeToFluidOz(12.0, VolumeMeasurement.BEERS)).isEqualTo(144.0)
        assertThat(volumeToFluidOz(6.0, VolumeMeasurement.SHOTS)).isEqualTo(9.0)
        assertThat(volumeToFluidOz(17.0, VolumeMeasurement.WINE_GLASSES)).isEqualTo(85.0)
        assertThat(volumeToFluidOz(8.0, VolumeMeasurement.PINTS)).isEqualTo(128.0)
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
