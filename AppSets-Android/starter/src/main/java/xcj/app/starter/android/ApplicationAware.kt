package xcj.app.starter.android

import android.app.Application
import xcj.app.starter.foundation.Aware

interface ApplicationAware : Aware {
    fun setApplication(application: Application)
}