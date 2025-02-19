package xcj.app.starter.test

import xcj.app.starter.foundation.Aware

interface PurpleContextAware : Aware {
    fun setPurpleContext(purpleContext: PurpleContext)
}