package com.wit.jasonfagerberg.nightsout.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.wit.jasonfagerberg.nightsout.settings.SettingsScreen
import com.wit.jasonfagerberg.nightsout.ui.theme.NightsOutTheme
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    /** Regression #62 guard — verifies settings toggles fire callbacks (proving recomposition works). */
    @Test fun `toggling BAC notification fires callback`() {
        var called = false
        composeTestRule.setContent {
            NightsOutTheme(darkMode = false) {
                SettingsScreen(
                    showBac = true, isDarkMode = false, use24h = false,
                    onToggleBac = { _ -> called = true },
                    onToggleDarkMode = {}, onToggle24h = {}, onProfileInitCheck = { true }
                )
            }
        }
        composeTestRule.onNodeWithText("BAC", substring = true).performClick()
        assert(called) { "onToggleBac callback must fire" }
    }

    @Test fun `toggling dark mode fires callback`() {
        var called = false
        composeTestRule.setContent {
            NightsOutTheme(darkMode = false) {
                SettingsScreen(
                    showBac = true, isDarkMode = false, use24h = false,
                    onToggleBac = {},
                    onToggleDarkMode = { _ -> called = true },
                    onToggle24h = {}, onProfileInitCheck = { true }
                )
            }
        }
        composeTestRule.onNodeWithText("Dark Mode").performClick()
        assert(called) { "onToggleDarkMode callback must fire" }
    }

    @Test fun `toggling 24-hour time fires callback`() {
        var called = false
        composeTestRule.setContent {
            NightsOutTheme(darkMode = true) {
                SettingsScreen(
                    showBac = false, isDarkMode = false, use24h = false,
                    onToggleBac = {}, onToggleDarkMode = {},
                    onToggle24h = { _ -> called = true },
                    onProfileInitCheck = { true }
                )
            }
        }
        composeTestRule.onNodeWithText("24 Hour").performClick()
        assert(called) { "onToggle24h callback must fire" }
    }
}
