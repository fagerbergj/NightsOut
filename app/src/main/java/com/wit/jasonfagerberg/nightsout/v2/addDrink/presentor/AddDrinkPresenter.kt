package com.wit.jasonfagerberg.nightsout.v2.addDrink.presentor

import com.wit.jasonfagerberg.nightsout.v2.BasePresenter
import io.reactivex.Observable

class AddDrinkPresenter : BasePresenter<AddDrinkIntent, AddDrinkAction, AddDrinkResult, AddDrinkViewModel>(AddDrinkViewModel.Initial) {

    override fun intentToAction(intent: AddDrinkIntent): AddDrinkAction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun actionToResult(action: AddDrinkAction): Observable<AddDrinkResult> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stateReducer(previousState: AddDrinkViewModel, result: AddDrinkResult): AddDrinkViewModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

sealed class AddDrinkIntent {
}

sealed class AddDrinkAction {
}

sealed class AddDrinkResult {
}

sealed class AddDrinkViewModel {
    object Initial : AddDrinkViewModel()
}