package xcj.app.starter.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class AndroidContextFileDir(
    val dynamicAARDir: String? = null,
    val dynamicAAROPTDir: String? = null,
    val errorsCacheDir: String? = null,
    val logsCacheDir: String? = null,
    val tempCacheDir: String? = null,
    val tempFilesCacheDir: String? = null,
    val tempImagesCacheDir: String? = null,
    val tempVideosCacheDir: String? = null,
    val tempDbsCacheDir: String? = null,
    val tempAudiosCacheDir: String? = null,
    val appSetsShareDir: String? = null,
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