package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val TAG = "LogFragment"

class LogFragment : Fragment() {

    private val mSessionList : HashMap<Session, ArrayList<Drink>> = HashMap()
    private lateinit var mLogFragmentAdapter: LogFragmentAdapter
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var calendar: Calendar
    private lateinit var mLogListView: RecyclerView
    private lateinit var mMainActivity: MainActivity
    private lateinit var mLogList: ArrayList<Any>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log, container, false)
        mMainActivity = context as MainActivity

        // recycler view
        mLogListView = view.findViewById(R.id.recycler_log)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mLogListView.layoutManager = linearLayoutManager
        val itemDecor = DividerItemDecoration(mLogListView.context, DividerItemDecoration.VERTICAL)
        mLogListView.addItemDecoration(itemDecor)

        // toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_log)
        toolbar.inflateMenu(R.menu.empty_menu)

        calendar = Calendar.getInstance()

//        // todo remove test data
//        for (i in -4..4){
//            calendar = Calendar.getInstance()
//            calendar.add(Calendar.DATE, i)
//            Log.v(TAG, calendar.time.toString())
//
//            val session = Session(calendar.time, i.toDouble(), i.toDouble())
//
//            mSessionList[session] = ArrayList()
//
//            val end = (Math.random()*10).toInt()
//            for (x in 0..end){
//                val drink = Drink(ByteArray(0), "This is an Example Drink # $x",
//                        x*10 + x + x.toDouble()/10, (x*10 + x + x.toDouble()/10), "oz")
//                mSessionList[session]!!.add(drink)
//            }
//        }

        // take date from calender, pull correct session, pass to adapter
        mLogList = ArrayList()

        // calender setup
        setupCalendar(view)

        // set adapter
        mLogFragmentAdapter = LogFragmentAdapter(context!!, mLogList)
        mLogListView.adapter = mLogFragmentAdapter

        // setup bottom nav bar
        mMainActivity.showBottomNavBar(R.id.bottom_nav_log)

        return view
    }

    companion object {
        fun newInstance(): LogFragment = LogFragment()
    }

    // format days to correct object and send to decorator
    private fun highlightDays(){
        val dates = ArrayList<CalendarDay>()
        for(session in mSessionList.keys){
            val day = CalendarDay.from(session.date)
            dates.add(day)
        }
        calendarView.addDecorator(EventDecorator(ContextCompat.getColor(context!!,
                R.color.colorPrimaryDark), dates))
    }

    private fun setupCalendar(view: View){
        calendarView = view.findViewById(R.id.calender_log)
        calendarView.selectedDate = CalendarDay.today()
        calendar.time = calendarView.selectedDate.date
        var curSession = Session(calendar.time,0.0,0.0)
        mLogList.add(curSession)

        if(curSession in mSessionList.keys){ mLogList.addAll(mSessionList[curSession]!!) }

        // show or hide empty text
        showOrHideEmptyTextViews(view)

        // add blue dots to days you drank
        highlightDays()

        // when date is changed, change recycler list
        calendarView.setOnDateChangedListener{_ , day, _ ->
            mLogList.clear()
            calendar.set(day.year, day.month, day.day)
            curSession = Session(calendar.time,0.0,0.0)
            mLogList.add(curSession)

            if(curSession in mSessionList.keys){ mLogList.addAll(mSessionList[curSession]!!) }
            mLogFragmentAdapter.notifyDataSetChanged()
            mLogListView.layoutManager?.scrollToPosition(0)
            showOrHideEmptyTextViews(mLogListView.parent as View)
        }
    }

    private fun showOrHideEmptyTextViews(view: View){
        val emptyLog = view.findViewById<TextView>(R.id.text_log_empty_list)
        if(mLogList.size == 1){
            emptyLog.visibility = View.VISIBLE
        }else{
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
