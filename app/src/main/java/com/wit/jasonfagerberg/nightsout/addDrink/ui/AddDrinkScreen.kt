package com.wit.jasonfagerberg.nightsout.addDrink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wit.jasonfagerberg.nightsout.R

private val PrimaryColor = Color(0xFF2196F3)
private val GreenColor = Color(0xFF4CAF50)
private val LightRedColor = Color(0xFFF44336)
private val BlueGrayColor = Color(0xFFB0BEC5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun AddDrinkScreen(viewModel: com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val ingredientsList by viewModel.complexIngredients.collectAsState()
    var complexChecked by remember { mutableStateOf(uiState.complexMode) }
    var showConfirmDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_drink)) },
                navigationIcon = {
                    TextButton(onClick = viewModel::navigateBack) { 
                        Text("\u2190", fontSize = 24.sp) 
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Favorites horizontal list
                if (uiState.favorites.isNotEmpty()) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(stringResource(R.string.favorites_quick_fill), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(modifier = Modifier.fillMaxWidth().background(BlueGrayColor.copy(alpha = 0.3f))) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
                            itemsIndexed(uiState.favorites) { _, drink ->
                                FavoriteDrinkCard(drink) { selected -> viewModel.onDrinkSelected(selected) }
                            }
                        }
                    }
                }

                // Recents horizontal list  
                if (uiState.recents.isNotEmpty()) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(stringResource(R.string.recents_quick_fill), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(modifier = Modifier.fillMaxWidth().background(BlueGrayColor.copy(alpha = 0.3f))) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
                            itemsIndexed(uiState.recents) { _, drink ->
                                RecentDrinkCard(drink) { selected -> viewModel.onDrinkSelected(selected) }
                            }
                        }
                    }
                }

                // Submit + menu
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        TextButton(onClick = { showConfirmDialog = "clear_favorites" }) { 
                            Text(stringResource(R.string.clear_favorites_list)) 
                        }
                        TextButton(onClick = { showConfirmDialog = "clear_recents" }) { 
                            Text(stringResource(R.string.clear_recents_list)) 
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        IconButton(onClick = viewModel::toggleFavorite) {
                            Text(
                                if (uiState.favorited) "\uD83D\uDC96" else "\u2661",
                                fontSize = 28.sp,
                                color = if (uiState.favorited) LightRedColor else Color.Gray
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.submitDrinkAsync() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (uiState.favorited) LightRedColor else GreenColor)
                    ) { 
                        Text(if (uiState.favorited) stringResource(R.string.add_and_favorite) else stringResource(R.string.add), fontSize = 18.sp) 
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Drink name autocomplete
            item {
                var showSuggestions by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = showSuggestions && uiState.searchResults.isNotEmpty(),
                    onExpandedChange = { showSuggestions = it && uiState.searchResults.isNotEmpty() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { text -> 
                            viewModel.onNameChanged(text)
                            viewModel.onSearchChanged(text)
                        },
                        label = { Text(stringResource(R.string.name)) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        supportingText = if ("name" in uiState.inputErrors) { { Text("Invalid input") } } else null,
                        isError = "name" in uiState.inputErrors,
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSuggestions) }
                    )
                    if (showSuggestions && uiState.searchResults.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = showSuggestions,
                            onDismissRequest = { showSuggestions = false }
                        ) {
                            uiState.searchResults.forEach { drink ->
                                DropdownMenuItem(
                                    onClick = { viewModel.onDrinkSelected(drink); showSuggestions = false },
                                    text = {
                                        Column {
                                            Text(drink.name)
                                            if (drink.abv > 0 || drink.amount > 0) {
                                                Text("${"%.1f".format(drink.abv)}% · ${drink.amount} ${drink.measurement}")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ABV input
            item {
                OutlinedTextField(
                    value = uiState.abvText,
                    onValueChange = viewModel::onAbvChanged,
                    label = { Text(stringResource(R.string.abv)) },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = if ("abv" in uiState.inputErrors) { { Text("Invalid input") } } else null,
                    isError = "abv" in uiState.inputErrors,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = { Text("%", modifier = Modifier.padding(end = 8.dp)) }
                )
            }

            // Amount input + measurement dropdown
            item {
                Text(stringResource(R.string.amount), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                var measurementExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = measurementExpanded,
                    onExpandedChange = { measurementExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.amountText,
                        onValueChange = viewModel::onAmountChanged,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        supportingText = if ("amount" in uiState.inputErrors) { { Text("Invalid input") } } else null,
                        isError = "amount" in uiState.inputErrors,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    ExposedDropdownMenu(expanded = measurementExpanded, onDismissRequest = { measurementExpanded = false }) {
                        MeasurementOptions(
                            measurements = LocalContext.current.resources.getStringArray(R.array.volume_measurements)?.toList() ?: emptyList(),
                            onSelect = { meas -> viewModel.onMeasurementChanged(meas); measurementExpanded = false }
                        )
                    }
                }
            }

            // Complex mode checkbox
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = complexChecked, onCheckedChange = {
                        complexChecked = it
                        viewModel.onComplexModeChanged(it)
                    })
                    Text(stringResource(R.string.multiple_alcohol_sources), Modifier.padding(start = 8.dp), fontSize = 18.sp)
                }
            }

            // Complex mode: alcohol source list
            if (complexChecked && ingredientsList.isNotEmpty()) {
                itemsIndexed(ingredientsList) { index, source ->
                    AlcoholSourceCard(source, viewModel, index, ingredientsList.size)
                }
            }

            // Add alcohol source button when in complex mode
            if (complexChecked) {
                item {
                    Button(
                        onClick = { viewModel.addAlcoholSource() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.abvText.isNotBlank() && uiState.amountText.isNotBlank()
                    ) { Text(stringResource(R.string.add_alcohol_source)) }
                }
            }

            // Spacer so content doesn't hide behind bottomBar
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }

    // Confirm dialog
    showConfirmDialog?.let { action ->
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text(if (action == "clear_favorites") "Clear Favorites?" else "Clear Recents?") },
            text = { Text(if (action == "clear_favorites") "Are you sure?" else "Are you sure?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = null
                    if (action == "clear_favorites") viewModel.clearFavoritesAndDB {}
                    else viewModel.clearRecentsAndDB {}
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) { Text("No") }
            }
        )
    }
}

@Composable
private fun MeasurementOptions(measurements: List<String>, onSelect: (String) -> Unit) {
    measurements.forEach { meas ->
        DropdownMenuItem(
            onClick = { onSelect(meas) },
            text = { Text(meas) }
        )
    }
}

@Composable
private fun FavoriteDrinkCard(drink: com.wit.jasonfagerberg.nightsout.models.Drink, onClick: (com.wit.jasonfagerberg.nightsout.models.Drink) -> Unit) {
    Card(
        modifier = Modifier.padding(4.dp).clickable { onClick(drink) },
        colors = CardDefaults.cardColors(containerColor = PrimaryColor.copy(alpha = 0.1f))
    ) {
        Text(drink.name, fontWeight = FontWeight.Medium, modifier = Modifier.padding(8.dp))
    }
}

@Composable
private fun RecentDrinkCard(drink: com.wit.jasonfagerberg.nightsout.models.Drink, onClick: (com.wit.jasonfagerberg.nightsout.models.Drink) -> Unit) {
    Card(
        modifier = Modifier.padding(4.dp).clickable { onClick(drink) },
        colors = CardDefaults.cardColors(containerColor = BlueGrayColor.copy(alpha = 0.3f))
    ) {
        Text(drink.name, fontWeight = FontWeight.Medium, modifier = Modifier.padding(8.dp))
    }
}

@Composable
private fun AlcoholSourceCard(source: com.wit.jasonfagerberg.nightsout.addDrink.AlcoholSource, viewModel: com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkViewModel, index: Int, total: Int) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Remove Alcohol Source?") },
            confirmButton = {
                TextButton(onClick = { viewModel.removeAlcoholSourceAt(index); showConfirm = false }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("No") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { showConfirm = true },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Text("#${index + 1}", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            Text(String.format("%.2f%%", source.abv), modifier = Modifier.weight(1f))
            Text(source.amount.toString(), modifier = Modifier.weight(1f))
            Text(source.measurement, modifier = Modifier.weight(1f))
        }
    }
}
