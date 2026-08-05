package com.wit.jasonfagerberg.nightsout.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wit.jasonfagerberg.nightsout.settings.SettingsScreen
import com.wit.jasonfagerberg.nightsout.ui.theme.NightsOutTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Regression guard for #62: the settings toggles must reach their callbacks. */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    private companion object {
        const val BAC = 0
        const val DARK_MODE = 1
        const val USE_24H = 2
    }

    @get:Rule val composeTestRule = createComposeRule()

    // Only the Switch is clickable - the row label is not - and the labels are
    // not siblings of it once semantics merge, so select by position. The count
    // assertion fails loudly if a row is ever added or removed.
    private fun switchAt(index: Int) =
        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(3)[index]

    private fun setContent(
        onToggleBac: (Boolean) -> Unit = {},
        onToggleDarkMode: (Boolean) -> Unit = {},
        onToggle24h: (Boolean) -> Unit = {},
    ) = composeTestRule.setContent {
        NightsOutTheme(darkMode = false) {
            SettingsScreen(
                showBac = true, isDarkMode = false, use24h = false,
                onToggleBac = onToggleBac,
                onToggleDarkMode = onToggleDarkMode,
                onToggle24h = onToggle24h,
                onProfileInitCheck = { true },
            )
        }
    }

    @Test fun togglingBacNotificationFiresCallback() {
        var called = false
        setContent(onToggleBac = { called = true })
        switchAt(BAC).performScrollTo().performClick()
        assert(called) { "onToggleBac must fire when the BAC switch is toggled" }
    }

    @Test fun togglingDarkModeFiresCallback() {
        var called = false
        setContent(onToggleDarkMode = { called = true })
        switchAt(DARK_MODE).performScrollTo().performClick()
        assert(called) { "onToggleDarkMode must fire when the dark mode switch is toggled" }
    }

    @Test fun toggling24HourTimeFiresCallback() {
        var called = false
        setContent(onToggle24h = { called = true })
        switchAt(USE_24H).performScrollTo().performClick()
        assert(called) { "onToggle24h must fire when the 24 hour switch is toggled" }
    }
}
