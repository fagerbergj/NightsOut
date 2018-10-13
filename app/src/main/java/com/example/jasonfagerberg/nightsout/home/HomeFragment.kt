package com.example.jasonfagerberg.nightsout.home

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.*
import android.widget.*
import com.example.jasonfagerberg.nightsout.main.MainActivity
import com.example.jasonfagerberg.nightsout.R
import com.example.jasonfagerberg.nightsout.main.Converter
import java.util.*
import android.widget.RelativeLayout
import com.example.jasonfagerberg.nightsout.log.LogHeader
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import kotlin.collections.ArrayList
import android.widget.Toast


private const val TAG = "HomeFragment"

class HomeFragment : Fragment(){
    private lateinit var mDrinkListAdapter: HomeFragmentDrinkListAdapter
    private lateinit var mRelativeLayout: RelativeLayout
    private lateinit var mMainActivity: MainActivity
    private val mConverter = Converter()
    private var bac = 0.000

    private var drinkingDuration = 0.0
    private var standardDrinksConsumed = 0.0

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
        when (resId){
            R.id.btn_toolbar_home_done ->  showDatePicker()
            R.id.btn_clear_drink_list -> {
                mMainActivity.mDrinksList.clear()
                mDrinkListAdapter.notifyDataSetChanged()
                showOrHideEmptyListText(view!!)
            }
            R.id.btn_disclaimer -> showDisclaimerDialog()
        }
        return true
    }

    private fun showDisclaimerDialog(){
        val builder = android.app.AlertDialog.Builder(view!!.context)
        val parent:ViewGroup? = null
        val dialogView = mMainActivity.layoutInflater
                .inflate(R.layout.fragment_home_dialog_disclaimer, parent, false)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
        dialog.findViewById<Button>(R.id.btn_disclaimer_dismiss).setOnClickListener { dialog.dismiss() }
    }

    private fun showDatePicker(){
        val calendar = Calendar.getInstance()

        val dp = DatePickerDialog(context!!, null, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        dp.setButton(DatePickerDialog.BUTTON_POSITIVE, "OK") { _ , _ ->
            val logYear = dp.datePicker.year
            val logMonth = dp.datePicker.month
            val logDay = dp.datePicker.dayOfMonth
            val logDate = Integer.parseInt(mConverter.yearMonthDayTo8DigitString(logYear, logMonth, logDay))

            val testHeader = LogHeader(logDate, 0.0, 0.0)
            if (testHeader in mMainActivity.mLogHeaders){
                showOverrideLogDialog(logDate)
            }else{
                mMainActivity.mLogHeaders.add(LogHeader(logDate, bac, drinkingDuration))
                mMainActivity.mDatabaseHelper.pushDrinksToLogDrinks(logDate)
                val message = "Log created on ${testHeader.monthName} ${testHeader.day}, ${testHeader.year}"
                showToast(message)
            }
        }

        dp.setTitle("Log Day")
        dp.show()
    }

    private fun showToast(message: String){
        val toast = Toast.makeText(context!!, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 450)
        toast.show()
    }

    private fun showOverrideLogDialog(logDate: Int){
        val headerIndex = mMainActivity.mLogHeaders.indexOf(LogHeader(logDate, 0.0, 0.0))
        val header = mMainActivity.mLogHeaders[headerIndex]

        val builder = android.app.AlertDialog.Builder(view!!.context)
        val parent:ViewGroup? = null
        val dialogView = mMainActivity.layoutInflater
                .inflate(R.layout.fragment_home_dialog_update_log, parent, false)
        var message = "There is already a log on ${header.monthName} ${header.day}," +
                " ${header.year}.\nWould you like to update the old log?"
        dialogView.findViewById<TextView>(R.id.text_update_log_body).text = message

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()

        dialogView.findViewById<Button>(R.id.btn_update_log_cancel)
                .setOnClickListener { _ ->
                    showDatePicker()
                    dialog.dismiss()
                }

        dialogView.findViewById<Button>(R.id.btn_update_log_update)
                .setOnClickListener { _ ->
                    mMainActivity.mDatabaseHelper.deleteLog(header.date)
                    mMainActivity.mLogHeaders[headerIndex] = LogHeader(header.date, bac, drinkingDuration)
                    mDrinkListAdapter.notifyDataSetChanged()
                    mMainActivity.mDatabaseHelper.pushDrinksToLogDrinks(header.date)
                    message = "Log on ${header.monthName} ${header.day}," +
                            " ${header.year} was overwritten"
                    showToast(message)
                    dialog.dismiss()
                }
    }

    private fun setupToolbar(view: View){
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_home)
        toolbar.inflateMenu(R.menu.home_menu)
        mMainActivity.setSupportActionBar(toolbar)
        mMainActivity.supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun setupRecycler(view: View){
        // mDrinkList recycler view setup
        val drinksListView: RecyclerView = view.findViewById(R.id.recycler_drink_list)
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

        if(mMainActivity.startTimeMin == -1) {
            mMainActivity.startTimeMin = getCurrentTimeInMinuets()
            startPicker.setText(mConverter.timeTo12HourString(mMainActivity.startTimeMin))
        }
        if(mMainActivity.endTimeMin == -1){
            mMainActivity.endTimeMin = getCurrentTimeInMinuets()
            endPicker.setText(mConverter.timeTo12HourString(mMainActivity.endTimeMin))
        }

        if(mMainActivity.startTimeMin > -1) startPicker.setText(mConverter.timeTo12HourString(
                mMainActivity.startTimeMin))

        if(mMainActivity.endTimeMin > -1) endPicker.setText(mConverter.timeTo12HourString(
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
                    startPicker.setText(mConverter.timeTo12HourString(selectedHour, selectedMinute))
                    mMainActivity.startTimeMin = mConverter.c24HourTimeToMinutes(selectedHour, selectedMinute)
                    if(mMainActivity.endTimeMin == -1) mMainActivity.endTimeMin = mMainActivity.startTimeMin
                    calculateBAC()
                }, hour, minute, false)

        mTimePicker.setButton(DialogInterface.BUTTON_NEUTRAL, "Now") { _, _ ->
            mMainActivity.startTimeMin = getCurrentTimeInMinuets()
            startPicker.setText(mConverter.timeTo12HourString(mMainActivity.startTimeMin))
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
                    endPicker.setText(mConverter.timeTo12HourString(selectedHour, selectedMinute))
                    mMainActivity.endTimeMin = mConverter.c24HourTimeToMinutes(selectedHour, selectedMinute)
                    calculateBAC()
                }, hour, minute, false)

        mTimePicker.setButton(DialogInterface.BUTTON_NEUTRAL, "Now") { _, _ ->
            mMainActivity.endTimeMin = getCurrentTimeInMinuets()
            endPicker.setText(mConverter.timeTo12HourString(mMainActivity.endTimeMin))
            calculateBAC()
        }

        mTimePicker.setTitle("End Time")
        mTimePicker.show()
    }

    private fun getCurrentTimeInMinuets(): Int{
        val calendar = GregorianCalendar.getInstance()
        val date = Date()
        calendar.time = date
        val curHour = calendar.get(Calendar.HOUR_OF_DAY)
        val curMin = calendar.get(Calendar.MINUTE)
        return mConverter.c24HourTimeToMinutes(curHour, curMin)
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
        var a = 0.0
        for (drink in mMainActivity.mDrinksList){
            val volume = mConverter.drinkVolumeToFluidOz(drink.amount, drink.measurement)
            val abv = drink.abv/100
            a += (volume * abv )
        }
        standardDrinksConsumed = mConverter.fluidOzToGrams(a)/14.0

        val r = if(mMainActivity.sex!!) .73 else .66

        val weightInOz = mConverter.weightToLbs(mMainActivity.weight, mMainActivity.weightMeasurement)

        val sexModifiedWeight = weightInOz * r

        val instantBAC = (a * 5.14) / sexModifiedWeight

        var hoursElapsed = (mMainActivity.endTimeMin - mMainActivity.startTimeMin)/60.0
        if (mMainActivity.endTimeMin < mMainActivity.startTimeMin){
            val minInDay = 1440
            hoursElapsed = ((mMainActivity.endTimeMin + minInDay) - mMainActivity.startTimeMin)/60.0
        }

        drinkingDuration = hoursElapsed

        val bacDecayPerHour = 0.015
        bac = instantBAC - (hoursElapsed * bacDecayPerHour)
        bac = if (bac < 0.0) 0.0 else bac
        //Log.v(TAG, "Calculated bac: $bac")
        updateBACText()
    }

    private fun showBacInfoDialog(){
        val builder = android.app.AlertDialog.Builder(view!!.context)
        val parent:ViewGroup? = null
        val dialogView = mMainActivity.layoutInflater
                .inflate(R.layout.fragment_home_dialog_bac_info, parent, false)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
        setupBacDeclineChart(dialog)

        dialog.findViewById<Button>(R.id.btn_bac_info_dismiss).setOnClickListener {
            dialog.dismiss()
        }

        val bacInfoTitleString = "BAC Level: " + String.format("%.3f", bac)
        dialog.findViewById<TextView>(R.id.text_bac_info_title).text = bacInfoTitleString

        var hoursMin = mConverter.decimalTimeToHoursAndMinuets(drinkingDuration)
        //Log.v(TAG, "$drinkingDuration")
        var hoursMinStrings = mConverter.hoursAndMinuetsToTwoDigitStrings(hoursMin)
        val durationString =  "${hoursMinStrings.first} hours  ${hoursMinStrings.second} min"
        dialog.findViewById<TextView>(R.id.text_bac_info_duration).text = durationString

        val standardDrinksString = String.format("%.2f", standardDrinksConsumed) + " drinks"
        dialog.findViewById<TextView>(R.id.text_bac_info_standard_drinks).text = standardDrinksString

        val hoursToSober = if ((bac - 0.04) / 0.015 < 0) 0.0 else (bac - 0.04)/0.015
        hoursMin = mConverter.decimalTimeToHoursAndMinuets(hoursToSober)
        hoursMinStrings = mConverter.hoursAndMinuetsToTwoDigitStrings(hoursMin)
        val hoursToSoberString = "${hoursMinStrings.first} hours  ${hoursMinStrings.second} min"
        dialog.findViewById<TextView>(R.id.text_bac_info_time_to_sober).text = hoursToSoberString
    }

    private fun setupBacDeclineChart(dialog: AlertDialog){
        val graph = dialog.findViewById<GraphView>(R.id.graph_bac_info_declining_bac)
        graph.title = "BAC Decline Over Time"
        val points = ArrayList<DataPoint>()
        var projectedBac = bac
        var elapsedTime = 0.0

        while (projectedBac > 0.0075){
            points.add(DataPoint(elapsedTime, projectedBac))
            elapsedTime += .5
            projectedBac -= 0.0075
        }
        val series = LineGraphSeries<DataPoint>(points.toTypedArray())
        series.setOnDataPointTapListener { series, dataPoint ->
            val pointBac = dataPoint.y.toString().substring(0,4)
            val time = mConverter.decimalTimeToTwoDigitStrings(dataPoint.x)
            showToast("BAC after ${time.first} hours and ${time.second} minuets: $pointBac")
        }

        val soberLine = ArrayList<DataPoint>()
        if (points.size > 0){
            soberLine.add(DataPoint(0.0,0.04))
            soberLine.add(DataPoint(100.0, 0.04))
        }
        val soberLineSeries = LineGraphSeries<DataPoint>(soberLine.toTypedArray())
        soberLineSeries.color = ContextCompat.getColor(context!!, R.color.colorLightGreen)
        soberLineSeries.backgroundColor = ContextCompat.getColor(context!!, R.color.colorLightGreen)
        soberLineSeries.isDrawBackground = true

        graph.addSeries(soberLineSeries)
        graph.addSeries(series)

        graph.gridLabelRenderer.labelVerticalWidth = 96

        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMaxY(bac + .0008)
        graph.viewport.setMaxX(elapsedTime + .5)
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
