package com.wit.jasonfagerberg.nightsout.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.main.MainActivity

class AddDrinkReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intent = Intent(context, AddDrinkActivity::class.java)
        intent.putExtra("FRAGMENT_ID", (context as MainActivity).pager.currentItem)
        intent.putExtra("BACK_STACK", context.mBackStack.toIntArray())
        context.startActivity(intent)
        context.startActivity(intent)
    }
}