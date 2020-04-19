package com.fagerberg.jason.common.android

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fagerberg.jason.common.constants.BACK_STACK
import com.fagerberg.jason.common.constants.FRAGMENT_ID
import com.fagerberg.jason.common.constants.MAX_BACK_STACK_SIZE
import com.google.android.material.button.MaterialButton
import java.util.Stack
import android.util.Log

abstract class NightsOutActivity : AppCompatActivity() {

    private val logTag = this::class.java.name

    val mBackStack = Stack<Int>()
    var fragmentId = -1
    lateinit var sharedPreferences: NightsOutSharedPreferences

    private var mApp: NightsOutApplication? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(logTag, "OnCreate")
        sharedPreferences = getSharedPreferences()
        Log.d(logTag, "Shared Preference Values: $sharedPreferences")
        setTheme(sharedPreferences.activeTheme)
        super.onCreate(savedInstanceState)
        mApp = this.applicationContext as NightsOutApplication
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        Log.d(logTag, "Saving state. FRAGMENT_ID = $fragmentId BACK_STACK = ${mBackStack.toIntArray()}")
        outState.putInt(FRAGMENT_ID, fragmentId)
        outState.putIntArray(BACK_STACK, mBackStack.toIntArray())
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        Log.i(logTag, "onResume")
        super.onResume()
        mApp?.mCurrentActivity = this
        intent.getIntArrayExtra(BACK_STACK)?.let { stack -> stack.forEach { pushToBackStack(it) } }
        fragmentId = intent.getIntExtra(FRAGMENT_ID, -1)
    }

    fun pushToBackStack(i: Int) {
        Log.d(logTag,"Pushing fragment $i to back stack")
        mBackStack.push(i)
        if (mBackStack.size >= MAX_BACK_STACK_SIZE) {
            mBackStack.removeAt(0)
        }
    }

    override fun onPause() {
        Log.i(logTag, "onPause")
        clearReferences()
        super.onPause()
    }

    override fun onDestroy() {
        Log.i(logTag, "onDestroy")
        clearReferences()
        super.onDestroy()
    }

    private fun clearReferences() {
        val currActivity = mApp?.mCurrentActivity
        if (this == currActivity) {
            mApp?.mCurrentActivity = null
        }
    }

    override fun startActivity(newIntent: Intent?) {
        // this happens on notification intents, just pass it along
        if (intent == null) {
            super.startActivity(intent)
            return
        }
        newIntent?.putExtra(BACK_STACK, mBackStack.toIntArray())
        newIntent?.putExtra(FRAGMENT_ID, fragmentId)
        super.startActivity(newIntent)
    }

// TODO Add common code for settings? OR make it activity by activity
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.btn_toolbar_settings) {
//            val intent = Intent(this, SettingsActivity::class.java)
//            startActivity(intent)
//        }
//        return super.onOptionsItemSelected(item)
//    }

    fun setButtonColor(target: MaterialButton, color: Int) {
        target.backgroundTintList = ContextCompat.getColorStateList(this, color)
    }

    fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) =
        Toast.makeText(this, message, length).apply {
            setGravity(Gravity.CENTER, 0, 450)
        }.show()
}
