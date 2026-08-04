package com.wit.jasonfagerberg.nightsout.log

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.log.ui.LogCalendarScreen
import com.wit.jasonfagerberg.nightsout.log.ui.LogItem
import com.wit.jasonfagerberg.nightsout.settings.SettingsShim
import com.wit.jasonfagerberg.nightsout.ui.theme.NightsOutTheme
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import com.wit.jasonfagerberg.nightsout.profile.showToast
import com.wit.jasonfagerberg.nightsout.utils.Converter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LogFragment : Fragment() {

    private val converter = Converter()
    private val repository: NightsOutRepository by inject()
    private var selectedDate by mutableIntStateOf(20260101)
    private val logListState = mutableStateListOf<LogItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedDate = converter.currentDateTo8DigitString().toInt()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val themeMode = runCatching { SettingsShim(requireContext()).getString(Constants.PREFERENCE.ACTIVE_THEME_MODE, "light") }.getOrDefault("light")
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                NightsOutTheme(darkMode = themeMode == "dark") {
                    LogCalendarScreen(
                        logList = logListState, selectedDate = selectedDate,
                        onMoveDayRequested = ::onMoveDayConfirmed
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { loadLogData(selectedDate) }
        }

        return composeView
    }

    override fun onResume() { super.onResume(); lifecycleScope.launch { loadLogData(selectedDate) } }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        (requireActivity() as? com.wit.jasonfagerberg.nightsout.main.MainActivity)?.supportActionBar?.title = "Log"
        inflater.inflate(R.menu.log_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_clear_all_logs -> clearAllLogs()
            R.id.btn_clear_selected_day_log -> clearSelectedDayLog()
            R.id.btn_move_selected_log -> onMoveFromMenu(selectedDate)
        }
        return (activity as? android.app.Activity)?.onOptionsItemSelected(item) ?: false
    }

    private fun clearAllLogs() {
        val light = com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog(context!!)
        light.setActions({ lifecycleScope.launch { deleteAllLogHeaders(); logListState.clear(); context?.showToast("All logs cleared") } }, {})
        light.show("Are you sure you want to clear all logs?")
    }

    private suspend fun deleteAllLogHeaders() {
        val headers = repository.pullLogHeaders()
        for (header in headers) repository.deleteLog(header.date)
    }

    private fun clearSelectedDayLog() {
        lifecycleScope.launch { try { repository.deleteLog(selectedDate) } catch (_: Exception) {} ; logListState.clear() }
    }

    private fun onMoveFromMenu(date: Int) {
        lifecycleScope.launch { doCheckAndShowPicker(date) }
    }

    private fun onMoveDayConfirmed(date: Int) {
        lifecycleScope.launch { doCheckAndShowPicker(date) }
    }

    private suspend fun doCheckAndShowPicker(date: Int) {
        val hasLog = try { repository.pullLogHeaders().any { it.date == date } } catch (_: Exception) { false }
        if (hasLog) showDatePicker(date) else context?.showToast("Cannot move empty log")
    }

    private fun showDatePicker(originalDate: Int) {
        val dateStr = originalDate.toString()
        val year = dateStr.substring(0, 4).toIntOrNull() ?: 2026
        val month = dateStr.substring(4, 6).toIntOrNull()?.minus(1) ?: 0
        val dayOfMonth = dateStr.substring(6, 8).toIntOrNull() ?: 1

        DatePickerDialog(requireContext(), { _, newYear, newMonth, newDay ->
            val logDate = converter.yearMonthDayTo8DigitString(newYear, newMonth, newDay).toInt()
            doMoveLog(originalDate, logDate)
        }, year, month, dayOfMonth).apply { setTitle("Move Log On $originalDate"); show() }
    }

    private fun doMoveLog(oldDate: Int, newDate: Int) {
        lifecycleScope.launch {
            val headers = repository.pullLogHeaders()
            val oldHeader = headers.find { it.date == oldDate } ?: run { context?.showToast("Cannot move empty log"); return@launch }
            when (val existingAtNewDate = headers.find { it.date == newDate }) {
                null -> doDoMove(oldHeader, newDate)
                else -> doShowOverrideDialog(oldHeader, newDate, existingAtNewDate)
            }
        }
    }

    private suspend fun doDoMove(oldHeader: LogHeader, newDate: Int) {
        repository.insertRowInLogTable(newDate, oldHeader.bac, oldHeader.duration)
        repository.changeLogDate(oldHeader.date, newDate)
        logListState.clear()
        context?.showToast("Log moved")
    }

    private suspend fun doShowOverrideDialog(oldHeader: LogHeader, newDate: Int, existing: LogHeader) {
        val dialog = SimpleDialog(requireContext(), (requireActivity() as? android.app.Activity)?.layoutInflater ?: layoutInflater)
        dialog.setTitle(resources.getString(R.string.update_log))
        dialog.setBody("There is already a log on ${existing.monthName} ${existing.day},\n" +
                "${existing.year}. Would you like to update the old log?")
        dialog.setNegativeButtonText(resources.getString(R.string.cancel))
        dialog.setNegativeFunction { _ -> /* Auto-dismissed */ }
        dialog.setPositiveButtonText(resources.getString(R.string.update))
        val thisOldHeader = oldHeader; val thisNewDate = newDate; val thisExisting = existing
        dialog.setPositiveFunction { _ ->
            context?.showToast("Log on ${thisExisting.monthName} ${thisExisting.day}, ${thisExisting.year} was updated")
            lifecycleScope.launch { repository.deleteLog(thisNewDate); repository.changeLogDate(thisOldHeader.date, thisNewDate); logListState.clear() }
        }
    }

    private suspend fun loadLogData(date: Int) {
        runCatching {
            logListState.clear()
            val headers = repository.pullLogHeaders()
            val headerIndex = headers.indexOf(LogHeader(date))
            if (headerIndex >= 0) {
                val header = headers[headerIndex]
                logListState.add(LogItem.Header(header))
                val drinks = repository.getLoggedDrinks(header.date)
                for (drink in drinks) logListState.add(LogItem.Drink(drink))
            } else {
                logListState.add(LogItem.Header(LogHeader(date)))
            }
        }.getOrDefault(Unit)
    }
}
