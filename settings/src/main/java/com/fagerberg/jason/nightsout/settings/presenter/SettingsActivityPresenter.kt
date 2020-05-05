package com.fagerberg.jason.nightsout.settings.presenter

import com.fagerberg.jason.common.android.AbstractPresenter
import com.fagerberg.jason.common.android.NightsOutActivity
import com.fagerberg.jason.common.android.NightsOutSharedPreferences
import com.fagerberg.jason.nightsout.settings.R
import com.fagerberg.jason.nightsout.settings.repository.SettingsActivityRepository
import io.reactivex.Observable

class SettingsActivityPresenter :
    AbstractPresenter<SettingsIntent, SettingsAction, SettingsResult, SettingsViewModel>(SettingsViewModel.Empty) {

    private lateinit var repo: SettingsActivityRepository

    override fun intentToAction(intent: SettingsIntent) =
        when(intent) {
            is SettingsIntent.Init -> {
                repo = SettingsActivityRepository(intent.activity)
                SettingsAction.Init(intent.activity)
            }
            is SettingsIntent.ShowNotification -> SettingsAction.ShowNotification(intent.isOn)
            SettingsIntent.ShowNotificationInfo -> SettingsAction.ShowNotificationInfo
            SettingsIntent.ShowNotificationDemo -> SettingsAction.ShowNotificationDemo
            is SettingsIntent.Theme -> SettingsAction.Theme(intent.isDarkTheme)
            is SettingsIntent.Use24HourTime -> SettingsAction.Use24HourTime(intent.isOn)
            SettingsIntent.Use24HourTimeInfo -> SettingsAction.Use24HourTimeInfo
        }

    override fun actionToResult(action: SettingsAction): Observable<SettingsResult> =
        when(action) {
            is SettingsAction.Init ->
                repo.getSharedPrefs().map(SettingsResult::Init)
            is SettingsAction.ShowNotification ->
                repo.updateShowNotification(action.isOn).map { SettingsResult.ShowNotification(it.showBacNotification) }
            SettingsAction.ShowNotificationInfo ->
                Observable.just(SettingsResult.ShowNotificationInfo)
            SettingsAction.ShowNotificationDemo ->
                Observable.just(SettingsResult.ShowNotificationDemo) // todo make this show notification
            is SettingsAction.Theme ->
                repo.updateTheme(action.isDarkTheme).map { SettingsResult.Theme(it.activeTheme == R.style.DarkAppTheme) }
            is SettingsAction.Use24HourTime ->
                repo.updateUse24HourTime(action.isOn).map { SettingsResult.Use24HourTime(it.use24HourTime) }
            SettingsAction.Use24HourTimeInfo ->
                Observable.just(SettingsResult.Use24HourTimeInfo)
        }

    override fun stateReducer(
        previousState: SettingsViewModel,
        result: SettingsResult
    ): SettingsViewModel =
        when(result) {
            is SettingsResult.Init ->
                SettingsViewModel.Init(result.sharedPreferences)
            is SettingsResult.ShowNotification ->
                SettingsViewModel.ShowNotification(result.isOn)
            SettingsResult.ShowNotificationInfo ->
                SettingsViewModel.ShowNotificationInfo
            SettingsResult.ShowNotificationDemo ->
                SettingsViewModel.ShowNotificationDemo
            is SettingsResult.Theme ->
                SettingsViewModel.Theme(result.isDarkTheme)
            is SettingsResult.Use24HourTime ->
                SettingsViewModel.Use24HourTime(result.isOn)
            SettingsResult.Use24HourTimeInfo ->
                SettingsViewModel.Use24HourTimeInfo
        }
}

sealed class SettingsIntent {
    data class Init(val activity: NightsOutActivity) : SettingsIntent()

    // Show notification intents
    data class ShowNotification(val isOn: Boolean) : SettingsIntent()
    object ShowNotificationInfo : SettingsIntent()
    object ShowNotificationDemo : SettingsIntent()

    // theme intents
    data class Theme(val isDarkTheme: Boolean) : SettingsIntent()

    // time intents
    data class Use24HourTime(val isOn: Boolean) : SettingsIntent()
    object Use24HourTimeInfo : SettingsIntent()
}

sealed class SettingsAction {
    data class Init(val activity: NightsOutActivity) : SettingsAction()

    // Show notification actions
    data class ShowNotification(val isOn: Boolean) : SettingsAction()
    object ShowNotificationInfo : SettingsAction()
    object ShowNotificationDemo : SettingsAction()

    // theme actions
    data class Theme(val isDarkTheme: Boolean) : SettingsAction()

    // time actions
    data class Use24HourTime(val isOn: Boolean) : SettingsAction()
    object Use24HourTimeInfo : SettingsAction()
}

sealed class SettingsResult {
    data class Init(val sharedPreferences: NightsOutSharedPreferences) : SettingsResult()

    // Show notification actions
    data class ShowNotification(val isOn: Boolean) : SettingsResult()
    object ShowNotificationInfo : SettingsResult()
    object ShowNotificationDemo : SettingsResult()

    // theme actions
    data class Theme(val isDarkTheme: Boolean) : SettingsResult()

    // time actions
    data class Use24HourTime(val isOn: Boolean) : SettingsResult()
    object Use24HourTimeInfo : SettingsResult()
}

sealed class SettingsViewModel {
    object Empty : SettingsViewModel()
    data class Init(val sharedPreferences: NightsOutSharedPreferences) : SettingsViewModel()

    // Show notification actions
    data class ShowNotification(val isOn: Boolean) : SettingsViewModel()
    object ShowNotificationInfo : SettingsViewModel()
    object ShowNotificationDemo : SettingsViewModel()

    // theme actions
    data class Theme(val isDarkTheme: Boolean) : SettingsViewModel()

    // time actions
    data class Use24HourTime(val isOn: Boolean) : SettingsViewModel()
    object Use24HourTimeInfo : SettingsViewModel()
}
