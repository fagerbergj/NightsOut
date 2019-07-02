package com.wit.jasonfagerberg.nightsout.constants

import com.wit.jasonfagerberg.nightsout.utils.Converter
import java.util.*

class Constants {
    companion object {
        const val BACK_STACK = "BACK_STACK"
        const val FRAGMENT_ID = "FRAGMENT_ID"
        const val DB_NAME = "nights_out_db.db"
        const val DB_VERSION = 40
        const val MAX_BACK_STACK_SIZE = 10
        const val DRINK_COUNT_TO_ASK_FOR_RATING = 5
        const val DAYS_UNTIL_ASK_FOR_RATING = 3
        val VOLUME_MEASUREMENTS_METRIC_FIRST = arrayOf("ml", "oz", "beers", "shots", "wine glasses", "pints")

        fun getLongTimeNow(): Long {
            return Calendar.getInstance().timeInMillis
        }

        fun getCurrentTimeInMinuets(): Int {
            val calendar = GregorianCalendar.getInstance()
            val date = Date()
            calendar.time = date
            val curHour = calendar.get(Calendar.HOUR_OF_DAY)
            val curMin = calendar.get(Calendar.MINUTE)
            return Converter().militaryHoursAndMinutesToMinutes(curHour, curMin)
        }
    }

    interface ACTION {
        companion object {
            // generic service actions
            const val START_SERVICE = "com.wit.jasonfagerberg.nightsout.action.START_SERVICE"
            const val STOP_SERVICE = "com.wit.jasonfagerberg.nightsout.action.STOP_SERVICE"
            const val UPDATE_NOTIFICATION = "com.wit.jasonfagerberg.nightsout.action.UPDATE_NOTIFICATION"

            const val REFRESH_BAC = "com.wit.jasonfagerberg.nightsout.action.REFRESH_BAC"
            const val ADD_DRINK = "com.wit.jasonfagerberg.nightsout.action.ADD_DRINK"
        }
    }

    interface CHANNEL {
        companion object {
            const val BAC = "com.wit.jasonfagerberg.nightsout.channel.BAC"
        }
    }

    interface SHARED_PREFERENCE {
        companion object {
            const val PROFILE_INIT = "profileInit"
            const val DATE_INSTALLED = "dateInstalled"
            const val DRINKS_ADDED_COUNT = "drinksAddedCount"
            const val DONT_SHOW_RATE_DIALOG = "dontShowRateDialog"
            const val DONT_SHOW_BAC_NOTIFICATION = "dontShowCurrentBacNotification"
            const val SHOW_BAC_NOTIFICATION = "showCurrentBacNotification"
            const val ACTIVE_THEME = "activeTheme"
            const val PROFILE_SEX = "profileSex"
            const val PROFILE_WEIGHT = "profileWeight"
            const val PROFILE_WEIGHT_MEASUREMENT = "profileWeightMeasurement"
            const val USE_24_HOUR_TIME = "homeUse24HourTime"
            const val START_TIME = "homeStartTimeMin"
            const val END_TIME = "homeEndTimeMin"
        }
    }
}