package com.wit.jasonfagerberg.nightsout.log

// import android.util.Log
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.utils.Converter
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import java.util.Calendar
import kotlin.collections.ArrayList
import kotlin.collections.Collection
import kotlin.collections.HashSet
import kotlinx.coroutines.launch

// private const val TAG = "LogFragment"

class LogFragment : Fragment() {

    private lateinit var mLogFragmentAdapter: LogFragmentAdapter
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var calendar: Calendar
    private lateinit var mLogListView: RecyclerView
    private lateinit var mMainActivity: MainActivity
    private lateinit var mLogList: ArrayList<Any>
    private val converter: Converter = Converter()
    private val repository by lazy { mMainActivity.repository }

    override fun onCreate(savedInstanceState: Bundle?) {
        mMainActivity = context as MainActivity
        mMainActivity.logFragment = this
        calendar = Calendar.getInstance()
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

        return view
    }

    override fun onResume() {
        // pull before binding; MainActivity's own fill of the shared list may lose the race
        lifecycleScope.launch {
            mMainActivity.mLogHeaders.clear()
            mMainActivity.mLogHeaders.addAll(repository.pullLogHeaders())
            if (view == null) return@launch
            setAdapter()
            setupCalendar(view!!)
            calendarView.selectedDate = CalendarDay.today()
            calendarView.selectionColor = if (mMainActivity.activeTheme == R.style.AppTheme) {
                ContextCompat.getColor(context!!, R.color.colorLightBlueGray)
            } else {
                ContextCompat.getColor(context!!, R.color.colorGray)
            }
        }

        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        mMainActivity.supportActionBar?.title = "Log"
        inflater.inflate(R.menu.log_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_clear_all_logs -> {
                if (mMainActivity.mLogHeaders.isEmpty()) return false
                val lightSimpleDialog = LightSimpleDialog(context!!)
                val posAction = {
                    val dates = mMainActivity.mLogHeaders.map { it.date }
                    lifecycleScope.launch {
                        for (date in dates) {
                            repository.deleteLog(date)
                        }
                    }
                    mMainActivity.mLogHeaders.clear()
                    resetCalendar()
                }
                lightSimpleDialog.setActions(posAction, {})
                lightSimpleDialog.show("Are you sure you want to clear all logs?")
            }
            R.id.btn_clear_selected_day_log -> {
                // material-calendarview 2.x months are 1-based; app stores 0-based
                val sel = calendarView.selectedDate ?: return false
                val date = converter.yearMonthDayTo8DigitString(sel.year,
                        sel.month - 1, sel.day).toInt()
                if (mMainActivity.mLogHeaders.indexOf(LogHeader(date)) == -1) return false
                mMainActivity.mLogHeaders.remove(LogHeader(date))
                lifecycleScope.launch { repository.deleteLog(date) }
                resetCalendar()
            }
            R.id.btn_move_selected_log -> {
                val sel = calendarView.selectedDate ?: return false
                val date = converter.yearMonthDayTo8DigitString(sel.year,
                        sel.month - 1, sel.day).toInt()
                val index = mMainActivity.mLogHeaders.indexOf(LogHeader(date))
                if (index == -1) {
                    mMainActivity.showToast("Cannot move empty log")
                    return false
                }
                val header = mMainActivity.mLogHeaders[index]
                val datePicker = LogFragmentDatePicker(this, mMainActivity, Converter(), header, mMainActivity.activeTheme)
                datePicker.showDatePicker()
            }
        }
        return mMainActivity.onOptionsItemSelected(item)
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
        val today = CalendarDay.today()
        calendarView.selectedDate = today
        calendar.set(today.year, today.month - 1, today.day)

        showOrHideEmptyTextViews(view)

        // add blue dots to days you drank
        highlightDays()
        val selectedDay = calendarView.selectedDate ?: today
        val selectedDate = Integer.parseInt(converter.yearMonthDayTo8DigitString(selectedDay.year, selectedDay.month - 1, selectedDay.day))
        setLogListBasedOnDay(selectedDate)

        // when date is changed, change recycler list
        calendarView.setOnDateChangedListener { _, day, _ ->
            calendar.set(day.year, day.month - 1, day.day)
            mLogList.clear()

            val date = Integer.parseInt(converter.yearMonthDayTo8DigitString(day.year, day.month - 1, day.day))
            setLogListBasedOnDay(date)
        }
    }

    private fun highlightDays() {
        val dates = ArrayList<CalendarDay>()
        for (log in mMainActivity.mLogHeaders) {
            dates.add(CalendarDay.from(log.year, log.month + 1, log.day))
        }
        calendarView.addDecorator(EventDecorator(ContextCompat.getColor(context!!,
                R.color.colorPrimary), dates))
    }

    private fun setLogListBasedOnDay(date: Int) {
        lifecycleScope.launch {
            mLogList.clear()
            val index = mMainActivity.mLogHeaders.indexOf(LogHeader(date))
            if (index >= 0) {
                val header = mMainActivity.mLogHeaders[index]
                mLogList.add(header)
                mLogList.addAll(repository.getLoggedDrinks(header.date))
            } else {
                mLogList.add(LogHeader(date))
            }
            if (!::mLogFragmentAdapter.isInitialized) return@launch
            mLogFragmentAdapter.notifyDataSetChanged()
            mLogListView.layoutManager?.scrollToPosition(0)
            showOrHideEmptyTextViews(mLogListView.parent as View)
        }
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
