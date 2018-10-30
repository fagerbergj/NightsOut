package com.wit.jasonfagerberg.nightsout.home

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import com.google.android.material.button.MaterialButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import java.util.*

class HomeFragmentDrinkListAdapter(private val mContext: Context, drinksList: ArrayList<Drink>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<HomeFragmentDrinkListAdapter.ViewHolder>() {

    private val mDrinksList: MutableList<Drink> = drinksList
    private lateinit var mMainActivity: MainActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.fragment_home_list_item, parent, false)
        mMainActivity = mContext as MainActivity
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mDrinksList[position]

        when {
            drink.abv > 20 -> holder.image.setImageBitmap(BitmapFactory.decodeResource(mContext.resources, R.mipmap.cocktail))

            drink.abv > 9.5 -> holder.image.setImageBitmap(BitmapFactory.decodeResource(mContext.resources, R.mipmap.wine))

            else -> holder.image.setImageBitmap(BitmapFactory
                    .decodeResource(mContext.resources, R.mipmap.beer))
        }

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

    private fun setupFavoritesOption(dialogView: View, drink: Drink, dialog: AlertDialog) {
        val favorite = dialogView.findViewById<TextView>(R.id.text_drink_modify_favorite_drink)
        if (drink.favorited) {
            favorite.setText(R.string.unfavorite_drink)
            favorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_border_red_18dp, 0, 0, 0)
        } else {
            favorite.setText(R.string.favorite_drink)
            favorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_red_18dp, 0, 0, 0)
        }

        favorite.setOnClickListener { _ ->
            onFavoriteClicked(drink)
            dismissDialog(dialog)
        }
    }

    private fun showEditRemoveDialog(position: Int) {
        val drink = mDrinksList[position]
        val builder = AlertDialog.Builder(mContext)
        val parent: ViewGroup? = null
        val dialogView = mMainActivity.layoutInflater.inflate(R.layout.fragment_home_dialog_drink_modify, parent)

        dialogView.findViewById<TextView>(R.id.text_drink_modify_title).text = drink.name

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        // add another button
        val another = dialogView.findViewById<TextView>(R.id.text_drink_modify_add_another)
        another.setOnClickListener { _ ->
            onAddAnotherClicked(position)
            dismissDialog(dialog)
        }

        setupFavoritesOption(dialogView, drink, dialog)

        // edit button clicked
        val edit = dialogView.findViewById<TextView>(R.id.text_drink_modify_edit_drink)
        edit.setOnClickListener { _ ->
            showEditDialog(position)
            dialog.dismiss()
        }

        //delete button clicked
        val delete = dialogView.findViewById<TextView>(R.id.text_drink_modify_remove_drink)
        delete.setOnClickListener { _ ->
            removeItem(position)
            dismissDialog(dialog)
            mMainActivity.homeFragment.showOrHideEmptyListText(mMainActivity.homeFragment.view!!)
        }
    }

    private fun onAddAnotherClicked(position: Int) {
        val d = mDrinksList[position]
        val copy = Drink(d.id, d.name, d.abv, d.amount, d.measurement, d.favorited, d.recent, d.modifiedTime)

        if (position <= mDrinksList.size / 2) {
            mDrinksList.add(position + 1, copy)
            notifyItemInserted(position + 1)
            notifyItemRangeChanged(position + 1, mDrinksList.size)
        } else {
            mDrinksList.add(position, copy)
            notifyItemInserted(position)
            notifyItemRangeChanged(position, mDrinksList.size)
        }
    }

    private fun onFavoriteClicked(drink: Drink) {
        drink.favorited = !drink.favorited
        if (drink.favorited) {
            mMainActivity.mFavoritesList.add(drink)
            mMainActivity.mDatabaseHelper.insertRowInFavoritesTable(drink.name, drink.id)
        } else {
            mMainActivity.mFavoritesList.remove(drink)
            mMainActivity.mDatabaseHelper.deleteRowsInTable("favorites", "drink_name = \"${drink.name}\"")
        }

        for (i in mMainActivity.mDrinksList.indices) {
            val d = mMainActivity.mDrinksList[i]
            if (d == drink) {
                d.favorited = drink.favorited
                notifyItemChanged(i)
            }
        }
    }

    private fun removeItem(position: Int) {
        mDrinksList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mDrinksList.size)
    }

    private fun showEditDialog(position: Int) {
        val drink = mDrinksList[position]

        val builder = AlertDialog.Builder(mContext)
        val parent: ViewGroup? = null
        val dialogView = mMainActivity.layoutInflater
                .inflate(R.layout.fragment_home_dialog_edit, parent, false)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        val title = dialogView.findViewById<TextView>(R.id.text_edit_drink_title)
        val titleString = "Edit: ${drink.name}"
        title.text = titleString

        val editName = dialogView.findViewById<EditText>(R.id.edit_edit_drink_name)
        editName.setText(drink.name)

        val editABV = dialogView.findViewById<EditText>(R.id.edit_edit_drink_abv)
        editABV.setText(drink.abv.toString())

        val editAmount = dialogView.findViewById<EditText>(R.id.edit_edit_drink_amount)
        editAmount.setText(drink.amount.toString())

        val dropdown: Spinner = dialogView.findViewById(R.id.spinner_edit_drink_amount)
        val country = Locale.getDefault().country
        val items = arrayOf("ml", "oz", "beers", "shots", "wine glasses")
        if (country == "US" || country == "LR" || country == "MM") {
            items[0] = "oz"
            items[1] = "ml"
        }
        val adapter = ArrayAdapter(mContext, android.R.layout.simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter
        dropdown.setSelection(items.indexOf(drink.measurement))

        dialog.findViewById<MaterialButton>(R.id.btn_edit_drink_edit).setOnClickListener { _ ->
            onDialogEditClick(drink, editName, editABV, editAmount, dropdown)
            this.notifyItemChanged(position)
            drink.modifiedTime = mMainActivity.getLongTimeNow()
            dismissDialog(dialog)
        }
    }


    private fun onDialogEditClick(drink: Drink, editName: EditText, editABV: EditText,
                                  editAmount: EditText, dropdown: Spinner) {
        val other = Drink(drink.id, drink.name, drink.abv, drink.amount, drink.measurement,
                false, false, mMainActivity.getLongTimeNow())
        //pad 0s to end
        if (!editABV.text.isEmpty() && "${editABV.text}"["${editABV.text}".length - 1] == '.') {
            val padded = "${editABV.text}0"
            editABV.setText(padded)
        }

        if (!editAmount.text.isEmpty() && "${editAmount.text}"["${editAmount.text}".length - 1] == '.') {
            val padded = "${editAmount.text}0"
            editAmount.setText(padded)
        }

        // set drink to new values
        if (!editName.text.isEmpty()) {
            drink.name = editName.text.toString()
        }
        if (!editABV.text.isEmpty()) {
            drink.abv = "${editABV.text}".toDouble()
        }
        if (!editAmount.text.isEmpty()) {
            drink.amount = "${editAmount.text}".toDouble()
        }
        drink.measurement = dropdown.selectedItem.toString()

        val foundID = mMainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        val existsInDB = foundID != -1
        drink.id = foundID
        if (!drink.isExactSameDrink(other) && !existsInDB) {
            if (drink.name != other.name) drink.favorited = false
            mMainActivity.mDatabaseHelper.insertDrinkIntoDrinksTable(drink)
            drink.id = mMainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        }
        mMainActivity.mDatabaseHelper.updateDrinkModifiedTime(drink.id, drink.modifiedTime)
    }

    private fun dismissDialog(dialog: AlertDialog) {
        mMainActivity.homeFragment.calculateBAC()
        dialog.dismiss()
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        internal var image: ImageView = itemView.findViewById(R.id.image_drink)
        internal var name: TextView = itemView.findViewById(R.id.text_home_drink_name)
        internal var abv: TextView = itemView.findViewById(R.id.text_home_drink_abv)
        internal var amount: TextView = itemView.findViewById(R.id.text_home_drink_amount)
        internal var foreground: LinearLayout = itemView.findViewById(R.id.layout_foreground)
        internal var favorited: ImageView = itemView.findViewById(R.id.image_home_drink_favored)
    }
}
