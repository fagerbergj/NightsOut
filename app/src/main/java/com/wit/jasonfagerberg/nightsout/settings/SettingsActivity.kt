package com.wit.jasonfagerberg.nightsout.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.ImageButton
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.main.NightsOutActivity
import com.wit.jasonfagerberg.nightsout.notification.BacNotificationService
import com.wit.jasonfagerberg.nightsout.utils.Converter

class SettingsActivity : NightsOutActivity() {

    private var showCurrentBacNotification: Boolean = true
    private var use24HourTime = false
    private var profileInit = false

    private lateinit var showCurrentBacNotificationBox : CheckBox
    private lateinit var darkThemeBox : CheckBox
    private lateinit var use24HourBox : CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getNotificationStatus()
        setContentView(R.layout.activity_setting)
    }

    override fun onStart() {
        setupToolbar()
        setupCheckBoxes()
        super.onStart()
    }

    private fun setupToolbar() {
        supportActionBar?.title = getString(R.string.settings)
        // adds back button to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back_white_24dp)
    }

    private fun setupCheckBoxes() {
        showCurrentBacNotificationBox = findViewById(R.id.checkbox_show_current_bac_notification)
        showCurrentBacNotificationBox.isChecked = showCurrentBacNotification
        showCurrentBacNotificationBox.setOnClickListener {
            setSetting(showCurrentBacNotification = showCurrentBacNotificationBox.isChecked)
        }
        findViewById<ImageButton>(R.id.btn_current_bac_notification_info).setOnClickListener {
            val dialog = SimpleDialog(this, layoutInflater)
            dialog.setTitle("Current BAC Notification")
            dialog.setBody("This notification appears when the user sets either the start or end time to" +
                    " within 5 minuets of the current time. It is meant to allow the user to see their" +
                    " current BAC without having to open Nights Out." )
            dialog.setPositiveButtonText("Show")
            dialog.setPositiveFunction {
                if (!profileInit){
                    showToast("Must create a profile to show the BAC notification")
                } else {
                    val startIntent = Intent(this, BacNotificationService::class.java)
                    startIntent.action = Constants.ACTION.START_SERVICE
                    startService(startIntent)
                    dialog.dismiss()
                    showToast("Notification Created")
                }
            }
            dialog.setNegativeButtonText(getString(R.string.dismiss))
            dialog.setNegativeFunction()
        }

        darkThemeBox = findViewById(R.id.checkbox_dark_mode)
        darkThemeBox.isChecked = this.activeTheme == R.style.DarkAppTheme
        darkThemeBox.setOnClickListener {
            if (this.activeTheme == R.style.DarkAppTheme) {
                this.setSetting(activeTheme = R.style.AppTheme)
            } else {
                this.setSetting(activeTheme = R.style.DarkAppTheme)
            }
            this.finish()
            this.startActivity(this.intent)
        }

        use24HourBox = findViewById(R.id.checkbox_use_24_hour)
        use24HourBox.isChecked = this.use24HourTime
        use24HourBox.setOnClickListener {
            use24HourTime = !use24HourTime
            this.setSetting(use24HourTime = use24HourTime)
            val startIntent = Intent(this, BacNotificationService::class.java)
            startIntent.action = Constants.ACTION.UPDATE_NOTIFICATION
            startService(startIntent)
        }
        findViewById<ImageButton>(R.id.btn_use_24_hour_info).setOnClickListener {
            val dialog = LightSimpleDialog(this)
            dialog.setActions({}, {})
            dialog.show("12 Hour Time:    ${Converter().timeToString(Constants.getCurrentTimeInMinuets(), false)}\n" +
                    "24 Hour Time:     ${Converter().timeToString(Constants.getCurrentTimeInMinuets(), true)}", "Dismiss", "")
        }
    }

    private fun getNotificationStatus() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        showCurrentBacNotification = pref.getBoolean(Constants.PREFERENCE.SHOW_BAC_NOTIFICATION, true)
        use24HourTime = pref.getBoolean(Constants.PREFERENCE.USE_24_HOUR_TIME, false)
        profileInit = pref.getBoolean(Constants.PREFERENCE.PROFILE_INIT, false)
    }


    private fun setSetting(showCurrentBacNotification : Boolean = true, activeTheme : Int = this.activeTheme, use24HourTime : Boolean = this.use24HourTime) {
        val edit = PreferenceManager.getDefaultSharedPreferences(this).edit()
        edit.putBoolean(Constants.PREFERENCE.SHOW_BAC_NOTIFICATION, showCurrentBacNotification)
        edit.putInt(Constants.PREFERENCE.ACTIVE_THEME, activeTheme)
        edit.putBoolean(Constants.PREFERENCE.USE_24_HOUR_TIME, use24HourTime)
        edit.apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }

    override fun onBackPressed() {
        val intent = if (mBackStack.peek() == 4) {
            mBackStack.pop()
            Intent(this, AddDrinkActivity::class.java)
        } else {
            Intent(this,MainActivity::class.java)
        }
        this.startActivity(intent)
    }
}
