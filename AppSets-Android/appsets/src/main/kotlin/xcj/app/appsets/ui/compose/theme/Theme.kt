@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package xcj.app.appsets.ui.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val DarkColorPalette = darkColorScheme(
    /*primary = Purple200,
    onPrimary = Purple700,
    secondary = Teal200
     */
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
)

@Composable
fun AppSetsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && darkTheme ->
            dynamicDarkColorScheme(LocalContext.current).copy(
                outline = colorDarkOutline,
                outlineVariant = colorDarkOutlineVariant
            )

        dynamicColor && !darkTheme ->
            dynamicLightColorScheme(LocalContext.current).copy(
                outline = colorLightOutline,
                outlineVariant = colorLightOutlineVariant
            )

        darkTheme -> DarkColorPalette.copy(
            outline = colorDarkOutline,
            outlineVariant = colorDarkOutlineVariant
        )

        else -> LightColorPalette.copy(
            outline = colorLightOutline,
            outlineVariant = colorLightOutlineVariant
        )
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}