package com.wit.jasonfagerberg.nightsout.notification

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.ImageButton
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.main.Constants
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.main.NightsOutActivity
import java.util.*

class SettingActivity : NightsOutActivity() {

    private var showCurrentBacNotification: Boolean = true

    private lateinit var showCurrentBacNotificationBox : CheckBox
    private lateinit var darkThemeBox : CheckBox

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
                val startIntent = Intent(this, BacNotificationService::class.java)
                startIntent.action = Constants.ACTION.START_SERVICE
                startService(startIntent)
                dialog.dismiss()
                showToast("Notification Created")
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
    }

    private fun getNotificationStatus() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        showCurrentBacNotification = pref.getBoolean("showCurrentBacNotification", true)
    }


    private fun setSetting(showCurrentBacNotification : Boolean = true, activeTheme : Int = this.activeTheme) {
        val edit = PreferenceManager.getDefaultSharedPreferences(this).edit()
        edit.putBoolean("showCurrentBacNotification", showCurrentBacNotification)
        edit.putInt("activeTheme", activeTheme)
        edit.apply()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                val mainActivityIntent = Intent(this,MainActivity::class.java)
                this.startActivity(mainActivityIntent)
            }
        }
        return true
    }
}
