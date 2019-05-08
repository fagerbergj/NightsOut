package com.wit.jasonfagerberg.nightsout.log

import android.app.DatePickerDialog
import android.view.ContextThemeWrapper
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.converter.Converter
import com.wit.jasonfagerberg.nightsout.databaseHelper.LogDatabaseHelper
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import java.util.*

class LogFragmentDatePicker(
    private val logFragment: LogFragment,
    private val mainActivity: MainActivity,
    private val converter: Converter,
    private val header: LogHeader,
    private val activeTheme: Int
) {
    private val logDatabaseHelper = LogDatabaseHelper(mainActivity.mDatabaseHelper, mainActivity)

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dp = DatePickerDialog(ContextThemeWrapper(logFragment.context!!, Converter().appThemeToDialogTheme[activeTheme]),
                null, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        dp.setButton(DatePickerDialog.BUTTON_POSITIVE, "OK") { _, _ ->

            val logYear = dp.datePicker.year
            val logMonth = dp.datePicker.month
            val logDay = dp.datePicker.dayOfMonth
            val logDate = Integer.parseInt(converter.yearMonthDayTo8DigitString(logYear, logMonth, logDay))

            val testHeader = LogHeader(logDate)
            if (testHeader in mainActivity.mLogHeaders) {
                showOverrideLogDialog(logDate)
            } else {
                mainActivity.mLogHeaders.add(LogHeader(logDate, header.bac, header.duration))
                logDatabaseHelper.changeLogDate(header.date, logDate)
                mainActivity.mLogHeaders.remove(header)
                val message = "Log moved to ${testHeader.monthName} ${testHeader.day}, ${testHeader.year}"
                logFragment.resetCalendar()
                mainActivity.showToast(message)
            }
        }

        dp.setTitle("Move Log On ${header.dateString}")
        dp.show()
    }

    private fun showOverrideLogDialog(logDate: Int) {
        val headerIndex = mainActivity.mLogHeaders.indexOf(LogHeader(logDate))
        val header = mainActivity.mLogHeaders[headerIndex]

        val simpleDialog = SimpleDialog(logFragment.context!!, mainActivity.layoutInflater)

        simpleDialog.setTitle(mainActivity.resources.getString(R.string.update_log))
        var message = "There is already a log on ${header.monthName} ${header.day}," +
                " ${header.year}.\nWould you like to update the old log?"
        simpleDialog.setBody(message)

        simpleDialog.setNegativeButtonText(mainActivity.resources.getString(R.string.cancel))
        simpleDialog.setNegativeFunction {
            simpleDialog.dismiss()
        }

        simpleDialog.setPositiveButtonText(mainActivity.resources.getString(R.string.update))
        simpleDialog.setPositiveFunction {
            logDatabaseHelper.deleteLog(logDate)
            mainActivity.mLogHeaders.add(LogHeader(logDate, header.bac, header.duration))
            logDatabaseHelper.changeLogDate(header.date, logDate)
            mainActivity.mLogHeaders.remove(header)
            message = "Log on ${header.monthName} ${header.day}, ${header.year} was updated"
            mainActivity.showToast(message)
            logFragment.resetCalendar()
            simpleDialog.dismiss()
        }
    }
}