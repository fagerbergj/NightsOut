package com.fagerberg.jason.profile.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fagerberg.jason.common.models.Drink
import com.fagerberg.jason.profile.R
import com.fagerberg.jason.profile.presenter.ProfileIntent

class ProfileFragmentFavoritesAdapter(private val fragment: ProfileFragment, val drinksList: MutableList<Drink>) :
    RecyclerView.Adapter<ProfileFragmentFavoritesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(fragment.requireContext())
        val view = inflater.inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = drinksList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = drinksList[position]
        holder.name.text = drink.name
        holder.card.setOnClickListener {
            // TODO show simple dialog asking for conformation
            val confirmAction = { fragment.sendIntent(ProfileIntent.RemoveFavorite(drink)) }
        }
    }

    // ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val name: TextView = itemView.findViewById(R.id.text_profile_favorite_drink_name)
        internal val card: CardView = itemView.findViewById(R.id.card_profile_favorite_item)
    }
}
