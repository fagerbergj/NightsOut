package com.example.jasonfagerberg.nightsout.addDrink

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jasonfagerberg.nightsout.main.MainActivity
import com.example.jasonfagerberg.nightsout.addDrink.AddDrinkFragmentComplexDrink.AlcoholSource
import com.example.jasonfagerberg.nightsout.R
import java.util.ArrayList

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
        val abv = "${"%.3f".format(alcoholSource.abv)}%"

        holder.textNumber.text = number
        holder.textAbv.text = abv
        holder.textAmount.text = alcoholSource.amount.toString()
        holder.textMeasurement.text = alcoholSource.measurement
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
    }
}