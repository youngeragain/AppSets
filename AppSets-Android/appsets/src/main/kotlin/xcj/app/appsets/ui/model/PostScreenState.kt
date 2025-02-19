package xcj.app.appsets.ui.model;

import androidx.compose.runtime.MutableState
import xcj.app.appsets.util.model.UriProvider

data class PostScreen(
    val isPublic: Boolean = true,
    val content: String? = null,
    val associateTopics: String? = null,
    val associatePeoples: String? = null,
    val pictures: List<UriProvider> = emptyList(),
    val videos: List<UriProvider> = emptyList(),
    val addToMediaFall: Boolean = false,
) {
    companion object {
        fun updateStateIsPublic(state: MutableState<PostScreenState>, isPublic: Boolean) {
            val newPostScreen = state.value as? PostScreenState.NewPostScreen
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.postScreen
            state.value = newPostScreen.copy(postScreen.copy(isPublic = isPublic))
        }

        fun updateStateContent(state: MutableState<PostScreenState>, content: String) {
            val newPostScreen = state.value as? PostScreenState.NewPostScreen
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.postScreen
            state.value = newPostScreen.copy(postScreen.copy(content = content))
        }

        fun updateStateAssociateTopics(
            state: MutableState<PostScreenState>,
            associateTopics: String
        ) {
            val newPostScreen = state.value as? PostScreenState.NewPostScreen
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.postScreen
            state.value =
                newPostScreen.copy(postScreen.copy(associateTopics = associateTopics))
        }

        fun updateStateAssociatePeoples(
            state: MutableState<PostScreenState>,
            associatePeoples: String
        ) {
            val newPostScreen = state.value as? PostScreenState.NewPostScreen
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.postScreen
            state.value =
                newPostScreen.copy(postScreen.copy(associatePeoples = associatePeoples))
        }

        fun updateStateAddMediaFallClick(state: MutableState<PostScreenState>) {
            val newPostScreen = state.value as? PostScreenState.NewPostScreen
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.postScreen
            state.value =
                newPostScreen.copy(postScreen.copy(addToMediaFall = !postScreen.addToMediaFall))
        }

        fun updateStateSelectPictures(
            state: MutableState<PostScreenState>,
            uriProviderList: List<UriProvider>
        ) {
            val newPostScreen = state.value as? PostScreenState.NewPostScreen
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.postScreen
            val newPictures = postScreen.pictures.toMutableList().apply {
                addAll(uriProviderList)
            }
            state.value = newPostScreen.copy(postScreen.copy(pictures = newPictures))
        }

        fun updateStateSelectVideos(
            state: MutableState<PostScreenState>,
            uriProviderList: List<UriProvider>
        ) {
            val newPostScreen = state.value as? PostScreenState.NewPostScreen
            if (newPostScreen == null) {
                return
            }
            val postScreen = newPostScreen.postScreen
            state.value =
                newPostScreen.copy(postScreen.copy(videos = uriProviderList))
        }
    }
}

sealed interface PostScreenState : TipsState {

    val postScreen: PostScreen

    data class NewPostScreen(
        override val postScreen: PostScreen = PostScreen(),
        override val tips: Int? = null
    ) : PostScreenState

    data class Posting(
        override val postScreen: PostScreen,
        override val tips: Int = xcj.app.appsets.R.string.adding
    ) : PostScreenState

    data class PostSuccess(
        override val postScreen: PostScreen,
        override val tips: Int = xcj.app.appsets.R.string.create_success
    ) :
        PostScreenState

    data class PostFailed(
        override val postScreen: PostScreen,
        override val tips: Int = xcj.app.appsets.R.string.create_failed
    ) : PostScreenState
}