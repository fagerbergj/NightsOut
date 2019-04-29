package com.wit.jasonfagerberg.nightsout.main

import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.widget.Toast
import java.util.Stack

abstract class NightsOutActivity : AppCompatActivity() {
    val mBackStack = Stack<Int>()

    fun showToast(message: String, isLongToast: Boolean = false) {
        val toast = if (isLongToast) Toast.makeText(this, message, Toast.LENGTH_LONG)
        else Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 450)
        toast.show()
    }
}
