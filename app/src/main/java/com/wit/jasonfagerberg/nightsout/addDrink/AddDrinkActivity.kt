package com.wit.jasonfagerberg.nightsout.addDrink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.core.context.GlobalContext.get
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import android.widget.Toast
import com.wit.jasonfagerberg.nightsout.addDrink.ui.AddDrinkScreen
import com.wit.jasonfagerberg.nightsout.ui.theme.NightsOutTheme

class AddDrinkActivity : ComponentActivity() {

    private val viewModel: AddDrinkViewModel by lazy { get().get() }
    private var canUnfavorite = true
    private var isFavorited = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        canUnfavorite = intent.getBooleanExtra("CAN_UNFAVORITE", true)
        isFavorited = intent.getBooleanExtra("FAVORITED", false)

        viewModel.canUnfavorite = canUnfavorite
        
        viewModel.onDrinkAdded = {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("drinkAdded", true)
            intent.putExtra(Constants.FRAGMENT_ID, if (canUnfavorite) 0 else 2)
            startActivity(intent)
        }
        viewModel.onErrorToast = { msg -> showToast(msg, false) }
        viewModel.onNavigateBack = {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(Constants.FRAGMENT_ID, if (canUnfavorite) 0 else 2)
            startActivity(intent)
        }

        if (isFavorited) {
            viewModel.setFavorited(true)
        }
        viewModel.setVolumeMeasurementLocale()

        setContent {
            NightsOutTheme(darkMode = false) {
                AddDrinkScreen(viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadInitialData()
    }

    private fun showToast(message: String, isLongToast: Boolean) {
        Toast.makeText(this, message, if (isLongToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }
}
