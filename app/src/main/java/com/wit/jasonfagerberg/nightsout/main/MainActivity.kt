package com.wit.jasonfagerberg.nightsout.main

// import android.util.Log
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.databaseHelper.DatabaseHelper
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.home.HomeFragment
import com.wit.jasonfagerberg.nightsout.log.LogFragment
import com.wit.jasonfagerberg.nightsout.log.LogHeader
import com.wit.jasonfagerberg.nightsout.notification.BacNotificationService
import com.wit.jasonfagerberg.nightsout.profile.ProfileFragment
import java.util.*
import kotlin.collections.ArrayList

//private const val TAG = "MainActivity"

class MainActivity : NightsOutActivity() {

    // fragments and navigation
    var homeFragment = HomeFragment()
    var logFragment = LogFragment()
    var profileFragment = ProfileFragment()

    private lateinit var botNavBar: BottomNavigationView
    lateinit var pager: ViewPager
    private lateinit var pagerAdapter: MyPagerAdapter
    private var prevMenuItem: MenuItem? = null

    // shared pref data
    private lateinit var preferences: SharedPreferences
    var profileInit = false
    var sex: Boolean? = null
    var weight: Double = 0.0
    var weightMeasurement = ""
    var startTimeMin: Int = Constants.getCurrentTimeInMinuets()
    var endTimeMin: Int = Constants.getCurrentTimeInMinuets()
    private val country = Locale.getDefault().country
    private val twelveHourCountries = arrayListOf("US", "UK", "PH", "CA", "AU", "NZ", "IN", "EG", "SA", "CO", "PK", "MY")
    var use24HourTime = !twelveHourCountries.contains(country)
    private var dateInstalled: Long = 0
    var drinksAddedCount: Int = 0
    private var dontShowRateDialog: Boolean = false
    private var dontShowCurrentBacNotification: Boolean = false
    private var showBacNotification: Boolean = true

    // database entries as lists
    lateinit var mDatabaseHelper: DatabaseHelper

    var mDrinksList: ArrayList<Drink> = ArrayList()
    var mFavoritesList: ArrayList<Drink> = ArrayList()
    var mLogHeaders: ArrayList<LogHeader> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        // bottom nav bar
        botNavBar = findViewById(R.id.bottom_navigation_view)

        botNavBar.setOnNavigationItemSelectedListener { listener ->
            when (listener.itemId) {
                R.id.bottom_nav_home -> { alertUserBeforeNavigation(homeFragment); true }
                R.id.bottom_nav_log -> { alertUserBeforeNavigation(logFragment); true }
                R.id.bottom_nav_profile -> { pager.currentItem = 2; true }
                else -> false
            }
        }
        mDatabaseHelper = DatabaseHelper(this, Constants.DB_NAME, null, Constants.DB_VERSION)

