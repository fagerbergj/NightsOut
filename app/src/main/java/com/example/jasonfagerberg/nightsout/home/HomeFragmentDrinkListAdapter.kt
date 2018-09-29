package com.example.jasonfagerberg.nightsout.home

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.support.design.button.MaterialButton
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.jasonfagerberg.nightsout.main.Drink
import com.example.jasonfagerberg.nightsout.main.MainActivity
import com.example.jasonfagerberg.nightsout.R
import com.example.jasonfagerberg.nightsout.main.Converter
import java.util.*

private const val TAG = "HomeFragmentAdapter"

class HomeFragmentDrinkListAdapter(private val mContext: Context, drinksList: ArrayList<Drink>) :
        RecyclerView.Adapter<HomeFragmentDrinkListAdapter.ViewHolder>() {

    // vars
    private val mDrinksList: MutableList<Drink> = drinksList
    private lateinit var mMainActivity: MainActivity

    // set layout inflater & inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.fragment_home_list_item, parent, false)
        mMainActivity = mContext as MainActivity
        return ViewHolder(view)
    }

    // When view is rendered bind the correct holder to it
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mDrinksList[position]
        // fixme set picture based on abv
        holder.image.setImageBitmap(BitmapFactory.decodeResource(mContext.resources,
                    R.mipmap.beer))
        holder.name.text = drink.name

        val abv = "ABV: " + "%.1f".format(drink.abv) + "%"
        holder.abv.text = abv
        val amount = "%.1f".format(drink.amount) + " " + drink.measurement
        holder.amount.text = amount

        if (drink.favorited) holder.favorited.setImageResource(R.drawable.favorite_red_18dp)
        else holder.favorited.setImageResource(R.drawable.favorite_border_red_18dp)

        holder.foreground.setOnClickListener { _ -> showEditRemoveDialog(position) }
    }

    override fun getItemCount(): Int {
        return mDrinksList.size
    }

    // ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var image: ImageView = itemView.findViewById(R.id.image_drink)
        internal var name: TextView = itemView.findViewById(R.id.text_home_drink_name)
        internal var abv: TextView = itemView.findViewById(R.id.text_home_drink_abv)
        internal var amount: TextView = itemView.findViewById(R.id.text_home_drink_amount)
        internal var foreground: LinearLayout = itemView.findViewById(R.id.layout_foreground)
        internal var favorited: ImageView = itemView.findViewById(R.id.image_home_drink_favored)
    }

    private fun removeItem(position: Int) {
        mDrinksList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mDrinksList.size)
    }

    private fun showEditRemoveDialog(position: Int){
        val drink = mDrinksList[position]
        val builder = AlertDialog.Builder(mContext)
        val dialogView = mMainActivity.layoutInflater.inflate(
                R.layout.fragment_home_edit_or_remove_drink_dialog, null)

        dialogView.findViewById<TextView>(R.id.text_edit_remove_drink_title).text = drink.name

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        // add another button
        val another = dialogView.findViewById<TextView>(R.id.text_dialog_add_another)
        another.setOnClickListener{ _ ->
            onAddAnotherClicked(position)
            dismissDialog(dialog)
        }

        setupFavoritesOption(dialogView, drink, dialog)

        // edit button clicked
        val edit = dialogView.findViewById<TextView>(R.id.text_dialog_edit_drink)
        edit.setOnClickListener{ _ ->
            showEditDialog(position)
            dialog.dismiss()
        }

        //delete button clicked
        val delete = dialogView.findViewById<TextView>(R.id.text_dialog_remove_drink)
        delete.setOnClickListener{ _ ->
            removeItem(position)
            dismissDialog(dialog)
            mMainActivity.homeFragment.showOrHideEmptyListText(mMainActivity.homeFragment.view!!)
        }
    }

    private fun setupFavoritesOption(dialogView: View, drink: Drink, dialog: AlertDialog){
        val favorite = dialogView.findViewById<TextView>(R.id.text_dialog_favorite_drink)
        if (drink.favorited){
            favorite.setText(R.string.text_unfavorite_drink)
            favorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_border_red_18dp,0,0,0)
        }else{
            favorite.setText(R.string.text_favorite_drink)
            favorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_red_18dp,0,0,0)
        }

        favorite.setOnClickListener{ _ ->
            onFavoriteClicked(drink)
            dismissDialog(dialog)
        }
    }

    private fun onAddAnotherClicked(position: Int){
        val d = mDrinksList[position]
        val copy = Drink(d.id, d.name, d.abv, d.amount, d.measurement, d.favorited, d.recent)

        if(position <= mDrinksList.size/2){
            mDrinksList.add(position + 1, copy)
            notifyItemInserted(position + 1)
            notifyItemRangeChanged(position + 1, mDrinksList.size)
        }
        else{
            mDrinksList.add(position, copy)
            notifyItemInserted(position)
            notifyItemRangeChanged(position, mDrinksList.size)
        }
    }

    private fun onFavoriteClicked(drink: Drink){
        drink.favorited = !drink.favorited
        if(drink.favorited) {
            mMainActivity.mFavoritesList.add(drink)
            mMainActivity.mDatabaseHelper.insertRowInFavoritesTable(drink.name, drink.id)
        } else{
            mMainActivity.mFavoritesList.remove(drink)
            mMainActivity.mDatabaseHelper.deleteRowInFavoritesTable(drink.name)
        }

        for (i in mMainActivity.mDrinksList.indices){
            val d = mMainActivity.mDrinksList[i]
            if (d == drink){
                d.favorited = drink.favorited
                notifyItemChanged(i)
            }
        }
    }

    private fun showEditDialog(position: Int){
        val drink = mDrinksList[position]

        val builder = AlertDialog.Builder(mContext)
        val parent:ViewGroup? = null
        val dialogView = mMainActivity.layoutInflater.inflate(
                R.layout.fragment_home_edit_dialog, parent, false)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        val editName = dialogView.findViewById<EditText>(R.id.edit_edit_drink_name)
        editName.setText(drink.name)

        val editABV = dialogView.findViewById<EditText>(R.id.edit_edit_drink_abv)
        editABV.setText(drink.abv.toString())

        val editAmount = dialogView.findViewById<EditText>(R.id.edit_edit_drink_amount)
        editAmount.setText(drink.amount.toString())

        val dropdown: Spinner = dialogView.findViewById(R.id.spinner_edit_drink_amount)
        val items = arrayOf("oz", "beers", "shots", "wine glasses")
        val adapter = ArrayAdapter(mContext, android.R.layout.simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter
        dropdown.setSelection(items.indexOf(drink.measurement))

        dialog.findViewById<MaterialButton>(R.id.btn_edit_drink_edit).setOnClickListener{ _ ->
            onDialogEditClick(drink, editName, editABV, editAmount, dropdown)
            this.notifyItemChanged(position)
            dismissDialog(dialog)
        }
    }

    private fun onDialogEditClick(drink: Drink, editName: EditText, editABV: EditText,
                                  editAmount: EditText, dropdown: Spinner){
        val other = Drink(drink.id, drink.name, drink.abv, drink.amount, drink.measurement, false, false)
        //pad 0s to end
        if(!editABV.text.isEmpty() && "${editABV.text}"["${editABV.text}".length-1] == '.'){
            val padded = "${editABV.text}0"
            editABV.setText(padded)
        }

        if(!editAmount.text.isEmpty() && "${editAmount.text}"["${editAmount.text}".length-1] == '.'){
            val padded = "${editAmount.text}0"
            editAmount.setText(padded)
        }

        // set drink to new values
        if(!editName.text.isEmpty()){
            drink.name = editName.text.toString()
        }
        if (!editABV.text.isEmpty()){
            drink.abv = "${editABV.text}".toDouble()
        }
        if (!editAmount.text.isEmpty()){
            drink.amount = "${editAmount.text}".toDouble()
        }
        drink.measurement = dropdown.selectedItem.toString()

        val foundID = mMainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        val existsInDB = foundID != -1
        drink.id = foundID
        if(!drink.isExactSameDrink(other) && !existsInDB){
            mMainActivity.mDatabaseHelper.insertDrinkIntoDrinksTable(drink)
            drink.id = mMainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        }
    }

    private fun dismissDialog(dialog: AlertDialog){
        mMainActivity.homeFragment.calculateBAC()
        dialog.dismiss()
    }
}
