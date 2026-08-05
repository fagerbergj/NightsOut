package com.wit.jasonfagerberg.nightsout.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.utils.Converter
import com.wit.jasonfagerberg.nightsout.constants.Constants

@Composable
fun SettingsScreen(
    showBac: Boolean,
    isDarkMode: Boolean,
    use24h: Boolean,
    onToggleBac: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggle24h: (Boolean) -> Unit,
    onProfileInitCheck: () -> Boolean
) {
    var showBacInfoDialog by remember { mutableStateOf(false) }
    var showTimeInfoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                Spacer(Modifier.weight(1f))
            }
        }

        Card(
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(16.dp))

                PrefRow(
                    iconInfo = true,
                    titleText = "Current BAC Notification",
                    subtitleText = "Show current BAC in the notification tray",
                    checked = showBac,
                    onCheckedChange = onToggleBac,
                    infoOnClick = { showBacInfoDialog = true }
                )

                Divider(
                    modifier = Modifier.padding(start = 56.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(8.dp))

                PrefRow(
                    iconInfo = false,
                    titleText = "Dark Mode",
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )

                Divider(
                    modifier = Modifier.padding(start = 56.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(8.dp))

                PrefRow(
                    iconInfo = true,
                    titleText = "Use 24 Hour Time",
                    subtitleText = getCurrentTimePreview(use24h),
                    checked = use24h,
                    onCheckedChange = onToggle24h,
                    infoOnClick = { showTimeInfoDialog = true }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showBacInfoDialog) BacNotificationDialog(onDismiss = { showBacInfoDialog = false })
    if (showTimeInfoDialog) TimeFormatDialog(onDismiss = { showTimeInfoDialog = false })
}

fun getCurrentTimePreview(use24h: Boolean): String {
    val converter = Converter()
    val minutes = Constants.getCurrentTimeInMinuets() ?: System.currentTimeMillis().run { (this / 60000).toInt() % 1440 }
    return if (use24h) "Current time: ${converter.timeToString(minutes, true)}" else "Current time: ${converter.timeToString(minutes, false)}"
}

@Composable
private fun PrefRow(
    iconInfo: Boolean,
    titleText: String,
    subtitleText: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    infoOnClick: (() -> Unit)? = null
) {
    Row(modifier = Modifier.padding(horizontal = 56.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (iconInfo) Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            BasicText(titleText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface))
            subtitleText?.let { sub ->
                Spacer(Modifier.height(2.dp))
                BasicText(sub, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
        }

        Switch(checked = checked, onCheckedChange = onCheckedChange)


        infoOnClick?.let { onClick ->
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onClick) {
                Icon(Icons.Filled.Info, "Information", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun BacNotificationDialog(onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { BasicText(text = "Current BAC Notification") },
        text = {
            BasicText(
                "This notification appears when the user sets either the start or end time to within 5 minuets of the current time. It is meant to allow the user to see their current BAC without having to open Nights Out.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
            )
        },
        confirmButton = {
            BasicText(
                "Show",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).clickable(onClick = onDismiss),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
            )
        },
        dismissButton = {
            BasicText(
                stringResource(R.string.dismiss),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).clickable(onClick = onDismiss),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    )
}

@Composable
private fun TimeFormatDialog(onDismiss: () -> Unit) {
    val converter = remember { Converter() }
    val minutes = Constants.getCurrentTimeInMinuets() ?: (System.currentTimeMillis() / 60000).toInt() % 1440

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { BasicText(text = "24 Hour Time Format") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                BasicText("12 Hour Time: ${converter.timeToString(minutes, false)}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                BasicText("24 Hour Time: ${converter.timeToString(minutes, true)}", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            BasicText(
                "Dismiss",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).clickable(onClick = onDismiss),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
            )
        }
    )
}
