package com.example.jasonfagerberg.nightsout

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*

// log tag
private const val TAG = "LogFragmentAdapter"

// view type identifier
private const val DRINK = 0
private const val HEADER = 1

class LogFragmentAdapter(private val mContext: Context, logList: ArrayList<Any>) :
RecyclerView.Adapter<LogFragmentAdapter.ViewHolder>() {

    // log to display
    private val mLogList: MutableList<Any> = logList

    // inflate correct layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogFragmentAdapter.ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        when (viewType) {
            DRINK -> return ViewHolder(inflater.inflate(R.layout.fragment_log_list_drink_item,
                    parent, false))
            HEADER -> return ViewHolder(inflater.inflate(R.layout.fragment_log_list_date_header,
                    parent, false))
        }
        return ViewHolder(View(mContext))
    }

    // When view is rendered set appropriate elements in the view holder
    override fun onBindViewHolder(holder: LogFragmentAdapter.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when(viewType){
            DRINK -> {
                val drink: Drink = mLogList[position] as Drink

                holder.name!!.text = drink.name

                val aav = "AAV: " + "%.1f".format(drink.aav) + "%"
                holder.aav!!.text = aav
                val amount = "%.1f".format(drink.amount) + drink.measurement
                holder.amount!!.text = amount
            }
            HEADER -> {
                val session: Session = mLogList[position] as Session

                // Log.v(TAG, getItemViewType(position).toString())
                holder.day!!.text = session.dateString

                val duration = "Duration: ${session.duration}"
                holder.duration!!.text = duration

                val maxBac = "Max BAC: ${session.maxBAC}"
                holder.maxBAC!!.text = maxBac

            }
        }
    }

    // read element fro log, determine if drink or header
    override fun getItemViewType(position: Int): Int {
        return if (mLogList[position] is Drink) {
            DRINK
        } else {
            HEADER
        }
    }

    override fun getItemCount(): Int {
        return mLogList.size
    }

    // holder, elements may be null depending on which layout is inflated
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // drink views
        internal var name: TextView ?= itemView.findViewById(R.id.text_log_drink_name)
        internal var aav: TextView ?= itemView.findViewById(R.id.text_log_drink_aav)
        internal var amount: TextView ?= itemView.findViewById(R.id.text_log_drink_amount)

        // header views
        internal var day: TextView ?= itemView.findViewById(R.id.text_log_day)
        internal var duration: TextView ?= itemView.findViewById(R.id.text_log_duration)
        internal var maxBAC: TextView ?= itemView.findViewById(R.id.text_log_max_BAC)
    }
}