package com.fagerberg.jason.common.models

import com.fagerberg.jason.common.utils.getLocal

enum class WeightMeasurement(val displayName: String) {
    LBS("lbs"),
    KG("kg");

    companion object {
        fun fromLowercaseString(string: String) =
            valueOf(string.toUpperCase(getLocal()).replace(' ', '_'))
    }
}
