package com.wit.jasonfagerberg.nightsout.notification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.converter.Converter
import com.wit.jasonfagerberg.nightsout.main.Constants

class NotificationsSettingActivity : AppCompatActivity() {

    private lateinit var mSpinnerShowBacNotification: Spinner
    private var showBacNotification = Constants.ShowBacNotificationEnum.END_TIME_IS_NOW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_setting)
        setupSpinner()
    }

    private fun setupSpinner() {
        mSpinnerShowBacNotification = findViewById(R.id.spinner_show_bac_notification)
        mSpinnerShowBacNotification.setSelection(1)
        val items = Converter().showBacNotificationMap.keys.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        mSpinnerShowBacNotification.adapter = adapter
    }
}
