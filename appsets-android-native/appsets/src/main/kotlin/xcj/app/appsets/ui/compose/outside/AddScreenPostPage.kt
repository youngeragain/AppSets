package xcj.app.appsets.ui.compose.outside

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stfalcon.imageviewer.StfalconImageViewer
import xcj.app.appsets.R
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.ui.nonecompose.ui.dialog.ScalableItemAdapter
import xcj.app.appsets.ui.nonecompose.ui.dialog.ScalableItemState
import xcj.app.compose_share.compose.BackActionTopBar

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreenPostPage(
    tabVisibilityState: MutableState<Boolean>,
    onBackAction: (Boolean) -> Unit,
    onConfirmClick: () -> Unit,
    onAutoGenerateClick: () -> Unit,
    onAddMediaContent: (String) -> Unit,
    onRemoveMediaContent: (String, ScalableItemState) -> Unit,
    onVideoPlayClick: (MediaStoreDataUriWrapper) -> Unit,
) {
    val mainViewModel = viewModel<MainViewModel>(LocalContext.current as AppCompatActivity)
    LaunchedEffect(key1 = mainViewModel.screenPostUseCase!!.postFinishState.value, block = {
        if (mainViewModel.screenPostUseCase!!.postFinishState.value) {
            onBackAction(true)
        }
    })
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
            mainViewModel.screenPostUseCase!!.clear()
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    Column(modifier = Modifier.imePadding()) {
        BackActionTopBar(
            backButtonRightText = "添加Screen",
            endButtonText = "确认",
            onBackAction = {
                onBackAction(false)
            },
            onEndButtonClick = onConfirmClick
        )
        val rememberScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .verticalScroll(rememberScrollState)
                .weight(1f)
        ) {
            AnimatedVisibility(visible = mainViewModel.screenPostUseCase!!.posting.value) {
                Column(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(550.dp), contentAlignment = Alignment.Center
                    ) {
                        Column {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "添加中")
                        }
                    }
                    Divider(
                        modifier = Modifier.height(0.5.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Text(text = "状态", Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold)
            val statusTip = if (mainViewModel.screenPostUseCase!!.isPublic.value) {
                "Screen在审核通过后将会随机出现在首页里面!"
            } else {
                "Screen只有自己可见,不必被审核。后面可以选择公开，公开后本条内容需要被审核!"
            }
            Text(
                text = statusTip, fontSize = 11.sp, modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            )
            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth()) {
                FilterChip(
                    selected = mainViewModel.screenPostUseCase!!.isPublic.value,
                    onClick = {
                        mainViewModel.screenPostUseCase!!.isPublic.value = true
                    },
                    label = {
                        Text(text = "公开", fontWeight = FontWeight.Bold)
                    })
                Spacer(modifier = Modifier.width(20.dp))
                FilterChip(
                    selected = !mainViewModel.screenPostUseCase!!.isPublic.value,
                    onClick = {
                        mainViewModel.screenPostUseCase!!.isPublic.value = false
                    },
                    label = {
                        Text(text = "私有", fontWeight = FontWeight.Bold)
                    })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "内容",
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onAutoGenerateClick)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_gesture_24),
                            contentDescription = "use ai generate"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "生成")
                    }
                }
            }
            TextField(
                placeholder = {
                    Text(text = "示例: 今天下雨哦!", fontSize = 11.sp)
                },
                value = mainViewModel.screenPostUseCase!!.content.value ?: "",
                onValueChange = {
                    mainViewModel.screenPostUseCase!!.content.value = it
                },
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "关联的话题", modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold)
            TextField(
                placeholder = {
                    Text(text = "示例: 智能汽车,华为", fontSize = 11.sp)
                },
                value = mainViewModel.screenPostUseCase!!.associateTopics.value ?: "",
                onValueChange = {
                    mainViewModel.screenPostUseCase!!.associateTopics.value = it
                },
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "关联的人", modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold)
            TextField(
                placeholder = {
                    Text(text = "示例: 蒋开心,李文亦", fontSize = 11.sp)
                },
                value = mainViewModel.screenPostUseCase!!.associatePeoples.value ?: "",
                onValueChange = {
                    mainViewModel.screenPostUseCase!!.associatePeoples.value = it
                },
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "图片(至多15张)",
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            AndroidView(
                factory = {
                    RecyclerView(it).apply {
                        val scalableItemAdapter = ScalableItemAdapter()
                        scalableItemAdapter.itemClickListener = { _, position ->
                            when (scalableItemAdapter.data[position].type) {
                                ScalableItemAdapter.TYPE_TAKE_A_PIC -> {
                                    onAddMediaContent("picture")
                                }
                            }
                        }
                        scalableItemAdapter.addClickableIdsInsideItem(R.id.tv_delete, R.id.iv_img)
                        scalableItemAdapter.itemChildClickListener = { _, view, position ->
                            val scalableItemState = scalableItemAdapter.data[position]
                            if (view.id == R.id.tv_delete) {
                                scalableItemAdapter.tvDeleteClick(scalableItemState)
                                onRemoveMediaContent("picture", scalableItemState)
                            } else if (view.id == R.id.iv_img) {
                                StfalconImageViewer.Builder<Any>(
                                    context,
                                    listOf((scalableItemState.any))
                                ) { imageView, any ->
                                    val uri = (any as MediaStoreDataUriWrapper).uri
                                    Glide.with(view.context)
                                        .load(uri)
                                        .into(imageView)
                                }.withHiddenStatusBar(false).show()
                            }
                        }
                        adapter = scalableItemAdapter
                        layoutManager = GridLayoutManager(it, 3)
                    }
                }, modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .wrapContentHeight()
                    .fillMaxWidth()
            )
            {
                (it.adapter as ScalableItemAdapter).updateUI(
                    mainViewModel.screenPostUseCase!!.selectPictures,
                    true
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "视频(至多1个)",
                    modifier = Modifier
                        .padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilterChip(
                    selected = mainViewModel.screenPostUseCase!!.videoPostToStream.value,
                    onClick = {
                        mainViewModel.screenPostUseCase!!.videoPostToStream.value =
                            !mainViewModel.screenPostUseCase!!.videoPostToStream.value
                    },
                    label = {
                        Text(text = "添加视频到流中")
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    onAddMediaContent("video")
                }) {
                    Text(text = "选择")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier
                .size(220.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    val mediaUriWrapper =
                        mainViewModel.screenPostUseCase?.selectVideo?.value?.any as? MediaStoreDataUriWrapper
                    if (mediaUriWrapper != null)
                        onVideoPlayClick(mediaUriWrapper)
                }) {
                val scalableItemState = mainViewModel.screenPostUseCase?.selectVideo?.value
                if (scalableItemState != null) {
                    val mediaStoreDataUriWrapper = scalableItemState.any as MediaStoreDataUriWrapper
                    if (mediaStoreDataUriWrapper.thumbnail != null) {
                        LocalOrRemoteImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            any = mediaStoreDataUriWrapper.thumbnail
                        )
                    }
                }
                Box(
                    Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter =
                        painterResource(id = R.drawable.ic_baseline_slow_motion_video_24),
                        contentDescription = null,
                    )
                }
            }
            Spacer(modifier = Modifier.height(128.dp))
        }
    }
}