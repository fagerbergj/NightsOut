package com.example.jasonfagerberg.nightsout

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.util.ArrayList

class AddDrinkFragmentFavoritesListAdapter(private val mContext: Context, drinksList: ArrayList<Drink>) :
        RecyclerView.Adapter<AddDrinkFragmentFavoritesListAdapter.ViewHolder>() {
    // vars
    private val mFavoriteDrinksList: MutableList<Drink> = drinksList
    private lateinit var mMainActivity: MainActivity

    // set layout inflater & inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.favorites_item, parent, false)
        mMainActivity = mContext as MainActivity
        return ViewHolder(view)
    }

    // When view is rendered bind the correct holder to it
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mFavoriteDrinksList[position]
        holder.name.text = drink.name
        holder.card.setOnClickListener { _ ->
            val toast = Toast.makeText(mContext, "${holder.name.text} information filled in", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 600)
            toast.show()
            holder.image.setImageResource(R.drawable.favorite_white_24dp)
            mMainActivity.addDrinkFragment.fillViews(drink.name, drink.abv, drink.amount, drink.measurement)
        }

        // remove from favorites on long press
        holder.card.setOnLongClickListener { v: View ->
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        // Yes is pressed
                        // show to user
                        val toast = Toast.makeText(
                                v.context, "${mFavoriteDrinksList[position].name} " +
                                "Removed From Favorites List", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        // actual removal
                        mFavoriteDrinksList.remove(drink)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, mFavoriteDrinksList.size)
                        mMainActivity.addDrinkFragment.showOrHideEmptyTextViews(
                                mMainActivity.addDrinkFragment.view!!)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {/* no action for clicking no */
                    }
                }
            }
            //Build Actual box
            val builder = AlertDialog.Builder(v.context!!)
            builder.setMessage("Remove from favorites?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
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