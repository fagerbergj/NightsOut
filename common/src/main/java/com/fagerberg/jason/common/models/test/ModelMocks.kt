package com.fagerberg.jason.common.models.test

import com.fagerberg.jason.common.models.Drink
import com.fagerberg.jason.common.models.LogHeader
import com.fagerberg.jason.common.models.VolumeMeasurement
import java.util.Calendar
import java.util.UUID

fun createDrink(
    id: UUID = UUID.randomUUID(),
    name: String = UUID.randomUUID().toString(),
    abv: Double = 5.0,
    amount: Double = 1.0,
    measurement: VolumeMeasurement = VolumeMeasurement.OZ,
    favorited: Boolean = false,
    recent: Boolean = false,
    modifiedTime: Long = Calendar.getInstance().timeInMillis,
    dontSuggest: Boolean = false
) = Drink (
    id = id,
        name = name,
        abv = abv,
        amount = amount,
        measurement = measurement,
        favorited = favorited,
        recent = recent,
        modifiedTime = modifiedTime,
        dontSuggest = dontSuggest
)

fun createLogHeader(
    date: Int = 20191231,
    bac: Double = 0.080,
    duration: Double = 1.5
) = LogHeader(
    date = date,
    bac = bac,
    duration = duration
)
