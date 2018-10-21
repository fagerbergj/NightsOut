package com.example.jasonfagerberg.nightsout.main

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class LightSimpleDialog(private val context: Context) {
    private var dialogClickListener = DialogInterface.OnClickListener { _ , _ ->  }
    private var builder = AlertDialog.Builder(context)

    fun show(message: String, posText: String = "Yes", negText: String = "No"){
        builder.setMessage(message)
        builder.setPositiveButton(posText, dialogClickListener)
        builder.setNegativeButton(negText, dialogClickListener)
        builder.show()
    }

    fun setActions(posAction: () -> Unit, negAction: () -> Unit){
        dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> posAction.invoke()
                DialogInterface.BUTTON_NEGATIVE -> negAction.invoke()
            }
        }
    }
}