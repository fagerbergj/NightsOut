package com.fagerberg.jason.profile.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fagerberg.jason.common.android.NightsOutActivity
import com.fagerberg.jason.profile.R
import com.fagerberg.jason.profile.presenter.ProfileFragmentPresenter
import com.fagerberg.jason.profile.repository.ProfileFragmentRepository
import io.reactivex.disposables.CompositeDisposable

class ProfileFragment : Fragment() {

    private val logTag = this::class.java.name

    private val subscriptions = CompositeDisposable()
    private lateinit var presenter: ProfileFragmentPresenter
    private lateinit var viewManager: ProfileFragmentViewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = ProfileFragmentPresenter(activity as NightsOutActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewManager = ProfileFragmentViewManager(activity = activity as NightsOutActivity)
        subscriptions.add(
            presenter.viewModelStream().subscribe(viewManager::render) {
                Log.e(logTag, "Failed to setup presenter subscription", it)
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        subscriptions.clear()
    }
}
