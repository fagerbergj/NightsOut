package com.wit.jasonfagerberg.nightsout.main

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import com.wit.jasonfagerberg.nightsout.converter.Converter
import java.util.*

class Constants {
    enum class ShowBacNotificationEnum {NEVER, END_TIME_IS_NOW, TIME_IS_CHANGED, BAC_IS_CALCULATED, APP_LAUNCHED}
    companion object {
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

        fun showToast(context: Context, message: String, isLongToast: Boolean = false) {
            val toast = if (isLongToast) Toast.makeText(context, message, Toast.LENGTH_LONG)
            else Toast.makeText(context, message, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 450)
            toast.show()
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
}