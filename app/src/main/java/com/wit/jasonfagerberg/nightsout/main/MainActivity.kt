package com.wit.jasonfagerberg.nightsout.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wit.jasonfagerberg.nightsout.addDrink.AddDrinkViewModel
import com.wit.jasonfagerberg.nightsout.addDrink.ui.AddDrinkScreen
import com.wit.jasonfagerberg.nightsout.home.HomeViewModel
import com.wit.jasonfagerberg.nightsout.home.ui.HomeScreen
import com.wit.jasonfagerberg.nightsout.log.ui.LogNavScreen
import com.wit.jasonfagerberg.nightsout.manageDB.ui.ManageDBScreen
import com.wit.jasonfagerberg.nightsout.manageDB.ui.ManageDBViewModel
import com.wit.jasonfagerberg.nightsout.profile.LocalProfileActions
import com.wit.jasonfagerberg.nightsout.profile.ProfileActions
import com.wit.jasonfagerberg.nightsout.profile.ProfileScreen
import com.wit.jasonfagerberg.nightsout.profile.ProfileViewModel
import com.wit.jasonfagerberg.nightsout.settings.SettingsRepository
import com.wit.jasonfagerberg.nightsout.settings.SettingsScreen
import com.wit.jasonfagerberg.nightsout.settings.SettingsViewModel
import com.wit.jasonfagerberg.nightsout.ui.theme.NightsOutTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    @Suppress("DEPRECATION") // ponytail: theme_mode is String key; SettingsRepository returns Int — phase 3+
    private val settingsRepo: SettingsRepository by inject()

    private val homeViewModel           : HomeViewModel       by viewModel()
    private val profileViewModel        : ProfileViewModel    by viewModel()
    private val addDrinkViewModel       : AddDrinkViewModel   by viewModel()
    private val manageDBViewModel       : ManageDBViewModel   by viewModel()
    private val settingsViewModel       : SettingsViewModel   by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkMode by settingsViewModel.showDarkMode.collectAsStateWithLifecycle(false)
            
            NightsOutTheme(darkMode = darkMode) {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent() {
        val navController = rememberNavController()
        
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        val showBottomBar = currentRoute in listOf("home", "log", "profile")

        Scaffold(
            topBar = { TopAppBar(title = { Text("Nights Out") }) },
            bottomBar = {
                if (showBottomBar) BottomNav(navController, currentRoute!!)
                else {}
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                NavHost(navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onAddDrinkClicked = { navController.navigate("add_drink/false/false") },
                            onDrinksLoadFailed = {}
                        )
                    }

                    composable("log") {
                        LogNavScreen(onBack = { navController.popBackStack() })
                    }

                    composable("profile") { ProfileRoute(navController) }

                    composable(
                        route = "add_drink/{canUnfavorite}/{favorited}",
                        arguments = listOf(
                            navArgument("canUnfavorite") { type = NavType.BoolType; defaultValue = true },
                            navArgument("favorited") { type = NavType.BoolType; defaultValue = false }
                        )
                    ) { entry ->
                        val canUnfavorite = runCatching { 
                            entry.arguments?.getBoolean("canUnfavorite", true) ?: true 
                        }.getOrElse { true }
                        val fAvorited = runCatching {
                            entry.arguments?.getBoolean("favorited", false) ?: false
                        }.getOrElse { false }

                        addDrinkViewModel.canUnfavorite = canUnfavorite
                        if (fAvorited) addDrinkViewModel.setFavorited(true)
                        AddDrinkScreen(addDrinkViewModel)
                    }

                    composable("settings") { SettingsRoute() }

                    composable("manage_db") {
                        ManageDBScreen(
                            viewModel = manageDBViewModel,
                            onBack = { navController.popBackStack() },
                            onDeleteConfirmed = {}
                        )
                    }
                }
            }
        }

        androidx.activity.compose.BackHandler {
            if (!navController.popBackStack()) {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @Composable
    private fun BottomNav(
        navController: androidx.navigation.NavHostController, 
        currentRoute: String
    ) {
        NavigationBar {
            val items = listOf(
                BottomNavItem.Home, BottomNavItem.Log, BottomNavItem.Profile,
            )
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    },
                    label = { Text(item.label) },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun ProfileRoute(navController: androidx.navigation.NavHostController) {
        val profileInit by settingsRepo.profileInit.collectAsStateWithLifecycle(false)

        if (profileInit) {
            CompositionLocalProvider(LocalProfileActions provides ProfileActions(
                context = LocalContext.current,
                favoritesList = java.util.ArrayList(),
                drinksList = java.util.ArrayList(),
                onAddFavorite = { navController.navigate("add_drink/false/true") },
                onRemoveFavorite = {},
                clearFavorites = {}
            )) {
                ProfileScreen(viewModel = profileViewModel)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Setup your profile to get started", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    @Composable
    private fun SettingsRoute() {
        val scope = lifecycleScope
        val showBac by settingsViewModel.showBacNotification.collectAsStateWithLifecycle()
        val isDarkMode by settingsViewModel.showDarkMode.collectAsStateWithLifecycle()
        val h24 by settingsViewModel.use24HourTime.collectAsStateWithLifecycle()

        // ponytail: onToggleBac/onToggle24h are non-suspend (Boolean) -> Unit; 
        SettingsScreen(
            showBac = showBac,
            isDarkMode = isDarkMode,
            use24h = h24,
            onToggleBac = { enabled ->
                scope.launch {
                    settingsRepo.setShowBacNotification(enabled)
                    if (enabled) {
                        val intent = android.content.Intent(
                            this@MainActivity, com.wit.jasonfagerberg.nightsout.notification.BacNotificationService::class.java).apply {
                                action = com.wit.jasonfagerberg.nightsout.constants.Constants.ACTION.START_SERVICE }
                        startService(intent)
                    } else if (!enabled) {
                        val intent = android.content.Intent(
                            application, com.wit.jasonfagerberg.nightsout.notification.BacNotificationService::class.java).apply {
                                action = com.wit.jasonfagerberg.nightsout.constants.Constants.ACTION.UPDATE_NOTIFICATION }
                        startService(intent)
                    }
                }
            },
            onToggleDarkMode = { toggledOn -> settingsViewModel.toggleDarkTheme(toggledOn) },
            onToggle24h = { use24 -> 
                scope.launch { settingsRepo.setUse24HourTime(use24) }
            },
            onProfileInitCheck = { true }
        )
    }

    override fun onResume() {
        super.onResume()
        mApp?.mCurrentActivity = this
    }

    private var mApp: NightsOutApplication? = null
}

private sealed class BottomNavItem(
    val route: String, 
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    // Log reuses Person icon — matches legacy nav bar where both were Person icons on this app
    data object Log : BottomNavItem("log", "Log", Icons.Filled.Person, Icons.Outlined.Person)
    data object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}
