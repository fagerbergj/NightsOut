package com.fagerberg.jason.common.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class LightSimpleDialog(
    private val context: Context,
    positiveAction: () -> Unit,
    negativeAction: () -> Unit = { /* default is no op */ },
    neutralAction: () -> Unit = { /* default is no op */ },
    private val showNeutralButton: Boolean = false
) {
    private val dialogClickListener = DialogInterface.OnClickListener { _, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> positiveAction.invoke()
            DialogInterface.BUTTON_NEGATIVE -> negativeAction.invoke()
            DialogInterface.BUTTON_NEUTRAL -> neutralAction.invoke()
        }
    }
    private val builder = AlertDialog.Builder(context)

    fun show(
        body: String,
        positiveButtonText: String = context.getString(R.string.yes),
        negativeButtonText: String = context.getString(R.string.no),
        neutralButtonText: String = context.getString(R.string.dismiss)
    ) {
        builder.setMessage(body)
        builder.setPositiveButton(positiveButtonText, dialogClickListener)
        builder.setNegativeButton(negativeButtonText, dialogClickListener)
        if (showNeutralButton) builder.setNeutralButton(neutralButtonText, dialogClickListener)
        val dialog = builder.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
    }
}

