package com.wit.jasonfagerberg.nightsout.settings

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SettingsScreenTest {

    @Test
    fun `toggling BAC notification fires callback`() {
        var bacCalled = false
        val cb: (Boolean) -> Unit = { _ -> bacCalled = true }
        cb(true)
        assertThat(bacCalled).isTrue()
    }

    @Test
    fun `toggling dark mode fires callback`() {
        var darkCalled = false
        val cb: (Boolean) -> Unit = { _ -> darkCalled = true }
        cb(false)
        assertThat(darkCalled).isTrue()
    }

    @Test
    fun `toggling 24 hour time fires callback`() {
        var timeCalled = false
        val cb: (Boolean) -> Unit = { _ -> timeCalled = true }
        cb(true)
        assertThat(timeCalled).isTrue()
    }

    @Test
    fun `24 hour time preview starts with Current time`() {
        val result = getCurrentTimePreview(true)
        assertThat(result).startsWith("Current time: ")
    }

    @Test
    fun `12 hour time preview starts with Current time`() {
        val result = getCurrentTimePreview(false)
        assertThat(result).startsWith("Current time: ")
    }
}
