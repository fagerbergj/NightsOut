package com.fagerberg.jason.profile.repository

import com.fagerberg.jason.common.android.NightsOutActivity
import com.fagerberg.jason.common.android.NightsOutSharedPreferences
import com.fagerberg.jason.common.android.getNightsOutSharedPreferences
import com.fagerberg.jason.common.models.Drink
import com.fagerberg.jason.common.models.WeightMeasurement
import com.wit.jasonfagerberg.nightsout.db.FAVORITES_TABLE
import com.wit.jasonfagerberg.nightsout.db.SimpleDatabaseManager
import io.reactivex.Observable

class ProfileFragmentRepository(private val activity: NightsOutActivity) {

    private lateinit var sharedPreferences: NightsOutSharedPreferences
    private val databaseManager = SimpleDatabaseManager(context = activity)

    fun getSharedPrefs(): Observable<NightsOutSharedPreferences> =
        Observable.just(activity.getNightsOutSharedPreferences()).doOnNext { sharedPreferences = it }

    fun getFavorites() =
        Observable.just(databaseManager.readFavoriteDrinks())

    fun clearFavorites() =
        Observable.just(databaseManager.deleteRowsInTable(tableName = FAVORITES_TABLE))

    fun saveSharedPrefs(
        sex: Boolean,
        weight: Double,
        weightMeasurement: WeightMeasurement
    ): Observable<NightsOutSharedPreferences> =
        Observable.just(
            sharedPreferences.update(
                sex = sex,
                weight = weight,
                weightMeasurement = weightMeasurement
            )
        ).doOnNext { sharedPreferences = it }

    fun removeFavoriteDrink(drink: Drink) = Observable.just(
        databaseManager.deleteRowsInTable(FAVORITES_TABLE, "drink_name=\"${drink.name}\"")
    )

}
