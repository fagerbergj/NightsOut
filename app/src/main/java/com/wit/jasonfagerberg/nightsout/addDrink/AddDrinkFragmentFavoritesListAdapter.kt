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

class AddDrinkFragmentFavoritesListAdapter(private val mContext: Context, drinksList: ArrayList<Drink>) :
        RecyclerView.Adapter<AddDrinkFragmentFavoritesListAdapter.ViewHolder>() {
    private val mFavoriteDrinksList: MutableList<Drink> = drinksList
    private lateinit var mMainActivity: MainActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.item_favorite, parent, false)
        mMainActivity = mContext as MainActivity
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mFavoriteDrinksList[position]
        holder.name.text = drink.name
        holder.card.setOnClickListener {
            mMainActivity.showToast("${holder.name.text} information filled in")
            holder.image.setImageResource(R.drawable.favorite_white_24dp)
            mMainActivity.addDrinkFragment.fillViews(drink.name, drink.abv, drink.amount, drink.measurement)
        }

        // remove from favorites on long press
        holder.card.setOnLongClickListener { v: View ->
            val lightSimpleDialog = LightSimpleDialog(v.context!!)
            val posAction = {
                mMainActivity.showToast("${mFavoriteDrinksList[position].name} Removed From Favorites List")
                mFavoriteDrinksList.remove(drink)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, mFavoriteDrinksList.size)
                mMainActivity.addDrinkFragment.showOrHideEmptyTextViews(mMainActivity.addDrinkFragment.view!!)
                for (d in mMainActivity.mDrinksList) {
                    if (d == drink) d.favorited = false
                }
                mMainActivity.mDatabaseHelper.deleteRowsInTable("favorites", "drink_name = \"${drink.name}\"")
            }
            lightSimpleDialog.setActions(posAction, {})
            lightSimpleDialog.show("Remove from favorites?")
            true
        }
    }

    override fun getItemCount(): Int {
        return mFavoriteDrinksList.size
    }

    // ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView = itemView.findViewById(R.id.text_profile_favorite_drink_name)
        internal val card: CardView = itemView.findViewById(R.id.card_profile_favorite_item)
        internal var image: ImageView = itemView.findViewById(R.id.image_profile_favorite)
    }
}