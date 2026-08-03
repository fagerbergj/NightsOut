package com.wit.jasonfagerberg.nightsout.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.utils.Converter
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.domain.BacCalculator
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.main.NightsOutApplication
import com.wit.jasonfagerberg.nightsout.settings.SettingsShim
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject


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
    private val repository: NightsOutRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        // when the service is created / rerun after app closes, build notification to keep intents fresh
        // create intents for actions
        super.onCreate()
        val refreshIntent = Intent(this, BacNotificationService::class.java)
        refreshIntent.action = Constants.ACTION.REFRESH_BAC
        val pendingRefreshIntent = PendingIntent.getService(this, 0, refreshIntent, PendingIntent.FLAG_IMMUTABLE)

        val addDrinkIntent = Intent(this, AddDrinkActivity::class.java)
        refreshIntent.action = Constants.ACTION.ADD_DRINK
        val pendingAddDrinkIntent = PendingIntent.getActivity(this, 0, addDrinkIntent, PendingIntent.FLAG_IMMUTABLE)

        // build notification
        notificationHelper = NotificationHelper(this, Constants.CHANNEL.BAC)
        notificationHelper.addAction(R.drawable.image_border, getString(R.string.add_drink), pendingAddDrinkIntent)
        notificationHelper.addAction(R.drawable.image_border, getString(R.string.update), pendingRefreshIntent)
        notificationHelper.build("","", false)

        isStarted = isNotificationActive()
    }

    @Suppress("DEPRECATION") // shim phase; #54 replaces with SettingsRepository
    private fun isNotificationActive() : Boolean {
        return SettingsShim(this).getBoolean(Constants.PREFERENCE.IS_BAC_NOTIFICATION_STARTED, false)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            Constants.ACTION.START_SERVICE -> {
                updateNotification()
                isStarted = true
                saveNotificationState(true)
            }

            Constants.ACTION.UPDATE_NOTIFICATION -> {
                if (!isStarted) return START_REDELIVER_INTENT
                updateNotification()
            }

            Constants.ACTION.REFRESH_BAC -> {
                endTime = Constants.getCurrentTimeInMinuets()
                saveEndTime()
                serviceScope.launch {
                    val bac = calculateBAC()
                    withContext(Dispatchers.Main) {
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
                            currentActivity.setPreference(endTimeMin = endTime)
                            currentActivity.homeFragment.refreshBacFromTimeChange()
                        }
                    }
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
        return START_REDELIVER_INTENT
    }

    private fun updateNotification() {
        getPreferencesData()
        serviceScope.launch {
            val bac = calculateBAC()
            withContext(Dispatchers.Main) {
                notificationHelper.updateOrShow("BAC: ${"%.3f".format(bac)}",
                        "${mConverter.timeToString(startTime/60, startTime%60, use24HourTime)} - " +
                                mConverter.timeToString(endTime/60, endTime%60, use24HourTime), false)
            }
        }
    }

    @Suppress("DEPRECATION") // shim phase; #54 replaces with SettingsRepository
    private fun getPreferencesData() {
        val settings = SettingsShim(this)
        startTime = settings.getInt(Constants.PREFERENCE.START_TIME, 0)
        endTime = settings.getInt(Constants.PREFERENCE.END_TIME, 0)
        use24HourTime = settings.getBoolean(Constants.PREFERENCE.USE_24_HOUR_TIME, false)
        sex = settings.getBoolean(Constants.PREFERENCE.PROFILE_SEX, true)
        weight = settings.getFloat(Constants.PREFERENCE.PROFILE_WEIGHT, 0.toFloat()).toDouble()
        weightMeasurement = settings.getString(Constants.PREFERENCE.PROFILE_WEIGHT_MEASUREMENT, "oz")!!
    }

    @Suppress("DEPRECATION") // shim phase; #54 replaces with SettingsRepository
    private fun saveEndTime() {
        val editor = SettingsShim(this).edit()
        editor.putInt(Constants.PREFERENCE.END_TIME, endTime)
        editor.apply()
    }

    private suspend fun calculateBAC() : Double{
        getPreferencesData()

        val drinks = repository.pullCurrentSessionDrinks().map {
            BacCalculator.Drink(mConverter.drinkVolumeToFluidOz(it.amount, it.measurement), it.abv)
        }

        val weightInLbs = mConverter.weightToLbs(weight, weightMeasurement)
        return BacCalculator.calculate(drinks, weightInLbs, sex, startTime, endTime)
    }

    override fun onBind(intent: Intent): IBinder? {
        // Used only in case of bound services.
        return null
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    @Suppress("DEPRECATION") // shim phase; #54 replaces with SettingsRepository
    private fun saveNotificationState(started : Boolean) {
        val editor = SettingsShim(this).edit()
        editor.putBoolean(Constants.PREFERENCE.IS_BAC_NOTIFICATION_STARTED, started)
        editor.apply()
    }
}