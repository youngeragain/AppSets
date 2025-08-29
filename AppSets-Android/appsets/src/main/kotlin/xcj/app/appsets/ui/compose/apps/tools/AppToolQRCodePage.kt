package xcj.app.appsets.ui.compose.apps.tools

import android.graphics.Bitmap
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.appsets.util.encrypt.AESHelper
import xcj.app.appsets.util.encrypt.EncryptionUtil
import xcj.app.appsets.util.encrypt.RSAHelper
import xcj.app.appsets.util.message_digest.MessageDigestUtil
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.DesignTextField
import xcj.app.starter.util.QrCodeUtil
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

sealed class TransformedContent(val transformedContent: String?, val bitmap: Bitmap?) {
    data class SimpleContent(
        val content: String?,
        val qr: Bitmap?
    ) : TransformedContent(content, qr)

    data class AESContent(
        val content: String?,
        val qr: Bitmap?,
        val encryptionResult: AESHelper.EncryptionResult?
    ) : TransformedContent(content, qr)

    data class RSAContent(
        val content: String?,
        val qr: Bitmap?,
        val encryptionResult: RSAHelper.EncryptionResult?
    ) : TransformedContent(content, qr)
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun AppToolQRCodePage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit
) {
    Column {
        BackActionTopBar(
            onBackClick = onBackClick,
            backButtonRightText = stringResource(xcj.app.appsets.R.string.transform_content)
        )
        Box(modifier = Modifier.weight(1f)) {
            val coroutineScope = rememberCoroutineScope()
            var transformType by rememberSaveable {
                mutableStateOf("None")
            }
            var content by rememberSaveable {
                val initString: String =
                    if (quickStepContents.isNullOrEmpty()) {
                        ""
                    } else {
                        val textQuickStepContents =
                            quickStepContents.filterIsInstance<TextQuickStepContent>()
                        if (!textQuickStepContents.isEmpty()) {
                            val content = textQuickStepContents.joinToString { it.text }
                            content
                        } else {
                            ""
                        }
                    }
                mutableStateOf(initString)
            }

            var transformedContent: TransformedContent by remember {
                mutableStateOf(TransformedContent.SimpleContent("", null))
            }


            val transformRunnable: suspend CoroutineScope.() -> Unit = remember {
                {
                    if (content.isNotEmpty()) {
                        when (transformType) {
                            "MD5" -> {
                                val outContent =
                                    MessageDigestUtil.transformWithMD5(content)?.outContent
                                val qrBitmap = outContent?.let {
                                    QrCodeUtil.encodeAsBitmap(it)
                                }
                                transformedContent = TransformedContent.SimpleContent(
                                    outContent,
                                    qrBitmap
                                )
                            }

                            "SHA2" -> {
                                val outContent =
                                    MessageDigestUtil.transformWithSHA256(content)?.outContent
                                val qrBitmap = outContent?.let {
                                    QrCodeUtil.encodeAsBitmap(it)
                                }
                                transformedContent = TransformedContent.SimpleContent(
                                    outContent,
                                    qrBitmap
                                )
                            }

                            "Base64" -> {
                                val outContent = Base64.encode(content.encodeToByteArray())
                                val qrBitmap = QrCodeUtil.encodeAsBitmap(outContent)
                                transformedContent = TransformedContent.SimpleContent(
                                    outContent,
                                    qrBitmap
                                )
                            }

                            "AES", "DES" -> {
                                val encryptionResult = if (transformType == "AES") {
                                    EncryptionUtil.encryptWithAES(content)
                                } else {
                                    EncryptionUtil.encryptWithDES(content)
                                }
                                val outContent = encryptionResult?.outContentBase64
                                val qrBitmap = outContent?.let {
                                    QrCodeUtil.encodeAsBitmap(it)
                                }
                                transformedContent = TransformedContent.AESContent(
                                    outContent,
                                    qrBitmap,
                                    encryptionResult
                                )
                            }

                            "RSA" -> {
                                val encryptionResult = EncryptionUtil.encryptWithRSA(content)
                                val outContent = encryptionResult?.outContentBase64
                                val qrBitmap = outContent?.let {
                                    QrCodeUtil.encodeAsBitmap(it)
                                }
                                transformedContent = TransformedContent.RSAContent(
                                    outContent,
                                    qrBitmap,
                                    encryptionResult
                                )
                            }

                            else -> {
                                val qrBitmap = QrCodeUtil.encodeAsBitmap(content)
                                transformedContent =
                                    TransformedContent.SimpleContent(content, qrBitmap)
                            }
                        }
                    }
                }
            }

            LaunchedEffect(key1 = transformType, key2 = content) {
                transformRunnable(coroutineScope)
            }

            PortraitQRCodeComponent(
                transformType,
                content,
                transformedContent,
                transformTypeChanged = {
                    transformType = it
                },
                contentChanged = {
                    content = it
                }
            )
        }
    }
}

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun PortraitQRCodeComponent(
    transformType: String,
    content: String,
    transformedContent: TransformedContent,
    transformTypeChanged: (String) -> Unit,
    contentChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val pagerState = rememberPagerState {
            2
        }
        PageIndicator(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(12.dp),
            pagerState = pagerState
        )
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState
        ) { pageIndex ->
            if (pageIndex == 0) {
                TransformedQRPage(
                    transformType,
                    content,
                    transformedContent,
                    transformTypeChanged,
                    contentChanged
                )
            } else {
                TransformedContentPage(
                    transformType,
                    content,
                    transformedContent
                )
            }
        }
    }
}

