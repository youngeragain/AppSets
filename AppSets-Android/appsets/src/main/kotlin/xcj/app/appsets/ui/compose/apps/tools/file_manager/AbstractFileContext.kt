package xcj.app.appsets.ui.compose.apps.tools.file_manager

class AbstractFileContext(
    val rootAbstractFile: AbstractFile<*>,
    val onCurrentChanged: (String, AbstractFile<*>) -> Unit
) {

    private var currentAbstractFile: AbstractFile<*>? = null

    init {
        setCurrent(rootAbstractFile, "push_new")
    }

    fun setCurrent(abstractFile: AbstractFile<*>, type: String) {
        if (abstractFile != currentAbstractFile) {
            this.currentAbstractFile = abstractFile
            onCurrentChanged(type, abstractFile)
        }
    }

    fun getCurrent(): AbstractFile<*> {
        return currentAbstractFile ?: rootAbstractFile
    }

    fun navigateUp() {
        val current = getCurrent()
        if (current.isRoot) {
            return
        }
        val parent = current.getParent()
        val abstractFile = parent as? AbstractFile<*>
        if (abstractFile == null) {
            return
        }
        setCurrent(abstractFile, "pop_up")
    }

}