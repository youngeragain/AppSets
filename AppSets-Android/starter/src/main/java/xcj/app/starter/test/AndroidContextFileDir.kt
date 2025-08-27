package xcj.app.starter.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class AndroidContextFileDir(
    var dynamicAARDir: String? = null,
    var dynamicAAROPTDir: String? = null,
    var errorsCacheDir: String? = null,
    var logsCacheDir: String? = null,
    var tempCacheDir: String? = null,
    var tempFilesCacheDir: String? = null,
    var tempImagesCacheDir: String? = null,
    var tempVideosCacheDir: String? = null,
    var tempDbsCacheDir: String? = null,
    var tempAudiosCacheDir: String? = null,
    var appSetsShareDir: String? = null,
) {
    suspend fun cleanCaches() = withContext(Dispatchers.IO) {
        runCatching {
            listOf(
                tempFilesCacheDir,
                tempImagesCacheDir,
                tempVideosCacheDir,
                tempDbsCacheDir,
                tempAudiosCacheDir
            ).forEach {
                if (!it.isNullOrEmpty()) {
                    val file = File(it)
                    if (file.exists() && file.isDirectory) {
                        file.listFiles()?.forEach(File::delete)
                    }
                }
            }
        }
    }
}