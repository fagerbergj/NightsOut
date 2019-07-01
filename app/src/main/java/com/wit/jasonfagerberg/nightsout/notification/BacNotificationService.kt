package com.wit.jasonfagerberg.nightsout.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.converter.Converter
import com.wit.jasonfagerberg.nightsout.databaseHelper.DatabaseHelper
import com.wit.jasonfagerberg.nightsout.main.Constants
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.main.NightsOutApplication


class BacNotificationService : Service() {
    private var startTime : Int = 0
    private var endTime : Int = 0
    private var weight : Double = 0.0
    private var weightMeasurement : String = "lbs"
    private var sex : Boolean = true
    private var use24HourTime : Boolean = false

    private var isStarted: Boolean = false

    private lateinit var notificationHelper : NotificationHelper
    private val mConverter = Converter()

    override fun onCreate() {
        // when the service is created / rerun after app closes, build notification to keep intents fresh
        // create intents for actions
        super.onCreate()
        val refreshIntent = Intent(this, BacNotificationService::class.java)
        refreshIntent.action = Constants.ACTION.REFRESH_BAC
        val pendingRefreshIntent = PendingIntent.getService(this, 0 , refreshIntent, 0)

        val addDrinkIntent = Intent(this, AddDrinkActivity::class.java)
        refreshIntent.action = Constants.ACTION.ADD_DRINK
        val pendingAddDrinkIntent = PendingIntent.getActivity(this, 0, addDrinkIntent, 0)

        // build notification
        notificationHelper = NotificationHelper(this, Constants.CHANNEL.BAC)
        notificationHelper.addAction(R.drawable.image_border, getString(R.string.add_drink), pendingAddDrinkIntent)
        notificationHelper.addAction(R.drawable.image_border, getString(R.string.update), pendingRefreshIntent)
        notificationHelper.build("","", false)

        isStarted = isNotificationActive()
    }

    private fun isNotificationActive() : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isBacNotificationStarted", false)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            Constants.ACTION.START_SERVICE -> {
                updateNotification()
                isStarted = true
                saveNotificationState(true)
            }

            Constants.ACTION.UPDATE_NOTIFICATION -> {
                if (!isStarted) return START_STICKY
                updateNotification()
            }

            Constants.ACTION.REFRESH_BAC -> {
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

                val currentActivity = (applicationContext as NightsOutApplication).mCurrentActivity

                if (currentActivity is MainActivity && currentActivity.homeFragment.isResumed) {
                    currentActivity.showToast("End time updated by notification")
                    currentActivity.homeFragment.updateBACText(bac)
                    currentActivity.setPreference(endTimeMin = endTime)
                    currentActivity.homeFragment.setupEditTexts(currentActivity.homeFragment.view!!)
                }
            }

            Constants.ACTION.ADD_DRINK -> {
                // no additional action required, intent is the action
            }

            Constants.ACTION.STOP_SERVICE -> {
                saveNotificationState(false)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(Constants.CHANNEL.BAC.hashCode())
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun updateNotification() {
        getPreferencesData()
        notificationHelper.updateOrShow("BAC: ${"%.3f".format(calculateBAC())}",
                "${mConverter.timeToString(startTime/60, startTime%60, use24HourTime)} - " +
                        mConverter.timeToString(endTime/60, endTime%60, use24HourTime), false)
    }

    private fun getPreferencesData() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        startTime = preferences.getInt(Constants.SHARED_PREFERENCE.START_TIME, 0)
        endTime = preferences.getInt(Constants.SHARED_PREFERENCE.END_TIME, 0)
        use24HourTime = preferences.getBoolean(Constants.SHARED_PREFERENCE.USE_24_HOUR_TIME, false)
        sex = preferences.getBoolean(Constants.SHARED_PREFERENCE.PROFILE_SEX, true)
        weight = preferences.getFloat(Constants.SHARED_PREFERENCE.PROFILE_WEIGHT, 0.toFloat()).toDouble()
        weightMeasurement = preferences.getString(Constants.SHARED_PREFERENCE.PROFILE_WEIGHT_MEASUREMENT, "oz")!!
    }

    private fun saveEndTime() {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putInt(Constants.SHARED_PREFERENCE.END_TIME, endTime)
        editor.apply()
    }

    private fun calculateBAC() : Double{
        val dbh = DatabaseHelper(this, Constants.DB_NAME, null, Constants.DB_VERSION)
        dbh.openDatabase()
        getPreferencesData()

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

    private fun saveNotificationState(started : Boolean) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putBoolean("isBacNotificationStarted", started)
        editor.apply()
    }
}