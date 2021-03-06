package com.wit.jasonfagerberg.nightsout.home

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.dialogs.EditDrinkDialog
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.models.Drink
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
            drink.abv > 20 -> holder.image.setImageBitmap(BitmapFactory.decodeResource(mContext.resources, R.drawable.cocktail))
            drink.abv > 9.5 -> holder.image.setImageBitmap(BitmapFactory.decodeResource(mContext.resources, R.drawable.wine))
            else -> holder.image.setImageBitmap(BitmapFactory
                    .decodeResource(mContext.resources, R.drawable.beer))
        }

        holder.name.text = drink.name

        val abv = "ABV: " + "%.1f".format(drink.abv) + "%"
        holder.abv.text = abv
        val amount = "%.1f".format(drink.amount) + " " + drink.measurement
        holder.amount.text = amount

        if (drink.favorited) holder.favorited.setImageResource(R.drawable.favorite_red_18dp)
        else holder.favorited.setImageResource(R.drawable.favorite_border_red_18dp)

        holder.foreground.setOnClickListener { showEditRemoveDialog(position) }
        holder.foreground.setOnLongClickListener { showEditRemoveDialog(position); true }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.image.setImageDrawable(null)
        super.onViewRecycled(holder)
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
            mMainActivity.setPreference(drinksAddedCount = mMainActivity.drinksAddedCount + 1)
            mMainActivity.showPleaseRateDialog()
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
        }

        dialogView.findViewById<ImageView>(R.id.imgBtn_drink_modify_close).setOnClickListener { dialog.dismiss() }
    }

    private fun onAddAnotherClicked(position: Int) {
        val d = mDrinksList[position]
        val copy = Drink(d.id, d.name, d.abv, d.amount, d.measurement, d.favorited, d.recent, d.modifiedTime)

        if (position <= mDrinksList.size / 2) {
            mDrinksList.add(position, copy)
            notifyItemInserted(position)
            notifyItemRangeChanged(position, mDrinksList.size)
            mMainActivity.mDatabaseHelper.insertRowInCurrentSessionTable(copy.id, position + 1)

        } else {
            mDrinksList.add(position, copy)
            notifyItemInserted(position)
            notifyItemRangeChanged(position, mDrinksList.size)
            mMainActivity.mDatabaseHelper.insertRowInCurrentSessionTable(copy.id, position)
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

    fun removeItem(position: Int) {
        if (position >= mMainActivity.mDrinksList.size) return
        val deletedDrink = mMainActivity.mDrinksList[position]
        val snackbar = Snackbar.make(mMainActivity.findViewById(R.id.placeSnackBar), "${mMainActivity.mDrinksList[position].name} removed", Snackbar.LENGTH_LONG)
        val undoAction: (v: View) -> Unit = {
            mMainActivity.mDrinksList.add(position, deletedDrink)
            mMainActivity.mDatabaseHelper.insertRowInCurrentSessionTable(deletedDrink.id, position)
            notifyItemInserted(position)
            notifyItemRangeChanged(position, mMainActivity.mDrinksList.size)
            mMainActivity.homeFragment.showOrHideEmptyListText(mMainActivity.homeFragment.view!!)
            mMainActivity.homeFragment.updateBACText(mMainActivity.homeFragment.calculateBAC())
        }
        snackbar.setAction("Undo", undoAction)
        snackbar.setActionTextColor(ContextCompat.getColor(mMainActivity.homeFragment.context!!, R.color.colorWhite))
        mDrinksList.removeAt(position)
        mMainActivity.mDatabaseHelper.deleteRowsInTable("current_session_drinks", "position=$position")
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mDrinksList.size)
        mMainActivity.homeFragment.updateBACText(mMainActivity.homeFragment.calculateBAC())
        mMainActivity.homeFragment.showOrHideEmptyListText(mMainActivity.homeFragment.view!!)
        snackbar.show()
    }

    private fun showEditDialog(position: Int) {
        val drink = mDrinksList[position]

        val dialog = EditDrinkDialog(mContext, mMainActivity.layoutInflater)
        val titleString = "Edit: ${drink.name}"
        dialog.setTitle(titleString)
        dialog.fillViews(drink.name, drink.abv.toString(), drink.amount.toString(), drink.measurement)
        dialog.setEditOnClickAction {
            onDialogEditClick(drink, dialog.editName, dialog.editAbv, dialog.editAmount, dialog.spinnerMeasurement, position)
            this.notifyItemChanged(position)
            drink.modifiedTime = Constants.getLongTimeNow()
            mMainActivity.homeFragment.updateBACText(mMainActivity.homeFragment.calculateBAC())
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
            dropdown: Spinner,
            position: Int
    ) {
        val other = Drink(drink.id, drink.name, drink.abv, drink.amount, drink.measurement,
                favorited = false, recent = false, modifiedTime = Constants.getLongTimeNow())
        // pad 0s to end
        if (editABV.text.isNotEmpty() && "${editABV.text}"["${editABV.text}".length - 1] == '.') {
            val padded = "${editABV.text}0"
            editABV.setText(padded)
        }

        if (editAmount.text.isNotEmpty() && "${editAmount.text}"["${editAmount.text}".length - 1] == '.') {
            val padded = "${editAmount.text}0"
            editAmount.setText(padded)
        }

        // set drink to new values
        if (editName.text.isNotEmpty()) {
            drink.name = editName.text.toString()
        }
        if (editABV.text.isNotEmpty()) {
            drink.abv = "${editABV.text}".toDouble()
        }
        if (editAmount.text.isNotEmpty()) {
            drink.amount = "${editAmount.text}".toDouble()
        }
        drink.measurement = dropdown.selectedItem.toString()

        val id = mMainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        drink.id = id
        mMainActivity.mDatabaseHelper.deleteRowsInTable("current_session_drinks", "position=$position")

        if (!drink.isExactDrink(other) && !mMainActivity.mDatabaseHelper.idInDb(id)) {
            if (drink.name != other.name) drink.favorited = false
            mMainActivity.mDatabaseHelper.insertDrinkIntoDrinksTable(drink)
            drink.id = mMainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        }
        mMainActivity.mDatabaseHelper.updateDrinkModifiedTime(drink.id, drink.modifiedTime)
        mMainActivity.mDatabaseHelper.insertRowInCurrentSessionTable(drink.id, position)
    }

    private fun dismissDialog(dialog: AlertDialog) {
        mMainActivity.homeFragment.updateBACText(mMainActivity.homeFragment.calculateBAC())
        dialog.dismiss()
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        internal var image: ImageView = itemView.findViewById(R.id.image_drink)
        internal var name: TextView = itemView.findViewById(R.id.text_home_drink_name)
        internal var abv: TextView = itemView.findViewById(R.id.text_home_drink_abv)
        internal var amount: TextView = itemView.findViewById(R.id.text_home_drink_amount)
        internal var foreground: ConstraintLayout = itemView.findViewById(R.id.layout_foreground)
        internal var favorited: ImageView = itemView.findViewById(R.id.image_home_drink_favored)
    }
}
