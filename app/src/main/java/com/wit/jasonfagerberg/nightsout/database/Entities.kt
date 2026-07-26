package com.wit.jasonfagerberg.nightsout.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Non-PK columns stay nullable to match the pre-Room DDL, which declares no NOT NULL.
// favorites, current_session_drinks and log_drink are intentionally not entities: they
// have no viable primary key (duplicate rows are legal in all three) and Room requires one.

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val abv: Double?,
    val amount: Double?,
    val measurement: String?,
    val recent: Int?,
    val modifiedTime: Long?,
    val dontSuggest: Int?
)

@Entity(tableName = "log")
data class LogEntity(
    @PrimaryKey val date: Int,
    val bac: Double?,
    val duration: Double?
)
