package xcj.app.starter.android.util

import xcj.app.starter.foundation.Identifiable
import xcj.app.starter.foundation.Provider

class LogModuleInfoProvider(
    var enable: Boolean,
    private val id: Identifiable<String>,
) : Provider<String, String> {
    override fun key(): Identifiable<String> {
        return id
    }

    override fun provide(): String {
        return id.id
    }
}