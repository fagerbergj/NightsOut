package com.wit.jasonfagerberg.nightsout.manageDB.ui

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.models.Drink

/** Main ManageDB Compose screen: toolbar, search bar, drink list, clean/reset buttons. */
@Composable
fun ManageDBScreen(
    viewModel: ManageDBViewModel,
    onBack: () -> Unit,
    onDeleteConfirmed: (Drink) -> Unit
) {
    val drinks by viewModel.filteredDrinks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fragment_manage_db_name)) },
                navigationIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, "Back",
                        Modifier.padding(start = 12.dp).clickable(onClick = onBack)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {

            // Search bar row
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = searchQuery, onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(Modifier.fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.4f), MaterialTheme.shapes.medium)
                            .padding(horizontal = 16.dp, vertical = 10.dp)) {
                            if (searchQuery.isEmpty()) Text("Search", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            innerTextField()
                        }
                    })
            }

            // Drink list
            LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(drinks) { idx: Int, drink: Drink ->
                    ManageDBDrinkListItem(drink, viewModel, onDeleteConfirmed)
                    if (idx < drinks.size - 1) HorizontalDivider(
                        Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
                }
            }

            // Clean DB button
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
                androidx.compose.material3.TextButton(onClick = { showCleanDialog(context, viewModel) }) {
                    Text(stringResource(id = R.string.clean_db))
                }
            }

            // Reset DB button
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End) {
                androidx.compose.material3.TextButton(onClick = { showResetDialog(context, viewModel) }) {
                    Text(stringResource(id = R.string.reset_db), color = Color.Red)
                }
            }
        }
    }
}

private fun showCleanDialog(ctx: android.content.Context, vm: ManageDBViewModel) {
    AlertDialog.Builder(ctx).run {
        setMessage(ctx.getString(R.string.clean_db) + ": " +
                "\n    Not Currently in Use\n" +
                "    Not in Favorited\n" +
                "    Not Recently Used\n" +
                "    Not Logged")
        setPositiveButton(ctx.getString(R.string.yes)) { _, _ -> vm.cleanDatabase() }
        setNegativeButton(ctx.getString(R.string.no), null)
        show()
    }
}

private fun showResetDialog(ctx: android.content.Context, vm: ManageDBViewModel) {
    AlertDialog.Builder(ctx).run {
        setMessage("Are you sure? You will lose everything.")
        setPositiveButton(ctx.getString(R.string.yes)) { _, _ -> vm.resetDatabase() }
        setNegativeButton(ctx.getString(R.string.no), null)
        show()
    }
}

/** Single drink row with name/abv/amount and trailing more-options menu. */
@Composable
private fun ManageDBDrinkListItem(drink: Drink, vm: ManageDBViewModel, onDeleteConfirmed: (Drink) -> Unit) {
    var expanded by mutableStateOf(false)
    val ctx = LocalContext.current

    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {

        Column(Modifier.weight(1f)) {
            Text(drink.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val abvText = "ABV: %.1f".format(drink.abv) + "%"
            val amtText = "%.1f ".format(drink.amount).trimEnd() + drink.measurement
            Text("$abvText    $amtText", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Box {
            Icon(Icons.Filled.MoreVert, "Options",
                Modifier.size(48.dp).clickable { expanded = true })
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                val favLabel = if (drink.favorited) ctx.getString(R.string.unfavorite_drink)
                    else ctx.getString(R.string.favorite_drink)
                DropdownMenuItem(text = { Text(favLabel) }, onClick = { vm.toggleFavorite(drink); expanded = false })

                val dontSuggest = !drink.recent  // mirror original adapter's dontSuggest
                val sugLabel = if (dontSuggest) ctx.getString(R.string.show_auto_complete_suggestion)
                    else ctx.getString(R.string.hide_auto_complete_suggestion)
                DropdownMenuItem(
                    text = { Text(sugLabel) },
                    onClick = { vm.toggleSuggestion(drink); expanded = false })

                DropdownMenuItem(
                    text = { Text(ctx.getString(R.string.delete_drink)) },
                    onClick = { onDeleteConfirmed(drink); expanded = false })
            }
        }
    }
}
