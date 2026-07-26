package com.wit.jasonfagerberg.nightsout.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.SkipQueryVerification

@Dao
interface DrinkDao {

    // drinks table

    @Query("SELECT * FROM drinks WHERE id = :id")
    suspend fun getDrinkById(id: String): DrinkEntity?

    @Query("SELECT COUNT(*) FROM drinks WHERE id = :id")
    suspend fun countDrinkById(id: String): Int

    @Query("SELECT id FROM drinks WHERE name = :name AND abv = :abv AND amount = :amount AND measurement = :measurement LIMIT 1")
    suspend fun findDrinkId(name: String, abv: Double, amount: Double, measurement: String): String?

    @Query("SELECT * FROM drinks WHERE recent = 1 ORDER BY modifiedTime ASC")
    suspend fun getRecentDrinks(): List<DrinkEntity>

    @Query("SELECT * FROM drinks WHERE name LIKE '%' || :filter || '%' ORDER BY name, modifiedTime DESC")
    suspend fun getSuggestedDrinks(filter: String): List<DrinkEntity>

    @Insert
    suspend fun insertDrink(drink: DrinkEntity)

    @Query("UPDATE drinks SET name = :name, abv = :abv, amount = :amount, measurement = :measurement, recent = :recent WHERE id = :id")
    suspend fun updateDrink(id: String, name: String, abv: Double, amount: Double, measurement: String, recent: Int)

    @Query("UPDATE drinks SET recent = :recent WHERE name = :name")
    suspend fun setRecentByName(name: String, recent: Int)

    @Query("UPDATE drinks SET recent = :recent WHERE id = :id")
    suspend fun setRecentById(id: String, recent: Int)

    @Query("UPDATE drinks SET modifiedTime = :modifiedTime WHERE id = :id")
    suspend fun updateModifiedTime(id: String, modifiedTime: Long)

    @Query("UPDATE drinks SET dontSuggest = :dontSuggest WHERE id = :id")
    suspend fun updateSuggestionStatus(id: String, dontSuggest: Int)

    @Query("SELECT dontSuggest FROM drinks WHERE id = :id")
    suspend fun getSuggestionStatus(id: String): Int?

    @Query("DELETE FROM drinks WHERE recent = 1")
    suspend fun deleteRecentDrinks()

    @Query("DELETE FROM drinks WHERE id = :id")
    suspend fun deleteDrinkById(id: String)

    // favorites table (not an entity, has no primary key)

    @SkipQueryVerification
    @Query("SELECT COUNT(*) FROM favorites WHERE drink_name = :name")
    suspend fun countFavoritesByName(name: String): Int

    @SkipQueryVerification
    @Query("SELECT drinks.* FROM drinks, favorites WHERE drinks.id = favorites.origin_id ORDER BY modifiedTime ASC")
    suspend fun getFavoriteDrinks(): List<DrinkEntity>

    @SkipQueryVerification
    @Query("SELECT drink_name FROM favorites")
    suspend fun getFavoriteNames(): List<String>

    @SkipQueryVerification
    @Query("INSERT INTO favorites VALUES (:name, :originId)")
    suspend fun insertFavorite(name: String, originId: String)

    @SkipQueryVerification
    @Query("DELETE FROM favorites WHERE drink_name = :name")
    suspend fun deleteFavoriteByName(name: String)

    @SkipQueryVerification
    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()

    // current_session_drinks table (not an entity, has no primary key)

    @SkipQueryVerification
    @Query("SELECT drinks.* FROM drinks, current_session_drinks WHERE drinks.id = current_session_drinks.drink_id ORDER BY current_session_drinks.position ASC")
    suspend fun getCurrentSessionDrinks(): List<DrinkEntity>

    @SkipQueryVerification
    @Query("INSERT INTO current_session_drinks VALUES (:drinkId, :position)")
    suspend fun insertCurrentSession(drinkId: String, position: Int)

    @SkipQueryVerification
    @Query("DELETE FROM current_session_drinks")
    suspend fun clearCurrentSession()

    @SkipQueryVerification
    @Query("DELETE FROM current_session_drinks WHERE position = :position")
    suspend fun deleteCurrentSessionAt(position: Int)

    @SkipQueryVerification
    @Query("DELETE FROM current_session_drinks WHERE drink_id = :drinkId")
    suspend fun deleteCurrentSessionByDrinkId(drinkId: String)

    @SkipQueryVerification
    @Query("SELECT COUNT(*) FROM current_session_drinks")
    suspend fun currentSessionCount(): Int
}
