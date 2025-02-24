@file:OptIn(ExperimentalLayoutApi::class)

package xcj.app.appsets.ui.compose.quickstep

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import xcj.app.appsets.ui.compose.LocalQuickStepContentHandlerRegistry
import xcj.app.compose_share.components.SearchTextField

@Composable
fun QuickStepSheet(
    quickStepContentList: List<QuickStepContent>
) {
    val requester = remember {
        FocusRequester()
    }
    var searchContent by remember {
        mutableStateOf(TextFieldValue())
    }
    val quickStepContentHandlerRegistry = LocalQuickStepContentHandlerRegistry.current
    val filteredContentHandlersMap by remember {
        derivedStateOf {
            val allContentTypes = quickStepContentList.flatMap { it.getContentTypes() }
            quickStepContentHandlerRegistry.findHandlers(allContentTypes, searchContent.text)
                .groupBy { it.getCategory() }
        }
    }
    val filteredContentHandlerCategories = filteredContentHandlersMap.keys.toList()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchTextField(
            value = searchContent.text,
            onValueChange = {
                searchContent = TextFieldValue(it)

            },
            placeholder = {
                Text(text = "Search Quick Step")
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(requester)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraLarge)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(filteredContentHandlerCategories) { contentHandlerCategory ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = contentHandlerCategory, fontWeight = FontWeight.Bold)
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val contentHandlers = filteredContentHandlersMap[contentHandlerCategory]
                        contentHandlers?.forEach { contentHandler ->
                            contentHandler.getContent().invoke()
                        }
                    }
                }
            }
        }
    }
}