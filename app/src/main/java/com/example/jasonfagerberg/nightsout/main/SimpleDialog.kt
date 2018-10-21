package com.example.jasonfagerberg.nightsout.main

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.jasonfagerberg.nightsout.R

class SimpleDialog(context: Context, layoutInflater: LayoutInflater) {
    private val dialog: AlertDialog
    private val posButton: Button
    private val negButton: Button
    init {
        val builder = AlertDialog.Builder(context)
        val parent: ViewGroup? = null
        val dialogView = layoutInflater.inflate(R.layout.simple_dialog, parent, false)

        builder.setView(dialogView)
        dialog = builder.create()
        dialog.show()

        posButton = dialog.findViewById(R.id.btn_simple_dialog_positive)
        negButton = dialog.findViewById(R.id.btn_simple_dialog_negative)
    }

    fun setTitle(title: String){
        dialog.findViewById<TextView>(R.id.text_simple_dialog_title).text = title
    }

    fun setBody(body: String){
        dialog.findViewById<TextView>(R.id.text_simple_dialog_body).text = body
    }

    fun setPositiveFunction(listener: (View) -> Unit){
        posButton.setOnClickListener(listener)
    }

    fun setNegativeFunction(listener: (View) -> Unit){
        negButton.setOnClickListener(listener)
    }

    fun setPositiveButtonText(text: String){
        posButton.text = text
    }

    fun setNegativeButtonText(text: String){
        negButton.text = text
    }

    fun dismiss(){
        dialog.dismiss()
    }
}