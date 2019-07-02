package com.wit.jasonfagerberg.nightsout.addDrink.drinkSuggestion

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkActivity
import com.wit.jasonfagerberg.nightsout.models.Drink

class DrinkSuggestionArrayAdapter(
    private var Activity: AddDrinkActivity,
    private var layoutResourceId: Int,
    var data: ArrayList<Drink>
) : ArrayAdapter<Drink>(Activity, layoutResourceId, data) {

    override fun getCount(): Int {
        return data.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            // inflate the layout
            val inflater = Activity.layoutInflater
            view = inflater.inflate(layoutResourceId, parent, false)
        }

        val drink = data[position]

        val nameTextView = view!!.findViewById<TextView>(R.id.text_add_drink_suggestion_name)
        val abvTextView = view.findViewById<TextView>(R.id.text_add_drink_suggestion_abv)
        val amountTextView = view.findViewById<TextView>(R.id.text_add_drink_suggestion_amount)

        view.findViewById<ImageView>(R.id.imgBtn_add_drink_suggestion_cancel).setOnClickListener {
            remove(drink)
        }

        // round decimals to 2 decimals
        nameTextView.text = drink.name
        val abv = "${"%.2f".format(drink.abv)}%"
        abvTextView.text = abv
        val amount = "${"%.1f".format(drink.amount)} ${drink.measurement}"
        amountTextView.text = amount

        return view
    }

    override fun remove(`object`: Drink?) {
        var i = data.indexOf(`object`)
        // have to make sure to delete drink based on UUID since equals only compares names
        for (x in data.indices) {
            val d = data[x]
            if (d.isExactDrink(`object` as Drink)) {
                // save index to avoid out of bounds exception
                i = x
                break
            }
        }
        data.removeAt(i)
        notifyDataSetChanged()
        Activity.mDatabaseHelper.updateDrinkSuggestionStatus(`object`!!.id, true)
    }
}