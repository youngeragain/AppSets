package xcj.app.appsets.ui.compose.apps

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.ktx.toast
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.nonecompose.base.UriHolder
import xcj.app.appsets.usecase.CreateApplicationState
import xcj.app.appsets.usecase.models.Application
import xcj.app.appsets.usecase.models.ApplicationForCreate
import xcj.app.appsets.usecase.models.PlatForm
import xcj.app.appsets.usecase.models.PlatformForCreate
import xcj.app.appsets.usecase.models.VersionInfo
import xcj.app.appsets.usecase.models.VersionInfoForCreate

@UnstableApi
@Preview(showBackground = true)
@Composable
fun CreateAppPagePreview() {
    //CreateAppPage()
}

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CreateAppPage(
    tabVisibilityState: MutableState<Boolean>,
    createStep: String = "application",
    application: Application? = null,
    platform: PlatForm? = null,
    versionInfo: VersionInfo? = null,
    applicationForCreate: ApplicationForCreate,
    createApplicationState: State<CreateApplicationState?>,
    onBackClick: () -> Unit,
    onChoosePictureClick: (String, MutableState<UriHolder?>) -> Unit,
    onConfirmClick: () -> Unit,
    onDispose: () -> Unit,
) {
    DisposableEffect(key1 = true, effect = {
        if (application != null)
            applicationForCreate.inflateFromApplication(application)
        onDispose {
            tabVisibilityState.value = true
            onDispose()
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    LaunchedEffect(key1 = createApplicationState.value, block = {
        if (createApplicationState.value is CreateApplicationState.CreateFinish) {
            onBackClick()
        }
    })
    Column(
        modifier = Modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_round_arrow_24),
                    contentDescription = "go back",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onBackClick)
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Create Application",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 12.dp)
            ) {
                Button(onClick = onConfirmClick) {
                    Text(text = "确认")
                }
            }
        }
        AnimatedVisibility(visible = createApplicationState.value != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    when (val creatingState = createApplicationState.value) {
                        is CreateApplicationState.Creating -> {
                            Text(text = creatingState.tips ?: "")
                        }

                        is CreateApplicationState.CreateFinish -> {
                            Text(text = creatingState.tips ?: "")
                        }

                        is CreateApplicationState.CreateFailed -> {
                            Text(text = creatingState.tips ?: "")
                        }

                        else -> Unit
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Divider(modifier = Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(10.dp))
        IconAndBanner(
            createStep = createStep,
            showSelect = applicationForCreate.appId.isNullOrEmpty(),
            iconUseAge = "app_icon",
            bannerUseAge = "banner_icon",
            iconUriHolderState = applicationForCreate.iconUriHolderState,
            bannerUriHolderState = applicationForCreate.bannerUriHolderState,
            onChoosePictureClick = onChoosePictureClick
        )
        TextOrTextFiled(
            isFiled = createStep == "application",
            placeHolderText = "应用名称",
            valueState = applicationForCreate.name
        )
        TextOrTextFiled(
            isFiled = createStep == "application",
            placeHolderText = "应用类型",
            valueState = applicationForCreate.category
        )
        TextOrTextFiled(
            isFiled = createStep == "application",
            placeHolderText = "网站",
            valueState = applicationForCreate.website
        )
        TextOrTextFiled(
            modifier = Modifier.heightIn(min = 120.dp),
            isFiled = createStep == "application",
            placeHolderText = "开发者信息",
            valueState = applicationForCreate.developerInfo
        )

        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp)) {
            Text(text = "选择平台信息", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        val platformNames = remember {
            if (platform != null && createStep != "application" && createStep != "platform") {
                listOf(platform.name ?: "")
            } else {
                listOf(
                    "Android", "IOS", "Windows", "Linux", "Chrome OS",
                    "Mac OS", "Harmony OS", "Kai OS", "Sailfish OS", "Tizen OS", "Wear OS"
                )
            }
        }

        val selectedPlatformNames = remember {
            mutableSetOf<String>().apply {
                if (platform != null && createStep != "application" && createStep != "platform") {
                    add(platform.name ?: "")
                } else {
                    if (!application?.platforms.isNullOrEmpty()) {
                        application?.platforms?.mapNotNull { it.name }?.let { addAll(it) }
                    }
                }
            }
        }
        var platformForCreate: PlatformForCreate? by remember {
            mutableStateOf(null)
        }

        LaunchedEffect(key1 = platform, block = {
            if (platform != null)
                platformForCreate = applicationForCreate.getPlatformForCreateById(platform.id ?: "")
        })

        LazyRow(verticalAlignment = Alignment.CenterVertically) {
            items(platformNames) { platformName ->
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (platformName == platformForCreate?.name) {
                        Divider(
                            modifier = Modifier
                                .height(2.dp)
                                .width(18.dp), color = MaterialTheme.colorScheme.primary
                        )
                    }
                    FilterChip(
                        selected = selectedPlatformNames.contains(platformName),
                        onClick = {
                            if (createStep != "application" && createStep != "platform")
                                return@FilterChip
                            if (platformForCreate != null) {
                                if (platformForCreate!!.name == platformName) {
                                    if (application?.platforms?.firstOrNull { it.name == platformName } == null) {
                                        selectedPlatformNames.remove(platformName)
                                        applicationForCreate.removePlatformForCreateByName(
                                            platformName
                                        )
                                        platformForCreate =
                                            applicationForCreate.getLastPlatformForCreateOrNull()
                                    } else {
                                        "无法删除已经添加的平台信息".toast()
                                    }
                                } else {
                                    selectedPlatformNames.add(platformName)
                                    platformForCreate =
                                        applicationForCreate.getPlatformForCreateByName(platformName)
                                }
                            } else {
                                selectedPlatformNames.add(platformName)
                                platformForCreate =
                                    applicationForCreate.getPlatformForCreateByName(platformName)
                            }
                        },
                        label = {
                            Text(text = platformName, fontSize = 16.sp)
                        })
                }
            }
            item {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Divider(modifier = Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(10.dp))

        AnimatedContent(
            targetState = platformForCreate,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) +
                        slideInVertically(
                            animationSpec = tween(350),
                            initialOffsetY = { it / 4 }) with
                        fadeOut(animationSpec = tween(350)) +
                        slideOutVertically(
                            animationSpec = tween(350),
                            targetOffsetY = { it / 4 })
            },
            label = "platform_animate_content"
        ) {
            if (it != null) {
                PlatformForApp(
                    createStep = createStep,
                    platformForCreate = it,
                    versionInfo = versionInfo,
                    onChoosePictureClick = onChoosePictureClick
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "点击平台添加或选择已添加平台信息")
                }
            }
        }
    }
}

