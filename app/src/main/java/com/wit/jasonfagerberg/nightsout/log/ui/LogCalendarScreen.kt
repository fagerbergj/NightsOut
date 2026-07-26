package com.wit.jasonfagerberg.nightsout.log.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import androidx.compose.ui.res.stringResource
import com.wit.jasonfagerberg.nightsout.R

@Composable
fun LogCalendarScreen(
    logList: List<LogItem>,
    selectedDate: Int,
    onMoveDayRequested: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Calendar row (replaces MaterialCalendarView)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable {
                    onMoveDayRequested(selectedDate)
                }
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = LogHeader(selectedDate).dateString,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 8.dp))

        // Drink list
        if (logList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.you_did_no_drink_this_day))
            }
        } else {
         Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                for (item in logList) {
                    HorizontalDivider()
                    when (item) {
                        is LogItem.Header -> LogHeaderRow(item.logHeader)
                        is LogItem.Drink -> DrinkCard(item.drink)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

// Sealed hierarchy for log list items
sealed interface LogItem {
    data class Header(val logHeader: com.wit.jasonfagerberg.nightsout.models.LogHeader) : LogItem
    data class Drink(val drink: com.wit.jasonfagerberg.nightsout.models.Drink) : LogItem
}
