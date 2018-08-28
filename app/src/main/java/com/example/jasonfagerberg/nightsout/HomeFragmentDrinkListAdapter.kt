package com.example.jasonfagerberg.nightsout

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import java.util.ArrayList


class HomeFragmentDrinkListAdapter//constructor
(private val mContext: Context, drinksList: ArrayList<Drink>) : RecyclerView.Adapter<HomeFragmentDrinkListAdapter.ViewHolder>() {

    //vars
    private val drinksList: MutableList<Drink>

    private val mainActivity: MainActivity

    init {
        this.drinksList = drinksList
        mainActivity = mContext as MainActivity
    }

    //set layout inflater & inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.fragment_home_list_item, parent, false)
        return ViewHolder(view)
    }

    //When view is rendered bind the correct holder to it
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = drinksList[position]
        if (!drink.image.isEmpty()) {
            holder.image.setImageBitmap(BitmapFactory.decodeByteArray(drink.image, 0, drink.image.size))
        } else {
            holder.image.setImageBitmap(BitmapFactory.decodeResource(mContext.resources, R.drawable.beer))
        }
        holder.name.text = drink.name

        val aav = "AAV: " + "%.1f".format(drink.aav) + "%"
        holder.aav.text = aav
        val amount = "%.1f".format(drink.amount) + drink.measurement
        holder.amount.text = amount

        holder.foreground.setOnClickListener { v: View ->
        }
    }

    override fun getItemCount(): Int {
        return drinksList.size
    }

    //ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var image: ImageView
        internal var name: TextView
        internal var aav: TextView
        internal var amount: TextView
        internal var foreground: LinearLayout
        internal var background: LinearLayout

        init {
            image = itemView.findViewById(R.id.image_drink)
            name = itemView.findViewById(R.id.drink_name)
            aav = itemView.findViewById(R.id.drink_aav)
            amount = itemView.findViewById(R.id.drink_amount)

            foreground = itemView.findViewById(R.id.layout_foreground)
            background = itemView.findViewById(R.id.layout_background)
        }

    }

    //swipe remove
    fun removeItem(position: Int) {
        drinksList.removeAt(position)
        notifyItemRemoved(position)
    }

    //undo swipe remove
    fun restoreItem(item: Drink, position: Int) {
        drinksList.add(position, item)
        // notify item added by position
        notifyItemInserted(position)
    }
}
