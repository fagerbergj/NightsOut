package com.example.jasonfagerberg.nightsout

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.support.design.button.MaterialButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import java.util.ArrayList

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
        val amount = "%.1f".format(drink.amount) + " " + drink.measurement
        holder.amount.text = amount

        holder.foreground.setOnClickListener { _ -> showEditRemoveDialog(position) }
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

    // remove
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

        // edit button clicked
        val edit = dialogView.findViewById<TextView>(R.id.text_dialog_edit_drink)
        edit.setOnClickListener{ _ ->
            dialog.dismiss()
            showEditDialog(position)
        }

        //delete button clicked
        val delete = dialogView.findViewById<TextView>(R.id.text_dialog_remove_drink)
        delete.setOnClickListener{ _ ->
            dialog.dismiss()
            removeItem(position)
        }
    }

    private fun showEditDialog(position: Int){
        val drink = mDrinksList[position]
        val builder = AlertDialog.Builder(mContext)
        val dialogView = mMainActivity.layoutInflater.inflate(
                R.layout.fragment_home_edit_dialog, null)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        val editName = dialogView.findViewById<EditText>(R.id.edit_edit_drink_name)
        editName.setText(drink.name)

        val editAAV = dialogView.findViewById<EditText>(R.id.edit_edit_drink_aav)
        editAAV.setText(drink.aav.toString())

        val editAmount = dialogView.findViewById<EditText>(R.id.edit_edit_drink_amount)
        editAmount.setText(drink.amount.toString())

        val dropdown: Spinner = dialogView.findViewById(R.id.spinner_edit_drink_amount)
        val items = arrayOf("oz", "beers", "shots", "wine glasses")
        val adapter = ArrayAdapter(mContext, android.R.layout.simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter
        dropdown.setSelection(items.indexOf(drink.measurement))

        dialog.findViewById<MaterialButton>(R.id.btn_edit_drink_edit).setOnClickListener{ _ ->
            //padd 0's to end
            if(!editAAV.text.isEmpty() && "${editAAV.text}"["${editAAV.text}".length-1] == '.'){
                val padded = "${editAAV.text}0"
                editAAV.setText(padded)
            }

            if(!editAmount.text.isEmpty() && "${editAmount.text}"["${editAmount.text}".length-1] == '.'){
                val padded = "${editAmount.text}0"
                editAmount.setText(padded)
            }

            // set drink to new values
            if(!editName.text.isEmpty()){
                drink.name = editName.text.toString()
            }
            if (!editAAV.text.isEmpty()){
                drink.aav = "${editAAV.text}0".toDouble()
            }
            if (!editAmount.text.isEmpty()){
                drink.amount = "${editAmount.text}0".toDouble()
            }
            drink.measurement = dropdown.selectedItem.toString()
            dialog.dismiss()
            this.notifyItemChanged(position)
        }
    }
}
