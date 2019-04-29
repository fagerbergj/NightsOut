package com.wit.jasonfagerberg.nightsout.notification

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.converter.Converter
import com.wit.jasonfagerberg.nightsout.databaseHelper.DatabaseHelper
import com.wit.jasonfagerberg.nightsout.main.Constants
import com.wit.jasonfagerberg.nightsout.main.MainActivity

class BacNotificationService : Service() {
    private var startTime : Int = 0
    private var endTime : Int = 0
    private var weight : Double = 0.0
    private var weightMeasurement : String = "oz"
    private var sex : Boolean = true
    private var use24HourTime : Boolean = false

    private var isStarted: Boolean = false

    private lateinit var notificationHelper : NotificationHelper
    private val mConverter = Converter()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            Constants.ACTION.START_SERVICE -> {
                getPreferencesData()
                // if already started, update notification
                if (isStarted){
                    updateNotification()
                    return START_STICKY
                }

                // create intents for actions
                val refreshIntent = Intent(this, BacNotificationService::class.java)
                refreshIntent.action = Constants.ACTION.REFRESH_BAC
                val pendingRefreshIntent = PendingIntent.getService(this, 0 , refreshIntent, 0)

                val addDrinkIntent = Intent(this, AddDrinkActivity::class.java)
                refreshIntent.action = Constants.ACTION.ADD_DRINK
                val pendingAddDrinkIntent = PendingIntent.getActivity(this, 0, addDrinkIntent, 0)

                // build notification
                notificationHelper = NotificationHelper(this, Constants.CHANNEL.BAC)
                notificationHelper.addAction(R.drawable.image_border, "Refresh", pendingRefreshIntent)
                notificationHelper.addAction(R.drawable.image_border, "Add Drink", pendingAddDrinkIntent)
                val notification = notificationHelper.buildNotification("BAC: ${"%.3f".format(calculateBAC())}",
                        "${mConverter.timeToString(startTime/60, startTime%60, use24HourTime)} - " +
                                mConverter.timeToString(endTime/60, endTime%60, use24HourTime), false)
                startForeground(notificationHelper.id, notification)
                stopForeground(false)
                isStarted = true
            }

            Constants.ACTION.UPDATE_NOTIFICATION -> {
                if (!isStarted) return START_STICKY
                updateNotification()
            }

            Constants.ACTION.REFRESH_BAC -> {
                if (!isStarted) return START_STICKY
                endTime = Constants.getCurrentTimeInMinuets()
                saveEndTime()
                val bac = calculateBAC()
                notificationHelper.loadAndUpdate {
                    Thread.sleep(500)
                    val title = "BAC: ${"%.3f".format(bac)}"
                    val body = "${mConverter.timeToString(startTime/60, startTime%60, use24HourTime)} - " +
                            mConverter.timeToString(endTime/60, endTime%60, use24HourTime)
                    Triple(title, body, false)
                }

                if (applicationContext is MainActivity && (applicationContext as MainActivity).homeFragment.isResumed) {
                    Constants.showToast(this, "End time updated by notification")
                    (applicationContext as MainActivity).homeFragment.updateBACText(bac)
                    (applicationContext as MainActivity).homeFragment.setupEditTexts((applicationContext as MainActivity).homeFragment.view!!)
                }
            }

            Constants.ACTION.ADD_DRINK -> {
                // no additional action required, intent is the action
            }

            Constants.ACTION.STOP_SERVICE -> {
                isStarted = false
            }
        }
        return START_STICKY
    }

    private fun updateNotification() {
        notificationHelper.updateNotification("BAC: ${"%.3f".format(calculateBAC())}",
                "${mConverter.timeToString(startTime/60, startTime%60, use24HourTime)} - " +
                        mConverter.timeToString(endTime/60, endTime%60, use24HourTime), false)
    }

    private fun getPreferencesData() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        startTime = preferences.getInt("homeStartTimeMin", 0)
        endTime = preferences.getInt("homeEndTimeMin", 0)
        use24HourTime = preferences.getBoolean("homeUse24HourTime", false)
        sex = preferences.getBoolean("profileSex", true)
        weight = preferences.getFloat("profileWeight", 0.toFloat()).toDouble()
        weightMeasurement = preferences.getString("profileWeightMeasurement", "oz")!!
    }

    private fun saveEndTime() {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putInt("homeEndTimeMin", endTime)
        editor.apply()
    }

    private fun calculateBAC() : Double{
        val dbh = DatabaseHelper(this, Constants.DB_NAME, null, Constants.DB_VERSION)
        dbh.openDatabase()

        var a = 0.0
        for (drink in dbh.pullCurrentSessionDrinks()) {
            val volume = mConverter.drinkVolumeToFluidOz(drink.amount, drink.measurement)
            val abv = drink.abv / 100
            a += (volume * abv)
        }

        val r = if (sex) .73 else .66

        val weightInLbs = mConverter.weightToLbs(weight, weightMeasurement)

        val sexModifiedWeight = weightInLbs * r

        val instantBAC = (a * 5.14) / sexModifiedWeight

        var hoursElapsed = (endTime - startTime) / 60.0
        if (endTime < startTime) {
            val minInDay = 1440
            hoursElapsed = ((endTime + minInDay) - startTime) / 60.0
        }

        val bacDecayPerHour = 0.015
        var res = instantBAC - (hoursElapsed * bacDecayPerHour)
        res = if (res < 0.0) 0.0 else res
        dbh.closeDatabase()
        return res
    }

    override fun onBind(intent: Intent): IBinder? {
        // Used only in case of bound services.
        return null
    }
}