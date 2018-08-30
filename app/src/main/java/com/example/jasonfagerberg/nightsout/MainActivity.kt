package com.example.jasonfagerberg.nightsout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import java.text.DateFormat.getDateInstance
import java.util.*

class MainActivity : AppCompatActivity() {

    // init fragments
    val homeFragment = HomeFragment.newInstance()
    val logFragment = LogFragment.newInstance()
    val profileFragment = ProfileFragment.newInstance()
    val addDrinkFragment = AddDrinkFragment.newInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragment(homeFragment)

        // bottom nav bar
        val bottomNavigationView: BottomNavigationView? = findViewById(R.id.bottom_navigation_view)

        bottomNavigationView?.setOnNavigationItemSelectedListener { listener ->
            if (listener.itemId == R.id.bottom_nav_home) {
                setFragment(homeFragment)
                true
            }else if (listener.itemId == R.id.bottom_nav_log){
                setFragment(logFragment)
                true
            }else if (listener.itemId == R.id.bottom_nav_profile){
                setFragment(profileFragment)
                true
            }else{
                true
            }
        }
    }

    private fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_frame, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
