package com.fagerberg.jason.profile.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fagerberg.jason.profile.R
import com.fagerberg.jason.profile.presenter.ProfileFragmentPresenter
import io.reactivex.disposables.CompositeDisposable

class ProfileFragment : Fragment() {

    private val logTag = this::class.java.name

    private val subscriptions = CompositeDisposable()
    private val presenter = ProfileFragmentPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscriptions.add(
            presenter.viewModelStream().subscribe(viewManager::render) {
                Log.e(logTag, "Failed to setup presenter subscription", it)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }
}
