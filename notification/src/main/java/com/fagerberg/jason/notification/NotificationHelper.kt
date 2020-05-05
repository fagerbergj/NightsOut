package com.fagerberg.jason.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fagerberg.jason.common.android.NightsOutActivity
import com.fagerberg.jason.notification.bac.R
import com.fagerberg.jason.notification.bac.service.BacNotificationService
import com.fagerberg.jason.notification.constants.ACTION

class NotificationHelper (
    private val mContext: Context,
    private val CHANNEL_ID: String
){

    private val id = CHANNEL_ID.hashCode()
    private val builder : NotificationCompat.Builder

    init {
        createNotificationChannel()
        builder = initBuilder()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BAC"
            val descriptionText = "Tell the user their current BAC"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    (mContext as Service).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initBuilder(): NotificationCompat.Builder {
        // Create an explicit intent for an Activity in your app
        // TODO unsure if this actually works with the abstract class
        val intent = Intent(mContext, NightsOutActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(mContext, id, intent, 0)

        val deleteIntent = Intent(mContext, BacNotificationService::class.java)
        deleteIntent.action = ACTION.STOP_SERVICE
        val deletePendingIntent = PendingIntent.getService(mContext, id, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        return NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
    }

    fun addAction(drawable: Int, message: String, pendingIntent: PendingIntent){
        builder.addAction(drawable, message, pendingIntent)
    }

    fun build(title: String, body: String, autoCancel: Boolean = true) : Notification{
        return builder.setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setAutoCancel(autoCancel)
                .build()
    }

    fun updateOrShow(title: String, body: String, autoCancel: Boolean = true) {
        builder.setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setAutoCancel(autoCancel)

        with(NotificationManagerCompat.from(mContext)) {
            notify(id, builder.build())
        }
    }

    fun loadAndUpdate(title: String = "Calculating...", body: String = "", function: () -> Triple<String, String, Boolean>) {
        builder.setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setProgress(0,0, true)

        with(NotificationManagerCompat.from(mContext)) {
            notify(id, builder.build())
        }

        val result = function()
        builder.setProgress(0,0,false)
        updateOrShow(result.first, result.second, result.third)
    }
}
