package com.fagerberg.jason.common.android

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import androidx.lifecycle.ViewModel as AndroidViewModel

abstract class AbstractPresenter <Intent, Action, Result, ViewModel>(
    initialViewModel: ViewModel
) : AndroidViewModel() {

    private val disposables = CompositeDisposable()
    private val uiEventHandler = PublishRelay.create<Intent>().toSerialized()

    // By making this a behavior relay, view gets latest state whenever it subscribes
    private val viewModelStream: BehaviorRelay<ViewModel> =
        BehaviorRelay.createDefault(initialViewModel)

    init {
        disposables.add(
            uiEventHandler.observeOn(Schedulers.io())
                .map(::intentToAction)
                // Background Work
                .flatMap(::actionToResult)
                // State Reducer
                .scan(initialViewModel, ::stateReducer)
                .subscribe(viewModelStream)
        )
    }

    // Functions view cares about
    fun sendAction(intent: Intent) = uiEventHandler.accept(intent)

    fun viewModelStream(): Observable<ViewModel> =
        viewModelStream.observeOn(AndroidSchedulers.mainThread())

    // internal presenter functions
    abstract fun intentToAction(intent: Intent): Action

    abstract fun actionToResult(action: Action): Observable<Result>
    abstract fun stateReducer(previousState: ViewModel, result: Result): ViewModel

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
