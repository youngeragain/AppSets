package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import xcj.app.starter.server.requestNotNull
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.appsets.server.repository.GenerationAIRepository
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionVarargs
import xcj.app.appsets.ui.model.PostScreen
import xcj.app.appsets.ui.model.PostScreenState
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.dynamic.IComposeDispose

class ScreenPostUseCase(
    private val coroutineScope: CoroutineScope,
    private val screenRepository: ScreenRepository,
    private val generationAIRepository: GenerationAIRepository
) : IComposeDispose {

    companion object {
        private const val TAG = "ScreenPostUseCase"
    }

    val postScreenState: MutableState<PostScreenState> =
        mutableStateOf(PostScreenState.NewPostScreen())

    fun onIsPublicClick(isPublic: Boolean) {
        PostScreen.updateStateIsPublic(postScreenState, isPublic)
    }

    fun onInputContent(content: String) {
        PostScreen.updateStateContent(postScreenState, content)
    }

    fun onInputTopics(associateTopics: String) {
        PostScreen.updateStateAssociateTopics(postScreenState, associateTopics)
    }

    fun onInputPeoples(associatePeoples: String) {
        PostScreen.updateStateAssociatePeoples(postScreenState, associatePeoples)
    }

    fun onAddMediaFallClick() {
        PostScreen.updateStateAddMediaFallClick(postScreenState)
    }

    fun updateSelectPictures(uriProviderList: List<UriProvider>) {
        PostScreen.updateStateSelectPictures(postScreenState, uriProviderList)
    }

    fun updateSelectVideo(uriProviderList: List<UriProvider>) {
        PostScreen.updateStateSelectVideos(postScreenState, uriProviderList)
    }

    fun createScreen(context: Context) {
        val postScreenState = this.postScreenState.value
        if (postScreenState is PostScreenState.Posting) {
            return
        }
        if (postScreenState !is PostScreenState.NewPostScreen) {
            return
        }
        val postScreen = postScreenState.postScreen
        this.postScreenState.value = PostScreenState.Posting(postScreen)
        coroutineScope.launch {
            requestNotNull(
                action = {
                    screenRepository.addScreen(
                        context,
                        postScreenState.postScreen
                    )
                },
                onSuccess = { isAddSuccess ->
                    if (isAddSuccess) {
                        delay(1200)
                        context.getString(xcj.app.appsets.R.string.create_success).toastSuspend()
                        this@ScreenPostUseCase.postScreenState.value =
                            PostScreenState.PostSuccess(postScreen)
                    } else {
                        context.getString(xcj.app.appsets.R.string.create_failed).toastSuspend()
                        this@ScreenPostUseCase.postScreenState.value =
                            PostScreenState.PostFailed(postScreen)
                    }
                },
                onFailed = {
                    context.getString(xcj.app.appsets.R.string.create_failed).toastSuspend()
                    this@ScreenPostUseCase.postScreenState.value =
                        PostScreenState.PostFailed(postScreen)
                }
            )
        }
    }

    fun generateContent(context: Context) {
        val postScreenState = this@ScreenPostUseCase.postScreenState.value
        if (postScreenState !is PostScreenState.NewPostScreen) {
            return
        }
        coroutineScope.launch {
            requestNotNull(
                action = {
                    generationAIRepository.getGenerateContentWithNoneContext()
                },
                onSuccess = {
                    PostScreen.updateStateContent(this@ScreenPostUseCase.postScreenState, "")
                    flow {
                        it.toCharArray().forEach {
                            emit(it)
                            delay(10)
                        }
                    }.collectLatest {
                        val oldPostScreen =
                            (this@ScreenPostUseCase.postScreenState.value as? PostScreenState.NewPostScreen)?.postScreen
                        PostScreen.updateStateContent(
                            this@ScreenPostUseCase.postScreenState,
                            (oldPostScreen?.content ?: "") + it
                        )
                    }
                }
            )
        }
    }

    fun onRemoveMediaContent(type: String, uriProvider: UriProvider) {
        val postScreenState = postScreenState.value
        if (postScreenState !is PostScreenState.NewPostScreen) {
            return
        }
        val postScreen = postScreenState.postScreen
        if (type == ContentSelectionVarargs.PICTURE) {
            val newPostScreen =
                postScreen.copy(pictures = postScreen.pictures.toMutableList().apply {
                    remove(uriProvider)
                })
            this.postScreenState.value = postScreenState.copy(postScreen = newPostScreen)
        } else if (type == ContentSelectionVarargs.VIDEO) {
            val newPostScreen = postScreen.copy(videos = postScreen.videos.toMutableList().apply {
                remove(uriProvider)
            })
            this.postScreenState.value = postScreenState.copy(postScreen = newPostScreen)
        } else {
            null
        }
    }

    override fun onComposeDispose(by: String?) {
        postScreenState.value = PostScreenState.NewPostScreen()
    }

}