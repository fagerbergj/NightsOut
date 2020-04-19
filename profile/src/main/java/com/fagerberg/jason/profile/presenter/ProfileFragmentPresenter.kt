package com.fagerberg.jason.profile.presenter

import com.fagerberg.jason.common.android.AbstractPresenter
import io.reactivex.Observable

class ProfileFragmentPresenter : AbstractPresenter<ProfileIntent, ProfileAction, ProfileResult, ProfileViewModel>(ProfileViewModel()) {

    override fun intentToAction(intent: ProfileIntent): ProfileAction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun actionToResult(action: ProfileAction): Observable<ProfileResult> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stateReducer(
        previousState: ProfileViewModel,
        result: ProfileResult
    ): ProfileViewModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class ProfileIntent
class ProfileAction
class ProfileResult
class ProfileViewModel
