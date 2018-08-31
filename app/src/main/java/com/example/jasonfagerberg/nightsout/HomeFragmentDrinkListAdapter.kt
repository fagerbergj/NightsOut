package com.example.jasonfagerberg.nightsout

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import java.util.ArrayList


class HomeFragmentDrinkListAdapter(private val mContext: Context, drinksList: ArrayList<Drink>) :
        RecyclerView.Adapter<HomeFragmentDrinkListAdapter.ViewHolder>() {

    // vars
    private val mDrinksList: MutableList<Drink> = drinksList

    // set layout inflater & inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.fragment_home_list_item, parent, false)
        return ViewHolder(view)
    }

    // When view is rendered bind the correct holder to it
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mDrinksList[position]
        if (!drink.image.isEmpty()) {
            holder.image.setImageBitmap(BitmapFactory.decodeByteArray(drink.image, 0,
                    drink.image.size))
        } else {
            holder.image.setImageBitmap(BitmapFactory.decodeResource(mContext.resources,
                    R.drawable.beer))
        }
        holder.name.text = drink.name

        val aav = "AAV: " + "%.1f".format(drink.aav) + "%"
        holder.aav.text = aav
        val amount = "%.1f".format(drink.amount) + drink.measurement
        holder.amount.text = amount

        holder.foreground.setOnClickListener { _ ->
            Toast.makeText(mContext, "Drink: ${holder.name} clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return mDrinksList.size
    }

    // ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var image: ImageView = itemView.findViewById(R.id.image_drink)
        internal var name: TextView = itemView.findViewById(R.id.text_home_drink_name)
        internal var aav: TextView = itemView.findViewById(R.id.text_home_drink_aav)
        internal var amount: TextView = itemView.findViewById(R.id.text_home_drink_amount)
        internal var foreground: LinearLayout = itemView.findViewById(R.id.layout_foreground)

    }

    // swipe remove
    fun removeItem(position: Int) {
        mDrinksList.removeAt(position)
        notifyItemRemoved(position)
    }

    // undo swipe remove
    fun restoreItem(item: Drink, position: Int) {
        mDrinksList.add(position, item)
        // notify item added by position
        notifyItemInserted(position)
    }
}
