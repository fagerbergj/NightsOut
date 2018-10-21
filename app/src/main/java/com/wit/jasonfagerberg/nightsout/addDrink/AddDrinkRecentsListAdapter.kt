package com.wit.jasonfagerberg.nightsout.addDrink

import android.content.Context
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import java.util.ArrayList

class AddDrinkFragmentRecentsListAdapter(private val mContext: Context, drinksList: ArrayList<Drink>) :
        RecyclerView.Adapter<AddDrinkFragmentRecentsListAdapter.ViewHolder>() {
    // vars
    private val mRecentDrinksList: MutableList<Drink> = drinksList
    private lateinit var mMainActivity: MainActivity

    // set layout inflater & inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.recents_item, parent, false)
        mMainActivity = mContext as MainActivity
        return ViewHolder(view)
    }

    // When view is rendered bind the correct holder to it
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mRecentDrinksList[position]
        holder.name.text = drink.name
        holder.card.setOnClickListener { _ ->
            mMainActivity.showToast("${holder.name.text} information filled in")
            mMainActivity.addDrinkFragment.fillViews(drink.name, drink.abv, drink.amount, drink.measurement)
        }

        holder.card.setOnLongClickListener { v: View ->
            val lightSimpleDialog = LightSimpleDialog(v.context!!)
            val posAction = {
                mRecentDrinksList.remove(drink)
                this.notifyItemRemoved(position)
                mMainActivity.showToast("Drink Removed")
            }
            lightSimpleDialog.setActions(posAction, {})
            lightSimpleDialog.show("Remove from recents?")
            true
        }
    }

    override fun getItemCount(): Int {
        return mRecentDrinksList.size
    }

    // ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView = itemView.findViewById(R.id.text_recent_drink_name)
        internal val card: CardView = itemView.findViewById(R.id.card_recent_item)
        internal var image: ImageView = itemView.findViewById(R.id.image_recent)
    }
}