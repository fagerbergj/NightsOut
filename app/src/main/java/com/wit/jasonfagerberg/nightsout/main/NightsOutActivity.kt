package com.wit.jasonfagerberg.nightsout.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import java.util.*


abstract class NightsOutActivity : AppCompatActivity() {
    val mBackStack = Stack<Int>()

    protected var mMyApp: NightsOutApplication? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMyApp = this.applicationContext as NightsOutApplication
    }

    override fun onResume() {
        super.onResume()
        mMyApp!!.mCurrentActivity = this
    }

    override fun onPause() {
        clearReferences()
        super.onPause()
    }

    override fun onDestroy() {
        clearReferences()
        super.onDestroy()
    }

    private fun clearReferences() {
        val currActivity = mMyApp!!.mCurrentActivity
        if (this == currActivity)
            mMyApp!!.mCurrentActivity = null
    }

    fun showToast(message: String, isLongToast: Boolean = false) {
        val toast = if (isLongToast) Toast.makeText(this, message, Toast.LENGTH_LONG)
        else Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 450)
        toast.show()
    }
}