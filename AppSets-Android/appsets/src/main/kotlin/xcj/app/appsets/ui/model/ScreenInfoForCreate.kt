package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import xcj.app.appsets.ui.model.page_state.PostScreenPageState
import xcj.app.appsets.util.model.UriProvider

data class ScreenInfoForCreate(
    val isPublic: Boolean = true,
    val content: String? = null,
    val associateTopics: String? = null,
    val associatePeoples: String? = null,
    val pictures: List<UriProvider> = emptyList(),
    val videos: List<UriProvider> = emptyList(),
    val addToMediaFall: Boolean = false,
) {
    companion object {
        fun updateStateIsPublic(state: MutableState<PostScreenPageState>, isPublic: Boolean) {
            val newPostScreen = state.value as? PostScreenPageState.NewPostScreenPage
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.screenInfoForCreate
            state.value = newPostScreen.copy(postScreen.copy(isPublic = isPublic))
        }

        fun updateStateContent(state: MutableState<PostScreenPageState>, content: String) {
            val newPostScreen = state.value as? PostScreenPageState.NewPostScreenPage
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.screenInfoForCreate
            state.value = newPostScreen.copy(postScreen.copy(content = content))
        }

        fun updateStateAssociateTopics(
            state: MutableState<PostScreenPageState>,
            associateTopics: String
        ) {
            val newPostScreen = state.value as? PostScreenPageState.NewPostScreenPage
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.screenInfoForCreate
            state.value =
                newPostScreen.copy(postScreen.copy(associateTopics = associateTopics))
        }

        fun updateStateAssociatePeoples(
            state: MutableState<PostScreenPageState>,
            associatePeoples: String
        ) {
            val newPostScreen = state.value as? PostScreenPageState.NewPostScreenPage
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.screenInfoForCreate
            state.value =
                newPostScreen.copy(postScreen.copy(associatePeoples = associatePeoples))
        }

        fun updateStateAddMediaFallClick(state: MutableState<PostScreenPageState>) {
            val newPostScreen = state.value as? PostScreenPageState.NewPostScreenPage
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.screenInfoForCreate
            state.value =
                newPostScreen.copy(postScreen.copy(addToMediaFall = !postScreen.addToMediaFall))
        }

        fun updateStateSelectPictures(
            state: MutableState<PostScreenPageState>,
            uriProviderList: List<UriProvider>
        ) {
            val newPostScreen = state.value as? PostScreenPageState.NewPostScreenPage
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.screenInfoForCreate
            val newPictures = postScreen.pictures.toMutableList().apply {
                addAll(uriProviderList)
            }
            state.value = newPostScreen.copy(postScreen.copy(pictures = newPictures))
        }

        fun updateStateSelectVideos(
            state: MutableState<PostScreenPageState>,
            uriProviderList: List<UriProvider>
        ) {
            val newPostScreen = state.value as? PostScreenPageState.NewPostScreenPage
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.screenInfoForCreate
            state.value =
                newPostScreen.copy(postScreen.copy(videos = uriProviderList))
        }
    }
}