        pager = findViewById(R.id.main_frame)
        pagerAdapter = MyPagerAdapter(supportFragmentManager)
        pager.adapter = pagerAdapter
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                supportActionBar?.title = pagerAdapter.getTitle(position)
                invalidateFragmentMenus(position)
                prevMenuItem?.isChecked = false
                if (prevMenuItem == null)
                    botNavBar.menu.getItem(0).isChecked = false
                botNavBar.menu.getItem(position).isChecked = true
                prevMenuItem = botNavBar.menu.getItem(position)
                pushToBackStack(position)
            }
        })

        val fragmentId = savedInstanceState?.getInt("FRAGMENT_ID")
        if (fragmentId != null ) { pager.currentItem = fragmentId; pushToBackStack(fragmentId) }
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        outState?.putInt("FRAGMENT_ID", pager.currentItem)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onStart() {
        super.onStart()
        mDatabaseHelper.openDatabase()
        initData()
    }

    private fun initData() {
        getDataFromStorage()
        if (intent.getBooleanExtra("drinkAdded", false)) setPreference(drinksAddedCount = drinksAddedCount + 1)
        showPleaseRateDialog()

        val fragmentId = intent.getIntExtra("FRAGMENT_ID", -1)

        if (!profileInit || fragmentId == 2) {
            pager.currentItem = 2
        } else if (fragmentId == 1) {
            pager.currentItem = 1
        }
        pushToBackStack(pager.currentItem)

        // init data
        mDrinksList = mDatabaseHelper.pullCurrentSessionDrinks()
        mFavoritesList = mDatabaseHelper.pullFavoriteDrinks()
        mLogHeaders = mDatabaseHelper.pullLogHeaders()
    }

    private fun getDataFromStorage() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        profileInit = preferences.getBoolean("profileInit", false)
        dateInstalled = preferences.getLong("dateInstalled", System.currentTimeMillis())
        drinksAddedCount = preferences.getInt("drinksAddedCount", 0)
        dontShowRateDialog = preferences.getBoolean("dontShowRateDialog", false)
        dontShowCurrentBacNotification = preferences.getBoolean("dontShowCurrentBacNotification", dontShowCurrentBacNotification)
        showBacNotification = preferences.getBoolean("showCurrentBacNotification", true)

        startTimeMin = Constants.getCurrentTimeInMinuets()
        endTimeMin = Constants.getCurrentTimeInMinuets()
        if (profileInit) {
            sex = true
            sex = preferences.getBoolean("profileSex", sex!!)
            var weightFloat: Float = 0.toFloat()
            weightFloat = preferences.getFloat("profileWeight", weightFloat)
            weight = weightFloat.toDouble()
            weightMeasurement = preferences.getString("profileWeightMeasurement", weightMeasurement)!!

            use24HourTime = preferences.getBoolean("homeUse24HourTime", use24HourTime)
            startTimeMin = preferences.getInt("homeStartTimeMin", startTimeMin)
            endTimeMin = preferences.getInt("homeEndTimeMin", endTimeMin)
        }

        val fragmentArray = intent.getIntArrayExtra("BACK_STACK")
        if (fragmentArray != null) {
            for (frag in fragmentArray) pushToBackStack(frag)
        }

        if (drinksAddedCount > 10000) setPreference(drinksAddedCount = 10)
    }

    private fun invalidateFragmentMenus(position: Int) {
        for (i in 0 until pagerAdapter.count) {
            pagerAdapter.getItem(i).setHasOptionsMenu(i == position)
        }
        invalidateOptionsMenu()
    }

    override fun onStop() {
        mDatabaseHelper.deleteRowsInTable("current_session_drinks", null)
        mDatabaseHelper.pushDrinks(mDrinksList, mFavoritesList)

        mFavoritesList.clear()
        mLogHeaders.clear()
        mDatabaseHelper.closeDatabase()
        super.onStop()
    }

    fun setPreference(profileInit : Boolean = this.profileInit, sex : Boolean? = this.sex,
                      weight : Double = this.weight, weightMeasurement : String = this.weightMeasurement,
                      endTimeMin: Int = this.endTimeMin, startTimeMin : Int = this.startTimeMin,
                      use24HourTime : Boolean = this.use24HourTime, dateInstalled : Long = this.dateInstalled,
                      drinksAddedCount : Int = this.drinksAddedCount, dontShowRateDialog : Boolean = this.dontShowRateDialog) {
        if (!profileInit) return

        // set values
        this.profileInit = profileInit
        this.sex = sex
        this.weight = weight
        this.weightMeasurement = weightMeasurement
        this.startTimeMin = startTimeMin
        this.endTimeMin = endTimeMin
        this.use24HourTime = use24HourTime
        this.dateInstalled = dateInstalled
        this.drinksAddedCount = drinksAddedCount
        this.dontShowRateDialog = dontShowRateDialog

        val editor = preferences.edit()

        editor.putBoolean("profileInit", true)
        if (sex != null) editor.putBoolean("profileSex", sex)

        editor.putFloat("profileWeight", weight.toFloat())
        editor.putString("profileWeightMeasurement", weightMeasurement)

        editor.putInt("homeStartTimeMin", startTimeMin)
        editor.putInt("homeEndTimeMin", endTimeMin)
        editor.putBoolean("homeUse24HourTime", use24HourTime)

        editor.putLong("dateInstalled", dateInstalled)
        editor.putInt("drinksAddedCount", drinksAddedCount)
        editor.putBoolean("dontShowRateDialog", dontShowRateDialog)

        editor.apply()
    }

    fun showPleaseRateDialog(){
        // at least 3 days since install, at least 5 drinks added total, don't show = false
        val lessThanThreeDaysSinceInstall = System.currentTimeMillis() < (dateInstalled + (Constants.DAYS_UNTIL_ASK_FOR_RATING * 24 * 60 * 60 * 1000))
        if (lessThanThreeDaysSinceInstall || dontShowRateDialog || drinksAddedCount < Constants.DRINK_COUNT_TO_ASK_FOR_RATING) return

        val dialog = SimpleDialog(this, layoutInflater)
        dialog.setTitle("Please Rate " + getString(R.string.app_name))
        dialog.setBody("If you are enjoying " + getString(R.string.app_name) + ", please take a moment to rate it. Thank you for your support!")
        dialog.setPositiveButtonText(getString(R.string.rate))
        dialog.setNegativeButtonText(getString(R.string.later))
        dialog.setNeutralButtonText(getString(R.string.dont_show_again))

        dialog.setPositiveFunction {
            try{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            } catch (notFound: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
            }
            dontShowRateDialog = true
            dialog.dismiss()
        }
        dialog.setNegativeFunction {
            // set drinks added count to 0 so the user doesn't get spammed
            setPreference(drinksAddedCount = 0, dateInstalled = System.currentTimeMillis() - 86400000 * 2)
            dialog.dismiss()
        }
        dialog.setNuetralFunction {
            setPreference(dontShowRateDialog = true)
            dialog.dismiss()
        }
    }

    fun resetTime() {
        setPreference(startTimeMin = Constants.getCurrentTimeInMinuets(), endTimeMin = Constants.getCurrentTimeInMinuets())
    }

    fun sendActionToBacNotificationService(action : String){
        if (!showBacNotification) return
        val startIntent = Intent(this, BacNotificationService::class.java)
        startIntent.action = action
        startService(startIntent)
    }

    fun pushToBackStack(i: Int){
        mBackStack.push(i)
        if(mBackStack.size >= Constants.MAX_BACK_STACK_SIZE) {
            mBackStack.removeAt(0)
        }
    }

    override fun onBackPressed() {
        while (mBackStack.isNotEmpty() && mBackStack.peek() == pager.currentItem) mBackStack.pop()
        when {
            mBackStack.isEmpty() -> super.finishAffinity()
            mBackStack.peek() == 4 -> {
                mBackStack.pop()
                val intent = Intent(this, AddDrinkActivity::class.java)
                intent.putExtra("FRAGMENT_ID", pager.currentItem)
                intent.putExtra("BACK_STACK", mBackStack.toIntArray())
                startActivity(intent)
            }
            else -> alertUserBeforeNavigation(null)
        }
    }

    private fun alertUserBeforeNavigation(destination: Fragment?) {
        val destinationInt = when (destination) {
            is HomeFragment -> 0
            is LogFragment -> 1
            is ProfileFragment -> 2
            else -> -1
        }
        if (pager.currentItem == 2 && profileFragment.hasUnsavedData()) {
            val simpleDialog = SimpleDialog(this, layoutInflater)
            simpleDialog.setTitle(resources.getString(R.string.unsaved_profile_changes))
            simpleDialog.setBody(resources.getString(R.string.are_you_sure_you_want_to_abandon_changes))

            simpleDialog.setNegativeFunction {
                botNavBar.selectedItemId = R.id.bottom_nav_profile
                simpleDialog.dismiss()
            }

            simpleDialog.setPositiveFunction {
                simpleDialog.dismiss()
                if (destination == null) {
                    pager.currentItem = mBackStack.pop()
                    mBackStack.pop()
                } else pager.currentItem = destinationInt
            }
        } else if (pager.currentItem != destinationInt) {
            if (destination == null) {
                pager.currentItem = mBackStack.pop()
                mBackStack.pop()
            } else pager.currentItem = destinationInt
        }
    }

    fun showBottomNavBar() {
        botNavBar.visibility = View.VISIBLE

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.ABOVE, R.id.bottom_navigation_view)
        findViewById<CoordinatorLayout>(R.id.placeSnackBar).layoutParams = params
    }

    fun hideBottomNavBar() {
        val botNavBar: BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        botNavBar.visibility = View.INVISIBLE

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        findViewById<CoordinatorLayout>(R.id.placeSnackBar).layoutParams = params
    }

    private inner class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(pos: Int): Fragment {
            return when (pos) {
                1 -> logFragment
                2 -> profileFragment
                else -> homeFragment
            }
        }

        override fun getCount(): Int {
            return 3
        }

        fun getTitle(position: Int): String {
            return when (position) {
                1 -> "Log"
                2 -> "Profile"
                else -> "Home"
            }
        }
    }
}
