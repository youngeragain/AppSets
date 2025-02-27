package xcj.app.appsets.ui.compose.apps.tools

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.UUID

sealed interface IntentCallerModel {
    var marker: String
    val time: Date
    val callResults: MutableList<CallResult>

    data class CallResult(
        val time: Date = Calendar.getInstance().time,
        var isSuccess: Boolean = false,
        var failureReason: Exception? = null
    )

    data class DeepLink(
        val deeplink: String,
        override var marker: String = UUID.randomUUID().toString(),
        override val time: Date = Calendar.getInstance().time,
        override val callResults: MutableList<CallResult> = mutableListOf(),
    ) : IntentCallerModel

    data class Intent(
        val packageName: String?,
        val className: String?,
        val categories: Set<String>?,
        val action: String?,
        override var marker: String = UUID.randomUUID().toString(),
        override val time: Date = Calendar.getInstance().time,
        override val callResults: MutableList<CallResult> = mutableListOf(),
    ) : IntentCallerModel

    companion object {
        fun call(
            context: Context,
            historyContainer: MutableList<IntentCallerModel>,
            model: IntentCallerModel
        ) {
            runCatching {
                val intent = buildIntent(context, model)
                context.startActivity(intent)
            }.onSuccess {
                val callResult = IntentCallerModel.CallResult(isSuccess = true)
                model.callResults.add(0, callResult)
            }.onFailure {
                val callResult = IntentCallerModel.CallResult(isSuccess = false)
                model.callResults.add(0, callResult)
            }
            historyContainer.add(0, model)
        }

        private fun buildIntent(
            context: Context,
            model: IntentCallerModel
        ): android.content.Intent {
            val intent = android.content.Intent()
            when (model) {
                is IntentCallerModel.DeepLink -> {
                    val uri = Uri.parse(model.deeplink)
                    intent.data = uri
                }

                is IntentCallerModel.Intent -> {
                    if (!model.packageName.isNullOrEmpty() && !model.className.isNullOrEmpty()) {
                        val component = ComponentName(model.packageName, model.className)
                        intent.component = component
                    }
                    model.categories?.forEach(intent::addCategory)
                    if (!model.action.isNullOrEmpty()) {
                        intent.action = model.action
                    }
                }
            }
            return intent
        }
    }
}

@Composable
fun AppToolIntentCallerPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val calledIntentList: MutableList<IntentCallerModel> = remember {
        mutableStateListOf<IntentCallerModel>()
    }
    val dateFormater = remember {
        SimpleDateFormat.getDateInstance()
    }
    Column {
        BackActionTopBar(
            onBackClick = onBackClick,
            backButtonRightText = stringResource(xcj.app.appsets.R.string.intent_caller)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var deeplinkInputText by remember {
                val firstTextQuickStepContent =
                    quickStepContents?.firstOrNull { it is TextQuickStepContent } as? TextQuickStepContent
                mutableStateOf(TextFieldValue(firstTextQuickStepContent?.text ?: ""))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.deeplink))
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = deeplinkInputText.text,
                    onValueChange = {
                        deeplinkInputText = TextFieldValue(it)
                    },
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.deeplink),
                            fontSize = 12.sp
                        )
                    },
                    maxLines = 3
                )
                FilledTonalButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        val deepLink =
                            IntentCallerModel.DeepLink(deeplink = deeplinkInputText.text)
                        IntentCallerModel.call(context, calledIntentList, deepLink)
                    }
                ) {
                    Text(text = stringResource(xcj.app.appsets.R.string.call))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.intent))
                var packageNameInputText by remember {
                    mutableStateOf(TextFieldValue(""))
                }
                var classNameInputText by remember {
                    mutableStateOf(TextFieldValue(""))
                }
                var actionInputText by remember {
                    mutableStateOf(TextFieldValue(""))
                }
                var categoryInputText by remember {
                    mutableStateOf(TextFieldValue(""))
                }
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = packageNameInputText.text,
                    onValueChange = {
                        packageNameInputText = TextFieldValue(it)
                    },
                    placeholder = {
                        Text(text = "Package Name, ps: xcj.app.container", fontSize = 12.sp)
                    },
                    maxLines = 3
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = classNameInputText.text,
                    onValueChange = {
                        classNameInputText = TextFieldValue(it)
                    },
                    placeholder = {
                        Text(
                            text = "Class Name, ps: xcj.app.share.ui.compose.AppSetsShareActivity",
                            fontSize = 12.sp
                        )
                    },
                    maxLines = 3
                )

                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = actionInputText.text,
                    onValueChange = {
                        actionInputText = TextFieldValue(it)
                    },
                    placeholder = {
                        Text(text = "Action, ps: xcj.app.share", fontSize = 12.sp)
                    },
                    maxLines = 3
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = categoryInputText.text,
                    onValueChange = {
                        categoryInputText = TextFieldValue(it)
                    },
                    placeholder = {
                        Text(text = "Category, ps: xcj.app.share.main", fontSize = 12.sp)
                    },
                    maxLines = 3
                )
                FilledTonalButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        val intent =
                            IntentCallerModel.Intent(
                                packageName = packageNameInputText.text,
                                className = classNameInputText.text,
                                categories = setOf(categoryInputText.text),
                                action = actionInputText.text
                            )
                        IntentCallerModel.call(context, calledIntentList, intent)
                    }
                ) {
                    Text(text = stringResource(xcj.app.appsets.R.string.call))
                }
            }
            if (calledIntentList.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = stringResource(xcj.app.appsets.R.string.history))
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(calledIntentList) { calledIntent ->
                            Card(
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .widthIn(max = 120.dp)
                                        .animateItem()
                                ) {
                                    if (calledIntent is IntentCallerModel.DeepLink) {
                                        Text(text = stringResource(xcj.app.appsets.R.string.deeplink))
                                    } else {
                                        Text(text = stringResource(xcj.app.appsets.R.string.intent))
                                    }
                                    Text(
                                        text = calledIntent.marker,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = dateFormater.format(calledIntent.time.time),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp
                                    )
                                    val firstCallResult = calledIntent.callResults.firstOrNull()
                                    if (firstCallResult != null) {
                                        if (firstCallResult.isSuccess) {
                                            Icon(
                                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_check_24),
                                                contentDescription = null,
                                                tint = Color.Green
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_error_outline_24),
                                                contentDescription = null,
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "id:pixel_9_pro", backgroundColor = 0xFFFFFFFF,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO or android.content.res.Configuration.UI_MODE_TYPE_NORMAL,
    wallpaper = androidx.compose.ui.tooling.preview.Wallpapers.YELLOW_DOMINATED_EXAMPLE,
    showSystemUi = false
)
@Composable
fun AppToolIntentCallerPagePreview() {
    AppToolIntentCallerPage(emptyList(), {})
}