package com.wit.jasonfagerberg.nightsout.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.ui.theme.ThemeShades
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class ProfileActions(
    val context: android.content.Context,
    val favoritesList: MutableList<Drink>,
    val drinksList: MutableList<Drink>,
    val onAddFavorite: () -> Unit,
    val onRemoveFavorite: suspend (Drink) -> Unit,
    val clearFavorites: suspend () -> Unit,
)


val LocalProfileActions = androidx.compose.runtime.compositionLocalOf { ProfileActions(
    context = android.content.ContextWrapper(null as android.content.Context?),
    favoritesList = java.util.ArrayList(),
    drinksList = java.util.ArrayList(),
    onAddFavorite = {},
    onRemoveFavorite = {},
    clearFavorites = {}
)}

fun android.content.Context.showToast(message: String, isLong: Boolean = false) {
    val duration = if (isLong) android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT
    val toast = android.widget.Toast.makeText(this, message, duration)
    toast.setGravity(android.view.Gravity.CENTER, 0, 450)
    toast.show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val acts = LocalProfileActions.current
    val ctx = acts.context
    val scope = rememberCoroutineScope()

    // bumped after a favorites mutation to force recomposition of the plain shared lists below
    var favoritesVersion by remember { mutableStateOf(0) }
    val favoritesSnapshot = remember(favoritesVersion) { acts.favoritesList.toList() }
    val onFavoritesChanged: () -> Unit = { favoritesVersion++ }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            if (uiState.sex != null) {
                androidx.compose.material3.TopAppBar(
                    title = { Text(ctx.getString(R.string.fragment_profile_name)) },
                    actions = { _OverflowMenuButton(ctx, acts, scope, onFavoritesChanged) }
                )
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                _SexRow(uiState, viewModel)
                Spacer(modifier = Modifier.height(24.dp))
                _WeightRow(uiState, viewModel)
      Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = ThemeShades.DividerGray, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
               _FavoritesHeader(ctx)

                HorizontalDivider(color = ThemeShades.DividerGray, modifier = Modifier.fillMaxWidth())

                if (favoritesSnapshot.isEmpty()) {
                    TextButton(onClick = acts.onAddFavorite, modifier = Modifier.padding(vertical = 16.dp)) {
                        androidx.compose.material3.Icon(painterResource(R.drawable.favorite_white_24dp), contentDescription = ctx.getString(R.string.add_favorite))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(ctx.getString(R.string.add_favorite))
                    }
                    Text(
                        text = ctx.getString(R.string.no_favorite_drinks),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                      style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                } else {
                    FavoritesRow(favoritesSnapshot) { drink -> _showRemoveDialog(ctx, acts, scope, drink, onFavoritesChanged) }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.saveProfile(ctx) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).padding(horizontal = 16.dp),
                ) {
                                   Text(ctx.getString(R.string.save_profile), color = ThemeShades.ButtonActiveRed)
                }
            }
        }
    }
}

@Composable
private fun _SexRow(state: ProfileUiState, vm: ProfileViewModel) {
    val ctx = LocalContext.current
    Text(
        text = ctx.getString(R.string.sex),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 24.dp),
        textAlign = TextAlign.Start
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        _SexButton(state.sex == true, "Male") { vm.setSex(true) }
        Box(modifier = Modifier.width(16.dp))
        _SexButton(state.sex == false, "Female") { vm.setSex(false) }
    }
}

@Composable
private fun _SexButton(pressed: Boolean, label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.width(136.dp).height(60.dp)) {
         val bgColor = if (pressed) ThemeShades.ButtonActiveRed else ThemeShades.ButtonInactiveBg
        Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
            Text(label, color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun _WeightRow(state: ProfileUiState, vm: ProfileViewModel) {
    val ctx = LocalContext.current
    Text(text = ctx.getString(R.string.weight), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = state.weightText, onValueChange = vm::setWeightText, modifier = Modifier.weight(3f), singleLine = true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            _UnitChip("lbs", state.weightMeasurement == "lbs") { vm.setWeightMeasurement("lbs") }
            Box(modifier = Modifier.width(4.dp))
            _UnitChip("kg", state.weightMeasurement == "kg") { vm.setWeightMeasurement("kg") }
        }
    }
}

@Composable
private fun _FavoritesHeader(ctx: android.content.Context) {
    Text(text = ctx.getString(R.string.favorites), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), textAlign = TextAlign.Start)
}

@Composable
private fun FavoritesRow(favorites: List<Drink>, onClick: (Drink) -> Unit) {
    val acts = LocalProfileActions.current
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(favorites, key = { it.id }) { drink ->
            Card(
                modifier = Modifier.width(140.dp).padding(8.dp).clickable(onClick = { onClick(drink) }),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text = drink.name, color = Color.White, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

private fun _showRemoveDialog(
    ctx: android.content.Context,
    acts: ProfileActions,
    scope: CoroutineScope,
    drink: Drink,
    onChanged: () -> Unit,
) {
    val dialog = LightSimpleDialog(ctx)
    val posAction: () -> Unit = {
        acts.favoritesList.removeAll { it.id == drink.id }
        for (d in acts.drinksList) if (d.id == drink.id) d.favorited = false
        onChanged()
        scope.launch { acts.onRemoveFavorite(drink) }
    }
    dialog.setActions(posAction = posAction, negAction = {})
    dialog.show("Remove ${drink.name} from favorites list?")
}


private fun _showClearDialog(ctx: android.content.Context, acts: ProfileActions, scope: CoroutineScope, onChanged: () -> Unit) {
    val dialog = LightSimpleDialog(ctx)
    val posAction: () -> Unit = {
        acts.favoritesList.clear()
        for (d in acts.drinksList) d.favorited = false
        onChanged()
        scope.launch { acts.clearFavorites() }
    }
    dialog.setActions(posAction = posAction, negAction = {})
    dialog.show("Are you sure you want to clear all favorites?")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun _OverflowMenuButton(ctx: android.content.Context, acts: ProfileActions, scope: CoroutineScope, onChanged: () -> Unit) {
    val expanded = androidx.compose.runtime.mutableStateOf(false)
    Box(modifier = Modifier.padding(4.dp)) {
        TextButton(onClick = { expanded.value = !expanded.value }) {
            androidx.compose.material3.Icon(painterResource(R.drawable.favorite_white_24dp), contentDescription = ctx.getString(R.string.clear_favorites_list))
        }
        if (expanded.value) {
            DropdownMenu(expanded = true, onDismissRequest = { expanded.value = false }) {
                DropdownMenuItem(text = { Text(ctx.getString(R.string.clear_favorites_list)) }, onClick = {
                    expanded.value = false; _showClearDialog(ctx, acts, scope, onChanged)
                })
            }
        }
    }
}

@Composable
private fun _UnitChip(unit: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) ThemeShades.ButtonActiveRed else ThemeShades.ButtonInactiveBg
    OutlinedButton(onClick = onClick, modifier = Modifier.width(48.dp).height(32.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
            Text(unit.uppercase(), color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}
