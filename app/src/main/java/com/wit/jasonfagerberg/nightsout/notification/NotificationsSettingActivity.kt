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
import com.wit.jasonfagerberg.nightsout.main.NightsOutActivity

class NotificationsSettingActivity : NightsOutActivity() {

    private var showCurrentBacNotification: Boolean = true

    private lateinit var showCurrentBacNotificationBox : CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getNotificationStatus()
        setContentView(R.layout.activity_notifications_setting)
    }

    override fun onStart() {
        setupToolbar()
        setupCheckBoxes()
        super.onStart()
    }

    private fun setupToolbar() {
        supportActionBar?.title = getString(R.string.notification_settings)
        // adds back button to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back_white_24dp)
    }

    private fun setupCheckBoxes() {
        showCurrentBacNotificationBox = findViewById(R.id.checkbox_show_current_bac_notification)
        showCurrentBacNotificationBox.isChecked = showCurrentBacNotification
        showCurrentBacNotificationBox.setOnClickListener {
            setNotificationStatus(showCurrentBacNotification = showCurrentBacNotificationBox.isChecked)
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
    }

    private fun getNotificationStatus() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        showCurrentBacNotification = pref.getBoolean("showCurrentBacNotification", true)
    }


    private fun setNotificationStatus(showCurrentBacNotification : Boolean = true) {
        val edit = PreferenceManager.getDefaultSharedPreferences(this).edit()
        edit.putBoolean("showCurrentBacNotification", showCurrentBacNotification)
        edit.apply()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }
}
