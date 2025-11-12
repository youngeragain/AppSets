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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.server.model.AppPlatform
import xcj.app.appsets.server.model.VersionInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfAppCreation
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.ui.model.PlatformForCreate
import xcj.app.appsets.ui.model.VersionInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateApplicationPageUIState
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.RuntimeSingleStateUpdater
import xcj.app.appsets.util.ktx.toast
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.DesignVDivider
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.android.util.UriProvider

private const val TAG = "CreateAppPage"

@UnstableApi
@Preview(showBackground = true)
@Composable
fun CreateAppPagePreview() {
    val createApplicationPageUIState by remember {
        mutableStateOf(CreateApplicationPageUIState.CreateStart())
    }
    val applicationForCreate by remember {
        mutableStateOf(ApplicationForCreate())
    }
    val mainViewModel = remember {
        MainViewModel()
    }
    CompositionLocalProvider(
        LocalUseCaseOfAppCreation provides mainViewModel.appCreationUseCase,
        LocalVisibilityComposeStateProvider provides mainViewModel
    ) {
        CreateAppPage(
            createApplicationPageUIState = createApplicationPageUIState,
            applicationForCreate = applicationForCreate,
            onBackClick = {},
            onChoosePictureClick = { requestKey, requestMaxCount, composeStateUpdater -> },
            onConfirmClick = { applicationForCreate ->

            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun CreateAppPage(
    createStep: String = ApplicationForCreate.CREATE_STEP_APPLICATION,
    platform: AppPlatform? = null,
    versionInfo: VersionInfo? = null,
    createApplicationPageUIState: CreateApplicationPageUIState,
    applicationForCreate: ApplicationForCreate,
    onBackClick: () -> Unit,
    onChoosePictureClick: (String, Int, ComposeStateUpdater<*>) -> Unit,
    onConfirmClick: (ApplicationForCreate) -> Unit,
) {
    HideNavBar()
    val appCreationUseCase = LocalUseCaseOfAppCreation.current
    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
    DisposableEffect(key1 = true) {
        onDispose {
            appCreationUseCase.onComposeDispose("page dispose")
        }
    }
    LaunchedEffect(createApplicationPageUIState) {
        if (createApplicationPageUIState is CreateApplicationPageUIState.CreateSuccess) {
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
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .imePadding()
        ) {

            var newPlatformName: String? by remember {
                mutableStateOf(null)
            }
            val platformForCreate by remember {
                derivedStateOf {
                    if (platform != null) {
                        appCreationUseCase.getPlatformForCreateById(
                            applicationForCreate,
                            platform.id ?: ""
                        )
                    } else {
                        appCreationUseCase.getPlatformForCreateByName(
                            applicationForCreate,
                            newPlatformName
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier, contentPadding = PaddingValues(
                    top = backActionsHeight + 12.dp
                )
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth()
                    ) {
                        IconAndBanner(
                            createStep = createStep,
                            showSelect = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            iconKey = "app_icon",
                            bannerKey = "banner_icon",
                            iconUriProviderState = applicationForCreate.iconUriProvider,
                            bannerUriProviderState = applicationForCreate.bannerUriProvider,
                            onChoosePictureClick = onChoosePictureClick
                        )
                        TextOrTextFiled(
                            isField = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            placeHolderText = stringResource(xcj.app.appsets.R.string.application_name),
                            value = applicationForCreate.name.value,
                            onValueChange = {
                                applicationForCreate.name.value = it
                            }
                        )
                        TextOrTextFiled(
                            isField = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            placeHolderText = stringResource(xcj.app.appsets.R.string.app_types),
                            value = applicationForCreate.category.value,
                            onValueChange = {
                                applicationForCreate.category.value = it
                            }
                        )
                        TextOrTextFiled(
                            isField = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            placeHolderText = stringResource(xcj.app.appsets.R.string.website),
                            value = applicationForCreate.website.value,
                            onValueChange = {
                                applicationForCreate.website.value = it
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Uri,
                                autoCorrectEnabled = true
                            )
                        )
                        TextOrTextFiled(
                            modifier = Modifier.heightIn(min = 120.dp),
                            isField = createStep == ApplicationForCreate.CREATE_STEP_APPLICATION,
                            placeHolderText = stringResource(xcj.app.appsets.R.string.developer_information),
                            value = applicationForCreate.developerInfo.value,
                            onValueChange = {
                                applicationForCreate.developerInfo.value = it
                            }
                        )
                        var free by remember {
                            mutableStateOf(true)
                        }
                        LaunchedEffect(key1 = true, block = {
                            free = applicationForCreate.price.value.isEmpty()
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
                                    value = applicationForCreate.price.value,
                                    onValueChange = {
                                        applicationForCreate.price.value = it
                                    },
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                                )
                                /*LaunchedEffect(key1 = free, block = {
                                    onApplicationForCreateFiledChanged(
                                        applicationForCreate,
                                        ApplicationForCreate.FILED_NAME_PRICE_UNIT,
                                        ApplicationForCreate.DEFAULT_PRICE_UNIT
                                    )
                                })*/
                                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                                    FilterChip(
                                        selected = applicationForCreate.priceUnit.value == ApplicationForCreate.PRICE_UNIT_RMB,
                                        onClick = {
                                            applicationForCreate.priceUnit.value =
                                                ApplicationForCreate.PRICE_UNIT_RMB
                                        },
                                        label = {
                                            Text(text = ApplicationForCreate.PRICE_UNIT_RMB)
                                        },
                                        shape = CircleShape
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    FilterChip(
                                        selected = applicationForCreate.priceUnit.value == ApplicationForCreate.PRICE_UNIT_USD,
                                        onClick = {
                                            applicationForCreate.priceUnit.value =
                                                ApplicationForCreate.PRICE_UNIT_USD
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
                                    text = "${applicationForCreate.price.value} ${applicationForCreate.priceUnit.value}",
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
                                    applicationForCreate.platformForCreates.map { it.name.value }
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
                                if (platformName == platformForCreate?.name?.value) {
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
                                            if (platformForCreate?.name?.value == platformName) {
                                                selectedPlatformNames.remove(platformName)
                                                appCreationUseCase.removePlatformForCreateByName(
                                                    applicationForCreate,
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
                            IconButton(
                                onClick = {
                                    val bottomSheetState =
                                        visibilityComposeStateProvider.bottomSheetState()
                                    bottomSheetState.show(null) {
                                        CustomPlatformAddSheetContent(
                                            platformNames = platformNames,
                                            onConfirmClick = {
                                                bottomSheetState.hide()
                                            }
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                                    contentDescription = stringResource(xcj.app.appsets.R.string.add)
                                )
                            }
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

        val titleText =
            if (createStep != ApplicationForCreate.CREATE_STEP_APPLICATION) {
                stringResource(xcj.app.appsets.R.string.template_add_xxx, createStep)
            } else {
                stringResource(xcj.app.appsets.R.string.create_application)
            }
        BackActionTopBar(
            modifier = Modifier.onPlaced {
                backActionBarSize = it.size
            },
            hazeState = hazeState,
            onBackClick = onBackClick,
            backButtonText = titleText,
            endButtonText = stringResource(id = xcj.app.appsets.R.string.ok),
            onEndButtonClick = {
                onConfirmClick(applicationForCreate)
            }
        )

        CreateApplicationIndicator(createApplicationPageUIState = createApplicationPageUIState)
    }
}

@Composable
fun CreateApplicationIndicator(createApplicationPageUIState: CreateApplicationPageUIState) {
    AnimatedVisibility(
        visible = createApplicationPageUIState is CreateApplicationPageUIState.Creating,
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
                        ContextCompat.getString(
                            context,
                            xcj.app.appsets.R.string.platform_already_exists
                        ).toast()
                        return@FilledTonalButton
                    }
                    platformNames.add(customPlatform)
                    onConfirmClick()

                }
            ) {
                Text(text = stringResource(id = xcj.app.appsets.R.string.ok))
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

/**
 * @param onChoosePictureClick param1:requestKey, param2:composeStateUpdater
 */
@Composable
fun IconAndBanner(
    createStep: String,
    showSelect: Boolean,
    iconKey: String,
    iconUriProviderState: MutableState<UriProvider?>,
    bannerKey: String,
    bannerUriProviderState: MutableState<UriProvider?>,
    onChoosePictureClick: (String, Int, ComposeStateUpdater<*>) -> Unit,
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
                        if (iconKey == "app_icon") {
                            if (createStep != ApplicationForCreate.CREATE_STEP_APPLICATION)
                                return@clickable
                        } else if (iconKey == "app_version_icon") {
                            if (createStep == ApplicationForCreate.CREATE_STEP_SCREENSHOT ||
                                createStep == ApplicationForCreate.CREATE_STEP_DOWNLOAD
                            )
                                return@clickable
                        }

                        val composeStateUpdater =
                            RuntimeSingleStateUpdater.fromState(iconUriProviderState) { markKey, input ->
                                PurpleLogger.current.d(
                                    TAG,
                                    "$iconKey/iconUriProviderState, inputHandleDSL:\nmarkKey:$markKey,\ninput:$input"
                                )
                                if (input !is ContentSelectionResult.RichMediaContentSelectionResult) {
                                    return@fromState
                                }
                                val uriProviders = input.selectedProvider.provide()
                                if (uriProviders.isEmpty()) {
                                    return@fromState
                                }
                                update(uriProviders.first())
                            }
                        onChoosePictureClick(
                            iconKey,
                            1,
                            composeStateUpdater
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                val uri =
                    iconUriProviderState.value?.provideUri()
                if (uri != null) {
                    AnyImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.extraLarge),
                        model = uri
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
                        if (bannerKey == "banner_icon") {
                            if (createStep != ApplicationForCreate.CREATE_STEP_APPLICATION)
                                return@clickable
                        } else if (bannerKey == "app_version_banner_icon") {
                            if (createStep == ApplicationForCreate.CREATE_STEP_SCREENSHOT ||
                                createStep == ApplicationForCreate.CREATE_STEP_DOWNLOAD
                            ) {
                                return@clickable
                            }
                        }
                        val composeStateUpdater =
                            RuntimeSingleStateUpdater.fromState(bannerUriProviderState) { markKey, input ->
                                PurpleLogger.current.d(
                                    TAG,
                                    "$bannerKey/bannerUriProviderState, inputHandleDSL:\nmarkKey:$markKey,\ninput:$input"
                                )
                                if (input !is ContentSelectionResult.RichMediaContentSelectionResult) {
                                    return@fromState
                                }
                                val uriProviders = input.selectedProvider.provide()
                                if (uriProviders.isEmpty()) {
                                    return@fromState
                                }
                                update(uriProviders.first())
                            }
                        onChoosePictureClick(
                            bannerKey,
                            1,
                            composeStateUpdater
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                val uri =
                    bannerUriProviderState.value?.provideUri()
                if (uri != null) {
                    AnyImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.extraLarge),
                        model = uri
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
            Text(text = stringResource(xcj.app.appsets.R.string.banner), fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlatformForApp(
    createStep: String,
    platformForCreate: PlatformForCreate,
    versionInfo: VersionInfo? = null,
    onChoosePictureClick: (String, Int, ComposeStateUpdater<*>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = "For ${platformForCreate.name.value}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        TextOrTextFiled(
            isField = (createStep == ApplicationForCreate.CREATE_STEP_APPLICATION ||
                    createStep == ApplicationForCreate.CREATE_STEP_PLATFORM),
            placeHolderText = stringResource(xcj.app.appsets.R.string.package_name),
            value = platformForCreate.packageName.value,
            onValueChange = {
                platformForCreate.packageName.value = it
            }
        )

        TextOrTextFiled(
            modifier = Modifier.heightIn(min = 120.dp),
            isField = (createStep == ApplicationForCreate.CREATE_STEP_APPLICATION ||
                    createStep == ApplicationForCreate.CREATE_STEP_PLATFORM),
            placeHolderText = stringResource(id = xcj.app.appsets.R.string.application_introduction),
            value = platformForCreate.introduction.value,
            onValueChange = {
                platformForCreate.introduction.value = it
            }
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
                    text = stringResource(
                        xcj.app.appsets.R.string.template_version_info_count_xxx,
                        platformForCreate.versionInfoForCreates.size
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(
                    onClick = {
                        appCreationUseCase.addVersionInfoForCreate(platformForCreate)
                    }
                ) {
                    Icon(
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                        contentDescription = stringResource(xcj.app.appsets.R.string.add)
                    )
                }
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
    onChoosePictureClick: (String, Int, ComposeStateUpdater<*>) -> Unit,
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

            IconButton(
                onClick = {
                    appCreationUseCase.deleteVersionInPlatform(
                        platformForCreate,
                        versionInfoForCreate
                    )
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_do_not_disturb_on_24),
                    contentDescription = stringResource(xcj.app.appsets.R.string.remove)
                )
            }
        }
        IconAndBanner(
            createStep = createStep,
            showSelect = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            iconKey = "app_version_icon",
            bannerKey = "app_version_banner_icon",
            iconUriProviderState = versionInfoForCreate.versionIconUriProvider,
            bannerUriProviderState = versionInfoForCreate.versionBannerUriProvider,
            onChoosePictureClick = onChoosePictureClick
        )
        TextOrTextFiled(
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(xcj.app.appsets.R.string.version),
            value = versionInfoForCreate.version.value,
            onValueChange = {
                versionInfoForCreate.version.value = it
            }
        )
        TextOrTextFiled(
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(xcj.app.appsets.R.string.version_code),
            value = versionInfoForCreate.versionCode.value,
            onValueChange = {
                versionInfoForCreate.versionCode.value = it
            }
        )
        TextOrTextFiled(
            modifier = Modifier.heightIn(min = 120.dp),
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(id = xcj.app.appsets.R.string.version_changes),
            value = versionInfoForCreate.changes.value,
            onValueChange = {
                versionInfoForCreate.changes.value = it
            }
        )
        TextOrTextFiled(
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(xcj.app.appsets.R.string.package_size_of_version),
            value = versionInfoForCreate.packageSize.value,
            onValueChange = {
                versionInfoForCreate.packageSize.value = it
            }
        )
        TextOrTextFiled(
            isField = createStep != ApplicationForCreate.CREATE_STEP_DOWNLOAD,
            placeHolderText = stringResource(xcj.app.appsets.R.string.privacy_link_url),
            value = versionInfoForCreate.privacyPolicyUrl.value,
            onValueChange = {
                versionInfoForCreate.privacyPolicyUrl.value = it
            }
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
                    IconButton(
                        onClick = {
                            val composeStateUpdater =
                                RuntimeSingleStateUpdater.fromState<Void>(null) { markKey, input ->
                                    PurpleLogger.current.d(
                                        TAG,
                                        "screenshotInfoForCreate.pictureUriProvider, inputHandleDSL:\nmarkKey:$markKey,\ninput:$input"
                                    )
                                    if (input !is ContentSelectionResult.RichMediaContentSelectionResult) {
                                        return@fromState
                                    }
                                    val uriProviders = input.selectedProvider.provide()
                                    if (uriProviders.isEmpty()) {
                                        return@fromState
                                    }
                                    uriProviders.forEach { uriProvider ->
                                        val screenshotInfoForCreate =
                                            appCreationUseCase.addScreenshotForCreate(
                                                versionInfoForCreate
                                            )
                                        screenshotInfoForCreate.pictureUriProvider.value =
                                            uriProvider
                                    }
                                }
                            onChoosePictureClick(
                                "CHOOSE_APP_VERSION_SCREEN_SHOT_PICTURE",
                                10,
                                composeStateUpdater
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                            contentDescription = stringResource(xcj.app.appsets.R.string.add)
                        )
                    }
                }
            }
            if (versionInfoForCreate.screenshotInfoForCreates.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(
                        items = versionInfoForCreate.screenshotInfoForCreates,
                        key = { item -> item.id }
                    ) { screenshotInfoForCreate ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .animateItem()
                        ) {
                            IconButton(
                                onClick = {
                                    appCreationUseCase.deleteScreenShotInfoInVersion(
                                        versionInfoForCreate,
                                        screenshotInfoForCreate
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_do_not_disturb_on_24),
                                    contentDescription = stringResource(xcj.app.appsets.R.string.remove)
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
                                model = screenshotInfoForCreate.pictureUriProvider.value?.provideUri()
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
                IconButton(
                    onClick = {
                        appCreationUseCase.addDownloadInfoForCreate(
                            versionInfoForCreate
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                        contentDescription = stringResource(xcj.app.appsets.R.string.add)
                    )
                }
            }
            if (versionInfoForCreate.downloadInfoForCreates.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    versionInfoForCreate.downloadInfoForCreates.forEachIndexed { index, downloadInfo ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DesignTextField(
                                value = downloadInfo.url.value,
                                modifier = Modifier
                                    .weight(1f),
                                onValueChange = {
                                    downloadInfo.url.value = it
                                },
                                placeholder = {
                                    Text(text = stringResource(xcj.app.appsets.R.string.download_link) + "${index + 1}")
                                },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Uri,
                                    autoCorrectEnabled = true
                                )
                            )
                            IconButton(
                                onClick = {
                                    appCreationUseCase.deleteDownloadInfoInVersion(
                                        versionInfoForCreate,
                                        downloadInfo
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_do_not_disturb_on_24),
                                    contentDescription = stringResource(xcj.app.appsets.R.string.remove)
                                )
                            }
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
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    if (isField) {
        DesignTextField(
            value = value,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            onValueChange = onValueChange,
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