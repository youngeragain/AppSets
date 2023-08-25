package xcj.app.purple_module

import android.util.Log
import xcj.app.core.test.PurpleContext
import xcj.app.core.test.PurpleContextAware

class MyPurpleContextAware:PurpleContextAware {

    override fun setPurpleContext(purpleContext: PurpleContext) {
        Log.i("MyPurpleContextAware", "appsets::setAAContext:${purpleContext}")
    }
}