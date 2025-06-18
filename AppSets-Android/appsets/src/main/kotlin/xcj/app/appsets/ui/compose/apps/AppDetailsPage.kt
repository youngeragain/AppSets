@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.appsets.ui.compose.apps

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.PlatForm
import xcj.app.appsets.server.model.VersionInfo
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBottomBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.compose.theme.BigAvatarShape
import xcj.app.compose_share.components.BackActionTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsPage(
    application: Application?,
    onBackClick: () -> Unit,
    onGetApplicationClick: (Application) -> Unit,
    onShowApplicationCreatorClick: (Application) -> Unit,
    onAddPlatformInfoClick: (PlatForm?) -> Unit,
    onAddVersionInfoClick: (PlatForm) -> Unit,
    onAddScreenshotInfoClick: (PlatForm, VersionInfo) -> Unit,
    onAddDownloadInfoClick: (PlatForm, VersionInfo) -> Unit,
    onJoinToChatClick: (Application) -> Unit,
) {

    HideNavBarWhenOnLaunch()
    if (application == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.no_corresponding_application_found),
                modifier = Modifier.align(
                    Alignment.Center
                )
            )
            DesignBottomBackButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = onBackClick
            )
        }
    } else {
        val hazeState = remember {
            HazeState()
        }
        var backActionBarSize by remember {
            mutableStateOf(IntSize.Zero)
        }
        Box {
            ApplicationContentComponent(
                backActionBarSize = backActionBarSize,
                hazeState = hazeState,
                application = application,
                onGetApplicationClick = {
                    onGetApplicationClick(application)
                },
                onShowApplicationCreatorClick = {
                    onShowApplicationCreatorClick(application)
                },
                onJoinToChatClick = {
                    onJoinToChatClick(application)
                },
                onAddPlatformInfoClick = { platformPosition ->
                    onAddPlatformInfoClick(
                        application.platforms?.getOrNull(
                            platformPosition
                        )
                    )
                },
                onAddVersionInfoClick = onAddVersionInfoClick,
                onAddScreenshotInfoClick = onAddScreenshotInfoClick,
                onAddDownloadInfoClick = onAddDownloadInfoClick,
            )
            BackActionTopBar(
                modifier = Modifier
                    .onPlaced {
                        backActionBarSize = it.size
                    }
                    .hazeEffect(hazeState, HazeMaterials.thin()),
                centerText = application.name ?: "",
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
fun ApplicationContentComponent(
    modifier: Modifier = Modifier,
    backActionBarSize: IntSize,
    hazeState: HazeState,
    application: Application,
    onGetApplicationClick: () -> Unit,
    onShowApplicationCreatorClick: () -> Unit,
    onJoinToChatClick: () -> Unit,
    onAddPlatformInfoClick: (Int) -> Unit,
    onAddVersionInfoClick: (PlatForm) -> Unit,
    onAddScreenshotInfoClick: (PlatForm, VersionInfo) -> Unit,
    onAddDownloadInfoClick: (PlatForm, VersionInfo) -> Unit,
) {
    val rememberScrollState = rememberScrollState()
    val context = LocalContext.current
    var getApplicationButtonVisible by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = true, block = {
        if (application.hasCurrentPlatformDownloadInfo()) {
            getApplicationButtonVisible = true
        }
    })
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .hazeSource(hazeState)
            .verticalScroll(rememberScrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val density = LocalDensity.current
        val spaceHeight = with(density) {
            backActionBarSize.height.toDp()
        }
        Spacer(Modifier.height(spaceHeight))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            AnyImage(
                modifier = Modifier
                    .size(250.dp)
                    .clip(BigAvatarShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = BigAvatarShape
                    ),
                any = application.bioUrl
            )
            if (getApplicationButtonVisible) {
                Row {
                    FilledTonalButton(
                        onClick = onGetApplicationClick
                    ) {
                        val getButtonText = if (application.price.isNullOrEmpty()) {
                            context.getString(xcj.app.appsets.R.string.get)
                        } else {
                            context.getString(
                                xcj.app.appsets.R.string.get_x,
                                application.price,
                                application.priceUnit
                            )
                        }
                        Text(text = getButtonText, fontSize = 12.sp)
                    }

                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SuggestionChip(
                onClick = onShowApplicationCreatorClick,
                shape = CircleShape,
                label = {
                    Text(text = stringResource(xcj.app.appsets.R.string.creator_information))
                }
            )
            SuggestionChip(
                onClick = onJoinToChatClick,
                shape = CircleShape,
                label = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.chat))
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(12.dp),
        ) {
            Text(text = stringResource(xcj.app.appsets.R.string.type), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = application.category
                    ?: stringResource(xcj.app.appsets.R.string.uncategorized)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = xcj.app.appsets.R.string.website),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = application.website
                    ?: stringResource(id = xcj.app.appsets.R.string.not_offered)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = xcj.app.appsets.R.string.developer_information),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = application.developerInfo
                    ?: stringResource(id = xcj.app.appsets.R.string.not_offered)
            )
        }
        var platformPosition by remember {
            val initPosition = if (application.currentVisiblePlatformPosition != -1) {
                application.currentVisiblePlatformPosition
            } else {
                0
            }
            mutableIntStateOf(initPosition)
        }
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(
                    xcj.app.appsets.R.string.x_platform_information,
                    application.platforms?.size ?: 0
                ),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = {
                        onAddPlatformInfoClick(platformPosition)
                    })
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.add_platform_information),
                    fontSize = 13.sp
                )
            }
        }
        if (!application.platforms.isNullOrEmpty()) {
            val platform by remember {
                derivedStateOf {
                    application.currentVisiblePlatformPosition = platformPosition
                    application.platforms[platformPosition]
                }
            }
            Column(Modifier.fillMaxWidth()) {
                if (application.platforms.size > 1) {
                    PreviousNextComponent(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        startText = null,
                        previousText = stringResource(xcj.app.appsets.R.string.previous_platform),
                        nextText = stringResource(xcj.app.appsets.R.string.next_platform),
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
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun DownloadBottomSheetComponent(application: Application) {
    Column(
        modifier = Modifier
            .heightIn(min = 440.dp)
            .padding(horizontal = 12.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            Row(Modifier.align(Alignment.Center)) {
                AnyImage(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(4.dp)
                        ),
                    any = application.bioUrl
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = application.name ?: "")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (!application.price.isNullOrEmpty()) {
            Column {
                Text(text = stringResource(xcj.app.appsets.R.string.application_paid_download_tips))
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.extraLarge
                        )
                ) {
                    Text(
                        text = "${application.price} ${application.priceUnit}",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        val context = LocalContext.current
        Box(Modifier.fillMaxWidth()) {
            FilledTonalButton(
                onClick = { },
                modifier = Modifier.align(Alignment.Center)
            ) {
                val buttonText = if (application.price.isNullOrEmpty()) {
                    context.getString(xcj.app.appsets.R.string.download)
                } else {
                    context.getString(xcj.app.appsets.R.string.buy)
                }
                Text(text = buttonText)
            }
        }

        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
fun PlatformInfo(
    platform: PlatForm,
    onAddVersionInfoClick: (PlatForm) -> Unit,
    onAddScreenshotInfoClick: (PlatForm, VersionInfo) -> Unit,
    onAddDownloadInfoClick: (PlatForm, VersionInfo) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(xcj.app.appsets.R.string.platform),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(text = platform.name ?: "")

        Text(
            text = stringResource(xcj.app.appsets.R.string.application_package_name),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(text = platform.packageName ?: "", maxLines = 8)

        Text(
            text = stringResource(xcj.app.appsets.R.string.application_introduction),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(text = platform.introduction ?: "", maxLines = 8)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
        ) {
            Text(
                text = stringResource(
                    xcj.app.appsets.R.string.x_version_information,
                    platform.versionInfos?.size ?: 0
                ),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = {
                        onAddVersionInfoClick(platform)
                    })
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.add_version_information),
                    fontSize = 13.sp
                )
            }
        }
        if (!platform.versionInfos.isNullOrEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                var versionPosition by remember {
                    mutableIntStateOf(0)
                }
                val version by remember {
                    derivedStateOf {
                        platform.versionInfos[versionPosition]
                    }
                }
                if (platform.versionInfos.size > 1) {
                    PreviousNextComponent(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        startText = null,
                        previousText = stringResource(xcj.app.appsets.R.string.previous_version),
                        nextText = stringResource(xcj.app.appsets.R.string.next_version),
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
    onAddDownloadInfoClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            text = stringResource(xcj.app.appsets.R.string.version_information),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Column(
            Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(
                    id = xcj.app.appsets.R.string.version_x,
                    version.version ?: ""
                )
            )

            Text(
                text = stringResource(
                    xcj.app.appsets.R.string.version_code_x,
                    version.versionCode ?: ""
                )
            )

            Text(
                text = stringResource(
                    xcj.app.appsets.R.string.version_changes_x,
                    version.changes ?: ""
                )
            )

            Text(
                text = stringResource(
                    xcj.app.appsets.R.string.version_package_size_x,
                    version.packageSize ?: ""
                )
            )
        }

        Box(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.screenshots),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = onAddScreenshotInfoClick)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = xcj.app.appsets.R.string.add_to), fontSize = 13.sp)
            }
        }
        if (!version.screenshotInfos.isNullOrEmpty()) {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                version.screenshotInfos.forEachIndexed { index, screenShot ->
                    val url = screenShot.url
                    if (url != null) {
                        Row {
                            Spacer(modifier = Modifier.width(12.dp))
                            AnyImage(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.shapes.extraLarge
                                    )
                                    .clip(MaterialTheme.shapes.extraLarge),
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
                Text(text = stringResource(xcj.app.appsets.R.string.no_screenshot))
            }
        }
        Box(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(
                    xcj.app.appsets.R.string.download_information_x,
                    version.downloadInfos?.size ?: 0
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = onAddDownloadInfoClick)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = xcj.app.appsets.R.string.add_to), fontSize = 13.sp)
            }
        }

        if (version.downloadInfos.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.no_download_link_provided))

            }
        }
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
                            MaterialTheme.shapes.extraLarge
                        )
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable(onClick = onPreviousClick)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_chevron_left_24),
                        contentDescription = "previous"
                    )
                    Text(
                        text = previousText,
                        fontSize = 13.sp,
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
                            MaterialTheme.shapes.extraLarge
                        )
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable(onClick = onNextClick)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = nextText,
                        fontSize = 13.sp,
                    )
                    Icon(
                        painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_chevron_right_24),
                        contentDescription = "next"
                    )
                }
            }
        }
    }
}