package com.wit.jasonfagerberg.nightsout.v1.addDrink

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.v1.dialogs.LightSimpleDialog

class AddDrinkActivityFavoritesListAdapter(private val mActivity: AddDrinkActivity) :
        RecyclerView.Adapter<AddDrinkActivityFavoritesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = mActivity.layoutInflater
        val view = inflater.inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mActivity.mFavoritesList[position]
        holder.name.text = drink.name
        holder.card.setOnClickListener {
            mActivity.showToast("${holder.name.text} information filled in")
            mActivity.fillViews(drink.name, drink.abv, drink.amount, drink.measurement)
        }

        // remove from favorites on long press
        holder.card.setOnLongClickListener { v: View ->
            val lightSimpleDialog = LightSimpleDialog(v.context!!)
            val posAction = {
                mActivity.showToast("${mActivity.mFavoritesList[position].name} Removed From Favorites List")
                mActivity.mFavoritesList.remove(drink)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, mActivity.mFavoritesList.size)
                mActivity.showOrHideEmptyTextViews()
                mActivity.mDatabaseHelper.deleteRowsInTable("favorites", "drink_name = \"${drink.name}\"")
            }
            lightSimpleDialog.setActions(posAction, {})
            lightSimpleDialog.show("Remove from favorites?")
            true
        }
    }

    override fun getItemCount(): Int {
        return mActivity.mFavoritesList.size
    }

    // ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView = itemView.findViewById(R.id.text_profile_favorite_drink_name)
        internal val card: CardView = itemView.findViewById(R.id.card_profile_favorite_item)
    }
}