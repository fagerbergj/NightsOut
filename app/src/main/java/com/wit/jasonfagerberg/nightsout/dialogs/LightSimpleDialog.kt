package com.wit.jasonfagerberg.nightsout.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.wit.jasonfagerberg.nightsout.R

class LightSimpleDialog(val context: Context) {
    private var dialogClickListener = DialogInterface.OnClickListener { _, _ -> }
    private var builder = AlertDialog.Builder(context)
    private var showNeutralButton = false

    fun show(message: String, posText: String = "Yes", negText: String = "No", neuText: String = "Neutral") {
        builder.setMessage(message)
        builder.setPositiveButton(posText, dialogClickListener)
        builder.setNegativeButton(negText, dialogClickListener)
        if (showNeutralButton) builder.setNeutralButton(neuText, dialogClickListener)
        val dialog = builder.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
    }

    fun setActions(posAction: () -> Unit, negAction: () -> Unit, neuAction: () -> Unit = {}) {
        dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> posAction.invoke()
                DialogInterface.BUTTON_NEGATIVE -> negAction.invoke()
                DialogInterface.BUTTON_NEUTRAL -> neuAction.invoke()
            }
        }
    }
}