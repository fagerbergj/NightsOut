package com.wit.jasonfagerberg.nightsout.log

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.converter.Converter
import com.wit.jasonfagerberg.nightsout.databaseHelper.LogDatabaseHelper
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.Calendar
import java.util.Date
import kotlin.collections.ArrayList

// private const val TAG = "LogFragment"

class LogFragment : Fragment() {

    private lateinit var mLogFragmentAdapter: LogFragmentAdapter
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var calendar: Calendar
    private lateinit var mLogListView: RecyclerView
    private lateinit var mMainActivity: MainActivity
    private lateinit var mLogList: ArrayList<Any>
    private val converter: Converter = Converter()
    private lateinit var logDatabaseHelper: LogDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        mMainActivity = context as MainActivity
        mMainActivity.logFragment = this
        calendar = Calendar.getInstance()
        logDatabaseHelper = LogDatabaseHelper(mMainActivity.mDatabaseHelper, mMainActivity)
        // take date from calender, pull correct session, pass to adapter
        mLogList = ArrayList()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log, container, false)
        // recycler view
        mLogListView = view.findViewById(R.id.recycler_log)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        mLogListView.layoutManager = linearLayoutManager
        val itemDecor = DividerItemDecoration(mLogListView.context, DividerItemDecoration.VERTICAL)
        mLogListView.addItemDecoration(itemDecor)

        setHasOptionsMenu(true)

        // set adapter
        setAdapter()
        return view
    }

    override fun onResume() {
        val myCalendar = Calendar.getInstance()
        setupCalendar(view!!)
        calendarView.selectedDate = CalendarDay.from(Date(myCalendar.time.time))
        // calender setup
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        mMainActivity.supportActionBar?.title = "Log"
        inflater!!.inflate(R.menu.log_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        when (resId) {
            R.id.btn_clear_all_logs -> {
                if (mMainActivity.mLogHeaders.isEmpty()) return false
                val lightSimpleDialog = LightSimpleDialog(context!!)
                val posAction = {
                    for (header in mMainActivity.mLogHeaders) {
                        logDatabaseHelper.deleteLog(header.date)
                    }
                    mMainActivity.mLogHeaders.clear()
                    resetCalendar()
                }
                lightSimpleDialog.setActions(posAction, {})
                lightSimpleDialog.show("Are you sure you want to clear all logs?")
            }
            R.id.btn_clear_selected_day_log -> {
                val date = converter.yearMonthDayTo8DigitString(calendarView.selectedDate.year,
                        calendarView.selectedDate.month, calendarView.selectedDate.day).toInt()
                if (mMainActivity.mLogHeaders.indexOf(LogHeader(date)) == -1) return false
                mMainActivity.mLogHeaders.remove(LogHeader(date))
                logDatabaseHelper.deleteLog(date)
                resetCalendar()
            }
            R.id.btn_move_selected_log -> {
                val date = converter.yearMonthDayTo8DigitString(calendarView.selectedDate.year,
                        calendarView.selectedDate.month, calendarView.selectedDate.day).toInt()
                val index = mMainActivity.mLogHeaders.indexOf(LogHeader(date))
                if (index == -1) {
                    mMainActivity.showToast("Cannot move empty log")
                    return false
                }
                val header = mMainActivity.mLogHeaders[index]
                val datePicker = LogFragmentDatePicker(this, mMainActivity, Converter(), header)
                datePicker.showDatePicker()
            }
        }
        return true
    }

    fun resetCalendar() {
        mLogList.clear()
        calendarView.removeDecorators()
        setAdapter()
        mLogFragmentAdapter.notifyDataSetChanged()
        highlightDays()
    }

    private fun setAdapter() {
        mLogFragmentAdapter = LogFragmentAdapter(context!!, mLogList)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val date = Integer.parseInt(converter.yearMonthDayTo8DigitString(year, month, day))
        setLogListBasedOnDay(date)
        mLogListView.adapter = mLogFragmentAdapter
    }

    private fun setupCalendar(view: View) {
        calendarView = view.findViewById(R.id.calender_log)
        calendarView.selectedDate = CalendarDay.today()
        calendar.time = calendarView.selectedDate.date

        // show or hide empty text
        showOrHideEmptyTextViews(view)

        // add blue dots to days you drank
        highlightDays()

        // when date is changed, change recycler list
        calendarView.setOnDateChangedListener { _, day, _ ->
            calendar.set(day.year, day.month, day.day)
            mLogList.clear()

            val date = Integer.parseInt(converter.yearMonthDayTo8DigitString(day.year, day.month, day.day))
            setLogListBasedOnDay(date)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) setupCalendar(view!!)
        super.setUserVisibleHint(isVisibleToUser)
    }

    private fun highlightDays() {
        val dates = ArrayList<CalendarDay>()
        val calendar = Calendar.getInstance()
        for (log in mMainActivity.mLogHeaders) {
            calendar.set(log.year, log.month, log.day)
            val day = CalendarDay.from(Date(calendar.time.time))
            dates.add(day)
        }
        calendarView.addDecorator(EventDecorator(ContextCompat.getColor(context!!,
                R.color.colorPrimaryDark), dates))
    }

    private fun setLogListBasedOnDay(date: Int) {
        mLogList.clear()
        val index = mMainActivity.mLogHeaders.indexOf(LogHeader(date))
        if (index >= 0) {
            val header = mMainActivity.mLogHeaders[index]
            mLogList.add(header)
            mLogList.addAll(logDatabaseHelper.getLoggedDrinks(header.date))
        } else {
            mLogList.add(LogHeader(date))
        }
        mLogFragmentAdapter.notifyDataSetChanged()
        mLogListView.layoutManager?.scrollToPosition(0)
        showOrHideEmptyTextViews(mLogListView.parent as View)
    }

    private fun showOrHideEmptyTextViews(view: View) {
        val emptyLog = view.findViewById<TextView>(R.id.text_log_empty_list)
        if (mLogList.size == 1) {
            emptyLog.visibility = View.VISIBLE
        } else {
            emptyLog.visibility = View.INVISIBLE
        }
    }
}

// decorator that draws circle
class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {
    private val dates: HashSet<CalendarDay> = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(10f, color))
    }
}
