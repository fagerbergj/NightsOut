package com.fagerberg.jason.common.models

<<<<<<< HEAD
import com.fagerberg.jason.common.utils.getLocal

enum class WeightMeasurement(val displayName: String) {
    LBS("lbs"),
    KG("kg");

    companion object {
        fun fromLowercaseString(string: String) =
            valueOf(string.toUpperCase(getLocal()).replace(' ', '_'))
    }
}
=======
enum class WeightMeasurement(val displayName: String) {
    LBS("lbs"),
    KG("kg")
}
>>>>>>> c2164663bf4ca522472eb12d3d857c4dadc756c5
