package com.wit.jasonfagerberg.nightsout.log

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.R
import java.util.*

// view type identifier
private const val DRINK = 0
private const val HEADER = 1

class LogFragmentAdapter(private val mContext: Context, logList: ArrayList<Any>) :
        RecyclerView.Adapter<LogFragmentAdapter.ViewHolder>() {

    // log to display
    private val mLogList: MutableList<Any> = logList

    // inflate correct layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when(viewType){
            DRINK -> {
                val drink: Drink = mLogList[position] as Drink

                holder.name!!.text = drink.name

                val abv = "ABV: " + "%.1f".format(drink.abv) + "%"
                holder.abv!!.text = abv
                val amount = "%.1f".format(drink.amount) + " " + drink.measurement
                holder.amount!!.text = amount
            }
            HEADER -> {
                val header: LogHeader = mLogList[position] as LogHeader

                holder.day!!.text = header.dateString

                val duration = "Duration: ${header.durationString}"
                holder.duration!!.text = duration

                val bac = "Bac: ${"%.3f".format(header.bac)}"
                holder.bac!!.text = bac
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
        internal var abv: TextView ?= itemView.findViewById(R.id.text_log_drink_abv)
        internal var amount: TextView ?= itemView.findViewById(R.id.text_log_drink_amount)

        // header views
        internal var day: TextView ?= itemView.findViewById(R.id.text_log_day)
        internal var duration: TextView ?= itemView.findViewById(R.id.text_log_duration)
        internal var bac: TextView ?= itemView.findViewById(R.id.text_log_BAC)
    }
}