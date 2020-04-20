package com.fagerberg.jason.profile.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fagerberg.jason.common.models.Drink
import com.fagerberg.jason.profile.R

class ProfileFragmentFavoritesAdapter(private val context: Context, drinksList: List<Drink>) :
    RecyclerView.Adapter<ProfileFragmentFavoritesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView = itemView.findViewById(R.id.text_profile_favorite_drink_name)
        internal val card: CardView = itemView.findViewById(R.id.card_profile_favorite_item)
        internal var favorited: Boolean = true
    }
}
