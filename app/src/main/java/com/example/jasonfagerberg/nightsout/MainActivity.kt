package com.example.jasonfagerberg.nightsout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.util.Log

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    // init fragments
    private val homeFragment = HomeFragment.newInstance()
    private val logFragment = LogFragment.newInstance()
    private val profileFragment = ProfileFragment.newInstance()
    val addDrinkFragment = AddDrinkFragment.newInstance()
    var startTimeMin: Int = 0
    var endTimeMin: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragment(homeFragment)

        // bottom nav bar
        val bottomNavigationView: BottomNavigationView? = findViewById(R.id.bottom_navigation_view)

        bottomNavigationView?.setOnNavigationItemSelectedListener { listener ->
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

    fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        //transaction.replace(R.id.main_frame, fragment)
        transaction.replace(R.id.main_frame, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
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
