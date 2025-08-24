@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package xcj.app.share.ui.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val DarkColorPalette = darkColorScheme(
    /*primary = Purple200,
    onPrimary = Purple700,
    secondary = Teal200
     */
    outline = Color(0x80363636)
)

val LightColorPalette = lightColorScheme(
    /*primary = Purple500,
    onPrimary = Purple700,
    secondary = Teal200
     Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
    outline = Color(0xD1EAEAEA)
)

@Composable
fun AppSetsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && darkTheme ->
            dynamicDarkColorScheme(LocalContext.current).copy(outline = Color(0x80363636))

        dynamicColor && !darkTheme ->
            dynamicLightColorScheme(LocalContext.current).copy(outline = Color(0xD1EAEAEA))

        darkTheme -> DarkColorPalette
        else -> LightColorPalette
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}