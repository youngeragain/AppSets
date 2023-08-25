package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ktx.toastSuspend
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.ui.nonecompose.ui.dialog.ScalableItemAdapter
import xcj.app.appsets.ui.nonecompose.ui.dialog.ScalableItemState
import xcj.app.core.foundation.usecase.NoConfigUseCase

class ScreenPostUseCase(private val coroutineScope: CoroutineScope) : NoConfigUseCase() {
    val isPublic: MutableState<Boolean> = mutableStateOf(true)
    val content: MutableState<String?> = mutableStateOf(null)
    val selectPictures: MutableList<ScalableItemState> = mutableStateListOf()
    val selectVideo: MutableState<ScalableItemState?> = mutableStateOf(null)
    val videoPostToStream: MutableState<Boolean> = mutableStateOf(false)
    val associateTopics: MutableState<String?> = mutableStateOf(null)
    val associatePeoples: MutableState<String?> = mutableStateOf(null)

    private val screenRepository: ScreenRepository =
        ScreenRepository(URLApi.provide(UserApi::class.java))

    val postFinishState: MutableState<Boolean> = mutableStateOf(false)

    val posting: MutableState<Boolean> = mutableStateOf(false)

    fun updateSelectPictures(pictureUris: List<MediaStoreDataUriWrapper>) {
        selectPictures.clear()
        selectPictures.addAll(pictureUris.map {
            ScalableItemState(
                any = it,
                type = ScalableItemAdapter.TYPE_PIC_DELETEABLE,
                showDelete = true
            )
        })
    }

    fun updateSelectVideo(videoUriWrapper: MediaStoreDataUriWrapper) {
        val scalableItemState = ScalableItemState(
            any = videoUriWrapper,
            type = ScalableItemAdapter.TYPE_AUDIO_SELECTABLE,
            selected = true
        )
        selectVideo.value = scalableItemState
    }

    fun post(context: Context) {
        if (posting.value)
            return
        posting.value = true
        coroutineScope.launch(Dispatchers.IO) {
            val addScreenRes = screenRepository.addScreen(
                context,
                isPublic.value,
                content.value,
                selectPictures.map { it.any as MediaStoreDataUriWrapper },
                selectVideo.value?.any as? MediaStoreDataUriWrapper,
                associateTopics.value,
                associatePeoples.value
            )
            delay(1200)
            if (addScreenRes.data == true) {
                "已发布".toastSuspend()
                postFinishState.value = true
            }
            posting.value = false
        }
    }

    fun onDestroy() {

    }

    fun clear() {
        selectPictures.clear()
        selectVideo.value = null
        videoPostToStream.value = false
        isPublic.value = true
        content.value = null
        associateTopics.value = null
        associatePeoples.value = null
        posting.value = false
        postFinishState.value = false
    }

    fun autoGenerateContent(context: Context) {
        coroutineScope.launch {
            content.value = ""
            delay(20)
            content.value += "今天"
            delay(50)
            content.value += "外面的天气"
            delay(30)
            content.value += "看起来"
            delay(60)
            content.value += "很好呢！"
            delay(40)
            content.value += "要"
            delay(20)
            content.value += "不要一起去"
            delay(50)
            content.value += "河边走一走呢?"
        }
    }

    fun onRemoveMediaContent(type: String, scalableItemState: ScalableItemState) {
        if (type == "picture") {
            selectPictures.removeIf { it == scalableItemState }
        } else if (type == "video") {
            if (selectVideo.value == scalableItemState)
                selectVideo.value = null
        }
    }

}