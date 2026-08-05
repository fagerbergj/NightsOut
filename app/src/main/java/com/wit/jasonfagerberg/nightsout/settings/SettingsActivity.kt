package com.wit.jasonfagerberg.nightsout.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.main.NightsOutActivity
import com.wit.jasonfagerberg.nightsout.ui.theme.NightsOutTheme
import com.wit.jasonfagerberg.nightsout.notification.BacNotificationService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import androidx.lifecycle.lifecycleScope

class SettingsActivity : NightsOutActivity() {

    private val repository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.let { actionBar ->
            actionBar.title = getString(R.string.settings)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_white_24dp)
        }

        val viewModel: SettingsViewModel by lazy {
            val factory = object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(application, repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
            ViewModelProvider(this, factory)[SettingsViewModel::class.java]
        }

        setContentView(ComposeView(this).apply {
            setContent {
                val darkMode by viewModel.showDarkMode.collectAsStateWithLifecycle()
                NightsOutTheme(darkMode = darkMode, dynamicColor = true) {
                    SettingsScreen(
                        showBac = viewModel.showBacNotification.value,
                        isDarkMode = darkMode,
                        use24h = viewModel.use24HourTime.value,
                        onToggleBac = { enabled ->
                            lifecycleScope.launch {
                                repository.setShowBacNotification(enabled)
                                val isInit = repository.profileInit.first()
                                if (enabled && isInit) {
                                    requestNotificationPermissionIfNeeded()
                                    val startIntent = Intent(this@SettingsActivity, BacNotificationService::class.java).apply {
                                        action = Constants.ACTION.START_SERVICE
                                    }
                                    startService(startIntent)
                                } else if (!enabled) {
                                    val stopIntent = Intent(application, BacNotificationService::class.java).apply {
                                        action = Constants.ACTION.UPDATE_NOTIFICATION
                                    }
                                    stopService(stopIntent)
                                }
                            }
                        },
                        onToggleDarkMode = viewModel::toggleDarkTheme,
                        onToggle24h = { enabled ->
                            lifecycleScope.launch {
                                repository.setUse24HourTime(enabled)
                                val intent = Intent(application, BacNotificationService::class.java).apply {
                                    action = Constants.ACTION.UPDATE_NOTIFICATION
                                }
                                startService(intent)
                            }
                        },
                        onProfileInitCheck = { false }
                    )
                }
            }
        })
    }

    override fun onBackPressed() {
        val intent = if (mBackStack.peek() == 4) {
            mBackStack.pop()
            Intent(this, AddDrinkActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
    }
}
