package xcj.app.appsets.usecase

import androidx.compose.runtime.mutableStateListOf
import xcj.app.appsets.account.UserAccountStateAware
import xcj.app.appsets.ui.model.state.NowSpaceContent
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.android.util.PurpleLogger

class NowSpaceContentUseCase() : ComposeLifecycleAware, UserAccountStateAware {

    companion object {
        private const val TAG = "NowSpaceContentUseCase"
    }


    val contents: MutableList<NowSpaceContent> =
        mutableStateListOf()

    fun addContent(content: NowSpaceContent) {
        PurpleLogger.current.d(TAG, "addContent, content:$content")
        contents.add(content)
    }

    /**
     * @return pair first is old, maybe null, pair second is new
     */
    fun replaceOrAddContent(replacer: (List<NowSpaceContent>) -> Pair<NowSpaceContent?, NowSpaceContent>) {
        val (old, new) = replacer(contents)
        PurpleLogger.current.d(TAG, "replaceOrAddContent, oldContent:$old, newContent:$new")
        if (old != null) {
            val oldPosition = contents.indexOfFirst { it == old }
            contents[oldPosition] = new
        } else {
            contents.add(new)
        }
    }

    fun removeContent(content: NowSpaceContent) {
        PurpleLogger.current.d(TAG, "removeContent, content:$content")
        contents.removeIf {
            content == it
        }
    }

    fun removeAllContent() {
        PurpleLogger.current.d(TAG, "removeAllContent")
        contents.clear()
    }

    fun removeContentIf(test: (List<NowSpaceContent>) -> NowSpaceContent?) {
        PurpleLogger.current.d(TAG, "removeContentIf")
        val removeNowSpaceContent = test(contents)
        if (removeNowSpaceContent != null) {
            removeContent(removeNowSpaceContent)
        }
    }

    override fun onComposeDispose(by: String?) {

    }

    override fun onUserLogout(by: String?) {
        removeAllContent()
    }
}