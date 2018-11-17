package com.wit.jasonfagerberg.nightsout.main

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
//import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkFragment
import com.wit.jasonfagerberg.nightsout.converter.Converter
import com.wit.jasonfagerberg.nightsout.databaseHelper.DatabaseHelper
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.home.HomeFragment
import com.wit.jasonfagerberg.nightsout.log.LogFragment
import com.wit.jasonfagerberg.nightsout.log.LogHeader
import com.wit.jasonfagerberg.nightsout.profile.ProfileFragment
import java.util.*

//private const val TAG = "MainActivity"

private const val DB_NAME = "nights_out_db.db"
private const val DB_VERSION = 39

class MainActivity : AppCompatActivity() {

    // init fragments
    val homeFragment = HomeFragment()
    private val logFragment = LogFragment()
    private val profileFragment = ProfileFragment()
    val addDrinkFragment = AddDrinkFragment()
    private lateinit var botNavBar: BottomNavigationView

    // shared pref data
    private lateinit var preferences: SharedPreferences
    var profileInt = false
    var sex: Boolean? = null
    var weight: Double = 0.0
    var weightMeasurement = ""
    var startTimeMin: Int = -1
    var endTimeMin: Int = -1
    private val country = Locale.getDefault().country
    private val twelveHourCountries = arrayListOf("US", "UK", "PH", "CA", "AU", "NZ", "IN", "EG", "SA", "CO", "PK", "MY")
    var use24HourTime = !twelveHourCountries.contains(country)
    var showRemoveSuggestionDialog = true

    // global lists
    var mDrinksList: ArrayList<Drink> = ArrayList()
    var mRecentsList: ArrayList<Drink> = ArrayList()
    var mFavoritesList: ArrayList<Drink> = ArrayList()
    var mLogHeaders: ArrayList<LogHeader> = ArrayList()

    lateinit var mDatabaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // bottom nav bar
        botNavBar = findViewById(R.id.bottom_navigation_view)

        botNavBar.setOnNavigationItemSelectedListener { listener ->
            val curFrag: Fragment = supportFragmentManager.findFragmentById(R.id.main_frame)!!
            when (listener.itemId) {
                R.id.bottom_nav_home -> {
                    alertUserBeforeNavigation(curFrag, homeFragment)
                    true
                }

                R.id.bottom_nav_log -> {
                    alertUserBeforeNavigation(curFrag, logFragment)
                    true
                }
                R.id.bottom_nav_profile -> {
                    if (curFrag !is ProfileFragment) setFragment(profileFragment)
                    true
                }
                else -> false
            }
        }

        // database
        mDatabaseHelper = DatabaseHelper(this, DB_NAME, null, DB_VERSION)
        mDatabaseHelper.openDatabase()
    }

    override fun onStart() {
        initData()
        super.onStart()
    }

    override fun onStop() {
        saveData()
        super.onStop()
    }

    override fun onDestroy() {
        mDatabaseHelper.closeDatabase()
        super.onDestroy()
    }

    private fun initData() {
        // get data
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        getProfileAndTimeData()

        if (!profileInt) {
            setFragment(profileFragment)
        } else if (supportFragmentManager.backStackEntryCount == 0) {
            setFragment(homeFragment)
        }

        // init data
        mDatabaseHelper.pullDrinks()
        mDatabaseHelper.pullLogHeaders()
    }

    private fun saveData() {
        setProfileAndTimeData()
        mDatabaseHelper.pushDrinks()
        mDatabaseHelper.pushLogHeaders()
    }

    private fun setProfileAndTimeData() {
        // profile not init
        if (weightMeasurement == "") return

        val editor = preferences.edit()
        editor.putBoolean("profileInit", true)
        if (sex != null) editor.putBoolean("profileSex", sex!!)
        editor.putFloat("profileWeight", weight.toFloat())
        editor.putString("profileWeightMeasurement", weightMeasurement)

        editor.putInt("homeStartTimeMin", startTimeMin)
        editor.putInt("homeEndTimeMin", endTimeMin)
        editor.putBoolean("homeUse24HourTime", use24HourTime)

        editor.putBoolean("showRemoveSuggestionDialog", showRemoveSuggestionDialog)
        editor.apply()
    }

    private fun getProfileAndTimeData() {
        profileInt = preferences.getBoolean("profileInit", profileInt)
        startTimeMin = getCurrentTimeInMinuets()
        endTimeMin = getCurrentTimeInMinuets()
        if (profileInt) {
            sex = true
            preferences.getBoolean("profileSex", sex!!)
            var weightFloat: Float = 0.toFloat()
            weightFloat = preferences.getFloat("profileWeight", weightFloat)
            weight = weightFloat.toDouble()
            weightMeasurement = preferences.getString("profileWeightMeasurement", weightMeasurement)!!

            use24HourTime = preferences.getBoolean("homeUse24HourTime", use24HourTime)
            startTimeMin = preferences.getInt("homeStartTimeMin", startTimeMin)
            endTimeMin = preferences.getInt("homeEndTimeMin", endTimeMin)

            showRemoveSuggestionDialog = preferences.getBoolean("showRemoveSuggestionDialog", showRemoveSuggestionDialog)
        }
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        val curFrag: Fragment = supportFragmentManager.findFragmentById(R.id.main_frame)!!

        if (count == 1) {
            finish()
        } else {
            alertUserBeforeNavigation(curFrag, null)
        }
    }

    private fun alertUserBeforeNavigation(curFrag: Fragment, destination: Fragment?) {
        if (curFrag == profileFragment && profileFragment.hasUnsavedData()) {
            val simpleDialog = SimpleDialog(this, layoutInflater)
            simpleDialog.setTitle(resources.getString(R.string.unsaved_profile_changes))
            simpleDialog.setBody(resources.getString(R.string.are_you_sure_you_want_to_abandon_changes))

            simpleDialog.setNegativeFunction {
                simpleDialog.dismiss()
                botNavBar.selectedItemId = R.id.bottom_nav_profile
            }

            simpleDialog.setPositiveFunction {
                simpleDialog.dismiss()
                if (destination == null) supportFragmentManager.popBackStack()
                else setFragment(destination)
            }

        } else if (curFrag != destination) {
            if (destination == null) supportFragmentManager.popBackStack()
            else setFragment(destination)
        }
    }

    fun showBottomNavBar(id: Int) {
        botNavBar.visibility = View.VISIBLE
        botNavBar.selectedItemId = id

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.ABOVE, R.id.bottom_navigation_view)
        findViewById<FrameLayout>(R.id.main_frame).layoutParams = params
    }

    fun hideBottomNavBar() {
        val botNavBar: BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        botNavBar.visibility = View.INVISIBLE

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        findViewById<FrameLayout>(R.id.main_frame).layoutParams = params
    }

    fun setFragment(fragment: Fragment) {
        //transaction.replace(R.id.main_frame, fragment)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_frame, fragment)
        transaction.addToBackStack(null)
        transaction.commitAllowingStateLoss()
    }

    fun getCurrentTimeInMinuets(): Int {
        val calendar = GregorianCalendar.getInstance()
        val date = Date()
        calendar.time = date
        val curHour = calendar.get(Calendar.HOUR_OF_DAY)
        val curMin = calendar.get(Calendar.MINUTE)
        return Converter().militaryHoursAndMinutesToMinutes(curHour, curMin)
    }

    fun getLongTimeNow(): Long {
        return Calendar.getInstance().timeInMillis
    }

    fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 450)
        toast.show()
    }
}
