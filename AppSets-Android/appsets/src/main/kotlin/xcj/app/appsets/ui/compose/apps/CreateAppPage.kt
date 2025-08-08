package xcj.app.appsets.ui.compose.apps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.server.model.AppPlatform
import xcj.app.appsets.server.model.VersionInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfAppCreation
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.ui.model.CreateApplicationState
import xcj.app.appsets.ui.model.DownloadInfoForCreate
import xcj.app.appsets.ui.model.PlatformForCreate
import xcj.app.appsets.ui.model.ScreenshotInfoForCreate
import xcj.app.appsets.ui.model.VersionInfoForCreate
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.DesignVDivider
import xcj.app.compose_share.components.LocalAnyStateProvider
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState


@UnstableApi
@Preview(showBackground = true)
@Composable
fun CreateAppPagePreview() {
    CreateAppPage(
        createApplicationState = CreateApplicationState.NewApplication(),
        onBackClick = {},
        onApplicationForCreateFiledChanged = { a, b, c -> },
        onChoosePictureClick = { a, b, c -> },
        onConfirmClick = {},
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun CreateAppPage(
    onBackClick: () -> Unit,
    createStep: String = ApplicationForCreate.CREATE_STEP_APPLICATION,
    platform: AppPlatform? = null,
    versionInfo: VersionInfo? = null,
    createApplicationState: CreateApplicationState,
    onApplicationForCreateFiledChanged: (Any, String, String) -> Unit,
    onChoosePictureClick: (Any, String, UriProvider?) -> Unit,
    onConfirmClick: () -> Unit,
) {
    HideNavBarWhenOnLaunch()
    val appCreationUseCase = LocalUseCaseOfAppCreation.current
    val anyStateProvider = LocalAnyStateProvider.current
    DisposableEffect(key1 = true) {
        onDispose {
            appCreationUseCase.onComposeDispose("page dispose")
        }
    }
    SideEffect {
        if (createApplicationState is CreateApplicationState.CreateSuccess) {
            onBackClick()
        }
    }
    val platformNames = remember {
        if (platform != null && createStep != ApplicationForCreate.CREATE_STEP_APPLICATION &&
            createStep != ApplicationForCreate.CREATE_STEP_PLATFORM
        ) {
            listOf(platform.name ?: "")
        } else {
            mutableStateListOf(
                Constants.Android,
                Constants.IOS,
                Constants.Windows,
                Constants.Linux,
                Constants.Chrome_OS,
                Constants.Mac_OS,
                Constants.Harmony_OS,
                Constants.Kai_OS,
                Constants.Sailfish_OS,
                Constants.Tizen_OS,
                Constants.Wear_OS,
                Constants.Ubuntu_Touch
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val backButtonRightText =
                if (createStep != ApplicationForCreate.CREATE_STEP_APPLICATION) {
                    "Add $createStep"
                } else {
                    "Create Application"
                }
            BackActionTopBar(
                onBackClick = onBackClick,
                backButtonRightText = backButtonRightText,
                endButtonText = stringResource(id = xcj.app.appsets.R.string.sure),
                onEndButtonClick = onConfirmClick
            )
            var newPlatformName: String? by remember {
                mutableStateOf(null)
            }
            val platformForCreate by remember {
                derivedStateOf {
                    if (platform != null) {
                        appCreationUseCase.getPlatformForCreateById(platform.id ?: "")
                    } else {
                        appCreationUseCase.getPlatformForCreateByName(newPlatformName)
                    }
                }
            }
            val applicationForCreate = createApplicationState.applicationForCreate
            LazyColumn(modifier = Modifier.imePadding()) {

                item {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth()
                    ) {
                        IconAndBanner(
                            createStep = createStep,
                            showSelect = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            valueHost = applicationForCreate,
                            iconUseAge = "app_icon",
                            bannerUseAge = "banner_icon",
                            iconUriHolder = applicationForCreate.iconUriHolder,
                            bannerUriProvider = applicationForCreate.bannerUriHolder,
                            onChoosePictureClick = onChoosePictureClick
                        )
                        TextOrTextFiled(
                            isField = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            placeHolderText = stringResource(xcj.app.appsets.R.string.application_name),
                            valueHost = applicationForCreate,
                            valueFiledNameOfHost = ApplicationForCreate.FILED_NAME_NAME,
                            value = applicationForCreate.name,
                            onFiledChanged = onApplicationForCreateFiledChanged
                        )
                        TextOrTextFiled(
                            isField = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            placeHolderText = stringResource(xcj.app.appsets.R.string.app_types),
                            valueHost = applicationForCreate,
                            valueFiledNameOfHost = ApplicationForCreate.FILED_NAME_CATEGORY,
                            value = applicationForCreate.category,
                            onFiledChanged = onApplicationForCreateFiledChanged
                        )
                        TextOrTextFiled(
                            isField = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            placeHolderText = stringResource(xcj.app.appsets.R.string.website),
                            valueHost = applicationForCreate,
                            valueFiledNameOfHost = ApplicationForCreate.FILED_NAME_WEBSITE,
                            value = applicationForCreate.website,
                            onFiledChanged = onApplicationForCreateFiledChanged,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Uri,
                                autoCorrectEnabled = true
                            )
                        )
                        TextOrTextFiled(
                            modifier = Modifier.heightIn(min = 120.dp),
                            isField = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            placeHolderText = stringResource(xcj.app.appsets.R.string.developer_information),
                            valueHost = applicationForCreate,
                            valueFiledNameOfHost = ApplicationForCreate.FILED_NAME_DEVELOPER_INFO,
                            value = applicationForCreate.developerInfo,
                            onFiledChanged = onApplicationForCreateFiledChanged
                        )
                        var free by remember {
                            mutableStateOf(true)
                        }
                        LaunchedEffect(key1 = true, block = {
                            free = applicationForCreate.price.isEmpty()
                        })
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                        ) {
                            FilterChip(
                                selected = free,
                                onClick = {
                                    if (createStep == ApplicationForCreate.CREATE_STEP_APPLICATION)
                                        free = true
                                },
                                label = {
                                    Text(text = stringResource(xcj.app.appsets.R.string.free))
                                },
                                shape = CircleShape
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            FilterChip(
                                selected = !free,
                                onClick = {
                                    if (createStep == ApplicationForCreate.CREATE_STEP_APPLICATION)
                                        free = false
                                },
                                label = {
                                    Text(text = stringResource(xcj.app.appsets.R.string.paid))
                                },
                                shape = CircleShape
                            )
                        }

                        if (createStep == ApplicationForCreate.CREATE_STEP_APPLICATION) {
                            if (!free) {
                                TextOrTextFiled(
                                    modifier = Modifier,
                                    isField = true,
                                    placeHolderText = stringResource(xcj.app.appsets.R.string.single_product_price),
                                    valueHost = applicationForCreate,
                                    valueFiledNameOfHost = ApplicationForCreate.FILED_NAME_PRICE,
                                    value = applicationForCreate.price,
                                    onFiledChanged = onApplicationForCreateFiledChanged,
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                                )
                                LaunchedEffect(key1 = free, block = {
                                    onApplicationForCreateFiledChanged(
                                        applicationForCreate,
                                        ApplicationForCreate.FILED_NAME_PRICE_UNIT,
                                        ApplicationForCreate.DEFAULT_PRICE_UNIT
                                    )
                                })
                                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                                    FilterChip(
                                        selected = applicationForCreate.priceUnit == ApplicationForCreate.PRICE_UNIT_RMB,
                                        onClick = {
                                            onApplicationForCreateFiledChanged(
                                                applicationForCreate,
                                                ApplicationForCreate.FILED_NAME_PRICE_UNIT,
                                                ApplicationForCreate.PRICE_UNIT_RMB
                                            )
                                        },
                                        label = {
                                            Text(text = ApplicationForCreate.PRICE_UNIT_RMB)
                                        },
                                        shape = CircleShape
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    FilterChip(
                                        selected = applicationForCreate.priceUnit == ApplicationForCreate.PRICE_UNIT_USD,
                                        onClick = {
                                            onApplicationForCreateFiledChanged(
                                                applicationForCreate,
                                                ApplicationForCreate.FILED_NAME_PRICE_UNIT,
                                                ApplicationForCreate.PRICE_UNIT_USD
                                            )
                                        },
                                        label = {
                                            Text(text = ApplicationForCreate.PRICE_UNIT_USD)
                                        },
                                        shape = CircleShape
                                    )
                                }
                            }
                        } else {
                            if (!free) {
                                Text(
                                    text = stringResource(xcj.app.appsets.R.string.single_product_price),
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${applicationForCreate.price} ${applicationForCreate.priceUnit}",
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp)) {
                            Text(
                                text = stringResource(xcj.app.appsets.R.string.select_platform_information),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                }


                item {
                    val selectedPlatformNames = remember {
                        mutableSetOf<String>().apply {
                            if (platform != null &&
                                createStep != ApplicationForCreate.CREATE_STEP_APPLICATION &&
                                createStep != ApplicationForCreate.CREATE_STEP_PLATFORM
                            ) {
                                add(platform.name ?: "")
                            } else {
                                if (applicationForCreate.platformForCreates.isNotEmpty()) {
                                    applicationForCreate.platformForCreates.map { it.name }
                                        .let { addAll(it) }
                                }
                            }
                        }
                    }


                    FlowRow(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        platformNames.forEach { platformName ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (platformName == platformForCreate?.name) {
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .height(2.dp)
                                            .width(18.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                FilterChip(
                                    selected = selectedPlatformNames.contains(platformName),
                                    onClick = {
                                        if (createStep != ApplicationForCreate.CREATE_STEP_APPLICATION &&
                                            createStep != ApplicationForCreate.CREATE_STEP_PLATFORM
                                        ) {
                                            return@FilterChip
                                        }

                                        if (platformForCreate != null) {
                                            if (platformForCreate!!.name == platformName) {
                                                selectedPlatformNames.remove(platformName)
                                                appCreationUseCase.removePlatformForCreateByName(
                                                    platformName
                                                )
                                                newPlatformName = null
                                            } else {
                                                selectedPlatformNames.add(platformName)
                                                newPlatformName = platformName
                                            }
                                        } else {
                                            selectedPlatformNames.add(platformName)
                                            newPlatformName = platformName
                                        }

                                    },
                                    label = {
                                        Text(text = platformName)
                                    },
                                    shape = CircleShape
                                )
                            }
                        }
                        if (createStep == ApplicationForCreate.CREATE_STEP_APPLICATION ||
                            createStep == ApplicationForCreate.CREATE_STEP_PLATFORM
                        ) {
                            SuggestionChip(
                                onClick = {
                                    val composeContainerState =
                                        anyStateProvider.bottomSheetState()
                                    composeContainerState.show {
                                        CustomPlatformAddSheetContent(
                                            platformNames = platformNames,
                                            onConfirmClick = {
                                                composeContainerState.hide()
                                            }
                                        )
                                    }
                                },
                                label = {
                                    Icon(
                                        painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_add_24),
                                        contentDescription = "add custom platform"
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = CircleShape
                            )
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.animateItem()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        DesignVDivider()
                        Spacer(modifier = Modifier.height(10.dp))
                        if (platformForCreate != null) {
                            PlatformForApp(
                                createStep = createStep,
                                platformForCreate = platformForCreate!!,
                                versionInfo = versionInfo,
                                onApplicationForCreateFiledChanged = onApplicationForCreateFiledChanged,
                                onChoosePictureClick = onChoosePictureClick
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(xcj.app.appsets.R.string.click_platform_to_add_or_select_the_added_platform_information),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(150.dp))
                }
            }

        }

        CreateApplicationIndicator(createApplicationState = createApplicationState)
    }
}

@Composable
fun CreateApplicationIndicator(createApplicationState: CreateApplicationState) {
    AnimatedVisibility(
        visible = createApplicationState is CreateApplicationState.Creating,
        enter = fadeIn(tween()) + scaleIn(
            tween(),
            2f
        ),
        exit = fadeOut(tween()) + scaleOut(
            tween(),
            0.2f
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .padding(vertical = 12.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        modifier = Modifier.size(68.dp),
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                        contentDescription = null
                    )
                    Text(stringResource(xcj.app.appsets.R.string.processing), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CustomPlatformAddSheetContent(
    platformNames: List<String>,
    onConfirmClick: () -> Unit,
) {
    val context = LocalContext.current
    var customPlatform by remember {
        mutableStateOf("")
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(xcj.app.appsets.R.string.add_custom_platform))
            Spacer(modifier = Modifier.weight(1f))
            FilledTonalButton(
                onClick = {
                    if (customPlatform.isEmpty()) {
                        return@FilledTonalButton
                    }
                    val regex = "^[a-zA-Z0-9]*$".toRegex()
                    val matches = regex.matches(customPlatform)
                    if (!matches) {
                        return@FilledTonalButton
                    }
                    if (platformNames !is MutableList) {
                        return@FilledTonalButton
                    }
                    if (platformNames.contains(customPlatform)) {
                        context.getString(xcj.app.appsets.R.string.platform_already_exists).toast()
                        return@FilledTonalButton
                    }
                    platformNames.add(customPlatform)
                    onConfirmClick()

                }
            ) {
                Text(text = stringResource(id = xcj.app.appsets.R.string.sure))
            }
        }
        Text(text = stringResource(xcj.app.appsets.R.string.platform_name_english_only))

        DesignTextField(
            modifier = Modifier.fillMaxWidth(),
            value = customPlatform,
            onValueChange = {
                if (it.isNotEmpty()) {
                    customPlatform = it
                }
            },
            placeholder = {
                Text(text = stringResource(id = xcj.app.appsets.R.string.name))
            })

    }
}

@Composable
fun IconAndBanner(
    createStep: String,
    showSelect: Boolean,
    valueHost: Any,
    iconUseAge: String,
    bannerUseAge: String,
    iconUriHolder: UriProvider?,
    bannerUriProvider: UriProvider?,
    onChoosePictureClick: (Any, String, UriProvider?) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clickable {
                        if (iconUseAge == "app_icon") {
                            if (createStep != ApplicationForCreate.CREATE_STEP_APPLICATION)
                                return@clickable
                            onChoosePictureClick(
                                valueHost,
                                ApplicationForCreate.FILED_NAME_ICON_URI_HOLDER,
                                iconUriHolder
                            )
                        } else if (iconUseAge == "app_version_icon") {
                            if (createStep == ApplicationForCreate.CREATE_STEP_SCREENSHOT ||
                                createStep == ApplicationForCreate.CREATE_STEP_DOWNLOAD
                            )
                                return@clickable
                            onChoosePictureClick(
                                valueHost,
                                VersionInfoForCreate.FILED_NAME_ICON_URI_HOLDER,
                                iconUriHolder
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val uri =
                    iconUriHolder?.provideUri()
                if (uri != null) {
                    AnyImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.extraLarge),
                        any = uri
                    )
                } else {
                    if (showSelect) {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.choose),
                            fontSize = 12.sp
                        )
                    }
                }

            }
            Text(text = stringResource(id = xcj.app.appsets.R.string.icon), fontSize = 12.sp)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 158.dp, height = 68.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clickable {
                        if (bannerUseAge == "banner_icon") {
                            if (createStep != ApplicationForCreate.CREATE_STEP_APPLICATION)
                                return@clickable
                            onChoosePictureClick(
                                valueHost,
                                ApplicationForCreate.FILED_NAME_BANNER_URI_HOLDER,
                                bannerUriProvider
                            )
                        } else if (bannerUseAge == "app_version_banner_icon") {
                            if (createStep == ApplicationForCreate.CREATE_STEP_SCREENSHOT ||
                                createStep == ApplicationForCreate.CREATE_STEP_DOWNLOAD
                            ) {
                                return@clickable
                            }
                            onChoosePictureClick(
                                valueHost,
                                VersionInfoForCreate.FILED_NAME_BANNER_URI_HOLDER,
                                bannerUriProvider
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val uri =
                    bannerUriProvider?.provideUri()
                if (uri != null) {
                    AnyImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.extraLarge),
                        any = uri
                    )
                } else {
                    if (showSelect) {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.choose),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Text(text = "Banner", fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlatformForApp(
    createStep: String,
    platformForCreate: PlatformForCreate,
    versionInfo: VersionInfo? = null,
    onApplicationForCreateFiledChanged: (Any, String, String) -> Unit,
    onChoosePictureClick: (Any, String, UriProvider?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = "For ${platformForCreate.name}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        TextOrTextFiled(
            isField = (createStep == ApplicationForCreate.CREATE_STEP_APPLICATION ||
                    createStep == ApplicationForCreate.CREATE_STEP_PLATFORM),
            placeHolderText = stringResource(xcj.app.appsets.R.string.package_name),
            valueHost = platformForCreate,
            valueFiledNameOfHost = PlatformForCreate.FILED_NAME_PACKAGE,
            value = platformForCreate.packageName,
            onFiledChanged = onApplicationForCreateFiledChanged
        )

        TextOrTextFiled(
            modifier = Modifier.heightIn(min = 120.dp),
            isField = (createStep == ApplicationForCreate.CREATE_STEP_APPLICATION ||
                    createStep == ApplicationForCreate.CREATE_STEP_PLATFORM),
            placeHolderText = stringResource(id = xcj.app.appsets.R.string.application_introduction),
            valueHost = platformForCreate,
            valueFiledNameOfHost = PlatformForCreate.FILED_NAME_INTRODUCTION,
            value = platformForCreate.introduction,
            onFiledChanged = onApplicationForCreateFiledChanged
        )
        val appCreationUseCase = LocalUseCaseOfAppCreation.current
        if (createStep == ApplicationForCreate.CREATE_STEP_APPLICATION ||
            createStep == ApplicationForCreate.CREATE_STEP_PLATFORM ||
            createStep == ApplicationForCreate.CREATE_STEP_VERSION
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "版本信息(${platformForCreate.versionInfoForCreates.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                SuggestionChip(
                    onClick = {
                        appCreationUseCase.addVersionInfoForCreate(platformForCreate)
                    },
                    label = {
                        Icon(
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_add_24),
                            contentDescription = "add custom platform"
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = CircleShape
                )
            }
        }
        if (versionInfo != null) {
            val versionInfoForCreate =
                appCreationUseCase.getVersionInfoForCreateById(
                    platformForCreate,
                    versionInfo.id ?: ""
                )
            if (versionInfoForCreate != null) {
                VersionForPlatform(
                    createStep = createStep,
                    platformForCreate = platformForCreate,
                    versionInfoForCreate = versionInfoForCreate,
                    versionInfoForCreateIndex = 0,
                    onApplicationForCreateFiledChanged = onApplicationForCreateFiledChanged,
                    onChoosePictureClick = onChoosePictureClick
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.no_version_information),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            if (platformForCreate.versionInfoForCreates.isNotEmpty()) {
                val pagerState =
                    rememberPagerState(0) {
                        platformForCreate.versionInfoForCreates.size
                    }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        Modifier
                            .wrapContentHeight()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val color =
                                if (pagerState.currentPage == iteration)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(4.dp)
                            )
                        }
                    }
                    HorizontalPager(state = pagerState) { index ->
                        val versionInfo = platformForCreate.versionInfoForCreates[index]
                        VersionForPlatform(
                            createStep = createStep,
                            platformForCreate = platformForCreate,
                            versionInfoForCreate = versionInfo,
                            versionInfoForCreateIndex = index,
                            onApplicationForCreateFiledChanged = onApplicationForCreateFiledChanged,
                            onChoosePictureClick = onChoosePictureClick
                        )
                    }
                }

            } else {
                Row(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.no_version_information),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VersionForPlatform(
    createStep: String,
    platformForCreate: PlatformForCreate,
    versionInfoForCreate: VersionInfoForCreate,
    versionInfoForCreateIndex: Int,
    onApplicationForCreateFiledChanged: (Any, String, String) -> Unit,
    onChoosePictureClick: (Any, String, UriProvider?) -> Unit,
) {
    Column {
        DesignVDivider()
        Spacer(modifier = Modifier.height(6.dp))
        val appCreationUseCase = LocalUseCaseOfAppCreation.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val versionOfXStringTemplate = stringResource(xcj.app.appsets.R.string.version_of_x)
            Text(
                text = String.format(versionOfXStringTemplate, versionInfoForCreateIndex + 1),
                modifier = Modifier.align(
                    Alignment.CenterStart
                )
            )
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = {
                        appCreationUseCase.deleteVersionInPlatform(
                            platformForCreate,
                            versionInfoForCreate
                        )
                    })
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.delete),
                        fontSize = 12.sp
                    )
                }
            }
        }
        IconAndBanner(
            createStep = createStep,
            showSelect = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            valueHost = versionInfoForCreate,
            iconUseAge = "app_version_icon",
            bannerUseAge = "app_version_banner_icon",
            iconUriHolder = versionInfoForCreate.versionIconUriHolder,
            bannerUriProvider = versionInfoForCreate.versionBannerUriHolder,
            onChoosePictureClick = onChoosePictureClick
        )
        TextOrTextFiled(
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(xcj.app.appsets.R.string.version),
            valueHost = versionInfoForCreate,
            valueFiledNameOfHost = VersionInfoForCreate.FILED_NAME_VERSION,
            value = versionInfoForCreate.version,
            onFiledChanged = onApplicationForCreateFiledChanged
        )
        TextOrTextFiled(
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(xcj.app.appsets.R.string.version_code),
            value = versionInfoForCreate.versionCode,
            valueHost = versionInfoForCreate,
            valueFiledNameOfHost = VersionInfoForCreate.FILED_NAME_VERSION_CODE,
            onFiledChanged = onApplicationForCreateFiledChanged
        )
        TextOrTextFiled(
            modifier = Modifier.heightIn(min = 120.dp),
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(id = xcj.app.appsets.R.string.version_changes),
            valueHost = versionInfoForCreate,
            valueFiledNameOfHost = VersionInfoForCreate.FILED_NAME_CHANGES,
            value = versionInfoForCreate.changes,
            onFiledChanged = onApplicationForCreateFiledChanged
        )
        TextOrTextFiled(
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(xcj.app.appsets.R.string.package_size_of_version),
            valueHost = versionInfoForCreate,
            valueFiledNameOfHost = VersionInfoForCreate.FILED_NAME_PACKAGE_SIZE,
            value = versionInfoForCreate.packageSize,
            onFiledChanged = onApplicationForCreateFiledChanged
        )
        TextOrTextFiled(
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(xcj.app.appsets.R.string.privacy_link_url),
            valueHost = versionInfoForCreate,
            valueFiledNameOfHost = VersionInfoForCreate.FILED_NAME_PRIVACY_POLICY_URL,
            value = versionInfoForCreate.privacyPolicyUrl,
            onFiledChanged = onApplicationForCreateFiledChanged
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            if (createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD) {
                Row(
                    modifier = Modifier
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.screenshots),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    SuggestionChip(
                        onClick = {
                            val screenshotInfoForCreate =
                                appCreationUseCase.addScreenshotForCreate(
                                    platformForCreate,
                                    versionInfoForCreate
                                )
                            onChoosePictureClick(
                                screenshotInfoForCreate,
                                ScreenshotInfoForCreate.FILED_NAME_URI_HOLDER,
                                screenshotInfoForCreate.uriHolder
                            )
                        },
                        label = {
                            Icon(
                                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_add_24),
                                contentDescription = "add custom platform"
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = CircleShape
                    )
                }
            }
            if (versionInfoForCreate.screenshotInfoForCreates.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .height(200.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    versionInfoForCreate.screenshotInfoForCreates.forEachIndexed { index, screenShot ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.shapes.extraLarge
                                    )
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .clickable(
                                        onClick = {
                                            appCreationUseCase.deleteScreenInfoInVersion(
                                                platformForCreate,
                                                versionInfoForCreate,
                                                screenShot
                                            )
                                        }
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(id = xcj.app.appsets.R.string.delete),
                                    fontSize = 12.sp
                                )
                            }
                            AnyImage(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(
                                        MaterialTheme.colorScheme.outline,
                                        MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium),
                                any = screenShot.uriHolder?.provideUri()
                            )
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
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.add_screenshot_information),
                            fontSize = 12.sp
                        )
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
                    text = stringResource(xcj.app.appsets.R.string.download_link_url),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                SuggestionChip(
                    onClick = {
                        appCreationUseCase.addDownloadInfoForCreate(
                            platformForCreate,
                            versionInfoForCreate
                        )
                    },
                    label = {
                        Icon(
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_add_24),
                            contentDescription = "add custom platform"
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = CircleShape
                )
            }
            if (versionInfoForCreate.downloadInfoForCreates.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    versionInfoForCreate.downloadInfoForCreates.forEachIndexed { index, downloadInfo ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.shapes.extraLarge
                                    )
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .clickable(onClick = {
                                        appCreationUseCase.deleteDownloadInfoInVersion(
                                            platformForCreate,
                                            versionInfoForCreate,
                                            downloadInfo
                                        )
                                    })
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(id = xcj.app.appsets.R.string.delete),
                                    fontSize = 12.sp
                                )
                            }
                            DesignTextField(
                                value = downloadInfo.url,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onValueChange = {
                                    onApplicationForCreateFiledChanged(
                                        downloadInfo,
                                        DownloadInfoForCreate.FILED_NAME_URL,
                                        it
                                    )
                                },
                                placeholder = {
                                    Text(text = stringResource(xcj.app.appsets.R.string.download_link) + "${index + 1}")
                                },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Uri,
                                    autoCorrectEnabled = true
                                )
                            )
                        }
                    }
                }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.add_link_information),
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TextOrTextFiled(
    modifier: Modifier = Modifier,
    isField: Boolean,
    placeHolderText: String,
    valueHost: Any,
    valueFiledNameOfHost: String,
    value: String,
    onFiledChanged: (Any, String, String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    if (isField) {
        DesignTextField(
            value = value,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            onValueChange = {
                onFiledChanged(valueHost, valueFiledNameOfHost, it)
            },
            placeholder = {
                Text(text = placeHolderText, fontSize = 12.sp)
            },
            keyboardOptions = keyboardOptions
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = placeHolderText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 13.sp)
        }
    }
}