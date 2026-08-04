package com.wit.jasonfagerberg.nightsout.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NightsOutTheme(
    darkMode: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dynamicColor ->
            if (darkMode || isSystemInDarkTheme())
                dynamicDarkColorScheme(LocalContext.current)
            else
                dynamicLightColorScheme(LocalContext.current)
        darkMode  -> DarkColors
        else      -> LightColors
    }

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        typography  = ComposeTypography(),
        shapes      = ComposeShapes(),
        content     = content
    )
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF40C4FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB3E5FC),
    secondary = Color(0xFF1E88E5),
    tertiary = Color(0xFFCFD8DC),
    background = Color.White,
    surface = Color.White,
    error = Color(0xFFF44336),
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF40C4FF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF64B5F6),
    tertiary = Color(0xFF90A4AE),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = Color(0xFFCF6679),
    onBackground = Color.White,
    onSurface = Color.White
)

object BacStateColors {
    val Dead = Color(0xFF000000)
    val ShitFaced = Color(0xFFF44336)
    val Drunk = Color(0xFFFF9800)
    val Tipsy = Color(0xFFCDDC39)
    val Sober = Color(0xFF4CAF50)
}

object StatusColors {
    val GreenColor = Color(0xFF4CAF50)
    val LightRedColor = Color(0xFFF44336)
}

object ThemeShades {
    val DeleteSwipe = Color.Red.copy(alpha = 0.7f)
    val LightBlueHeader = Color(0xFF40C4FF)
    val DividerGray = Color(0xFFCFD8DC)
    val ButtonInactiveBg = Color(0xFFBDBDBD)
    val ButtonActiveRed = Color(0xFFE57373)
    val SearchBarBg = Color.LightGray.copy(alpha = 0.4f)
    val ListDivider = Color.LightGray.copy(alpha = 0.5f)
}

fun ComposeTypography() = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize   = 32.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize   = 20.sp,
        fontWeight = FontWeight.SemiBold
    )
)

fun ComposeShapes() = androidx.compose.material3.Shapes(
    small  = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large  = RoundedCornerShape(16.dp)
)
