package com.wit.jasonfagerberg.nightsout.main

import java.util.*

class Constants {
    companion object {
        const val DB_NAME = "nights_out_db.db"
        const val DB_VERSION = 40
        const val MAX_BACK_STACK_SIZE = 10
        const val LAUNCH_COUNT_TO_ASK_FOR_RATING = 5
        const val DAYS_UNTIL_ASK_FOR_RATING = 3
        val VOLUME_MEASUREMENTS_METRIC_FIRST = arrayOf("ml", "oz", "beers", "shots", "wine glasses", "pints")

        fun getLongTimeNow(): Long {
            return Calendar.getInstance().timeInMillis
        }
    }
}