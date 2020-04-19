package com.fagerberg.jason.common.models

import java.util.UUID

fun createDrink(
    id: UUID = UUID.randomUUID(),
    name: String = UUID.randomUUID().toString(),
    abv: Double = 5.0,
    amount: Double = 1.0,
    measurement: VolumeMeasurement = VolumeMeasurement.OZ,
    favorited: Boolean = false,
    recent: Boolean = false,
    modifiedTime: Long = 0L,
    dontSuggest: Boolean = false
) = com.fagerberg.jason.common.models.Drink(
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
