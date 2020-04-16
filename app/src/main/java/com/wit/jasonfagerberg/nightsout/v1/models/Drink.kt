package com.wit.jasonfagerberg.nightsout.v1.models

import java.util.UUID

data class Drink(
    var id: UUID,
    var name: String,
    var abv: Double = 0.0,
    var amount: Double = 0.0,
    var measurement: String = "",
    var favorited: Boolean = false,
    var recent: Boolean = false,
    var modifiedTime: Long = 0
)