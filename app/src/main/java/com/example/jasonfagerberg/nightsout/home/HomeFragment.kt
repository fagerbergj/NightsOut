package com.example.jasonfagerberg.nightsout.home

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.jasonfagerberg.nightsout.main.MainActivity
import com.example.jasonfagerberg.nightsout.R
import java.util.*


private const val TAG = "HomeFragment"

class HomeFragment : Fragment(){
    private lateinit var mDrinkListAdapter: HomeFragmentDrinkListAdapter
    private lateinit var mRelativeLayout: RelativeLayout
    private lateinit var mMainActivity: MainActivity

    // create fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // inflate layout
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        mMainActivity = context as MainActivity

        // set layout
        mRelativeLayout = view.findViewById(R.id.layout_home)

        //toolbar setup
        setupToolbar(view)

        // setup list
        setupRecycler(view)

        // show or hide empty text
        showOrHideEmptyListText(view)

        // add a drink button setup
        val btnAdd: MaterialButton = view.findViewById(R.id.btn_home_add_drink)
        btnAdd.setOnClickListener{ _ ->
            val mainActivity: MainActivity = context as MainActivity
            mMainActivity.addDrinkFragment.mFavorited = false
            mainActivity.setFragment(mainActivity.addDrinkFragment)
        }

        // setup bottom nav bar
        mMainActivity.showBottomNavBar(R.id.bottom_nav_home)

        // set edit texts
        setupEditTexts(view)

        // return
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        if(resId == R.id.btn_toolbar_home_done){
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        //Yes is pressed
                        // todo push new log and log drinks
                        val toast = Toast.makeText(activity!!.applicationContext,
                                "Session Logged ", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {/* no action for clicking no */ }
                }
            }
            //Build Actual box
            val builder = AlertDialog.Builder(context!!)
            builder.setMessage("Log Session?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
        }
        return true
    }

    private fun setupToolbar(view: View){
        val toolbar:android.support.v7.widget.Toolbar = view.findViewById(R.id.toolbar_home)
        toolbar.inflateMenu(R.menu.home_menu)
        mMainActivity.setSupportActionBar(toolbar)
        mMainActivity.supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun setupRecycler(view: View){
        // mDrinkList recycler view setup
        val drinksListView:RecyclerView = view.findViewById(R.id.recycler_drink_list)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        drinksListView.layoutManager = linearLayoutManager
        val itemDecor = DividerItemDecoration(drinksListView.context, DividerItemDecoration.VERTICAL)
        drinksListView.addItemDecoration(itemDecor)

        // set adapter
        mDrinkListAdapter = HomeFragmentDrinkListAdapter(context!!, mMainActivity.mDrinksList)
        //update list
        drinksListView.adapter = mDrinkListAdapter //Update display with new list
        drinksListView.layoutManager!!.scrollToPosition(mMainActivity.mDrinksList.size - 1) //Nav to end of list
    }

    private fun setupEditTexts(view: View){
        val startPicker:EditText = view.findViewById(R.id.edit_start_time)
        val endPicker: EditText = view.findViewById(R.id.edit_end_time)

        if(mMainActivity.startTimeMin > -1) startPicker.setText(convertMinutesTo12HourTime(
                mMainActivity.startTimeMin))
        if(mMainActivity.endTimeMin > -1) endPicker.setText(convertMinutesTo12HourTime(
                mMainActivity.endTimeMin))

        startPicker.setOnClickListener{ _ ->
            startTimeEditTextOnCLickListener(startPicker)
        }

        endPicker.setOnClickListener{ _ ->
            endTimeEditTextOnCLickListener(endPicker)
        }
    }

    private fun startTimeEditTextOnCLickListener(startPicker: EditText){
        val currentTime = Calendar.getInstance()
        var hour = currentTime.get(Calendar.HOUR_OF_DAY)
        var minute = currentTime.get(Calendar.MINUTE)
        if(mMainActivity.startTimeMin != -1){
            hour = mMainActivity.startTimeMin/60
            minute = mMainActivity.startTimeMin%60
        }
        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(context!!,
                TimePickerDialog.OnTimeSetListener { _ , selectedHour, selectedMinute ->
                    mMainActivity.startTimeMin = selectedHour*60 + selectedMinute
                    val timePeriod: String
                    var displayHour = selectedHour
                    var displayMinuet = selectedMinute.toString()
                    if(selectedHour >= 12){
                        displayHour -= 12
                        timePeriod = "PM"
                    }else{
                        timePeriod = "AM"
                    }
                    if (displayHour == 0) displayHour = 12
                    if (displayMinuet.length == 1) displayMinuet = "0$displayMinuet"
                    val time = "$displayHour : $displayMinuet $timePeriod"
                    startPicker.setText(time)
                    mMainActivity.startTimeMin = convert24HourTimeToMinutes(selectedHour, selectedMinute)
                    if(mMainActivity.endTimeMin == -1) mMainActivity.endTimeMin = mMainActivity.startTimeMin
                }, hour, minute, false)
        mTimePicker.setTitle("Start Time")
        mTimePicker.show()
    }

    private fun endTimeEditTextOnCLickListener(endPicker: EditText){
        val currentTime = Calendar.getInstance()
        var hour = currentTime.get(Calendar.HOUR_OF_DAY)
        var minute = currentTime.get(Calendar.MINUTE)
        if(mMainActivity.endTimeMin != -1){
            hour = mMainActivity.startTimeMin/60
            minute = mMainActivity.startTimeMin%60
        }
        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(context!!,
                TimePickerDialog.OnTimeSetListener { _ , selectedHour, selectedMinute ->
                    mMainActivity.endTimeMin = selectedHour*60 + selectedMinute
                    val timePeriod: String
                    var displayHour = selectedHour
                    var displayMinuet = selectedMinute.toString()
                    if(selectedHour >= 12){
                        displayHour -= 12
                        timePeriod = "PM"
                    }else{
                        timePeriod = "AM"
                    }
                    if (displayHour == 0) displayHour = 12
                    if (displayMinuet.length == 1) displayMinuet = "0$displayMinuet"
                    val time = "$displayHour : $displayMinuet $timePeriod"
                    endPicker.setText(time)
                    mMainActivity.endTimeMin = convert24HourTimeToMinutes(selectedHour, selectedMinute)
                }, hour, minute, false)
        mTimePicker.setTitle("End Time")
        mTimePicker.show()
    }

    private fun convertMinutesTo12HourTime(min: Int): String{
        var hour = min/60
        val minutes = min%60
        val timePeriod: String
        if(hour >= 12){
            hour -= 12
            timePeriod = "PM"
        }else{
            timePeriod = "AM"
        }
        if (hour == 0) hour = 12
        var displayMinuet = minutes.toString()
        if (displayMinuet.length == 1) displayMinuet = "0$displayMinuet"
        return "$hour:$displayMinuet $timePeriod"
    }

    private fun convert24HourTimeToMinutes(hour: Int, min: Int):Int{
        return hour*60 + min
    }

    fun showOrHideEmptyListText(view: View){
        val emptyText = view.findViewById<TextView>(R.id.text_home_empty_list)
        if(mMainActivity.mDrinksList.isEmpty()){
            emptyText.visibility = View.VISIBLE
        }else{
            emptyText.visibility = View.INVISIBLE
        }
    }
}
