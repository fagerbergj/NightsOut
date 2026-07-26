package com.wit.jasonfagerberg.nightsout.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.app.DatePickerDialog
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
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import com.wit.jasonfagerberg.nightsout.log.ui.LogCalendarScreen
import com.wit.jasonfagerberg.nightsout.log.ui.LogItem
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import com.wit.jasonfagerberg.nightsout.utils.Converter
import kotlinx.coroutines.launch

class LogFragment : Fragment() {

    private val converter = Converter()
    private lateinit var mMainActivity: MainActivity
    private var selectedDate by mutableIntStateOf(20260101)
    private val logListState = mutableStateListOf<LogItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = requireActivity() as MainActivity
        mMainActivity.logFragment = this
        selectedDate = converter.currentDateTo8DigitString().toInt()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                LogCalendarScreen(
                    logList = logListState,
                    selectedDate = selectedDate,
                    onMoveDayRequested = ::onMoveDayConfirmed
                )
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadLogData(selectedDate)
            }
        }

        return composeView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        mMainActivity.supportActionBar?.title = "Log"
        inflater.inflate(R.menu.log_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_clear_all_logs -> clearAllLogs()
            R.id.btn_clear_selected_day_log -> clearSelectedDayLog()
            R.id.btn_move_selected_log -> onMoveFromMenu(selectedDate)
        }
        return mMainActivity.onOptionsItemSelected(item)
    }

    private fun clearAllLogs() {
        if (mMainActivity.mLogHeaders.isEmpty()) return
        val light = com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog(context!!)
        val posAction = {
            lifecycleScope.launch {
                for (date in mMainActivity.mLogHeaders.map { it.date }) {
                    mMainActivity.repository.deleteLog(date)
                }
            }
            mMainActivity.mLogHeaders.clear()
            logListState.clear()
        }
        light.setActions(posAction, {})
        light.show("Are you sure you want to clear all logs?")
    }

    private fun clearSelectedDayLog() {
        val header = LogHeader(selectedDate)
        if (mMainActivity.mLogHeaders.indexOf(header) == -1) return
        mMainActivity.mLogHeaders.remove(header)
        lifecycleScope.launch { mMainActivity.repository.deleteLog(selectedDate) }
        logListState.clear()
    }

    private fun onMoveFromMenu(date: Int) {
        val header = LogHeader(date)
        if (mMainActivity.mLogHeaders.indexOf(header) == -1) {
            mMainActivity.showToast("Cannot move empty log")
            return
        }
        showDatePicker(date)
    }

    private fun onMoveDayConfirmed(date: Int) {
        val header = LogHeader(date)
        if (mMainActivity.mLogHeaders.indexOf(header) == -1) {
            mMainActivity.showToast("Cannot move empty log")
            return
        }
        showDatePicker(date)
    }

    private fun showDatePicker(originalDate: Int) {
        val calendar = java.util.Calendar.getInstance()
        val dateStr = converter.currentDateTo8DigitString()
        val year = dateStr.substring(0, 4).toIntOrNull() ?: 2026
        val month = dateStr.substring(4, 6).toIntOrNull()?.minus(1) ?: 0
        val dayOfMonth = dateStr.substring(6, 8).toIntOrNull() ?: 1

        val dp = DatePickerDialog(
            requireContext(),
            { _, newYear, newMonth, newDay ->
                val newDateStr = "$newYear${if (newMonth + 1 < 10) "0" else ""}${newMonth + 1}${if (newDay < 10) "0" else ""}$newDay"
                val logDate = newDateStr.toInt()

                val oldHeader = LogHeader(originalDate)
                val testHeader = LogHeader(logDate)

                when {
                    mMainActivity.mLogHeaders.contains(testHeader) -> showOverrideDialog(oldHeader, logDate)
                    else -> moveToNewDate(oldHeader, logDate)
                }
            },
            year,
            month,
            dayOfMonth
        )

        dp.setTitle("Move Log On $originalDate")
    }

    private fun showOverrideDialog(oldHeader: LogHeader, newDate: Int) {
        val existing = mMainActivity.mLogHeaders.find { it.date == newDate } ?: return
        val dialog = SimpleDialog(requireContext(), requireActivity().layoutInflater)
        dialog.setTitle(resources.getString(R.string.update_log))
        dialog.setBody("There is already a log on ${existing.monthName} ${existing.day},\n" +
                "${existing.year}. Would you like to update the old log?")
        dialog.setNegativeButtonText(resources.getString(R.string.cancel))
        dialog.setNegativeFunction { dialog.dismiss() }
        dialog.setPositiveButtonText(resources.getString(R.string.update))
        dialog.setPositiveFunction {
            val oldLogIndex = mMainActivity.mLogHeaders.indexOf(oldHeader)
            if (oldLogIndex >= 0) {
                mMainActivity.mLogHeaders.add(LogHeader(newDate, existing.bac, oldHeader.duration))
                lifecycleScope.launch {
                    mMainActivity.repository.deleteLog(newDate)
                    mMainActivity.repository.changeLogDate(oldHeader.date, newDate)
                }
                mMainActivity.mLogHeaders.removeAt(oldLogIndex)
            }
            logListState.clear()
            val toast = "Log on ${existing.monthName} ${existing.day}, ${existing.year} was updated"
            mMainActivity.showToast(toast)
            dialog.dismiss()
        }
    }

    private fun moveToNewDate(oldHeader: LogHeader, newDate: Int) {
        mMainActivity.mLogHeaders.add(LogHeader(newDate, oldHeader.bac, oldHeader.duration))
        lifecycleScope.launch { mMainActivity.repository.changeLogDate(oldHeader.date, newDate) }
        mMainActivity.mLogHeaders.remove(oldHeader)
        logListState.clear()
    }

    private fun loadLogData(date: Int) {
        logListState.clear()
        val headerIndex = mMainActivity.mLogHeaders.indexOf(LogHeader(date))
        if (headerIndex >= 0) {
            val header = mMainActivity.mLogHeaders[headerIndex]
            logListState.add(LogItem.Header(header))
            lifecycleScope.launch {
                runCatching {
                    mMainActivity.repository.getLoggedDrinks(header.date).forEach { drink ->
                        logListState.add(LogItem.Drink(drink))
                    }
                }.getOrDefault(Unit)
            }
        } else {
            logListState.add(LogItem.Header(LogHeader(date)))
        }
    }
}
