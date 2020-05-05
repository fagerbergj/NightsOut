package com.fagerberg.jason.notification.constants

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
