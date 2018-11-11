package com.wit.jasonfagerberg.nightsout.editDB

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import com.wit.jasonfagerberg.nightsout.R

class EditDBFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_db, container, false)
    }
}
