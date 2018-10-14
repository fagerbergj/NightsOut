package com.example.jasonfagerberg.nightsout.home

import android.app.DatePickerDialog
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.jasonfagerberg.nightsout.R
import com.example.jasonfagerberg.nightsout.log.LogHeader
import com.example.jasonfagerberg.nightsout.converter.Converter
import com.example.jasonfagerberg.nightsout.databaseHelper.LogDatabaseHelper
import com.example.jasonfagerberg.nightsout.main.MainActivity
import java.util.*

class HomeFragmentLogDatePicker(private val homeFragment: HomeFragment,
                                private val mainActivity: MainActivity, private val converter: Converter) {
    private val logDatabaseHelper = LogDatabaseHelper(mainActivity.mDatabaseHelper, mainActivity)

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dp = DatePickerDialog(homeFragment.context!!, null, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        dp.setButton(DatePickerDialog.BUTTON_POSITIVE, "OK") { _, _ ->
            val logYear = dp.datePicker.year
            val logMonth = dp.datePicker.month
            val logDay = dp.datePicker.dayOfMonth
            val logDate = Integer.parseInt(converter.yearMonthDayTo8DigitString(logYear, logMonth, logDay))

            val testHeader = LogHeader(logDate, 0.0, 0.0)
            if (testHeader in mainActivity.mLogHeaders) {
                showOverrideLogDialog(logDate)
            } else {
                mainActivity.mLogHeaders.add(LogHeader(logDate, homeFragment.bac, homeFragment.drinkingDuration))
                logDatabaseHelper.pushDrinksToLogDrinks(logDate)
                val message = "Log created on ${testHeader.monthName} ${testHeader.day}, ${testHeader.year}"
                mainActivity.showToast(message)
            }
        }

        dp.setTitle("Log Day")
        dp.show()
    }

    private fun showOverrideLogDialog(logDate: Int) {
        val headerIndex = mainActivity.mLogHeaders.indexOf(LogHeader(logDate, 0.0, 0.0))
        val header = mainActivity.mLogHeaders[headerIndex]

        val builder = android.app.AlertDialog.Builder(homeFragment.context)
        val parent: ViewGroup? = null
        val dialogView = mainActivity.layoutInflater
                .inflate(R.layout.fragment_home_dialog_update_log, parent, false)
        var message = "There is already a log on ${header.monthName} ${header.day}," +
                " ${header.year}.\nWould you like to update the old log?"
        dialogView.findViewById<TextView>(R.id.text_update_log_body).text = message

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()

        dialogView.findViewById<Button>(R.id.btn_update_log_cancel).setOnClickListener { _ ->
            showDatePicker()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_update_log_update).setOnClickListener { _ ->
            logDatabaseHelper.deleteLog(header.date)
            mainActivity.mLogHeaders[headerIndex] = LogHeader(header.date, homeFragment.bac, homeFragment.drinkingDuration)
            homeFragment.mDrinkListAdapter.notifyDataSetChanged()
            logDatabaseHelper.pushDrinksToLogDrinks(header.date)
            message = "Log on ${header.monthName} ${header.day}," +
                    " ${header.year} was updated"
            mainActivity.showToast(message)
            dialog.dismiss()
        }
    }
}