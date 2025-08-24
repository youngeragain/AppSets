package xcj.app.appsets.ui.compose.apps.tools

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.util.ktx.asComponentActivityOrNull
import xcj.app.compose_share.components.BackActionTopBar
import java.io.File

private const val TAG = "AppToolFileManagerPage"

@Composable
fun AppToolFileManagerPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val lifecycleState by lifecycle.currentStateAsState()
    var hasManageStoragePermission by remember {
        mutableStateOf(false)
    }

    val rootFileItem: FileItem? by remember(hasManageStoragePermission) {
        derivedStateOf {
            if (!hasManageStoragePermission) {
                null
            } else {
                FileItem.DefaultFileItemCreator(context).create()
            }
        }
    }
    var currentFileItem: FileItem? by remember {
        mutableStateOf(null)
    }
    val fileItemChildren: List<FileItem>? by remember(currentFileItem) {
        derivedStateOf {
            currentFileItem?.listChildren()
        }
    }

    LaunchedEffect(rootFileItem) {
        if (rootFileItem != null && currentFileItem == null) {
            currentFileItem = rootFileItem
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

    Column {
        BackActionTopBar(
            onBackClick = onBackClick,
            backButtonRightText = stringResource(xcj.app.appsets.R.string.file_manager)
        )

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                contentPadding = PaddingValues(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 52.dp
                )
            ) {
                if (currentFileItem != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentFileItem?.file?.path ?: "",
                                maxLines = 1,
                                modifier = Modifier.horizontalScroll(
                                    rememberScrollState()
                                )
                            )
                        }

                    }
                }

                if (!fileItemChildren.isNullOrEmpty()) {
                    items(fileItemChildren!!) { fileItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = {
                                        if (fileItem.file.isDirectory) {
                                            currentFileItem = fileItem
                                        }
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .animateItem(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val icon = if (fileItem.file.isDirectory) {
                                xcj.app.compose_share.R.drawable.ic_folder_24
                            } else {
                                xcj.app.compose_share.R.drawable.ic_insert_drive_file_24
                            }
                            Icon(painter = painterResource(icon), contentDescription = null)
                            Text(text = fileItem.file.name)
                        }
                    }
                }
            }
            if (currentFileItem != null && fileItemChildren.isNullOrEmpty()) {
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
                                    // 启动这个Intent
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .animateContentSize()
                ) {
                    if (currentFileItem != null && !currentFileItem!!.isRoot) {
                        FilledTonalIconButton(
                            onClick = {
                                currentFileItem = currentFileItem?.getParent()
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
                            //currentFileItem = currentFileItem?.getParent()
                        }
                    ) {
                        Icon(
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

    }
}

data class FileItem(
    val file: File,
    val accessible: Boolean = true,
    var parentFileItem: FileItem? = null
) {
    val isRoot: Boolean
        get() = parentFileItem == null || parentFileItem == this

    fun getParent(): FileItem? {
        return parentFileItem
    }

    fun listChildren(): List<FileItem>? {
        if (!file.isDirectory) {
            return null
        }
        val files = file.listFiles()
        return files?.map {
            val fileItem = FileItem(it)
            fileItem.parentFileItem = this
            fileItem
        }
    }

    interface FileItemCreator {
        fun create(): FileItem
    }

    class DefaultFileItemCreator(private val context: Context) : FileItemCreator {
        override fun create(): FileItem {
            val storageDirectory = Environment.getExternalStorageDirectory()
            val fileItem = FileItem(
                storageDirectory,
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            )
            fileItem.parentFileItem = fileItem
            return fileItem
        }
    }
}