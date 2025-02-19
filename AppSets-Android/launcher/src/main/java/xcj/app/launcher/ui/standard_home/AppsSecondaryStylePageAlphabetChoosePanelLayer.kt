package xcj.app.launcher.ui.standard_home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppsSecondaryStylePageAlphabetChoosePanelLayer(onAlphabetClick: (String?) -> Unit) {
    val alphabets = remember {
        listOf(
            "A", "B", "C", "D", "E", "F",
            "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X",
            "Y", "Z", "*"
        )
    }
    val viewModel = viewModel<StandardWindowHomeViewModel>()
    val context = LocalContext.current
    val settings by viewModel.settings
    val borderColor = Color(settings.searchPageAppNameColor)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                onAlphabetClick(null)
            },
        contentAlignment = Alignment.Center
    ) {
        FlowRow(
            modifier = Modifier
                .width(350.dp)
        ) {
            alphabets.forEach { alphabet ->
                Box(modifier = Modifier.padding(12.dp)) {
                    AppsGroupAlphabetTitle(
                        appGroupTitle = alphabet,
                        borderColor = borderColor,
                        onAlphabetClick = {
                            onAlphabetClick(alphabet)
                        }
                    )
                }
            }
        }
    }
}