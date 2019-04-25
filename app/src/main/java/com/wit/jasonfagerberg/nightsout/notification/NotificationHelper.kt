package com.wit.jasonfagerberg.nightsout.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import java.util.*

class NotificationHelper (private val mContext: Context, private val CHANNEL_ID: String){

//    private val id = UUID.randomUUID().hashCode()
    private val id = CHANNEL_ID.hashCode()
    private val builder : NotificationCompat.Builder

    init {
        Log.v("Main", "notificationId is set to = $id")
        createNotificationChannel()
        builder = initBuilder()
    }

    private fun initBuilder(): NotificationCompat.Builder {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(mContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0)

        return NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
    }

    fun addAction(actionString: String, drawable: Int, message: String, receiver: BroadcastReceiver){
        mContext.registerReceiver(receiver, IntentFilter(actionString))
        val intent = Intent(actionString)
        intent.putExtra("notificationId", id)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.addAction(drawable, message, pendingIntent)
    }

    fun showNotification(title: String, body: String){
        builder.setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(body))

        with(NotificationManagerCompat.from(mContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(id, builder.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BAC"
            val descriptionText = "Tell the user their current BAC"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    (mContext as Activity).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}