@Composable
fun IconAndBanner(
    createStep: String,
    showSelect: Boolean,
    iconUseAge: String,
    bannerUseAge: String,
    iconUriHolderState: MutableState<UriHolder?>,
    bannerUriHolderState: MutableState<UriHolder?>,
    onChoosePictureClick: (String, MutableState<UriHolder?>) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.clickable {
                    if (createStep != "application")
                        return@clickable
                    onChoosePictureClick(iconUseAge, iconUriHolderState)
                },
                contentAlignment = Alignment.Center
            ) {
                val uri =
                    iconUriHolderState.value?.provideUri()
                if (uri != null) {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp)),
                        any = uri
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {

                    }
                    if (showSelect)
                        Text(text = "点击选择")
                }

            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "图标")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.clickable {
                    if (createStep != "application")
                        return@clickable
                    onChoosePictureClick(bannerUseAge, bannerUriHolderState)
                },
                contentAlignment = Alignment.Center
            )
            {
                val uri =
                    bannerUriHolderState.value?.provideUri()
                if (uri != null) {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .size(width = 260.dp, height = 120.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp)),
                        any = uri
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(width = 260.dp, height = 120.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {

                    }
                    if (showSelect)
                        Text(text = "点击选择")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Banner")
        }
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun PlatformForApp(
    createStep: String,
    platformForCreate: PlatformForCreate,
    versionInfo: VersionInfo? = null,
    onChoosePictureClick: (String, MutableState<UriHolder?>) -> Unit
) {
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = "For ${platformForCreate.name}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextOrTextFiled(
            isFiled = (createStep == "application" || createStep == "platform") && platformForCreate.id.isNullOrEmpty(),
            placeHolderText = "应用包名",
            valueState = platformForCreate.packageName
        )

        TextOrTextFiled(
            modifier = Modifier.heightIn(min = 120.dp),
            isFiled = (createStep == "application" || createStep == "platform") && platformForCreate.id.isNullOrEmpty(),
            placeHolderText = "应用介绍",
            valueState = platformForCreate.introduction
        )
        if (createStep == "application" || createStep == "platform" || createStep == "version") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "版本信息",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(modifier = Modifier
                        .clickable {
                            platformForCreate.addVersionInfoForCreate()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_add_24),
                            contentDescription = "add version"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "添加")
                    }
                }
            }
        }
        if (versionInfo != null) {
            val versionInfoForCreate =
                platformForCreate.getVersionInfoForCreateById(versionInfo.id ?: "")
            if (versionInfoForCreate != null) {
                VersionForPlatform(
                    createStep = createStep,
                    versionInfoForCreate = versionInfoForCreate,
                    onChoosePictureClick = onChoosePictureClick
                )
            } else {
                Row(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(text = "没有版本信息")
                    }
                }
            }
        } else {
            if (platformForCreate.versionInfoForCreates.isNotEmpty()) {
                platformForCreate.versionInfoForCreates.forEach { versionInfo ->
                    VersionForPlatform(
                        createStep = createStep,
                        versionInfoForCreate = versionInfo,
                        onChoosePictureClick = onChoosePictureClick
                    )
                }
            } else {
                Row(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(text = "没有版本信息")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(68.dp))
    }
}

