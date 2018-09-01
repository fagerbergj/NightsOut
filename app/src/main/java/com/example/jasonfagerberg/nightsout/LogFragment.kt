package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import com.prolificinteractive.materialcalendarview.DayViewFacade

private const val TAG = "LogFragment"

class LogFragment : Fragment() {

    private val mSessionList : HashMap<Session, ArrayList<Drink>> = HashMap()
    private lateinit var mLogFragmentAdapter: LogFragmentAdapter
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var calendar: Calendar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log, container, false)

        // recycler view
        val logListView: RecyclerView = view.findViewById(R.id.recycler_log)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        logListView.layoutManager = linearLayoutManager

        // toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_log)
        toolbar.inflateMenu(R.menu.empty_menu)

        calendar = Calendar.getInstance()

        // todo remove test data
        for (i in 9 downTo 0){
            calendar.set(2018, 8, i)

            val session = Session(calendar.time, i.toDouble(), i.toDouble())
            calendar.set(2018, 8, 1)

            mSessionList[session] = ArrayList()

            val end = (Math.random()*10).toInt()
            for (x in 0..end){
                val drink = Drink(ByteArray(0), "This is an Example Drink # $x",
                        x*10 + x + x.toDouble()/10, (x*10 + x + x.toDouble()/10), "oz")
                mSessionList[session]!!.add(drink)
            }
        }

        // take date from calender, pull correct session, pass to adapter
        val logList: ArrayList<Any> = ArrayList()

        // calender setup
        calendarView = view.findViewById(R.id.calender_log)
        calendarView.selectedDate = CalendarDay.today()
        calendar.time = calendarView.selectedDate.date
        var curSession = Session(calendar.time,0.0,0.0)
        logList.add(curSession)

        if(curSession in mSessionList.keys){ logList.addAll(mSessionList[curSession]!!) }

        // add blue dots to days you drank
        highlightDays()

        // when date is changed, change recycler list
        calendarView.setOnDateChangedListener{_, day, _ ->
            logList.clear()
            calendar.set(day.year, day.month, day.day)
            curSession = Session(calendar.time,0.0,0.0)
            logList.add(curSession)

            if(curSession in mSessionList.keys){ logList.addAll(mSessionList[curSession]!!) }
            mLogFragmentAdapter.notifyDataSetChanged()
            logListView.layoutManager!!.scrollToPosition(0)
        }


        // set adapter
        mLogFragmentAdapter = LogFragmentAdapter(context!!, logList)
        logListView.adapter = mLogFragmentAdapter

        // setup bottom nav bar
        val mainActivity: MainActivity = context as MainActivity
        val botNavBar: BottomNavigationView = mainActivity.findViewById(R.id.bottom_navigation_view)
        botNavBar.visibility = View.VISIBLE
        botNavBar.selectedItemId = R.id.bottom_nav_log

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.ABOVE, R.id.bottom_navigation_view)
        (mainActivity.findViewById(R.id.main_frame) as FrameLayout).layoutParams = params

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