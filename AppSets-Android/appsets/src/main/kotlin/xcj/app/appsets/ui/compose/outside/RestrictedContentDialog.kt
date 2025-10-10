@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.outside

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RestrictedContentDialog(
    restrictedContentHandleState: RestrictedContentHandleState
) {
    val isShow by restrictedContentHandleState.isShow
    if (isShow) {
        BasicAlertDialog(
            onDismissRequest = {
                restrictedContentHandleState.hide()
            }
        ) {
            var requestToView by remember {
                mutableStateOf(false)
            }
            Card(
                modifier = Modifier,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    var clickCount by remember {
                        mutableIntStateOf(0)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clickable {
                            if (requestToView) {
                                clickCount += 1
                                if (clickCount >= 10) {
                                    restrictedContentHandleState.hide(invokeCallback = true)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.prompt),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        Modifier
                            .padding(vertical = 12.dp)
                    ) {
                        val targetTextId = if (requestToView) {
                            xcj.app.appsets.R.string.restricted_content_continue_viewing_user_to_view_notify
                        } else {
                            xcj.app.appsets.R.string.restricted_content_continue_viewing
                        }
                        AnimatedContent(
                            targetState = targetTextId,
                            label = "target_text_id_animate",
                            contentAlignment = Alignment.TopCenter
                        ) { textId ->
                            Text(
                                modifier = Modifier.widthIn(max = 200.dp),
                                text = stringResource(id = textId)
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            if (!requestToView) {
                                requestToView = true
                            } else {
                                restrictedContentHandleState.hide()
                            }
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        val textId = if (requestToView) {
                            xcj.app.appsets.R.string.ok
                        } else {
                            xcj.app.appsets.R.string.request_to_view
                        }
                        Text(text = stringResource(textId))
                    }
                }
            }
        }
    }
}