package com.fagerberg.jason.common.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class SimpleDialog(
    private val context: Context,
    layoutInflater: LayoutInflater,
    parent: View
) {
    private val dialog: AlertDialog
    private val posButton: Button
    private val negButton: Button
    private val neuButton: Button

    init {
        val builder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_simple, parent as ViewGroup, false)

        builder.setView(dialogView)
        dialog = builder.create()

        posButton = dialog.findViewById(R.id.btn_simple_dialog_positive)
        negButton = dialog.findViewById(R.id.btn_simple_dialog_negative)
        neuButton = dialog.findViewById(R.id.btn_simple_dialog_neutral)
    }

    fun show(
        title: String,
        body: String,
        positiveButtonText: String = context.getString(R.string.yes),
        positiveAction: (() -> Unit)? = null,
        negativeButtonText: String = context.getString(R.string.no),
        negativeAction: (() -> Unit)? = null,
        neutralButtonText: String = context.getString(R.string.dismiss),
        neutralAction: (() -> Unit)? = null
    ) {
        with(dialog) {
            findViewById<TextView>(R.id.text_simple_dialog_title).text = title
            findViewById<TextView>(R.id.text_simple_dialog_body).text = body
        }
        if (positiveAction != null) {
            posButton.text = positiveButtonText
            posButton.visibility = View.VISIBLE
            posButton.setOnClickListener{ positiveAction.invoke() }
        }
        if (negativeAction != null) {
            negButton.text = negativeButtonText
            negButton.visibility = View.VISIBLE
            negButton.setOnClickListener{ negativeAction.invoke() }
        }
        if (neutralAction != null) {
            neuButton.text = neutralButtonText
            neuButton.visibility = View.VISIBLE
            neuButton.setOnClickListener{ neutralAction.invoke() }
        }
    }

    fun dismiss() {
        dialog.dismiss()
    }
}
