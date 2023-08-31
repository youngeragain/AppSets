package xcj.app.appsets.ui.compose.apps

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.R
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.usecase.models.Application
import xcj.app.appsets.usecase.models.PlatForm
import xcj.app.appsets.usecase.models.VersionInfo
import xcj.app.compose_share.compose.BackActionTopBar


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppDetailsPage(
    tabVisibilityState: MutableState<Boolean>,
    application: Application?,
    onBackAction: () -> Unit,
    onAddPlatformInfoClick: (PlatForm?) -> Unit,
    onAddVersionInfoClick: (PlatForm) -> Unit,
    onAddScreenshotInfoClick: (PlatForm, VersionInfo) -> Unit,
    onAddDownloadInfoClick: (PlatForm, VersionInfo) -> Unit
) {
    SideEffect {
        tabVisibilityState.value = false
    }
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
        }
    })
    if (application == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "没找到对应的应用")
        }
    } else {
        val rememberScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState)
        ) {
            BackActionTopBar(
                backButtonRightText = application.name ?: "",
                onBackAction = onBackAction
            )
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .size(160.dp),
                        any = application.iconUrl
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Button(onClick = {}) {
                            Text(text = "获取", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            var platformPosition by remember {
                val initPosition = if (application.currentVisiblePlatformPosition != -1) {
                    application.currentVisiblePlatformPosition
                } else {
                    0
                }
                mutableStateOf(initPosition)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "${application.platforms?.size ?: 0}个平台信息",
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = {
                            onAddPlatformInfoClick(application.platforms?.getOrNull(platformPosition))
                        })
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "添加平台信息", fontSize = 13.sp)
                }
            }
            if (!application.platforms.isNullOrEmpty()) {
                val platform by remember {
                    derivedStateOf {
                        application.currentVisiblePlatformPosition = platformPosition
                        application.platforms[platformPosition]
                    }
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    if (application.platforms.size > 1) {
                        PreviousNextComponent(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            startText = null,
                            previousText = "上个平台",
                            nextText = "下个平台",
                            currentPosition = platformPosition,
                            maxPosition = application.platforms.size - 1,
                            onPreviousClick = {
                                if (platformPosition - 1 >= 0)
                                    platformPosition -= 1
                            },
                            onNextClick = {
                                if (platformPosition + 1 < application.platforms.size)
                                    platformPosition += 1
                            })
                    }
                    AnimatedContent(
                        targetState = platform,
                        label = "platform_animate"
                    ) {
                        PlatformInfo(
                            platform = it,
                            onAddVersionInfoClick = onAddVersionInfoClick,
                            onAddScreenshotInfoClick = onAddScreenshotInfoClick,
                            onAddDownloadInfoClick = onAddDownloadInfoClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlatformInfo(
    platform: PlatForm,
    onAddVersionInfoClick: (PlatForm) -> Unit,
    onAddScreenshotInfoClick: (PlatForm, VersionInfo) -> Unit,
    onAddDownloadInfoClick: (PlatForm, VersionInfo) -> Unit
) {
    Column {
        Text(text = "平台", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = platform.name ?: "")
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "应用包名", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = platform.packageName ?: "", maxLines = 8)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "应用介绍", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = platform.introduction ?: "", maxLines = 8)
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
        ) {
            Text(
                text = "${platform.versionInfos?.size ?: 0}个版本信息",
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = {
                        onAddVersionInfoClick(platform)
                    })
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "添加版本信息", fontSize = 13.sp)
            }
        }
        if (!platform.versionInfos.isNullOrEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                var versionPosition by remember {
                    val initPosition = if (platform.currentVisibleVersionInfoPosition != -1) {
                        platform.currentVisibleVersionInfoPosition
                    } else {
                        0
                    }
                    mutableStateOf(initPosition)
                }
                val version by remember {
                    derivedStateOf {
                        platform.currentVisibleVersionInfoPosition = versionPosition
                        platform.versionInfos[versionPosition]
                    }
                }
                if (platform.versionInfos.size > 1) {
                    PreviousNextComponent(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        startText = null,
                        previousText = "上个版本",
                        nextText = "下个版本",
                        currentPosition = versionPosition,
                        maxPosition = platform.versionInfos.size - 1,
                        onPreviousClick = {
                            if (versionPosition - 1 >= 0)
                                versionPosition -= 1
                        },
                        onNextClick = {
                            if (versionPosition + 1 < platform.versionInfos.size)
                                versionPosition += 1
                        })
                }
                AnimatedContent(
                    targetState = version,
                    label = "version_animate"
                ) {
                    VersionInfo(
                        version = it,
                        onAddScreenshotInfoClick = {
                            onAddScreenshotInfoClick(platform, it)
                        },
                        onAddDownloadInfoClick = {
                            onAddDownloadInfoClick(platform, it)
                        })
                }
            }
        }
    }
}

@Composable
fun VersionInfo(
    version: VersionInfo,
    onAddScreenshotInfoClick: () -> Unit,
    onAddDownloadInfoClick: () -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "版本信息", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(text = "版本:${version.version}")
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "版本Code:${version.versionCode}")
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "版本日志:${version.changes}")
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "版本应用包体积:${version.packageSize}")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth()) {
            Text(text = "屏幕截图", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onAddScreenshotInfoClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "添加", fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (!version.screenshotInfos.isNullOrEmpty()) {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                version.screenshotInfos.forEachIndexed { index, screenShot ->
                    val url = screenShot.url
                    if (url != null) {
                        Row {
                            Spacer(modifier = Modifier.width(12.dp))
                            LocalOrRemoteImage(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp)),
                                any = url
                            )
                            if (index == version.screenshotInfos.size - 1) {
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "没有屏幕截图")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth()) {
            Text(
                text = "下载信息(${version.downloadInfos?.size ?: 0})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onAddDownloadInfoClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "添加", fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (version.downloadInfos.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "没有提供下载链接")

            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun PreviousNextComponent(
    modifier: Modifier,
    startText: String? = null,
    previousText: String,
    nextText: String,
    currentPosition: Int,
    maxPosition: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Row(
        modifier = modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!startText.isNullOrEmpty()) {
            Text(text = startText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier

                .animateContentSize()
        ) {
            if (currentPosition > 0) {
                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onPreviousClick)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_navigate_before_24),
                        contentDescription = "previous"
                    )
                    Text(
                        text = previousText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
            if (currentPosition in 1 until maxPosition) {
                Spacer(modifier = Modifier.width(12.dp))
            }
            if (currentPosition < maxPosition) {
                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onNextClick)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nextText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.round_navigate_next_24),
                        contentDescription = "next"
                    )
                }
            }
        }
    }
}