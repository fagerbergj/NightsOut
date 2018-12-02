package com.wit.jasonfagerberg.nightsout.home

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
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.view.MenuItem
import android.view.MenuInflater
import android.view.Menu
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.converter.Converter
import android.widget.RelativeLayout
import android.widget.Button
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import androidx.recyclerview.widget.ItemTouchHelper
import android.widget.TextView
import android.widget.ImageButton
import android.widget.EditText
import com.wit.jasonfagerberg.nightsout.dialogs.BacInfoDialog
import com.wit.jasonfagerberg.nightsout.manageDB.ManageDBFragment
import java.util.Calendar

// private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {
    lateinit var mDrinkListAdapter: HomeFragmentDrinkListAdapter
    private lateinit var mRelativeLayout: RelativeLayout
    private lateinit var mMainActivity: MainActivity
    val mConverter = Converter()
    var bac = 0.000

    var drinkingDuration = 0.0
    var standardDrinksConsumed = 0.0

    // android lifecycle
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // inflate layout
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        mMainActivity = context as MainActivity

        // set layout
        mRelativeLayout = view.findViewById(R.id.layout_home)

        // toolbar setup
        setupToolbar(view)

        // setup list
        setupRecycler(view)

        // add a drink button setup
        val btnAdd = view.findViewById<MaterialButton>(R.id.btn_home_add_drink)
        btnAdd.setOnClickListener {
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

    override fun onResume() {
        super.onResume()
        val bacInfoDialog = BacInfoDialog(context!!)
        view!!.findViewById<ImageButton>(R.id.btn_home_bac_info).setOnClickListener {
            bacInfoDialog.showBacInfoDialog()
        }
        calculateBAC()
        showOrHideEmptyListText(view!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.home_menu, menu)
        menu?.findItem(R.id.btn_toolbar_toggle_time_display)?.title = if (mMainActivity.use24HourTime) {
            "Use 12 Hour Time"
        } else "Use 24 Hour Time"
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        val datePicker = HomeFragmentLogDatePicker(context!!)
        when (resId) {
            R.id.btn_toolbar_home_done -> datePicker.showDatePicker()
            R.id.btn_clear_drink_list -> {
                if (mMainActivity.mDrinksList.isEmpty()) return false
                val lightSimpleDialog = LightSimpleDialog(context!!)
                val posAction = {
                    mMainActivity.mDrinksList.clear()
                    mDrinkListAdapter.notifyDataSetChanged()
                    calculateBAC()
                    showOrHideEmptyListText(view!!)
                }
                lightSimpleDialog.setActions(posAction, {})
                lightSimpleDialog.show("Are you sure you want to clear all drinks?")
            }
            R.id.btn_disclaimer -> showDisclaimerDialog()
            R.id.btn_toolbar_toggle_time_display -> {
                mMainActivity.use24HourTime = !mMainActivity.use24HourTime
                setupEditTexts(view!!)
            }
            R.id.btn_toolbar_manage_db -> {
                mMainActivity.hideBottomNavBar()
                mMainActivity.setFragment(ManageDBFragment())
            }
        }
        return true
    }

    private fun setupToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_home)
        toolbar.inflateMenu(R.menu.home_menu)
        mMainActivity.setSupportActionBar(toolbar)
        mMainActivity.supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun setupRecycler(view: View) {
        // mDrinkList recycler view setup
        val drinksListView: RecyclerView = view.findViewById(R.id.recycler_drink_list)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        drinksListView.layoutManager = linearLayoutManager

        val itemDecor = DividerItemDecoration(drinksListView.context, DividerItemDecoration.VERTICAL)
        drinksListView.addItemDecoration(itemDecor)

        // set adapter
        mDrinkListAdapter = HomeFragmentDrinkListAdapter(context!!, mMainActivity.mDrinksList)
        // update list
        drinksListView.adapter = mDrinkListAdapter // Update display with new list
        drinksListView.layoutManager!!.scrollToPosition(mMainActivity.mDrinksList.size - 1) // Nav to end of list

        setupDrinkItemTouchHelper(drinksListView)
    }

    private fun setupDrinkItemTouchHelper(drinksListView: RecyclerView) {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                mMainActivity.showToast("${mMainActivity.mDrinksList[viewHolder.adapterPosition].name} removed", true)
                mMainActivity.mDrinksList.removeAt(viewHolder.adapterPosition)
                mDrinkListAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                mDrinkListAdapter.notifyItemRangeChanged(0, mMainActivity.mDrinksList.size)
                showOrHideEmptyListText(view!!)
                calculateBAC()
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(drinksListView)
    }

    private fun setupEditTexts(view: View) {
        val startPicker: EditText = view.findViewById(R.id.edit_start_time)
        val endPicker: EditText = view.findViewById(R.id.edit_end_time)

        startPicker.setText(mConverter.timeToString(mMainActivity.startTimeMin, mMainActivity.use24HourTime))
        endPicker.setText(mConverter.timeToString(mMainActivity.endTimeMin, mMainActivity.use24HourTime))

        startPicker.setOnClickListener {
            startTimeEditTextOnCLickListener(startPicker)
        }

        endPicker.setOnClickListener {
            endTimeEditTextOnCLickListener(endPicker)
        }
    }

    private fun startTimeEditTextOnCLickListener(startPicker: EditText) {
        val currentTime = Calendar.getInstance()
        var hour = currentTime.get(Calendar.HOUR_OF_DAY)
        var minute = currentTime.get(Calendar.MINUTE)
        if (mMainActivity.startTimeMin != -1) {
            hour = mMainActivity.startTimeMin / 60
            minute = mMainActivity.startTimeMin % 60
        }
        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(context!!,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    startPicker.setText(mConverter.timeToString(selectedHour, selectedMinute, mMainActivity.use24HourTime))
                    mMainActivity.startTimeMin = mConverter.militaryHoursAndMinutesToMinutes(selectedHour, selectedMinute)
                    if (mMainActivity.endTimeMin == -1) mMainActivity.endTimeMin = mMainActivity.startTimeMin
                    calculateBAC()
                }, hour, minute, mMainActivity.use24HourTime)

        mTimePicker.setButton(DialogInterface.BUTTON_NEUTRAL, "Now") { _, _ ->
            mMainActivity.startTimeMin = mMainActivity.getCurrentTimeInMinuets()
            startPicker.setText(mConverter.timeToString(mMainActivity.startTimeMin, mMainActivity.use24HourTime))
            calculateBAC()
        }

        mTimePicker.setTitle("Start Time")
        mTimePicker.show()
    }

    private fun endTimeEditTextOnCLickListener(endPicker: EditText) {
        val currentTime = Calendar.getInstance()
        var hour = currentTime.get(Calendar.HOUR_OF_DAY)
        var minute = currentTime.get(Calendar.MINUTE)
        if (mMainActivity.endTimeMin != -1) {
            hour = mMainActivity.endTimeMin / 60
            minute = mMainActivity.endTimeMin % 60
        }

        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(context!!,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    endPicker.setText(mConverter.timeToString(selectedHour, selectedMinute, mMainActivity.use24HourTime))
                    mMainActivity.endTimeMin = mConverter.militaryHoursAndMinutesToMinutes(selectedHour, selectedMinute)
                    calculateBAC()
                }, hour, minute, mMainActivity.use24HourTime)

        mTimePicker.setButton(DialogInterface.BUTTON_NEUTRAL, "Now") { _, _ ->
            mMainActivity.endTimeMin = mMainActivity.getCurrentTimeInMinuets()
            endPicker.setText(mConverter.timeToString(mMainActivity.endTimeMin, mMainActivity.use24HourTime))
            calculateBAC()
        }

        mTimePicker.setTitle("End Time")
        mTimePicker.show()
    }

    fun showOrHideEmptyListText(view: View) {
        val emptyText = view.findViewById<TextView>(R.id.text_home_empty_list)
        if (mMainActivity.mDrinksList.isEmpty()) {
            emptyText.visibility = View.VISIBLE
        } else {
            emptyText.visibility = View.INVISIBLE
        }
    }

    private fun showDisclaimerDialog() {
        val builder = android.app.AlertDialog.Builder(view!!.context)
        val parent: ViewGroup? = null
        val dialogView = mMainActivity.layoutInflater
                .inflate(R.layout.fragment_home_dialog_disclaimer, parent, false)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
        dialog.findViewById<Button>(R.id.btn_disclaimer_dismiss).setOnClickListener { dialog.dismiss() }
    }

    fun calculateBAC() {
        var a = 0.0
        for (drink in mMainActivity.mDrinksList) {
            val volume = mConverter.drinkVolumeToFluidOz(drink.amount, drink.measurement)
            val abv = drink.abv / 100
            a += (volume * abv)
        }
        standardDrinksConsumed = mConverter.fluidOzToGrams(a) / 14.0

        val r = if (mMainActivity.sex!!) .73 else .66

        val weightInOz = mConverter.weightToLbs(mMainActivity.weight, mMainActivity.weightMeasurement)

        val sexModifiedWeight = weightInOz * r

        val instantBAC = (a * 5.14) / sexModifiedWeight

        var hoursElapsed = (mMainActivity.endTimeMin - mMainActivity.startTimeMin) / 60.0
        if (mMainActivity.endTimeMin < mMainActivity.startTimeMin) {
            val minInDay = 1440
            hoursElapsed = ((mMainActivity.endTimeMin + minInDay) - mMainActivity.startTimeMin) / 60.0
        }

        drinkingDuration = hoursElapsed

        val bacDecayPerHour = 0.015
        bac = instantBAC - (hoursElapsed * bacDecayPerHour)
        bac = if (bac < 0.0) 0.0 else bac
        updateBACText()
    }

    private fun updateBACText() {
        val bacValueView = view!!.findViewById<TextView>(R.id.text_home_bac_value)
        val bacResultView = view!!.findViewById<TextView>(R.id.text_home_bac_result)

        val bacInfo = BacInfoDialog(context!!)
        bacValueView.setOnClickListener { bacInfo.showBacInfoDialog() }
        bacResultView.setOnClickListener { bacInfo.showBacInfoDialog() }

        val bacText = "%.3f".format(bac)
        when {
            bac >= .2 -> {
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

    private fun changeTextViewColorAndText(textView: TextView, text: String, color: Int) {
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(context!!, color))
    }
}
