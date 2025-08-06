package xcj.app.launcher.ui.compose.standard_home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppsGroupAlphabetTitle(
    appGroupTitle: String,
    borderColor: Color,
    onAlphabetClick: () -> Unit
) {
    TextWithBorder(appGroupTitle, borderColor, borderColor, onAlphabetClick)
}

@Composable
fun TextWithBorder(
    text: String,
    borderColor: Color,
    textColor: Color,
    onAlphabetClick: () -> Unit
) {
    Box(
        Modifier
            .clickable(onClick = onAlphabetClick)
            .border(2.dp, borderColor, RectangleShape)
            .size(42.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}