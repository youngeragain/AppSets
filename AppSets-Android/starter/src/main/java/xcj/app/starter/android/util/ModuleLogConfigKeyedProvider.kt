package xcj.app.starter.android.util

import xcj.app.starter.foundation.Identifiable
import xcj.app.starter.foundation.KeyedProvider

class ModuleLogConfigKeyedProvider(
    var enable: Boolean,
    private val id: Identifiable<String>,
) : KeyedProvider<String, String> {
    override fun key(): Identifiable<String> {
        return id
    }

    override fun provide(): String {
        return id.id
    }
}