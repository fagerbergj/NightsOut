package com.fagerberg.jason.common.utils

<<<<<<< HEAD
=======
import com.fagerberg.jason.common.utils.getCurrentTimeInMinuets
import com.fagerberg.jason.common.utils.getLongTimeNow
>>>>>>> c2164663bf4ca522472eb12d3d857c4dadc756c5
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage.withPercentage
import org.junit.Test
import java.util.Calendar

class TimeUtilsTest {

    @Test
    fun `get current time returns as long returns current time in milliseconds`() {
        assertThat(getLongTimeNow()).isCloseTo(Calendar.getInstance().timeInMillis, withPercentage(0.00001))
    }

    @Test
    fun `get current time in minuets returns correct value`() {
        val calendar = Calendar.getInstance()
        val expected = calendar[Calendar.HOUR] * 60 + calendar[Calendar.MINUTE]
        assertThat(getCurrentTimeInMinuets()).isEqualTo(expected)
    }
}
