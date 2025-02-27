package xcj.app.appsets.ui.compose.quickstep

class QuickStepContentHandlerRegistry {
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
        quickStepContentHolder: QuickStepContentHolder,
        searchContent: String
    ): List<QuickStepContentHandler> {
        val quickStepContentHandlers = quickStepContentHandlers
        val filtered = quickStepContentHandlers.filter {
            if (quickStepContentHolder.quickStepContents.isEmpty()) {
                true
            } else {
                it.accept(quickStepContentHolder)
            }
        }.filter {
            if (searchContent.isEmpty()) {
                true
            } else {
                it.getName().contains(searchContent) ||
                        it.getCategory().contains(searchContent)
            }
        }
        return filtered
    }
}