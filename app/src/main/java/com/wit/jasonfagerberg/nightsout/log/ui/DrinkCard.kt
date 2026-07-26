package com.wit.jasonfagerberg.nightsout.log.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wit.jasonfagerberg.nightsout.models.Drink

@Composable
fun DrinkCard(drink: Drink) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = drink.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Row {
                Text(
                    text = "ABV: ${"%.1f".format(drink.abv)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${"%.1f".format(drink.amount)} ${drink.measurement}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LogHeaderRow(logHeader: com.wit.jasonfagerberg.nightsout.models.LogHeader) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF40C4FF))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(
                text = logHeader.dateString,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Row {
                Text(
                    text = "Duration: ${logHeader.durationString}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Bac: ${"%.3f".format(logHeader.bac)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
