package xcj.app.appsets.usecase

import android.content.Context
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import xcj.app.appsets.server.repository.GenerationAIRepository
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.appsets.ui.compose.quickstep.UriQuickStepContent
import xcj.app.appsets.ui.model.ScreenInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateScreenPageUIState
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.SingleStateUpdater
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.android.ktx.getSystemFileUri
import xcj.app.starter.android.util.UriProvider
import xcj.app.starter.server.request

class ScreenPostUseCase(
    private val screenRepository: ScreenRepository,
    private val generationAIRepository: GenerationAIRepository,
) : ComposeLifecycleAware {

    companion object {
        private const val TAG = "ScreenPostUseCase"
    }

    suspend fun createScreen(
        context: Context,
        screenInfoForCreate: ScreenInfoForCreate,
        composeStateUpdater: ComposeStateUpdater<CreateScreenPageUIState>
    ) {
        if (composeStateUpdater !is SingleStateUpdater) {
            return
        }
        val createScreenPageUIState = composeStateUpdater.getStateValue()
        if (createScreenPageUIState is CreateScreenPageUIState.Posting) {
            return
        }
        if (createScreenPageUIState !is CreateScreenPageUIState.CreateStart) {
            return
        }

        composeStateUpdater.update(CreateScreenPageUIState.Posting())
        request {
            screenRepository.addScreen(
                context,
                screenInfoForCreate
            )
        }
            .onSuccess { isAddSuccess ->
                if (isAddSuccess) {
                    delay(1200)
                    ContextCompat.getString(context, xcj.app.appsets.R.string.create_success)
                        .toastSuspend()
                    composeStateUpdater.update(CreateScreenPageUIState.CreateSuccess())
                } else {
                    ContextCompat.getString(context, xcj.app.appsets.R.string.create_failed)
                        .toastSuspend()
                    composeStateUpdater.update(CreateScreenPageUIState.CreateFailed())
                }
            }.onFailure {
                ContextCompat.getString(context, xcj.app.appsets.R.string.create_failed)
                    .toastSuspend()
                composeStateUpdater.update(CreateScreenPageUIState.CreateFailed())
            }
    }

    suspend fun generateContent(
        context: Context,
        screenInfoForCreate: ScreenInfoForCreate,
        composeStateUpdater: ComposeStateUpdater<CreateScreenPageUIState>
    ) {
        if (composeStateUpdater !is SingleStateUpdater) {
            return
        }
        val createScreenPageUIState = composeStateUpdater.getStateValue()
        if (createScreenPageUIState !is CreateScreenPageUIState.CreateStart) {
            return
        }
        request {
            generationAIRepository.getGenerateContentWithNoneContext()
        }.onSuccess { generatedContent ->
            screenInfoForCreate.content.value = ""
            flow {
                generatedContent.toCharArray().forEach { char ->
                    emit(char)
                    delay(10)
                }
            }.collectLatest { char ->
                val oldContent =
                    screenInfoForCreate.content.value
                screenInfoForCreate.content.value = oldContent + char
            }
        }
    }

    fun onRemoveMediaContent(
        type: String,
        uriProvider: UriProvider,
        screenInfoForCreate: ScreenInfoForCreate,
        composeStateUpdater: ComposeStateUpdater<CreateScreenPageUIState>
    ) {
        if (composeStateUpdater !is SingleStateUpdater) {
            return
        }
        val createScreenPageUIState = composeStateUpdater.getStateValue()
        if (createScreenPageUIState !is CreateScreenPageUIState.CreateStart) {
            return
        }
        screenInfoForCreate.mediaUriProviders.remove(uriProvider)
    }

    suspend fun updateWithQuickStepContentIfNeeded(
        context: Context,
        quickStepContents: List<QuickStepContent>?,
        screenInfoForCreate: ScreenInfoForCreate
    ) {
        if (quickStepContents.isNullOrEmpty()) {
            return
        }
        quickStepContents.filterIsInstance<TextQuickStepContent>()
            .joinToString { quickStepContent ->
                quickStepContent.text
            }.let {
                screenInfoForCreate.content.value = it
            }
        val uriQuickStepContents = quickStepContents.filterIsInstance<UriQuickStepContent>()
        uriQuickStepContents.forEach { quickStepContent ->
            val mediaStoreDataUri =
                context.getSystemFileUri(quickStepContent.uri) ?: UriProvider.fromUri(
                    quickStepContent.uri
                )
            screenInfoForCreate.mediaUriProviders.add(mediaStoreDataUri)
        }
    }

    override fun onComposeDispose(by: String?) {

    }

}