package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import xcj.app.appsets.server.repository.GenerationAIRepository
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionTypes
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.appsets.ui.compose.quickstep.UriQuickStepContent
import xcj.app.appsets.ui.model.ScreenInfoForCreate
import xcj.app.appsets.ui.model.page_state.PostScreenPageState
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.server.request
import xcj.app.starter.util.ContentType

class ScreenPostUseCase(
    private val screenRepository: ScreenRepository,
    private val generationAIRepository: GenerationAIRepository,
) : ComposeLifecycleAware {

    companion object {
        private const val TAG = "ScreenPostUseCase"
    }

    val postScreenPageState: MutableState<PostScreenPageState> =
        mutableStateOf(PostScreenPageState.NewPostScreenPage())

    fun onIsPublicClick(isPublic: Boolean) {
        ScreenInfoForCreate.updateStateIsPublic(postScreenPageState, isPublic)
    }

    fun onInputContent(content: String) {
        ScreenInfoForCreate.updateStateContent(postScreenPageState, content)
    }

    fun onInputTopics(associateTopics: String) {
        ScreenInfoForCreate.updateStateAssociateTopics(postScreenPageState, associateTopics)
    }

    fun onInputPeoples(associatePeoples: String) {
        ScreenInfoForCreate.updateStateAssociatePeoples(postScreenPageState, associatePeoples)
    }

    fun onAddMediaFallClick() {
        ScreenInfoForCreate.updateStateAddMediaFallStatus(postScreenPageState)
    }

    fun updateSelectPictures(uriProviderList: List<UriProvider>) {
        ScreenInfoForCreate.updateStateSelectPictures(postScreenPageState, uriProviderList)
    }

    fun updateSelectVideo(uriProviderList: List<UriProvider>) {
        ScreenInfoForCreate.updateStateSelectVideos(postScreenPageState, uriProviderList)
    }

    suspend fun createScreen(context: Context) {
        val postScreenState = this.postScreenPageState.value
        if (postScreenState is PostScreenPageState.Posting) {
            return
        }
        if (postScreenState !is PostScreenPageState.NewPostScreenPage) {
            return
        }
        val postScreen = postScreenState.screenInfoForCreate
        this.postScreenPageState.value = PostScreenPageState.Posting(postScreen)
        request {
            screenRepository.addScreen(
                context,
                postScreenState.screenInfoForCreate
            )
        }
            .onSuccess { isAddSuccess ->
                if (isAddSuccess) {
                    delay(1200)
                    ContextCompat.getString(context, xcj.app.appsets.R.string.create_success)
                        .toastSuspend()
                    this@ScreenPostUseCase.postScreenPageState.value =
                        PostScreenPageState.PostSuccessPage(postScreen)
                } else {
                    ContextCompat.getString(context, xcj.app.appsets.R.string.create_failed)
                        .toastSuspend()
                    this@ScreenPostUseCase.postScreenPageState.value =
                        PostScreenPageState.PostFailedPage(postScreen)
                }
            }.onFailure {
                ContextCompat.getString(context, xcj.app.appsets.R.string.create_failed)
                    .toastSuspend()
                this@ScreenPostUseCase.postScreenPageState.value =
                    PostScreenPageState.PostFailedPage(postScreen)
            }
    }

    suspend fun generateContent(context: Context) {
        val postScreenState = this@ScreenPostUseCase.postScreenPageState.value
        if (postScreenState !is PostScreenPageState.NewPostScreenPage) {
            return
        }
        request {
            generationAIRepository.getGenerateContentWithNoneContext()
        }.onSuccess {
            ScreenInfoForCreate.updateStateContent(
                this@ScreenPostUseCase.postScreenPageState,
                ""
            )
            flow {
                it.toCharArray().forEach {
                    emit(it)
                    delay(10)
                }
            }.collectLatest {
                val oldPostScreen =
                    (this@ScreenPostUseCase.postScreenPageState.value as? PostScreenPageState.NewPostScreenPage)?.screenInfoForCreate
                ScreenInfoForCreate.updateStateContent(
                    this@ScreenPostUseCase.postScreenPageState,
                    (oldPostScreen?.content ?: "") + it
                )
            }
        }
    }

    fun onRemoveMediaContent(type: String, uriProvider: UriProvider) {
        val postScreenState = postScreenPageState.value
        if (postScreenState !is PostScreenPageState.NewPostScreenPage) {
            return
        }
        val postScreen = postScreenState.screenInfoForCreate
        if (type == ContentSelectionTypes.IMAGE) {
            val newPostScreen =
                postScreen.copy(pictures = postScreen.pictures.toMutableList().apply {
                    remove(uriProvider)
                })
            this.postScreenPageState.value =
                postScreenState.copy(screenInfoForCreate = newPostScreen)
        } else if (type == ContentSelectionTypes.VIDEO) {
            val newPostScreen = postScreen.copy(videos = postScreen.videos.toMutableList().apply {
                remove(uriProvider)
            })
            this.postScreenPageState.value =
                postScreenState.copy(screenInfoForCreate = newPostScreen)
        } else {
            null
        }
    }

    fun updateWithQuickStepContentIfNeeded(quickStepContents: List<QuickStepContent>?) {
        if (quickStepContents.isNullOrEmpty()) {
            return
        }
        quickStepContents.filterIsInstance<TextQuickStepContent>().forEach { quickStepContent ->
            onInputContent(quickStepContent.text)
        }
        val uriQuickStepContents = quickStepContents.filterIsInstance<UriQuickStepContent>()
        uriQuickStepContents.forEach { quickStepContent ->
            if (ContentType.isImage(quickStepContent.uriContentType)) {
                val uriProvider = UriProvider.fromUri(quickStepContent.uri)
                val uriProviderList = listOf(uriProvider)
                updateSelectPictures(uriProviderList)
            } else if (ContentType.isVideo(quickStepContent.uriContentType)) {
                val uriProvider = UriProvider.fromUri(quickStepContent.uri)
                val uriProviderList = listOf(uriProvider)
                updateSelectVideo(uriProviderList)
            }
        }
    }

    override fun onComposeDispose(by: String?) {
        postScreenPageState.value = PostScreenPageState.NewPostScreenPage()
    }

}