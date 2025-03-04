package xcj.app.launcher.ui.compose.standard_home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xcj.app.launcher.ui.model.StyledAppDefinition
import xcj.app.starter.android.AppDefinition

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppsSecondaryStylePageBaseLayer(
    appsWithAlphabet: List<Any>,
    lazyListState: LazyListState,
    searchTextField: TextFieldValue,
    onSearchTextFieldValueChanged: (TextFieldValue) -> Unit,
    onAlphabetClick: () -> Unit,
    onAppClick: (StyledAppDefinition) -> Unit
) {
    val viewModel = viewModel<StandardWindowHomeViewModel>()
    val settings by viewModel.settings
    val space = settings.appCardSpace.dp
    val borderColor = Color(settings.searchPageAppNameColor)
    val appNameColor = Color(settings.searchPageAppNameColor)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = space),
        verticalArrangement = Arrangement.spacedBy(space)
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val borderColor = Color(settings.searchPageAppNameColor)
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, borderColor),
                value = searchTextField,
                onValueChange = onSearchTextFieldValueChanged,
                placeholder = {
                    Text(text = "Search Apps", color = borderColor)
                },

                shape = RectangleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = borderColor,
                    focusedBorderColor = borderColor,
                    unfocusedTextColor = borderColor,
                    focusedTextColor = borderColor
                ),
                singleLine = true
            )
        }
        LazyColumn(modifier = Modifier, state = lazyListState) {
            itemsIndexed(
                items = appsWithAlphabet,
                key = { index, app -> index }
            ) { index, styledApp ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    when (styledApp) {
                        is String -> {
                            AppsGroupAlphabetTitle(
                                appGroupTitle = styledApp,
                                borderColor = borderColor,
                                onAlphabetClick = onAlphabetClick
                            )
                        }

                        is StyledAppDefinition -> {
                            AppsGroupItem(
                                styledApp = styledApp,
                                appNameColor = appNameColor,
                                onAppClick = onAppClick
                            )
                        }
                    }
                }
            }
        }
    }
}