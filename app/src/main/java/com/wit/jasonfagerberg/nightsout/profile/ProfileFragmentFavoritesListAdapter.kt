package com.wit.jasonfagerberg.nightsout.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import java.util.*

class ProfileFragmentFavoritesListAdapter(private val mContext: Context, drinksList: ArrayList<Drink>) :
        RecyclerView.Adapter<ProfileFragmentFavoritesListAdapter.ViewHolder>() {
    // vars
    private val mFavoriteDrinksList: MutableList<Drink> = drinksList

    // set layout inflater & inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    // When view is rendered bind the correct holder to it
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mFavoriteDrinksList[position]
        holder.name.text = drink.name
        holder.card.setOnClickListener {
            holder.favorited = !holder.favorited

            if (holder.favorited) {
                // make the toast
                (mContext as MainActivity).showToast("${holder.name.text} favorited")
                mFavoriteDrinksList.add(position, drink)
                for (d in mContext.mDrinksList) {
                    if (d == drink) d.favorited = true
                }
                //(mContext as MainActivity).mDatabaseHelper.insertRowInFavoritesTable(drink.name, drink.id)

                holder.image.setImageResource(R.drawable.favorite_white_24dp)
                //mFavoriteDrinksList.add(drink)
            } else {
                (mContext as MainActivity).showToast("${holder.name.text} unfavored")
                for (d in mContext.mDrinksList) {
                    if (d == drink) d.favorited = false
                }
                mFavoriteDrinksList.removeAt(position)
                mContext.mDatabaseHelper.deleteRowsInTable("favorites", "drink_name = \"${drink.name}\"")

                holder.image.setImageResource(R.drawable.favorite_border_white_24dp)
                //mFavoriteDrinksList.remove(drink)
            }
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
        internal var favorited: Boolean = true
    }
}