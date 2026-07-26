package com.wit.jasonfagerberg.nightsout.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConverterTest {

    private val converter = Converter()

    @Test
    fun `convert weights to lbs`() {
        assertThat(converter.weightToLbs(1.0, "lbs")).isEqualTo(1.0)
        assertThat(converter.weightToLbs(5.0, "kg")).isEqualTo(11.025)
        assertThat(converter.weightToLbs(20.0, "kg")).isEqualTo(44.1)
    }

    @Test
    fun `convert volume to fluid oz`() {
        assertThat(converter.drinkVolumeToFluidOz(1.0, "oz")).isEqualTo(1.0)
        assertThat(converter.drinkVolumeToFluidOz(5.0, "oz")).isEqualTo(5.0)
        assertThat(converter.drinkVolumeToFluidOz(20.0, "ml")).isEqualTo(0.67628)
        assertThat(converter.drinkVolumeToFluidOz(12.0, "beers")).isEqualTo(144.0)
        assertThat(converter.drinkVolumeToFluidOz(6.0, "shots")).isEqualTo(9.0)
        assertThat(converter.drinkVolumeToFluidOz(17.0, "wine glasses")).isEqualTo(85.0)
        assertThat(converter.drinkVolumeToFluidOz(8.0, "pints")).isEqualTo(128.0)
    }

    @Test
    fun `convert fl oz to grams of alcohol`() {
        assertThat(converter.fluidOzToGrams(1.0)).isEqualTo(23.3333333)
        assertThat(converter.fluidOzToGrams(6.0)).isEqualTo(139.9999998)
    }

    @Test
    fun `convert military hours and min to raw minuets`() {
        assertThat(converter.militaryHoursAndMinutesToMinutes(1, 0)).isEqualTo(60)
        assertThat(converter.militaryHoursAndMinutesToMinutes(0, 15)).isEqualTo(15)
        assertThat(converter.militaryHoursAndMinutesToMinutes(14, 10)).isEqualTo(850)
    }

    @Test
    fun `convert decimal time to hours and minuets`() {
        assertThat(converter.decimalTimeToHoursAndMinuets(1.0)).isEqualTo(Pair(1, 0))
        assertThat(converter.decimalTimeToHoursAndMinuets(0.25)).isEqualTo(Pair(0, 15))
        assertThat(converter.decimalTimeToHoursAndMinuets(6.75)).isEqualTo(Pair(6, 45))
    }
}
