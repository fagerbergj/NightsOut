package com.wit.jasonfagerberg.nightsout.addDrink

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.main.Constants

class AddDrinkActivityRecentsListAdapter(private val mActivity: AddDrinkActivity) :
        RecyclerView.Adapter<AddDrinkActivityRecentsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = mActivity.layoutInflater
        val view = inflater.inflate(R.layout.item_recents, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mActivity.mRecentsList[position]
        holder.name.text = drink.name
        holder.card.setOnClickListener {
            mActivity.showToast("${holder.name.text} information filled in")
            mActivity.fillViews(drink.name, drink.abv, drink.amount, drink.measurement)
        }

        holder.card.setOnLongClickListener { v: View ->
            val lightSimpleDialog = LightSimpleDialog(v.context!!)
            val posAction = {
                mActivity.mRecentsList.remove(drink)
                this.notifyItemRemoved(position)
                mActivity.showToast("Drink Removed")
                mActivity.showOrHideEmptyTextViews()
                drink.recent = false
                mActivity.mDatabaseHelper.updateRowInDrinksTable(drink)
            }
            lightSimpleDialog.setActions(posAction, {})
            lightSimpleDialog.show("Remove from recents?")
            true
        }
    }

    override fun getItemCount(): Int {
        return mActivity.mRecentsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView = itemView.findViewById(R.id.text_recent_drink_name)
        internal val card: CardView = itemView.findViewById(R.id.card_recent_item)
    }
}