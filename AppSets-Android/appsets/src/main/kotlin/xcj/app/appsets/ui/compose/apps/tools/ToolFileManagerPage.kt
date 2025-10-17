@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.apps.tools

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.apps.tools.file_manager.AbstractFile
import xcj.app.appsets.ui.compose.apps.tools.file_manager.AbstractFileContext
import xcj.app.appsets.ui.compose.apps.tools.file_manager.DefaultFile
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.util.ktx.asComponentActivityOrNull
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.starter.android.util.FileUtil

private const val TAG = "ToolFileManagerPage"

@Composable
fun ToolFileManagerPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit,
    onCreateFileClick: (AbstractFile<*>) -> Unit,
) {
    HideNavBar()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val coroutineScope = rememberCoroutineScope()
    val lifecycleState by lifecycle.currentStateAsState()
    var hasManageStoragePermission by remember {
        mutableStateOf(false)
    }

    var abstractFileContext by remember {
        mutableStateOf<AbstractFileContext?>(null)
    }
    var currentAbstractFile by remember {
        mutableStateOf<AbstractFile<*>?>(null)
    }

    val abstractFileChildren by remember(currentAbstractFile) {
        derivedStateOf<List<AbstractFile<*>>?> {
            currentAbstractFile?.listChildren() as? List<AbstractFile<*>>?
        }
    }
    LaunchedEffect(hasManageStoragePermission) {
        if (hasManageStoragePermission && abstractFileContext == null) {
            val rootAbstractFile = DefaultFile.DefaultFileAbstractFileCreator().create(context)
            abstractFileContext =
                AbstractFileContext(
                    rootAbstractFile = rootAbstractFile,
                    onCurrentChanged = { updateType, abstractFile ->
                        currentAbstractFile = abstractFile
                    }
                )

        }
    }

    LaunchedEffect(lifecycleState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasManageStoragePermission = Environment.isExternalStorageManager()
        } else {
            hasManageStoragePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    var isShowFileCreation by remember {
        mutableStateOf(false)
    }

    var isShowCreateFolderSheet by remember {
        mutableStateOf(false)
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
    )
    {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            )
            {
                Spacer(
                    modifier = Modifier.height(
                        backActionsHeight + 12.dp
                    )
                )
                if (currentAbstractFile != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentAbstractFile?.path() ?: "",
                            maxLines = 1,
                            modifier = Modifier.horizontalScroll(
                                rememberScrollState()
                            )
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 52.dp
                    )
                ) {
                    if (!abstractFileChildren.isNullOrEmpty()) {
                        items(abstractFileChildren!!) { abstractFile ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        onClick = {
                                            abstractFileContext?.setCurrent(
                                                abstractFile,
                                                "push_new"
                                            )
                                        }
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .animateItem(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val icon = if (abstractFile.isFolder()) {
                                    xcj.app.compose_share.R.drawable.ic_folder_24
                                } else {
                                    xcj.app.compose_share.R.drawable.ic_insert_drive_file_24
                                }
                                Icon(painter = painterResource(icon), contentDescription = null)
                                Text(text = abstractFile.name)
                            }
                        }
                    }
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .align(Alignment.BottomCenter)
            )
            {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedVisibility(isShowFileCreation) {
                        FileItemCreation(
                            onCreateButtonClick = { createType ->
                                if (createType == "file_folder") {
                                    isShowCreateFolderSheet = true
                                } else if (createType == "file") {
                                    val abstractFile = currentAbstractFile
                                    if (abstractFile != null) {
                                        onCreateFileClick(abstractFile)
                                    }
                                }
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                    ) {
                        AnimatedVisibility(currentAbstractFile != null && !currentAbstractFile!!.isRoot) {
                            FilledTonalIconButton(
                                onClick = {
                                    abstractFileContext?.navigateUp()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_arrow_back_24),
                                    contentDescription = null
                                )
                            }
                        }
                        FilledTonalIconButton(
                            onClick = {
                                isShowFileCreation = !isShowFileCreation
                            }
                        ) {
                            Icon(
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = if (isShowFileCreation) {
                                        45f
                                    } else {
                                        0f
                                    }
                                },
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                                contentDescription = null
                            )
                        }

                        FilledTonalIconButton(
                            onClick = {
                                //currentFileItem = currentFileItem?.getParent()
                            }
                        ) {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_outline_more_vert_24),
                                contentDescription = null
                            )
                        }
                    }
                }

            }


            if (currentAbstractFile != null &&
                currentAbstractFile!!.isFolder() &&
                abstractFileChildren.isNullOrEmpty()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No files or dictionary")
                }
            }

            if (!hasManageStoragePermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No permission of manage files",
                        modifier = Modifier.clickable(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                if (!Environment.isExternalStorageManager()) {
                                    val intent: Intent =
                                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    val uri: Uri? =
                                        Uri.fromParts("package", context.packageName, null)
                                    intent.setData(uri)
                                    context.startActivity(intent)
                                }
                            } else {
                                context.asComponentActivityOrNull()?.requestPermissions(
                                    arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ),
                                    1001
                                )
                            }
                        })
                    )
                }
            }


            FileItemDetails(
                abstractFile = currentAbstractFile,
                onClose = {
                    abstractFileContext?.navigateUp()
                }
            )
        }

        BackActionTopBar(
            modifier = Modifier.onPlaced {
                backActionBarSize = it.size
            },
            hazeState = hazeState,
            onBackClick = onBackClick,
            backButtonText = stringResource(xcj.app.appsets.R.string.file_manager)
        )
    }

    if (isShowCreateFolderSheet) {
        var newFolderName by remember {
            mutableStateOf("")
        }
        ModalBottomSheet(
            onDismissRequest = {
                isShowCreateFolderSheet = false
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.new_folder))
                DesignTextField(
                    value = newFolderName,
                    onValueChange = {
                        newFolderName = it
                    },
                    placeholder = {
                        Text(text = stringResource(xcj.app.appsets.R.string.name))
                    }
                )
                FilledTonalButton(
                    modifier = Modifier.widthIn(min = TextFieldDefaults.MinWidth),
                    onClick = {
                        isShowCreateFolderSheet = false
                        coroutineScope.launch {
                            val abstractFile = abstractFileContext?.getCurrent()
                            if (abstractFile == null) {
                                return@launch
                            }
                            val isCreated = abstractFile.createFileFolder(newFolderName)
                            if (isCreated) {
                                val newAbstractFile = abstractFile.newInstance()
                                abstractFileContext?.setCurrent(newAbstractFile, "push_update")
                            }
                        }
                    }
                ) {
                    Text(text = stringResource(xcj.app.appsets.R.string.ok))
                }
            }
        }
    }
}

