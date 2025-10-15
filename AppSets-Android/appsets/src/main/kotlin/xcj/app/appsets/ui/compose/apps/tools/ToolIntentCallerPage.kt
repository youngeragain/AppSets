package xcj.app.appsets.ui.compose.apps.tools

import android.content.ComponentName
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
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
                    val uri = model.deeplink.toUri()
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
fun ToolIntentCallerPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit
) {
    HideNavBar()
    val dateFormater = remember {
        SimpleDateFormat.getDateInstance()
    }
    val calledIntentList = remember {
        mutableStateListOf<IntentCallerModel>()
    }
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    var backActionBarSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val backActionsHeight by remember {
        derivedStateOf {
            with(density) {
                backActionBarSize.height.toDp()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .hazeSource(hazeState)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )
        {
            Spacer(
                modifier = Modifier.height(
                    WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding() + backActionsHeight + 12.dp
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "${stringResource(xcj.app.appsets.R.string.history)}(${calledIntentList.size})")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(calledIntentList) { calledIntent ->
                        Card(
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier.animateItem()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .widthIn(max = 120.dp)
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
            val pagerState = rememberPagerState { 2 }
            PageIndicator(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 12.dp
                    ),
                pagerState = pagerState
            )
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top
            ) { index ->
                when (index) {
                    0 -> {
                        DeepLinkCaller(
                            quickStepContents = quickStepContents,
                            calledIntentList = calledIntentList
                        )
                    }

                    else -> {
                        IntentCaller(
                            quickStepContents = quickStepContents,
                            calledIntentList = calledIntentList
                        )
                    }
                }

            }
        }

        BackActionTopBar(
            modifier = Modifier.onPlaced {
                backActionBarSize = it.size
            },
            hazeState = hazeState,
            onBackClick = onBackClick,
            centerText = stringResource(xcj.app.appsets.R.string.intent_caller)
        )

    }
}

@Composable
fun IntentCaller(
    quickStepContents: List<QuickStepContent>?,
    calledIntentList: MutableList<IntentCallerModel>
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(),
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
        val categoryInputTexts = remember {
            mutableStateListOf(TextFieldValue(""))
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
        categoryInputTexts.forEachIndexed { index, categoryInputText ->
            Row {
                DesignTextField(
                    modifier = Modifier.weight(1f),
                    value = categoryInputText.text,
                    onValueChange = {
                        categoryInputTexts[index] = TextFieldValue(it)
                    },
                    placeholder = {
                        Text(text = "Category, ps: xcj.app.share.main", fontSize = 12.sp)
                    },
                    maxLines = 3
                )
                IconButton(
                    onClick = {
                        categoryInputTexts.removeAt(index)
                    }
                ) {
                    Icon(
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_do_not_disturb_on_24),
                        contentDescription = stringResource(xcj.app.appsets.R.string.remove)
                    )
                }
            }
        }

        IconButton(
            onClick = {
                categoryInputTexts.add(TextFieldValue(""))
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                contentDescription = stringResource(xcj.app.appsets.R.string.add)
            )
        }

        FilledTonalButton(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                val intent =
                    IntentCallerModel.Intent(
                        packageName = packageNameInputText.text,
                        className = classNameInputText.text,
                        categories = categoryInputTexts.map { it.text }.toSet(),
                        action = actionInputText.text
                    )
                IntentCallerModel.call(context, calledIntentList, intent)
            }
        ) {
            Text(text = stringResource(xcj.app.appsets.R.string.call))
        }
    }
}

@Composable
fun DeepLinkCaller(
    quickStepContents: List<QuickStepContent>?,
    calledIntentList: MutableList<IntentCallerModel>
) {
    val context = LocalContext.current

    var deeplinkInputText by remember {
        val firstTextQuickStepContent =
            quickStepContents?.firstOrNull { it is TextQuickStepContent } as? TextQuickStepContent
        mutableStateOf(TextFieldValue(firstTextQuickStepContent?.text ?: ""))
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
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
    ToolIntentCallerPage(emptyList(), {})
}