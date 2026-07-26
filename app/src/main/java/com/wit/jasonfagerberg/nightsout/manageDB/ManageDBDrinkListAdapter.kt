package com.wit.jasonfagerberg.nightsout.manageDB

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.models.Drink
import kotlinx.coroutines.launch

class ManageDBDrinkListAdapter(private val mContext: Context, private val mDrinksList: ArrayList<Drink>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ManageDBDrinkListAdapter.ViewHolder>() {

    private val mActivity = mContext as ManageDBActivity
    private val mFavoritesList = ArrayList<Drink>()
    private val mRecentsList = ArrayList<Drink>()

    init {
        mActivity.lifecycleScope.launch {
            mFavoritesList.addAll(mActivity.repository.pullFavoriteDrinks())
            mRecentsList.addAll(mActivity.repository.pullRecentDrinks())
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.activity_manage_db_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mDrinksList[position]

        holder.name.text = drink.name
        val abv = "ABV: " + "%.1f".format(drink.abv) + "%"
        holder.abv.text = abv
        val amount = "%.1f".format(drink.amount) + " " + drink.measurement
        holder.amount.text = amount

        drink.favorited = mFavoritesList.contains(drink)

        holder.options.setOnClickListener {
            mActivity.lifecycleScope.launch {
                val popup = PopupMenu(mContext, holder.options)
                popup.inflate(R.menu.manage_db_item_options)

                val favString = if (drink.favorited) mActivity.resources.getString(R.string.unfavorite_drink)
                else mActivity.resources.getString(R.string.favorite_drink)
                popup.menu.findItem(R.id.manage_db_item_favorite).title = favString

                val dontSuggest = mActivity.repository.getDrinkSuggestedStatus(drink.id)
                val suggestString = if (dontSuggest) mActivity.resources.getString(R.string.show_auto_complete_suggestion)
                else mActivity.resources.getString(R.string.hide_auto_complete_suggestion)
                popup.menu.findItem(R.id.manage_db_item_suggestion).title = suggestString

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.manage_db_item_favorite -> { favoriteItemOptionSelected(drink) }
                        R.id.manage_db_item_suggestion -> { suggestItemOptionSelected(drink, dontSuggest) }
                        R.id.manage_db_item_delete -> { deleteItemOptionSelected(drink, position) }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    private fun favoriteItemOptionSelected(drink: Drink): Boolean {
        drink.favorited = !drink.favorited
        mActivity.lifecycleScope.launch {
            mActivity.repository.updateDrinkModifiedTime(drink.id, Constants.getLongTimeNow())
            mActivity.repository.updateDrinkFavoriteStatus(drink)
        }
        for (d in mDrinksList) {
            if (d == drink) d.favorited = drink.favorited
        }
        notifyDataSetChanged()

        for (d in mDrinksList) {
            if (d == drink) d.favorited = drink.favorited
        }
        if (drink.favorited) mActivity.showToast("${drink.name} favorited")
        else mActivity.showToast("${drink.name} unfavorited")
        return true
    }

    private fun suggestItemOptionSelected(drink: Drink, dontSuggest: Boolean): Boolean {
        mActivity.lifecycleScope.launch {
            mActivity.repository.updateDrinkSuggestionStatus(drink.id, !dontSuggest)
        }
        if (!dontSuggest) mActivity.showToast("This drink will not be suggested")
        else mActivity.showToast("This drink will be suggested")
        return true
    }

    private fun deleteItemOptionSelected(drink: Drink, position: Int): Boolean {
        mActivity.lifecycleScope.launch {
            val loss = getLostReferenceString(drink)
            val dialog = LightSimpleDialog(mContext)
            val posAction = {
                mActivity.lifecycleScope.launch {
                    mActivity.repository.deleteDrinkById(drink.id)
                    removeCurrentSessionReference(drink)
                    mActivity.repository.updateDrinkFavoriteStatus(drink)
                }
                mDrinksList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, mDrinksList.size)
            }
            dialog.setActions(posAction, {})
            dialog.show("Are you sure that you want to delete \"${drink.name}\"" +
                    " from database, this will remove all references to the drink.\n\nReferences Lost:\n$loss")
        }
        return true
    }

    override fun getItemCount(): Int {
        return mDrinksList.size
    }

    suspend fun getLostReferenceString(drink: Drink): String {
        var loss = ""

        for (d in mActivity.repository.pullCurrentSessionDrinks()) {
            if (d.isExactDrink(drink)) {
                loss += "Drink in Current Drinks List\n"
                break
            }
        }
        for (f in mFavoritesList) {
            if (f.isExactDrink(drink)) {
                loss += "Favorite Drink Reference\n"
                break
            }
        }
        for (r in mRecentsList) {
            if (r.isExactDrink(drink)) {
                loss += "Recent Drink Reference\n"
                break
            }
        }

        if (mActivity.repository.isLoggedDrink(drink.id)) loss += "Logged Drink Reference"

        return loss
    }

    private suspend fun removeCurrentSessionReference(drink: Drink) {
        mActivity.repository.deleteCurrentSessionByDrinkId(drink.id)
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        internal var name: TextView = itemView.findViewById(R.id.text_manage_db_drink_name)
        internal var abv: TextView = itemView.findViewById(R.id.text_manage_db_drink_abv)
        internal var amount: TextView = itemView.findViewById(R.id.text_manage_db_drink_amount)
        internal var options: ImageView = itemView.findViewById(R.id.text_item_options)
    }
}