@Composable
fun FileItemDetails(abstractFile: AbstractFile<*>?, onClose: () -> Unit) {
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(animationSpec = tween(350)) + expandVertically(animationSpec = tween(350)),
        exit = fadeOut(animationSpec = tween(350)) + shrinkVertically(animationSpec = tween(350)),
        visible = abstractFile != null && !abstractFile.isFolder()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    FilledTonalIconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_close_24),
                            contentDescription = stringResource(xcj.app.appsets.R.string.close)
                        )
                    }
                }
                if (FileUtil.isImage(abstractFile!!.extension)) {
                    AnyImage(
                        model = abstractFile.asUri(),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(250.dp)
                    )
                }

                Text(
                    text = abstractFile.name,
                    maxLines = 1,
                    overflow = TextOverflow.StartEllipsis,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .widthIn(max = TextFieldDefaults.MinWidth)
                )
            }
        }
    }
}

@Composable
fun FileItemActions(abstractFile: AbstractFile<*>) {

}

@Composable
fun FileItemCreation(onCreateButtonClick: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(
            onClick = {
                onCreateButtonClick("file")
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                    contentDescription = null
                )
                Text(text = "Create File")
            }
        }

        FilledTonalButton(
            onClick = {
                onCreateButtonClick("file_folder")
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_folder_24),
                    contentDescription = null
                )
                Text(text = "Create Folder")
            }
        }
    }
}