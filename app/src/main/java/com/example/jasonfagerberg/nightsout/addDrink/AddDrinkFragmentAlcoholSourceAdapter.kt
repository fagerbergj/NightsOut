package com.example.jasonfagerberg.nightsout.addDrink

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jasonfagerberg.nightsout.R
import com.example.jasonfagerberg.nightsout.addDrink.AddDrinkFragmentComplexDrink.AlcoholSource
import com.example.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.example.jasonfagerberg.nightsout.main.MainActivity
import java.util.*


class AddDrinkFragmentAlcoholSourceAdapter(private val mContext: Context, alcoholSource: ArrayList<AlcoholSource>) :
        RecyclerView.Adapter<AddDrinkFragmentAlcoholSourceAdapter.ViewHolder>() {
    // vars
    private val mAlcoholSourceList: MutableList<AlcoholSource> = alcoholSource
    private lateinit var mMainActivity: MainActivity
    private lateinit var view: View

    // set layout inflater & inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        view = inflater.inflate(R.layout.fragment_add_drink_alcohol_source, parent, false)
        mMainActivity = mContext as MainActivity
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alcoholSource = mAlcoholSourceList[position]
        val number = "#${mAlcoholSourceList.size}"
        val abv = "${"%.2f".format(alcoholSource.abv)}%"

        holder.textNumber.text = number
        holder.textAbv.text = abv
        holder.textAmount.text = alcoholSource.amount.toString()
        holder.textMeasurement.text = alcoholSource.measurement

        holder.layout.setOnClickListener { _ ->
            val lightSimpleDialog = LightSimpleDialog(mContext)
            val posAction = {
                mAlcoholSourceList.remove(alcoholSource)
                notifyDataSetChanged()
            }
            lightSimpleDialog.setActions(posAction, {})
            lightSimpleDialog.show("Remove Alcohol Source?")
        }
    }

    override fun getItemCount(): Int {
        return mAlcoholSourceList.size
    }

    // ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val textNumber = itemView.findViewById<TextView>(R.id.alc_source_number)
        internal val textAbv = itemView.findViewById<TextView>(R.id.alc_source_abv)
        internal val textAmount = itemView.findViewById<TextView>(R.id.alc_source_amount)
        internal val textMeasurement = itemView.findViewById<TextView>(R.id.alc_source_measurement)
        internal val layout = itemView.findViewById<RelativeLayout>(R.id.alc_source_layout)
    }
}