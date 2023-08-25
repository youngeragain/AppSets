package xcj.app.core.test

import xcj.app.core.foundation.Aware

interface PurpleContextAware: Aware {
    fun setPurpleContext(purpleContext: PurpleContext)
}