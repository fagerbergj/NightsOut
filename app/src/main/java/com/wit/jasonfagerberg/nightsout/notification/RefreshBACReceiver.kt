package com.wit.jasonfagerberg.nightsout.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wit.jasonfagerberg.nightsout.main.MainActivity

class RefreshBACReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val mainActivity = context as MainActivity
        val oldId = intent.getIntExtra("notificationId", -1)
        mainActivity.endTimeMin = mainActivity.getCurrentTimeInMinuets()
        if (mainActivity.homeFragment.isAdded) {
            mainActivity.homeFragment.setupEditTexts(mainActivity.homeFragment.view!!)
            mainActivity.showToast("End time updated")
        }
        mainActivity.showBacNotification(if (oldId != -1) oldId else null)
    }
}