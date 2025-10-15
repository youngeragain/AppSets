package xcj.app.appsets.ui.compose.quickstep

import android.content.Context
import xcj.app.appsets.ui.compose.apps.quickstep.ToolAppSetsShareQuickStepHandler
import xcj.app.appsets.ui.compose.apps.quickstep.ToolContentTransformQuickStepHandler
import xcj.app.appsets.ui.compose.apps.quickstep.ToolFileCreateQuickStepHandler
import xcj.app.appsets.ui.compose.apps.quickstep.ToolGraphicQuickStepHandler
import xcj.app.appsets.ui.compose.apps.quickstep.ToolIntentCallerQuickStepHandler
import xcj.app.appsets.ui.compose.conversation.quickstep.AIQuickStepHandler
import xcj.app.appsets.ui.compose.conversation.quickstep.ConversationQuickStepHandler
import xcj.app.appsets.ui.compose.outside.quickstep.OutSideQuickStepHandler

class QuickStepContentHandlerRegistry {
    companion object {
        fun initHandlers(context: Context, registry: QuickStepContentHandlerRegistry) {
            registry.addContentHandler(ToolContentTransformQuickStepHandler())
            registry.addContentHandler(ToolAppSetsShareQuickStepHandler())
            registry.addContentHandler(ToolIntentCallerQuickStepHandler())
            registry.addContentHandler(ToolGraphicQuickStepHandler())
            registry.addContentHandler(ToolFileCreateQuickStepHandler())
            registry.addContentHandler(ConversationQuickStepHandler())
            registry.addContentHandler(AIQuickStepHandler())
            registry.addContentHandler(OutSideQuickStepHandler())
        }

        fun deInitHandlers(registry: QuickStepContentHandlerRegistry) {
            registry.removeAll()
        }
    }

    private val quickStepContentHandlers = mutableListOf<QuickStepContentHandler>()

    fun addContentHandler(quickStepContentHandler: QuickStepContentHandler) {
        quickStepContentHandlers.add(quickStepContentHandler)
    }

    fun removeContentHandler(quickStepContentHandler: QuickStepContentHandler) {
        quickStepContentHandlers.remove(quickStepContentHandler)
    }

    fun getAllHandlers(): List<QuickStepContentHandler> {
        return quickStepContentHandlers
    }

    fun removeAll() {
        quickStepContentHandlers.clear()
    }

    fun findHandlers(
        context: Context,
        quickStepContentHolder: QuickStepContentHolder,
        searchContent: String,
    ): List<QuickStepContentHandler> {
        val quickStepContentHandlers = quickStepContentHandlers
        val filtered = quickStepContentHandlers.filter {
            if (quickStepContentHolder.quickStepContents.isEmpty()) {
                true
            } else {
                it.canAccept(quickStepContentHolder)
            }
        }.filter {
            it.onSearch(context, searchContent)
        }
        return filtered
    }
}