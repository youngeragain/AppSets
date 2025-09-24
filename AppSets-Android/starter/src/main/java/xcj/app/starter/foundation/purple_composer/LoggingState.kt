package xcj.app.starter.foundation.purple_composer

import xcj.app.starter.foundation.Identifiable

class LoggingState(
    val tag: String,
    val tagPrefix: String? = null
) {
    var moduleId: Identifiable<String>? = null
    val messages: MutableList<Any?> = mutableListOf()
    var throwable: Throwable? = null


    val tagOverride: String
        get() {
            return buildString {
                if (!tagPrefix.isNullOrEmpty()) {
                    append("$tagPrefix|")
                }
                val moduleId = moduleId?.id
                if (!moduleId.isNullOrEmpty()) {
                    append("$moduleId|")
                }
                append(tag)
            }
        }

    fun clear() {
        messages.clear()
        throwable = null
    }
}