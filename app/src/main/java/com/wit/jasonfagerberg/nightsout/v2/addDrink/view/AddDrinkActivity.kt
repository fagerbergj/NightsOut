package com.wit.jasonfagerberg.nightsout.v2.addDrink.view

import android.os.Bundle
import android.util.Log
import com.wit.jasonfagerberg.nightsout.v1.main.NightsOutActivity
import com.wit.jasonfagerberg.nightsout.v2.addDrink.presentor.AddDrinkPresenter
import io.reactivex.disposables.CompositeDisposable

const val TAG = "AddDrinkActivity"

class AddDrinkActivity : NightsOutActivity() {

    private lateinit var presenter: AddDrinkPresenter
    private lateinit var viewManager: AddDrinkViewManager

    private val subscriptions = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = AddDrinkPresenter()
        viewManager = AddDrinkViewManager()

        subscriptions.add(
                presenter.viewModelStream().subscribe(viewManager::render) {
                    Log.e(TAG, "Subscribing to behavior relay failed", it)
                }
        )
    }
}