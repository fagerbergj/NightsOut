package com.wit.jasonfagerberg.nightsout.log.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private object KoinRepo : KoinComponent {
    val repository: NightsOutRepository get() = get()
}

/** NavHost entry-point for the Log screen. Populates calendar headers and log items from repo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogNavScreen(onBack: () -> Unit = {}) {
    var headers by remember { mutableStateOf<List<LogHeader>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try { headers = KoinRepo.repository.pullLogHeaders().toList() } 
            catch (_: Exception) { headers = emptyList() }
        }
    }

    var selectedDate by remember { mutableIntStateOf(currentDateToInt()) }
    
    // Log items built from headers + drinks, refreshed when date or headers change
    val logItems = remember(selectedDate, headers) { mutableStateListOf<LogItem>() }
    var isLoading by remember(selectedDate) { mutableStateOf(false) }

    LaunchedEffect(selectedDate) {
        if (headers.isEmpty()) return@LaunchedEffect
        isLoading = true
        withContext(Dispatchers.IO) {
            logItems.clear()
            val idx = headers.indexOfFirst { it.date == selectedDate }
            if (idx >= 0) {
                logItems.add(LogItem.Header(headers[idx]))
                val drinks = KoinRepo.repository.getLoggedDrinks(selectedDate)
                drinks.forEach { logItems.add(LogItem.Drink(it)) }
            } else {
                logItems.add(LogItem.Header(LogHeader(selectedDate)))
            }
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                val h = headers.find { it.date == selectedDate } ?: LogHeader(selectedDate)
                Text("${h.monthName} ${h.day}, ${h.year}")
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        LogCalendarScreen(
            logList = logItems.toList(),
            selectedDate = selectedDate,
            onMoveDayRequested = { newDate -> selectedDate = newDate }
        )
    }
}

fun currentDateToInt(): Int = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()).toInt()
