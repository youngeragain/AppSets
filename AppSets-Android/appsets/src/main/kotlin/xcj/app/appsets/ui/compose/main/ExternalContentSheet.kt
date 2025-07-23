package xcj.app.appsets.ui.compose.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentComponent
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHolder
import xcj.app.appsets.ui.compose.quickstep.QuickStepSheet
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.appsets.ui.compose.quickstep.UriQuickStepContent
import xcj.app.starter.android.AppDefinition
import xcj.app.starter.android.util.FileUtil
import xcj.app.starter.util.ContentType

private suspend fun makeQuickStepContentHolder(
    context: Context,
    intent: Intent,
): QuickStepContentHolder {
    val quickStepContentList = mutableListOf<QuickStepContent>()
    if (intent.type == ContentType.TEXT_PLAIN) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
            val textQuickStepContent = TextQuickStepContent(text)
            quickStepContentList.add(textQuickStepContent)
        }
        return QuickStepContentHolder(intent, quickStepContentList)
    }
    val isMulti = intent.action == Intent.ACTION_SEND_MULTIPLE
    if (!isMulti) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
            val contentType = context.contentResolver.getType(uri) ?: ContentType.ALL
            val androidUriFile = FileUtil.parseUriToAndroidUriFile(context, uri)
            val uriQuickStepContent = UriQuickStepContent(uri, androidUriFile, contentType)
            quickStepContentList.add(uriQuickStepContent)
        }
    } else {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
            ?.mapNotNull {
                it as? Uri
            }?.forEach { uri ->
                val contentType = context.contentResolver.getType(uri) ?: ContentType.ALL
                val androidUriFile = FileUtil.parseUriToAndroidUriFile(context, uri)
                val uriQuickStepContent =
                    UriQuickStepContent(uri, androidUriFile, contentType)
                quickStepContentList.add(uriQuickStepContent)
            }
    }
    return QuickStepContentHolder(intent, quickStepContentList)
}

@Composable
fun ExternalContentContainerSheet(
    intent: Intent,
    fromAppDefinition: AppDefinition?,
    onConfirmClick: (Int) -> Unit,
) {
    var isHandleByAppSets by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var quickStepContentHolder by remember {
        mutableStateOf(QuickStepContentHolder(intent, emptyList()))
    }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            quickStepContentHolder = makeQuickStepContentHolder(context, intent)
        }
    }
    Box(modifier = Modifier.animateContentSize()) {
        AnimatedContent(isHandleByAppSets) { targetIsHandleByAppSets ->
            if (targetIsHandleByAppSets) {
                QuickStepSheet(quickStepContentHolder)
            } else {
                ExternalContentTipsSheet(
                    quickStepContentHolder = quickStepContentHolder,
                    fromAppDefinition = fromAppDefinition,
                    onConfirmClick = { handleType ->
                        if (handleType == MainActivity.EXTERNAL_CONTENT_HANDLE_BY_APPSETS) {
                            isHandleByAppSets = true
                        }
                        onConfirmClick(handleType)
                    }
                )
            }
        }
    }
}

@Composable
fun ExternalContentTipsSheet(
    quickStepContentHolder: QuickStepContentHolder,
    fromAppDefinition: AppDefinition?,
    onConfirmClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quickStepContentHolder.quickStepContents) { quickStepContent ->
                QuickStepContentComponent(quickStepContent)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(32.dp),
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_web_stories_24),
                contentDescription = null
            )
            Text(
                text = stringResource(xcj.app.appsets.R.string.external_content),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (fromAppDefinition != null) {
            Row {
                AsyncImage(
                    model = fromAppDefinition.icon,
                    contentDescription = fromAppDefinition.description,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.large)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.large
                        ),
                )
                Text(text = fromAppDefinition.name ?: "")
            }
        }

        Text(
            text = stringResource(xcj.app.appsets.R.string.how_to), fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FilledTonalIconButton(
                    onClick = {
                        onConfirmClick(MainActivity.EXTERNAL_CONTENT_HANDLE_BY_APPSETS)
                    }
                ) {
                    Image(
                        modifier = Modifier,
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                        contentDescription = null
                    )
                }
                Text(text = stringResource(xcj.app.appsets.R.string.app_name), fontSize = 12.sp)
            }
        }
    }
}