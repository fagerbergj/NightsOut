package com.fagerberg.jason.common.models

<<<<<<< HEAD
import com.fagerberg.jason.common.utils.getLocal

=======
>>>>>>> c2164663bf4ca522472eb12d3d857c4dadc756c5
enum class VolumeMeasurement(val displayName: String) {
    OZ("oz"),
    ML("ml"),
    BEERS("beers"),
    WINE_GLASSES("wine glasses"),
    SHOTS("shots"),
<<<<<<< HEAD
    PINTS("pints");

    companion object {
        fun fromLowercaseString(string: String) =
            valueOf(string.toUpperCase(getLocal()).replace(' ', '_'))
    }
}
=======
    PINTS("pints")
}
>>>>>>> c2164663bf4ca522472eb12d3d857c4dadc756c5
