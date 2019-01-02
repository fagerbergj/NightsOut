package com.wit.jasonfagerberg.nightsout.main

import java.util.*

class Constants {
    companion object {
        const val DB_NAME = "nights_out_db.db"
        const val DB_VERSION = 40

        fun getLongTimeNow(): Long {
            return Calendar.getInstance().timeInMillis
        }
    }
}