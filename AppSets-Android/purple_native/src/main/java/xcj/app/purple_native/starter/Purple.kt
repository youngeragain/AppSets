package xcj.app.purple_native.starter

import android.util.Log

class Purple {
    external fun stringFromJNI(): String

    external fun getNumber(): Int

    companion object {
        private const val TAG = "PurpleNative"

        // Used to load the 'purple_native' library on application startup.
        init {
            runCatching {
                System.loadLibrary("purple_native")
            }.onSuccess {
                Log.d(TAG, "loadLibrary:purple_native success!")
            }.onFailure {
                Log.d(TAG, "loadLibrary:purple_native failed!", it)
            }
        }
    }
}