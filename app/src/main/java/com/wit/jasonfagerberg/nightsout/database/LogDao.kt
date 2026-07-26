package com.wit.jasonfagerberg.nightsout.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.SkipQueryVerification

@Dao
interface LogDao {

    // log table

    @Query("SELECT * FROM `log`")
    suspend fun getLogEntries(): List<LogEntity>

    @Query("INSERT INTO `log` VALUES (:date, :bac, :duration)")
    suspend fun insertLog(date: Int, bac: Double, duration: Double)

    @Query("UPDATE `log` SET date = :newDate WHERE date = :oldDate")
    suspend fun updateLogDate(oldDate: Int, newDate: Int)

    @Query("DELETE FROM `log` WHERE date = :date")
    suspend fun deleteLog(date: Int)

    // log_drink table (not an entity, has no primary key)

    @SkipQueryVerification
    @Query("SELECT drink_id FROM log_drink WHERE log_date = :date")
    suspend fun getLoggedDrinkIds(date: Int): List<String>

    @SkipQueryVerification
    @Query("SELECT COUNT(*) FROM log_drink WHERE drink_id = :drinkId")
    suspend fun countLogDrinksById(drinkId: String): Int

    @SkipQueryVerification
    @Query("INSERT INTO log_drink VALUES (:date, :drinkId)")
    suspend fun insertLogDrink(date: Int, drinkId: String)

    @SkipQueryVerification
    @Query("UPDATE log_drink SET log_date = :newDate WHERE log_date = :oldDate")
    suspend fun updateLogDrinkDate(oldDate: Int, newDate: Int)

    @SkipQueryVerification
    @Query("DELETE FROM log_drink WHERE log_date = :date")
    suspend fun deleteLogDrinks(date: Int)
}
