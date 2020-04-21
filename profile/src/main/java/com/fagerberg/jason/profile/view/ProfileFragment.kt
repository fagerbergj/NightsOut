package com.fagerberg.jason.profile.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.fagerberg.jason.profile.R
import com.fagerberg.jason.profile.presenter.ProfileFragmentPresenter
import com.fagerberg.jason.profile.presenter.ProfileIntent
import io.reactivex.disposables.CompositeDisposable

class ProfileFragment : Fragment() {

    private val logTag = this::class.java.name

    private val subscriptions = CompositeDisposable()
    private val presenter: ProfileFragmentPresenter = ProfileFragmentPresenter()
    private lateinit var viewManager: ProfileFragmentViewManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewManager = ProfileFragmentViewManager(this)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.actionBar?.title = "Profile"
        inflater.inflate(R.menu.profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_clear_favorites_list -> {
                val confirmAction = { sendIntent(ProfileIntent.ClearFavorites) }
                // TODO show light simple dialog that confirms choice
                confirmAction.invoke()
            }
            R.id.btn_toolbar_settings -> {
                // TODO create intent to switch to setting activity
            }
        }
        return true
    }

    fun sendIntent(intent: ProfileIntent) = presenter.sendAction(intent)

    override fun onDetach() {
        super.onDetach()
        subscriptions.clear()
    }
}
