package com.fagerberg.jason.profile.presenter

import com.fagerberg.jason.common.android.AbstractPresenter
import com.fagerberg.jason.common.android.NightsOutActivity
import com.fagerberg.jason.common.android.NightsOutSharedPreferences
import com.fagerberg.jason.common.models.Drink
import com.fagerberg.jason.common.models.WeightMeasurement
import com.fagerberg.jason.profile.repository.ProfileFragmentRepository
import io.reactivex.Observable

class ProfileFragmentPresenter(
    activity: NightsOutActivity,
    private val repo: ProfileFragmentRepository = ProfileFragmentRepository(activity)
) :
    AbstractPresenter<ProfileIntent, ProfileAction, ProfileResult, ProfileViewModel>(
        initialViewModel = ProfileViewModel.Empty
    ) {

    override fun intentToAction(intent: ProfileIntent): ProfileAction =
        when (intent) {
            is ProfileIntent.Init -> ProfileAction.Init(intent.activity)
            is ProfileIntent.InitFavorites -> ProfileAction.InitFavorites(intent.activity)
            is ProfileIntent.SelectSex -> ProfileAction.SelectSex(intent.sex)
            is ProfileIntent.Save -> ProfileAction.Save(
                sex = intent.sex,
                weight = intent.weight,
                weightMeasurement = intent.weightMeasurement
            )
            ProfileIntent.Settings -> ProfileAction.Settings
            ProfileIntent.ClearFavorites -> ProfileAction.ClearFavorites
        }

    override fun actionToResult(action: ProfileAction): Observable<ProfileResult> =
        when (action) {
            is ProfileAction.Init -> repo.getSharedPrefs().map<ProfileResult>(ProfileResult::Init)
            is ProfileAction.InitFavorites -> repo.getFavorites().map<ProfileResult>(ProfileResult::InitFavorites)
            is ProfileAction.SelectSex -> Observable.just(ProfileResult.SelectSex(action.sex))
            is ProfileAction.Save -> repo.saveSharedPrefs(
                sex = action.sex,
                weight = action.weight,
                weightMeasurement = action.weightMeasurement
            ).map<ProfileResult>(ProfileResult::Save)
            is ProfileAction.Settings -> Observable.just(ProfileResult.Settings)
            is ProfileAction.ClearFavorites -> repo.clearFavorites().map { ProfileResult.ClearFavorites }
        }

    override fun stateReducer(
        previousState: ProfileViewModel,
        result: ProfileResult
    ): ProfileViewModel =
        when(result) {
            is ProfileResult.Init -> ProfileViewModel.Init(result.sharedPreferences)
            is ProfileResult.InitFavorites -> ProfileViewModel.InitFavorites(result.favorites)
            is ProfileResult.SelectSex -> ProfileViewModel.SelectSex(result.sex)
            is ProfileResult.Save -> ProfileViewModel.Save(result.sharedPreferences)
            is ProfileResult.Settings -> ProfileViewModel.Settings
            is ProfileResult.ClearFavorites -> ProfileViewModel.ClearFavorites
        }
}

sealed class ProfileIntent {
    data class Init(val activity: NightsOutActivity) : ProfileIntent()
    data class InitFavorites(val activity: NightsOutActivity) : ProfileIntent()
    // button presses
    data class SelectSex(val sex: Boolean) : ProfileIntent()

    data class Save(
        val activity: NightsOutActivity,
        val sex: Boolean,
        val weight: Double, val weightMeasurement: WeightMeasurement
    ) : ProfileIntent()

    // options selected
    object Settings : ProfileIntent()

    object ClearFavorites : ProfileIntent()
}

sealed class ProfileAction {
    data class Init(val activity: NightsOutActivity) : ProfileAction()
    data class InitFavorites(val activity: NightsOutActivity) : ProfileAction()
    // button presses
    data class SelectSex(val sex: Boolean) : ProfileAction()

    data class Save(
        val sex: Boolean,
        val weight: Double,
        val weightMeasurement: WeightMeasurement
    ) : ProfileAction()

    // options selected
    object Settings : ProfileAction()

    object ClearFavorites : ProfileAction()
}

sealed class ProfileResult {
    data class Init(val sharedPreferences: NightsOutSharedPreferences) : ProfileResult()
    data class InitFavorites(val favorites: List<Drink>) : ProfileResult()
    // button presses
    data class SelectSex(val sex: Boolean) : ProfileResult()

    data class Save(val sharedPreferences: NightsOutSharedPreferences) : ProfileResult()
    // options selected
    object Settings : ProfileResult()

    object ClearFavorites : ProfileResult()
}

sealed class ProfileViewModel {
    object Empty : ProfileViewModel()
    data class Init(val sharedPreferences: NightsOutSharedPreferences) : ProfileViewModel()
    data class InitFavorites(val favorites: List<Drink>) : ProfileViewModel()
    // button presses
    data class SelectSex(val sex: Boolean) : ProfileViewModel()
    data class Save(val sharedPreferences: NightsOutSharedPreferences) : ProfileViewModel()
    // options selected
    object Settings : ProfileViewModel()

    object ClearFavorites : ProfileViewModel()
}
