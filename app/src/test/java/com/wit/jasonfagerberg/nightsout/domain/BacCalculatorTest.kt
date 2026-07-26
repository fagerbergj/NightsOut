package com.wit.jasonfagerberg.nightsout.domain

import com.wit.jasonfagerberg.nightsout.domain.BacCalculator.Drink
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.Test

class BacCalculatorTest {

    // 12 oz beer at 5% -> 0.6 fl oz pure alcohol; 180 lbs male -> 131.4
    // 0.6 * 5.14 / 131.4 = 0.02347031963...
    @Test
    fun `single drink matches hand-computed Widmark reference value`() {
        val drinks = listOf(Drink(12.0, 5.0))

        assertThat(BacCalculator.calculate(drinks, 180.0, true, 780, 780))
                .isCloseTo(0.0234703196, within(1e-9))
        // female: 180 * 0.66 = 118.8 -> 3.084 / 118.8 = 0.02595959595...
        assertThat(BacCalculator.calculate(drinks, 180.0, false, 780, 780))
                .isCloseTo(0.0259595960, within(1e-9))
    }

    // beer 0.6 + wine 5oz@12% 0.6 + shot 1.5oz@40% 0.6 = 1.8 fl oz
    // 1.8 * 5.14 / 131.4 = 0.07041095890...
    @Test
    fun `multiple drinks accumulate alcohol`() {
        val drinks = listOf(Drink(12.0, 5.0), Drink(5.0, 12.0), Drink(1.5, 40.0))

        assertThat(BacCalculator.calculate(drinks, 180.0, true, 780, 780))
                .isCloseTo(0.0704109589, within(1e-9))
    }

    // 4 beers -> 2.4 fl oz -> instant 0.09388127853...; 2h decay = 0.03
    @Test
    fun `bac decays 0 point 015 per hour elapsed`() {
        val drinks = listOf(Drink(48.0, 5.0))

        assertThat(BacCalculator.calculate(drinks, 180.0, true, 780, 900))
                .isCloseTo(0.0638812785, within(1e-9))
    }

    @Test
    fun `bac clamps to zero when decay exceeds instant bac`() {
        val drinks = listOf(Drink(12.0, 5.0))

        // 0.0234703... - 2 * 0.015 < 0
        assertThat(BacCalculator.calculate(drinks, 180.0, true, 780, 900)).isEqualTo(0.0)
    }

    @Test
    fun `end time before start time wraps past midnight`() {
        val drinks = listOf(Drink(48.0, 5.0))

        assertThat(BacCalculator.hoursElapsed(1380, 60)).isEqualTo(2.0)
        assertThat(BacCalculator.hoursElapsed(60, 1380)).isEqualTo(22.0)
        // 23:00 -> 01:00 is 2h: same as the decay case above
        assertThat(BacCalculator.calculate(drinks, 180.0, true, 1380, 60))
                .isCloseTo(0.0638812785, within(1e-9))
    }

    // original math divides by zero on unset weight; documented, not changed
    @Test
    fun `zero or blank weight divides by zero per original math`() {
        val drinks = listOf(Drink(12.0, 5.0))

        assertThat(BacCalculator.calculate(drinks, 0.0, true, 780, 780))
                .isEqualTo(Double.POSITIVE_INFINITY)
        assertThat(BacCalculator.calculate(emptyList(), 0.0, true, 780, 780).isNaN()).isTrue()
        // blank weight input converts to NaN via Converter.stringToDouble("")
        assertThat(BacCalculator.calculate(drinks, Double.NaN, true, 780, 780).isNaN()).isTrue()
    }
}
