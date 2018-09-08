package com.example.jasonfagerberg.nightsout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.util.Log
import android.preference.PreferenceManager
import android.content.SharedPreferences
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    // init fragments
    val homeFragment = HomeFragment.newInstance()
    private val logFragment = LogFragment.newInstance()
    private val profileFragment = ProfileFragment.newInstance()
    val addDrinkFragment = AddDrinkFragment.newInstance()
    private lateinit var botNavBar: BottomNavigationView

    // shared pref data
    private lateinit var preferences: SharedPreferences
    var profileInt = false
    var sex: Boolean = true
    var weight: Double = 0.0
    var weightMeasurement: String = ""
    var startTimeMin: Int = -1
    var endTimeMin: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // bottom nav bar
        botNavBar = findViewById(R.id.bottom_navigation_view)

        botNavBar.setOnNavigationItemSelectedListener { listener ->
            val curFrag: Fragment ?= supportFragmentManager.findFragmentById(R.id.main_frame)
            when(listener.itemId){
                R.id.bottom_nav_home -> {
                    if (curFrag !is HomeFragment) setFragment(homeFragment)
                    true
                }
                R.id.bottom_nav_log -> {
                    if(curFrag !is LogFragment) setFragment(logFragment)
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

    override fun onResume() {
        // get data
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        getGlobalData()

        if(!profileInt){
            setFragment(profileFragment)
        }else if (supportFragmentManager.backStackEntryCount == 0){
            setFragment(homeFragment)
        }

        super.onResume()
    }

    override fun onPause() {
        setGlobalData()
        super.onPause()
    }

    private fun setGlobalData(){
        // profile not init
        if(weightMeasurement == "") return

        val editor = preferences.edit()
        editor.putBoolean("profileInit", true)
        editor.putBoolean("profileSex", sex)
        editor.putFloat("profileWeight", weight.toFloat())
        editor.putString("profileWeightMeasurement", weightMeasurement)

        editor.putInt("homeStartTimeMin", startTimeMin)
        editor.putInt("homeEndTimeMin", endTimeMin)
        editor.apply()

        Log.v(TAG, "vars stored: profileInit=$profileInt sex=$sex weight=${weight.toFloat()} " +
                "weight measurment=$weightMeasurement start time=$startTimeMin end time=$endTimeMin")
    }

    private fun getGlobalData(){
        profileInt = preferences.getBoolean("profileInit", profileInt)
        sex = preferences.getBoolean("profileSex", sex)
        var weightFloat: Float = 0.toFloat()
        weightFloat = preferences.getFloat("profileWeight", weightFloat)
        weight = weightFloat.toDouble()
        weightMeasurement = preferences.getString("profileWeightMeasurement", weightMeasurement)!!

        startTimeMin = preferences.getInt("homeStartTimeMin", startTimeMin)
        endTimeMin = preferences.getInt("homeEndTimeMin", endTimeMin)

        Log.v(TAG, "vars retrieved: profileInit=$profileInt sex=$sex weight=$weight " +
                "weight measurment=$weightMeasurement start time=$startTimeMin end time=$endTimeMin")
    }

    fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        //transaction.replace(R.id.main_frame, fragment)
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

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        Log.v(TAG, "count = $count")
        if (count == 1) {
            finish()
            //additional code
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}
