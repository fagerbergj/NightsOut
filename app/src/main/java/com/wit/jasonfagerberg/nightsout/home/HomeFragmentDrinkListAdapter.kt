package com.wit.jasonfagerberg.nightsout.home

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.EditText
import android.widget.Spinner
import android.widget.LinearLayout
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.dialogs.EditDrinkDialog
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity

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

        holder.foreground.setOnClickListener { showEditRemoveDialog(position) }
        holder.foreground.setOnLongClickListener {
            val dialog = LightSimpleDialog(mContext)
            val posAction = {
                mMainActivity.showToast("${drink.name} removed")
                removeItem(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(0, mDrinksList.size)
            }
            dialog.setActions(posAction, {})
            dialog.show("Remove ${drink.name}?")
            true
        }
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

        favorite.setOnClickListener {
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
        another.setOnClickListener {
            onAddAnotherClicked(position)
            dismissDialog(dialog)
        }

        setupFavoritesOption(dialogView, drink, dialog)

        // edit button clicked
        val edit = dialogView.findViewById<TextView>(R.id.text_drink_modify_edit_drink)
        edit.setOnClickListener {
            showEditDialog(position)
            dialog.dismiss()
        }

        // delete button clicked
        val delete = dialogView.findViewById<TextView>(R.id.text_drink_modify_remove_drink)
        delete.setOnClickListener {
            removeItem(position)
            dismissDialog(dialog)
            mMainActivity.homeFragment.showOrHideEmptyListText(mMainActivity.homeFragment.view!!)
        }

        dialogView.findViewById<ImageView>(R.id.imgBtn_drink_modify_close).setOnClickListener { dialog.dismiss() }
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

        val dialog = EditDrinkDialog(mContext, mMainActivity.layoutInflater)
        val titleString = "Edit: ${drink.name}"
        dialog.setTitle(titleString)
        dialog.fillViews(drink.name, drink.abv.toString(), drink.amount.toString(), drink.measurement)
        dialog.setEditOnClickAction {
            onDialogEditClick(drink, dialog.editName, dialog.editAbv, dialog.editAmount, dialog.spinnerMeasurement)
            this.notifyItemChanged(position)
            drink.modifiedTime = mMainActivity.getLongTimeNow()
            mMainActivity.homeFragment.calculateBAC()
            drink.favorited = mMainActivity.mFavoritesList.contains(drink)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun onDialogEditClick(
        drink: Drink,
        editName: EditText,
        editABV: EditText,
        editAmount: EditText,
        dropdown: Spinner
    ) {
        val other = Drink(drink.id, drink.name, drink.abv, drink.amount, drink.measurement,
                false, false, mMainActivity.getLongTimeNow())
        // pad 0s to end
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

        val id = mMainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        drink.id = id

        if (!drink.isExactDrink(other) && !mMainActivity.mDatabaseHelper.idInDb(id)) {
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