@Composable
fun PageIndicator(modifier: Modifier, pagerState: PagerState) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color =
                if (pagerState.currentPage == iteration) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(height = 2.dp, width = 20.dp)
            )
        }
    }
}

@Composable
fun TransformedQRPage(
    transformType: String,
    content: String,
    transformedContent: TransformedContent,
    transformTypeChanged: (String) -> Unit,
    contentChanged: (String) -> Unit,
) {
    var qrCodeSize by remember {
        mutableIntStateOf(256)
    }
    val sizeOfQRState by animateIntAsState(targetValue = qrCodeSize)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            AnyImage(
                model = transformedContent.bitmap,
                modifier = Modifier
                    .size(sizeOfQRState.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (transformedContent.bitmap != null) {
                var sliderPosition by remember { mutableFloatStateOf(0.5f) }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
                            qrCodeSize = (200 + (256 * it).toInt())
                        }
                    )
                }
            }
            val transformTypes = remember {
                listOf("None", "MD5", "Base64", "SHA2", "DES", "AES", "RSA")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                transformTypes.forEach { type ->
                    Row {
                        FilterChip(
                            selected = transformType == type,
                            onClick = {
                                transformTypeChanged(type)
                            },
                            label = {
                                Text(text = type)
                            },
                            shape = CircleShape
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = content,
                    placeholder = {
                        Text(stringResource(xcj.app.appsets.R.string.text_based_content))
                    },
                    onValueChange = {
                        contentChanged(it)
                    },
                    maxLines = 3
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun TransformedContentPage(
    transformType: String,
    content: String,
    transformedContent: TransformedContent,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
        ) {
            SelectionContainer {
                Text(
                    text = transformedContent.transformedContent ?: "",
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            when (transformedContent) {
                is TransformedContent.RSAContent -> {
                    DesignHDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SuggestionChip(label = {
                            Text(text = "Base64 Content", fontSize = 10.sp)
                        }, onClick = {})
                        SuggestionChip(label = {
                            Text(
                                text = transformedContent.encryptionResult?.encryptTransformation
                                    ?: "", fontSize = 10.sp
                            )
                        }, onClick = {})
                    }
                }

                is TransformedContent.AESContent -> {
                    DesignHDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SuggestionChip(label = {
                            Text(text = "Base64 Content", fontSize = 10.sp)
                        }, onClick = {})
                        SuggestionChip(label = {
                            Text(
                                text = transformedContent.encryptionResult?.encryptTransformation
                                    ?: "", fontSize = 10.sp
                            )
                        }, onClick = {})
                    }
                }

                else -> Unit
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.width(2.dp))
            when (transformType) {
                "DES", "AES" -> {
                    SecretFileComponent(
                        name = stringResource(xcj.app.appsets.R.string.private_key),
                        file = (transformedContent as? TransformedContent.AESContent)?.encryptionResult?.privateKeyFile,
                        onClick = {

                        }
                    )
                }

                "RSA" -> {
                    SecretFileComponent(
                        name = stringResource(xcj.app.appsets.R.string.private_key),
                        file = (transformedContent as? TransformedContent.RSAContent)?.encryptionResult?.privateKeyFile,
                        onClick = {}
                    )
                    SecretFileComponent(
                        name = stringResource(xcj.app.appsets.R.string.public_key),
                        file = (transformedContent as? TransformedContent.RSAContent)?.encryptionResult?.publicKeyFile,
                        onClick = {}
                    )
                }
            }
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}

@Composable
fun SecretFileComponent(name: String, file: File?, onClick: () -> Unit) {
    var fileContentShow by remember {
        mutableStateOf(false)
    }
    var fileContent: String? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(key1 = file) {
        fileContentShow = false
        fileContent = null
    }

    LaunchedEffect(key1 = fileContentShow) {
        launch(Dispatchers.IO) {
            if (fileContentShow) {
                fileContent = file?.readText()
            } else {
                fileContent = null
            }
        }
    }
    Column(
        Modifier
            .widthIn(max = 200.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.shapes.extraLarge
            )
            .clickable(onClick = {
                fileContentShow = !fileContentShow
                onClick()
            })
            .padding(
                start = 12.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 12.dp
            )
            .animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                contentDescription = "Private key file"
            )
            Column() {
                Text(text = name, fontSize = 10.sp)
                file?.let {
                    Text(
                        text = it.name,
                        fontSize = 8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (fileContent != null) {
            DesignHDivider()
            Column(
                modifier = Modifier
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SelectionContainer {
                    Text(text = fileContent ?: "", fontSize = 8.sp)
                }
            }
        }
    }
}