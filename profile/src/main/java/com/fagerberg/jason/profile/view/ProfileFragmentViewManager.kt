package com.fagerberg.jason.profile.view

import androidx.fragment.app.Fragment
import com.fagerberg.jason.profile.presenter.ProfileViewModel

class ProfileFragmentViewManager(val fragment: Fragment) {

    fun render(viewModel: ProfileViewModel) { }
}

val ProfileFragment.viewManager
    get() = ProfileFragmentViewManager(this)