@Composable
fun VersionForPlatform(
    createStep: String,
    versionInfoForCreate: VersionInfoForCreate,
    onChoosePictureClick: (String, MutableState<UriHolder?>) -> Unit
) {
    Divider(modifier = Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
    Spacer(modifier = Modifier.height(6.dp))
    Column() {
        IconAndBanner(
            createStep = createStep,
            showSelect = versionInfoForCreate.id.isNullOrEmpty(),
            iconUseAge = "app_version_icon",
            bannerUseAge = "app_version_banner_icon",
            iconUriHolderState = versionInfoForCreate.versionIconUriHolderState,
            bannerUriHolderState = versionInfoForCreate.versionBannerUriHolderState,
            onChoosePictureClick = onChoosePictureClick
        )
        TextOrTextFiled(
            isFiled = createStep != "download" && versionInfoForCreate.id.isNullOrEmpty(),
            placeHolderText = "版本",
            valueState = versionInfoForCreate.version
        )
        TextOrTextFiled(
            isFiled = createStep != "download" && versionInfoForCreate.id.isNullOrEmpty(),
            placeHolderText = "版本Code",
            valueState = versionInfoForCreate.versionCode
        )
        TextOrTextFiled(
            modifier = Modifier.heightIn(min = 120.dp),
            isFiled = createStep != "download" && versionInfoForCreate.id.isNullOrEmpty(),
            placeHolderText = "版本日志",
            valueState = versionInfoForCreate.changes
        )
        TextOrTextFiled(
            isFiled = createStep != "download" && versionInfoForCreate.id.isNullOrEmpty(),
            placeHolderText = "版本包体积大小",
            valueState = versionInfoForCreate.packageSize
        )
        TextOrTextFiled(
            isFiled = createStep != "download" && versionInfoForCreate.id.isNullOrEmpty(),
            placeHolderText = "隐私政策链接(URL)",
            valueState = versionInfoForCreate.privacyPolicyUrl
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            if (createStep != "download") {
                Row(
                    modifier = Modifier
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "屏幕截图",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(modifier = Modifier
                            .clickable {
                                val screenshotInfoForCreate =
                                    versionInfoForCreate.addScreenshotForCreate()
                                onChoosePictureClick(
                                    "app_version_screenshot",
                                    screenshotInfoForCreate.uriHolderState
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.round_add_24),
                                contentDescription = "add screenshot"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "添加")
                        }
                    }
                }
            }
            if (versionInfoForCreate.screenshotInfoForCreates.isNotEmpty()) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    versionInfoForCreate.screenshotInfoForCreates.forEachIndexed { index, screenShot ->
                        val uri = screenShot.uriHolderState.value?.provideUri()
                        if (uri != null) {
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
                                    any = uri
                                )
                                if (index == versionInfoForCreate.screenshotInfoForCreates.size - 1) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Row(Modifier.padding(12.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(text = "添加截图信息")
                    }
                }
            }
        }

        Column {
            Row(
                Modifier
                    .padding(12.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "下载链接(URL)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(modifier = Modifier
                        .clickable {
                            versionInfoForCreate.addDownloadInfoForCreate()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_add_24),
                            contentDescription = "add version"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "添加")
                    }
                }
            }
            if (versionInfoForCreate.downloadInfoForCreates.isNotEmpty()) {
                Column() {
                    versionInfoForCreate.downloadInfoForCreates.forEach { downloadInfo ->
                        TextField(value = downloadInfo.url.value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            onValueChange = {
                                downloadInfo.url.value = it
                            },
                            placeholder = {
                                Text(text = "下载链接")
                            })
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            } else {
                Row(Modifier.padding(horizontal = 12.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(text = "添加链接信息")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun TextOrTextFiled(
    modifier: Modifier = Modifier,
    isFiled: Boolean,
    placeHolderText: String,
    valueState: MutableState<String>,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    if (isFiled) {
        TextField(
            value = valueState.value,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            onValueChange = {
                valueState.value = it
            },
            placeholder = {
                Text(text = placeHolderText)
            },
            maxLines = 1,
            keyboardOptions = keyboardOptions
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = placeHolderText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = valueState.value)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}