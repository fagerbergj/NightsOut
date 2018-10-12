package com.example.jasonfagerberg.nightsout.main

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.example.jasonfagerberg.nightsout.R
import com.example.jasonfagerberg.nightsout.addDrink.AddDrinkFragment
import com.example.jasonfagerberg.nightsout.home.HomeFragment
import com.example.jasonfagerberg.nightsout.log.LogFragment
import com.example.jasonfagerberg.nightsout.log.LogHeader
import com.example.jasonfagerberg.nightsout.profile.ProfileFragment
import java.util.*

private const val TAG = "MainActivity"

private const val DB_NAME = "nights_out_db.db"
private const val DB_VERSION = 36

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
            when(listener.itemId){
                R.id.bottom_nav_home -> {
                    alertUserBeforeNavigation(curFrag, homeFragment)
                    true
                }

                R.id.bottom_nav_log -> {
                    alertUserBeforeNavigation(curFrag, logFragment)
                    true
                }
                R.id.bottom_nav_profile -> {
                    if(curFrag !is ProfileFragment) setFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun alertUserBeforeNavigation(curFrag: Fragment, destination: Fragment?){
        if (curFrag == profileFragment && profileFragment.hasUnsavedData()){
            val builder = AlertDialog.Builder(this)
            val parent: ViewGroup? = null
            val dialogView = layoutInflater.inflate(
                    R.layout.activity_main_ensure_abandoing_of_profile_changes_dialog, parent, false)

            builder.setView(dialogView)
            val dialog = builder.create()
            dialog.show()

            dialog.findViewById<Button>(R.id.btn_main_dialog_negative).setOnClickListener {
                dialog.dismiss()
                botNavBar.selectedItemId = R.id.bottom_nav_profile
            }

            dialog.findViewById<Button>(R.id.btn_main_dialog_positive).setOnClickListener {
                dialog.dismiss()
                if (destination == null) supportFragmentManager.popBackStack()
                else setFragment(destination)
            }
        }else if(curFrag != destination){
            if (destination == null) supportFragmentManager.popBackStack()
            else setFragment(destination)
        }
    }

    override fun onResume(){
        initData()
        super.onResume()
    }

    override fun onPause() {
        saveData()
        super.onPause()
    }

    private fun initData(){
        // get data
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        getGlobalData()

        // database
        mDatabaseHelper = DatabaseHelper(this, DB_NAME, null, DB_VERSION)
        mDatabaseHelper.openDatabase()

        if(!profileInt){
            setFragment(profileFragment)
        }else if (supportFragmentManager.backStackEntryCount == 0){
            setFragment(homeFragment)
        }

        // init data
        mDatabaseHelper.pullDrinks()
        mDatabaseHelper.pullLogHeaders()
    }

    private fun saveData(){
        setGlobalData()
        mDatabaseHelper.pushLogHeaders()
        mDatabaseHelper.pushDrinks()
        mDatabaseHelper.closeDatabase()
    }

    private fun setGlobalData(){
        // profile not init
        if(weightMeasurement == "") return

        val editor = preferences.edit()
        editor.putBoolean("profileInit", true)
        if (sex != null) editor.putBoolean("profileSex", sex!!)
        editor.putFloat("profileWeight", weight.toFloat())
        editor.putString("profileWeightMeasurement", weightMeasurement)

        editor.putInt("homeStartTimeMin", startTimeMin)
        editor.putInt("homeEndTimeMin", endTimeMin)
        editor.apply()

        Log.v(TAG, "vars stored: profileInit=$profileInt sex=$sex weight=${weight.toFloat()} " +
                "weight measurement=$weightMeasurement start time=$startTimeMin end time=$endTimeMin")
    }

    private fun getGlobalData(){
        profileInt = preferences.getBoolean("profileInit", profileInt)
        if (profileInt) {
            sex = true
            sex = preferences.getBoolean("profileSex", sex!!)
            var weightFloat: Float = 0.toFloat()
            weightFloat = preferences.getFloat("profileWeight", weightFloat)
            weight = weightFloat.toDouble()
            weightMeasurement = preferences.getString("profileWeightMeasurement", weightMeasurement)!!

            startTimeMin = preferences.getInt("homeStartTimeMin", startTimeMin)
            endTimeMin = preferences.getInt("homeEndTimeMin", endTimeMin)

            Log.v(TAG, "vars retrieved: profileInit=$profileInt sex=$sex weight=$weight " +
                    "weight measurement=$weightMeasurement start time=$startTimeMin end time=$endTimeMin")
        }
    }

    fun setFragment(fragment: Fragment) {
        //transaction.replace(R.id.main_frame, fragment)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_frame, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun showBottomNavBar(id: Int){
        botNavBar.visibility = View.VISIBLE
        botNavBar.selectedItemId = id

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.ABOVE, R.id.bottom_navigation_view)
        findViewById<FrameLayout>(R.id.main_frame).layoutParams = params
    }

    fun hideBottomNavBar(){
        val botNavBar: BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        botNavBar.visibility = View.INVISIBLE

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        findViewById<FrameLayout>(R.id.main_frame).layoutParams = params
    }

    fun getTimeNow():Long{
        return Calendar.getInstance().timeInMillis
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
}
