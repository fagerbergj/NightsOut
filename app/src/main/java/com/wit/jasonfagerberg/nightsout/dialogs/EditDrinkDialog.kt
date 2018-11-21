package com.wit.jasonfagerberg.nightsout.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.ArrayAdapter
import com.google.android.material.button.MaterialButton
import com.wit.jasonfagerberg.nightsout.R
import java.util.Locale

class EditDrinkDialog(private val context: Context, layoutInflater: LayoutInflater) {
    private val dialog: AlertDialog
    private val textTitle: TextView
    val editName: EditText
    val editAbv: EditText
    val editAmount: EditText
    val spinnerMeasurement: Spinner
    private val btnEdit: MaterialButton
    private val btnClose: ImageView

    init {
        val builder = AlertDialog.Builder(context)
        val parent: ViewGroup? = null
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_drink, parent, false)

        builder.setView(dialogView)
        dialog = builder.create()

        textTitle = dialogView.findViewById(R.id.text_edit_drink_title)
        editName = dialogView.findViewById(R.id.edit_edit_drink_name)
        editAbv = dialogView.findViewById(R.id.edit_edit_drink_abv)
        editAmount = dialogView.findViewById(R.id.edit_edit_drink_amount)
        spinnerMeasurement = dialogView.findViewById(R.id.spinner_edit_drink_amount)
        btnEdit = dialogView.findViewById(R.id.btn_edit_drink_edit)
        btnClose = dialogView.findViewById(R.id.imgBtn_edit_drink_close)
        btnClose.setOnClickListener {
            dismiss()
        }
    }

    fun show() {
        dialog.show()
    }

    fun setTitle(title: String) {
        textTitle.text = title
    }

    fun fillViews(name: String, abv: String, amount: String, measure: String) {
        editName.setText(name)
        editAbv.setText(abv)
        editAmount.setText(amount)

        val country = Locale.getDefault().country
        val items = arrayOf("ml", "oz", "beers", "shots", "wine glasses")
        if (country == "US" || country == "LR" || country == "MM") {
            items[0] = "oz"
            items[1] = "ml"
        }
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, items)
        spinnerMeasurement.adapter = adapter
        spinnerMeasurement.setSelection(items.indexOf(measure))
    }

    fun setEditOnClickAction(listener: (View) -> Unit) {
        btnEdit.setOnClickListener(listener)
    }

    fun dismiss() {
        dialog.dismiss()
    }
}