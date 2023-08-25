package xcj.app.core.android

import android.app.Application
import xcj.app.core.foundation.Aware

interface ApplicationAware: Aware {
    fun setApplication(application: Application)
}