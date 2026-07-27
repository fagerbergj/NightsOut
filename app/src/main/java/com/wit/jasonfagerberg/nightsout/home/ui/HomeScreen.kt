package com.wit.jasonfagerberg.nightsout.home.ui

import android.app.TimePickerDialog
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.home.BacState
import com.wit.jasonfagerberg.nightsout.home.TimeSettings
import com.wit.jasonfagerberg.nightsout.home.HomeViewModel
import com.wit.jasonfagerberg.nightsout.models.Drink
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    drinks: List<Drink>,
    bacValue: Double,
    bacState: BacState,
    timeSettings: TimeSettings,
    onAddDrinkClicked: () -> Unit,
    onStartTimeChanged: (Int) -> Unit,
    onEndTimeChanged: (Int) -> Unit,
    onDeleteDrinkAt: (Int) -> Unit,
    onFavoriteToggle: (Drink) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        TimePickerDialogWrapper(
            context = LocalContext.current,
            initialHour = convertToHours(timeSettings.startTimeMin),
            initialMinute = convertToMinutes(timeSettings.startTimeMin),
            use24HourTime = timeSettings.use24HourTime,
            onTimeSet = { hour, minute ->
                onStartTimeChanged(hour * 60 + minute)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        TimePickerDialogWrapper(
            context = LocalContext.current,
            initialHour = convertToHours(timeSettings.endTimeMin),
            initialMinute = convertToMinutes(timeSettings.endTimeMin),
            use24HourTime = timeSettings.use24HourTime,
            onTimeSet = { hour, minute ->
                onEndTimeChanged(hour * 60 + minute)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onAddDrinkClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
        ) {
            Text(stringResource(R.string.add_drink), fontSize = 18.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.start_drinking),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = formatTime(timeSettings.startTimeMin, timeSettings.use24HourTime),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(0.5f),
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.end_drinking),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = formatTime(timeSettings.endTimeMin, timeSettings.use24HourTime),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(0.5f),
                    enabled = false,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

        if (drinks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_drinks_yet),
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(drinks, key = { _, drink -> drink.id }) { index, drink ->
                    DrinkListItem(
                        drink = drink,
                        onDelete = { onDeleteDrinkAt(index) },
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

        BacReadout(bacState = bacState, bacValue = bacValue)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun DrinkListItem(
    drink: Drink,
    onDelete: () -> Unit,
    onFavoriteToggle: (Drink) -> Unit
) {
    SwipeToDismissBox(
        state = rememberSwipeToDismissBoxState(),
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Delete", color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable(onClick = onDelete)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageRes = when {
                    drink.abv > 20 -> R.drawable.cocktail
                    drink.abv > 9.5 -> R.drawable.wine
                    else -> R.drawable.beer
                }

                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(62.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = drink.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        val favIconRes = if (drink.favorited) {
                            R.drawable.favorite_red_18dp
                        } else {
                            R.drawable.favorite_border_red_18dp
                        }
                        Image(
                            painter = painterResource(favIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clickable { onFavoriteToggle(drink) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        Text(
                            text = "ABV: ${"%.1f".format(drink.abv)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${"%.1f".format(drink.amount)} ${drink.measurement}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogWrapper(
    context: android.content.Context,
    initialHour: Int,
    initialMinute: Int,
    use24HourTime: Boolean,
    onTimeSet: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val activity = findActivity(context)
    if (activity != null) {
        val converter = com.wit.jasonfagerberg.nightsout.utils.Converter()
        val themeRes = converter.appThemeToDialogTheme[
            (activity as? com.wit.jasonfagerberg.nightsout.main.MainActivity)?.activeTheme
                ?: R.style.AppTheme
        ]
        TimePickerDialog(
            android.view.ContextThemeWrapper(context, themeRes),
            TimePickerDialog.OnTimeSetListener { _, hour, minute -> onTimeSet(hour, minute) },
            initialHour, initialMinute, use24HourTime
        ).apply {
            setButton(TimePickerDialog.BUTTON_NEUTRAL, "Now") { _, _ ->
                val now = Calendar.getInstance()
                onTimeSet(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
                dismiss()
            }
            show()
        }
    }
}

@Composable
fun BacReadout(
    bacState: BacState,
    bacValue: Double
) {
    val formattedBac = "%.3f".format(bacValue)
    val statusText = when (bacState) {
        BacState.Dead -> "Dead"
        BacState.InDanger -> "In Danger"
        BacState.ShitFaced -> "Shit Faced"
        BacState.Drunk -> "Drunk"
        BacState.Tipsy -> "Tipsy"
        BacState.Sober -> "Sober"
    }

    val bacColor = when (bacState) {
        BacState.Dead, BacState.InDanger -> Color(0xFF000000)
        BacState.ShitFaced -> Color(0xFFF44336)
        BacState.Drunk -> Color(0xFFFF9800)
        BacState.Tipsy -> Color(0xFFCDDC39)
        BacState.Sober -> Color(0xFF4CAF50)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedBac,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = bacColor
            )
        }
        Text(
            text = statusText,
            fontSize = 18.sp,
            color = bacColor,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

private fun findActivity(context: android.content.Context): android.app.Activity? {
    var ctx = context
    while (ctx is ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun convertToMinutes(timeMin: Int): Int = if (timeMin == -1) 0 else timeMin % 60
private fun convertToHours(timeMin: Int): Int = if (timeMin == -1) 0 else timeMin / 60

private fun formatTime(timeMin: Int, use24HourTime: Boolean): String {
    if (timeMin == -1) return ""
    val converter = com.wit.jasonfagerberg.nightsout.utils.Converter()
    return converter.timeToString(timeMin, use24HourTime)
}
