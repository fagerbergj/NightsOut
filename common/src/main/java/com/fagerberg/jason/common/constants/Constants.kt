package com.fagerberg.jason.common.constants

import com.fagerberg.jason.common.models.VolumeMeasurement

const val DB_NAME = "nights_out_db.db"
const val DB_VERSION = 40
val VOLUME_MEASUREMENTS = VolumeMeasurement.values().map { it.displayName }
val VOLUME_MEASUREMENTS_METRIC_FIRST = arrayOf(
        VolumeMeasurement.ML.displayName,
        VolumeMeasurement.OZ.displayName,
        VolumeMeasurement.BEERS.displayName,
        VolumeMeasurement.SHOTS.displayName,
        VolumeMeasurement.WINE_GLASSES.displayName,
        VolumeMeasurement.PINTS.displayName
)