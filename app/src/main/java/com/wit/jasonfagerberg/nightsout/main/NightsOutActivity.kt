package com.wit.jasonfagerberg.nightsout.main

import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.preference.PreferenceManager
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.settings.SettingsActivity
import java.util.*

abstract class NightsOutActivity : AppCompatActivity() {
    val mBackStack = Stack<Int>()
    var fragmentId = -1
    var activeTheme: Int = R.style.AppTheme

    private var mApp: NightsOutApplication? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        activeTheme = PreferenceManager.getDefaultSharedPreferences(this).getInt("activeTheme", activeTheme)
        setTheme(activeTheme)
        super.onCreate(savedInstanceState)
        mApp = this.applicationContext as NightsOutApplication
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putInt(Constants.FRAGMENT_ID, fragmentId)
        outState.putIntArray(Constants.BACK_STACK, mBackStack.toIntArray())
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        super.onResume()
        mApp!!.mCurrentActivity = this
        val backStackArray = intent.getIntArrayExtra(Constants.BACK_STACK)
        if (backStackArray != null) {
            for (entry in backStackArray) {
                pushToBackStack(entry)
            }
        }
        fragmentId = intent.getIntExtra(Constants.FRAGMENT_ID, -1)
    }

    fun pushToBackStack(i: Int){
        mBackStack.push(i)
        if(mBackStack.size >= Constants.MAX_BACK_STACK_SIZE) {
            mBackStack.removeAt(0)
        }
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
        val currActivity = mApp!!.mCurrentActivity
        if (this == currActivity)
            mApp!!.mCurrentActivity = null
    }

    override fun startActivity(newIntent: Intent?) {
        // this happens on notification intents, just pass it along
        if (intent == null) {
            super.startActivity(intent)
            return
        }
        newIntent?.putExtra(Constants.BACK_STACK, mBackStack.toIntArray())
        newIntent?.putExtra(Constants.FRAGMENT_ID, fragmentId)
        super.startActivity(newIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.btn_toolbar_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    fun setButtonColor(target: MaterialButton, color : Int) {
        target.backgroundTintList = ContextCompat.getColorStateList(this, color)
    }

    fun showToast(message: String, isLongToast: Boolean = false) {
        val toast = if (isLongToast) Toast.makeText(this, message, Toast.LENGTH_LONG)
        else Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 450)
        toast.show()
    }
}
