package com.example.jasonfagerberg.nightsout.home

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.*
import com.example.jasonfagerberg.nightsout.main.MainActivity
import com.example.jasonfagerberg.nightsout.R
import com.example.jasonfagerberg.nightsout.main.Converter
import java.util.*
import android.widget.RelativeLayout






private const val TAG = "HomeFragment"

class HomeFragment : Fragment(){
    private lateinit var mDrinkListAdapter: HomeFragmentDrinkListAdapter
    private lateinit var mRelativeLayout: RelativeLayout
    private lateinit var mMainActivity: MainActivity
    private val mConverter = Converter()
    var bac = 0.000

    private var drinkingDuration = 0.0
    private var gramsOfAlcoholConsumed = 0.0

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

        view.findViewById<ImageButton>(R.id.btn_home_bac_info).setOnClickListener{ _ -> showBacInfoDialog()}

        // return
        return view
    }

    override fun onResume() {
        calculateBAC()
        super.onResume()
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

        // set adapter
        mDrinkListAdapter = HomeFragmentDrinkListAdapter(context!!, mMainActivity.mDrinksList)
        //update list
        drinksListView.adapter = mDrinkListAdapter //Update display with new list
        drinksListView.layoutManager!!.scrollToPosition(mMainActivity.mDrinksList.size - 1) //Nav to end of list
    }

    private fun setupEditTexts(view: View){
        val startPicker:EditText = view.findViewById(R.id.edit_start_time)
        val endPicker: EditText = view.findViewById(R.id.edit_end_time)

        if(mMainActivity.startTimeMin > -1) startPicker.setText(mConverter.convertMinutesTo12HourTime(
                mMainActivity.startTimeMin))
        if(mMainActivity.endTimeMin > -1) endPicker.setText(mConverter.convertMinutesTo12HourTime(
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
                    startPicker.setText(mConverter.convertSelectedTimeToString(selectedHour, selectedMinute))
                    mMainActivity.startTimeMin = mConverter.convert24HourTimeToMinutes(selectedHour, selectedMinute)
                    if(mMainActivity.endTimeMin == -1) mMainActivity.endTimeMin = mMainActivity.startTimeMin
                    calculateBAC()
                }, hour, minute, false)

        mTimePicker.setButton(DialogInterface.BUTTON_NEUTRAL, "Now") { _, _ ->
            mMainActivity.startTimeMin = getCurrentTimeInMinuetsAndSetEditText(startPicker)
            calculateBAC()
        }

        mTimePicker.setTitle("Start Time")
        mTimePicker.show()
    }

    private fun endTimeEditTextOnCLickListener(endPicker: EditText){
        val currentTime = Calendar.getInstance()
        var hour = currentTime.get(Calendar.HOUR_OF_DAY)
        var minute = currentTime.get(Calendar.MINUTE)
        if(mMainActivity.endTimeMin != -1){
            hour = mMainActivity.endTimeMin/60
            minute = mMainActivity.endTimeMin%60
        }

        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(context!!,
                TimePickerDialog.OnTimeSetListener { _ , selectedHour, selectedMinute ->
                    endPicker.setText(mConverter.convertSelectedTimeToString(selectedHour, selectedMinute))
                    mMainActivity.endTimeMin = mConverter.convert24HourTimeToMinutes(selectedHour, selectedMinute)
                    calculateBAC()
                }, hour, minute, false)

        mTimePicker.setButton(DialogInterface.BUTTON_NEUTRAL, "Now") { _, _ ->
            mMainActivity.endTimeMin = getCurrentTimeInMinuetsAndSetEditText(endPicker)
            calculateBAC()
        }

        mTimePicker.setTitle("End Time")
        mTimePicker.show()
    }

    private fun getCurrentTimeInMinuetsAndSetEditText(pickerDisplay: EditText): Int{
        val calendar = GregorianCalendar.getInstance()
        val date = Date()
        calendar.time = date
        val curHour = calendar.get(Calendar.HOUR_OF_DAY)
        val curMin = calendar.get(Calendar.MINUTE)
        pickerDisplay.setText(mConverter.convertSelectedTimeToString(curHour, curMin))
        return mConverter.convert24HourTimeToMinutes(curHour, curMin)
    }

    fun showOrHideEmptyListText(view: View){
        val emptyText = view.findViewById<TextView>(R.id.text_home_empty_list)
        if(mMainActivity.mDrinksList.isEmpty()){
            emptyText.visibility = View.VISIBLE
        }else{
            emptyText.visibility = View.INVISIBLE
        }
    }

    fun calculateBAC(){
        var gramsOfAlcohol = 0.0
        for (drink in mMainActivity.mDrinksList){
            val volume = mConverter.convertDrinkVolumeToLeters(drink.amount, drink.measurement)
            val abv = drink.abv
            val alcoholConst = 0.789
            gramsOfAlcohol += (volume * abv * alcoholConst)
        }
        gramsOfAlcoholConsumed = gramsOfAlcohol

        val maleConst = 0.68
        val femaleConst = 0.55
        val weightInGrams = mConverter.convertWeightToGrams(mMainActivity.weight, mMainActivity.weightMeasurement)

        val sexModifiedWeight = if (mMainActivity.sex!!) weightInGrams * maleConst
        else weightInGrams * femaleConst

        val instantBAC = 100 * (gramsOfAlcohol/sexModifiedWeight)

        var hoursElapsed = (mMainActivity.endTimeMin - mMainActivity.startTimeMin)/60.0
        if (mMainActivity.endTimeMin < mMainActivity.startTimeMin){
            val minInDay = 1440
            hoursElapsed = ((mMainActivity.endTimeMin + minInDay) - mMainActivity.startTimeMin)/60.0
        }

        drinkingDuration = hoursElapsed

        val bacDecayPerHour = 0.015
        bac = instantBAC - (hoursElapsed * bacDecayPerHour)
        bac = if (bac < 0.0) 0.0 else bac
        Log.v(TAG, "Calculated bac: $bac")
        updateBACText()
    }

    private fun showBacInfoDialog(){
        val builder = android.app.AlertDialog.Builder(view!!.context)
        val parent:ViewGroup? = null
        val dialogView = mMainActivity.layoutInflater
                .inflate(R.layout.fragment_home_bac_info_dialog, parent, false)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        dialog.findViewById<Button>(R.id.btn_home_dismiss_bac_info_dialog).setOnClickListener {
            dialog.dismiss()
        }

        val bacInfoTitleString = "BAC Level: " + String.format("%.3f", bac)
        dialog.findViewById<TextView>(R.id.text_home_dialog_bac_info_title).text = bacInfoTitleString

        var hoursMin = mConverter.convertDecimalTimeToHoursAndMinuets(drinkingDuration)
        Log.v(TAG, "$drinkingDuration")
        var hoursMinStrings = mConverter.convertHoursAndMinuetsIntoTwoDigitStrings(hoursMin)
        val durationString =  "${hoursMinStrings.first} hours  ${hoursMinStrings.second} min"
        dialog.findViewById<TextView>(R.id.text_home_bac_info_duration_drinking).text = durationString

        val gramsString = String.format("%.2f", gramsOfAlcoholConsumed) + " grams"
        dialog.findViewById<TextView>(R.id.text_home_bac_info_grams_of_alc).text = gramsString

        val hoursToSober = if ((bac - 0.04) / 0.015 < 0) 0.0 else (bac - 0.04)/0.015
        hoursMin = mConverter.convertDecimalTimeToHoursAndMinuets(hoursToSober)
        hoursMinStrings = mConverter.convertHoursAndMinuetsIntoTwoDigitStrings(hoursMin)
        val hoursToSoberString = "${hoursMinStrings.first} hours  ${hoursMinStrings.second} min"
        dialog.findViewById<TextView>(R.id.text_home_bac_info_time_to_sober).text = hoursToSoberString

    }

    private fun updateBACText(){
        val bacValueView = view!!.findViewById<TextView>(R.id.text_home_bac_value)
        val bacResultView = view!!.findViewById<TextView>(R.id.text_home_bac_result)

        bacValueView.setOnClickListener { _ -> showBacInfoDialog() }
        bacResultView.setOnClickListener { _ -> showBacInfoDialog() }

        val bacText = "%.3f".format(bac)
        when{
            bac > .2 -> {
                changeTextViewColorAndText(bacValueView, bacText, R.color.colorBlack)
                changeTextViewColorAndText(bacResultView, "In Danger", R.color.colorBlack)
            }
            bac > .12 -> {
                changeTextViewColorAndText(bacValueView, bacText, R.color.colorRed)
                changeTextViewColorAndText(bacResultView, "Shit Faced", R.color.colorRed)
            }
            bac > .07 -> {
                changeTextViewColorAndText(bacValueView, bacText, R.color.colorOrange)
                changeTextViewColorAndText(bacResultView, "Drunk", R.color.colorOrange)
            }
            bac > .04 -> {
                changeTextViewColorAndText(bacValueView, bacText, R.color.colorLighterGreen)
                changeTextViewColorAndText(bacResultView, "Tipsy", R.color.colorLighterGreen)
            }
            else -> {
                changeTextViewColorAndText(bacValueView, bacText, R.color.colorGreen)
                changeTextViewColorAndText(bacResultView, "Sober", R.color.colorGreen)
            }
        }
        bacValueView.invalidate()
        bacValueView.requestLayout()
    }

    private fun changeTextViewColorAndText(textView: TextView, text: String, color: Int){
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(context!!, color))
    }
}
