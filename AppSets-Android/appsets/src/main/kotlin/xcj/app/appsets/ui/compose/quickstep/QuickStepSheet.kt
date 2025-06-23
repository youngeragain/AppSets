@file:OptIn(ExperimentalLayoutApi::class)

package xcj.app.appsets.ui.compose.quickstep

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.LocalQuickStepContentHandlerRegistry
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.compose_share.components.SearchTextField
import xcj.app.starter.util.ContentType

@Composable
fun QuickStepSheet(
    quickStepContentHolder: QuickStepContentHolder
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
            quickStepContentHandlerRegistry.findHandlers(
                quickStepContentHolder,
                searchContent.text
            ).groupBy { it.getCategory() }
        }
    }
    val filteredContentHandlerCategories = filteredContentHandlersMap.keys.toList()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quickStepContentHolder.quickStepContents) { quickStepContent ->
                QuickStepContentComponent(quickStepContent)
            }
        }
        SearchTextField(
            value = searchContent.text,
            onValueChange = {
                searchContent = TextFieldValue(it)

            },
            placeholder = {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.search_quick_step),
                    fontSize = 12.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(requester)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraLarge),
            maxLines = 1
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val contentHandlers = filteredContentHandlersMap[contentHandlerCategory]
                        contentHandlers?.forEach { contentHandler ->
                            contentHandler.getContent(
                                onClick = {}
                            ).invoke()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStepContentComponent(quickStepContent: QuickStepContent) {
    Row(
        modifier = Modifier
            .widthIn(max = 250.dp)
            .border(
                1.dp, MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.extraLarge
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (quickStepContent is TextQuickStepContent) {
            Icon(
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_notes_24),
                contentDescription = null
            )
            Text(
                text = quickStepContent.text,
                fontSize = 12.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        } else if (quickStepContent is UriQuickStepContent) {
            when {
                ContentType.isImage(quickStepContent.uriContentType) -> {
                    /* Icon(
                         painter = painterResource(xcj.app.compose_share.R.drawable.ic_photo_24),
                         contentDescription = null
                     )*/
                    AnyImage(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(MaterialTheme.shapes.extraLarge),
                        any = quickStepContent.uri
                    )
                }

                ContentType.isVideo(quickStepContent.uriContentType) -> {
                    /*  Icon(
                          painter = painterResource(xcj.app.compose_share.R.drawable.ic_slow_motion_video_24),
                          contentDescription = null
                      )*/
                    AnyImage(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(MaterialTheme.shapes.extraLarge),
                        any = quickStepContent.uri
                    )
                }

                else -> {
                    Icon(
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                        contentDescription = null
                    )
                    val displayName = quickStepContent.androidUriFile?.displayName
                    if (!displayName.isNullOrEmpty()) {
                        Text(text = displayName, fontSize = 12.sp)
                    }

                }
            }
        } else {
            Icon(
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                contentDescription = null
            )
        }
    }
}