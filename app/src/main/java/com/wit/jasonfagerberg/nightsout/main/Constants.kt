package com.wit.jasonfagerberg.nightsout.main

import java.util.*

class Constants {
    companion object {
        const val DB_NAME = "nights_out_db.db"
        const val DB_VERSION = 40
        const val MAX_BACK_STACK_SIZE = 10
        const val DRINK_COUNT_TO_ASK_FOR_RATING = 5
        const val DAYS_UNTIL_ASK_FOR_RATING = 3
        const val NOTIFICATION_BAC_CHANNEL = "com.wit.jasonfagerberg.nightsout.BAC"
        const val ACTION_REFRESH_BAC = "com.wit.jasonfagerberg.nightsout.REFRESH_BAC"
        const val ACTION_ADD_DRINK = "com.wit.jasonfagerberg.nightsout.ADD_DRINK"
        val VOLUME_MEASUREMENTS_METRIC_FIRST = arrayOf("ml", "oz", "beers", "shots", "wine glasses", "pints")

        fun getLongTimeNow(): Long {
            return Calendar.getInstance().timeInMillis
        }
    }
}