package com.example.jasonfagerberg.nightsout.log

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
//import android.util.Log
import android.view.*
import android.widget.TextView
import com.example.jasonfagerberg.nightsout.R
import com.example.jasonfagerberg.nightsout.converter.Converter
import com.example.jasonfagerberg.nightsout.databaseHelper.LogDatabaseHelper
import com.example.jasonfagerberg.nightsout.main.MainActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.*
import kotlin.collections.ArrayList

//private const val TAG = "LogFragment"

class LogFragment : Fragment() {

    private lateinit var mLogFragmentAdapter: LogFragmentAdapter
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var calendar: Calendar
    private lateinit var mLogListView: RecyclerView
    private lateinit var mMainActivity: MainActivity
    private lateinit var mLogList: ArrayList<Any>
    private val converter: Converter = Converter()
    private lateinit var logDatabaseHelper: LogDatabaseHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log, container, false)
        mMainActivity = context as MainActivity
        calendar = Calendar.getInstance()
        logDatabaseHelper = LogDatabaseHelper(mMainActivity.mDatabaseHelper, mMainActivity)

        // recycler view
        mLogListView = view.findViewById(R.id.recycler_log)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        mLogListView.layoutManager = linearLayoutManager
        val itemDecor = DividerItemDecoration(mLogListView.context, DividerItemDecoration.VERTICAL)
        mLogListView.addItemDecoration(itemDecor)

        // toolbar setup
        setupToolbar(view!!)

        // take date from calender, pull correct session, pass to adapter
        mLogList = ArrayList()

        // calender setup
        setupCalendar(view)

        // set adapter
        setAdapter()

        // setup bottom nav bar
        mMainActivity.showBottomNavBar(R.id.bottom_nav_log)

        return view
    }

    override fun onResume() {
        val myCalendar = Calendar.getInstance()
        calendarView.selectedDate = CalendarDay.from(Date(myCalendar.time.time))
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.log_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        when (resId) {
            R.id.btn_clear_all_logs -> {
                for (header in mMainActivity.mLogHeaders) {
                    logDatabaseHelper.deleteLog(header.date)
                }
                mMainActivity.mLogHeaders.clear()
                mLogList.clear()
                calendarView.removeDecorators()
                setAdapter()
                mLogFragmentAdapter.notifyDataSetChanged()
                mLogFragmentAdapter.notifyDataSetChanged()
            }
            R.id.btn_clear_selected_day_log -> {
                val date = converter.yearMonthDayTo8DigitString(calendarView.selectedDate.year,
                        calendarView.selectedDate.month, calendarView.selectedDate.day).toInt()
                mMainActivity.mLogHeaders.remove(LogHeader(date, 0.0, 0.0))
                logDatabaseHelper.deleteLog(date)
                mLogList.clear()
                calendarView.removeDecorators()
                setAdapter()
                mLogFragmentAdapter.notifyDataSetChanged()
                highlightDays()
            }
        }
        return true
    }

    private fun setupToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_log)
        toolbar.inflateMenu(R.menu.log_menu)
        mMainActivity.setSupportActionBar(toolbar)
        mMainActivity.supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)
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
        val index = mMainActivity.mLogHeaders.indexOf(LogHeader(date, 0.0, 0.0))
        if (index >= 0) {
            val header = mMainActivity.mLogHeaders[index]
            mLogList.add(header)
            mLogList.addAll(logDatabaseHelper.getLoggedDrinks(header.date))
        } else {
            mLogList.add(LogHeader(date, 0.0, 0.0))
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
