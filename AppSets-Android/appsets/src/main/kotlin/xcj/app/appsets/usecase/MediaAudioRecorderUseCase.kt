package xcj.app.appsets.usecase

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.util.DesignRecorder
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.android.ktx.asFileOrNull
import xcj.app.starter.android.usecase.PlatformUseCase
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File

class MediaAudioRecorderUseCase(
    private val coroutineScope: CoroutineScope,
) : IComposeLifecycleAware {
    companion object {
        private const val TAG = "MediaAudioRecorderUseCase"
    }

    private val designRecorder: DesignRecorder = DesignRecorder()

    val recorderState: MutableState<DesignRecorder.AudioRecorderState> =
        mutableStateOf(DesignRecorder.AudioRecorderState())

    fun startRecord(context: Context, navController: NavController?) {
        val platformPermissions = PlatformUseCase.providePlatformPermissions(context)
        val platformPermissionsUsageOfFile =
            platformPermissions.firstOrNull {
                it.name == context.getString(xcj.app.appsets.R.string.file)
            }
        if (platformPermissionsUsageOfFile == null) {
            return
        }
        if (!platformPermissionsUsageOfFile.granted) {
            navController?.navigate(PageRouteNames.PrivacyPage)
            return
        }
        val platformPermissionsUsageOfRecordAudio =
            platformPermissions.firstOrNull {
                it.name == context.getString(xcj.app.appsets.R.string.record_audio)
            }
        if (platformPermissionsUsageOfRecordAudio == null) {
            return
        }
        if (!platformPermissionsUsageOfRecordAudio.granted) {
            navController?.navigate(PageRouteNames.PrivacyPage)
            return
        }
        runCatching {
            getRecorder(context).startRecord(context)
        }.onFailure {
            recorderState.value =
                recorderState.value.copy(isStarted = false)
            context.getString(xcj.app.appsets.R.string.audio_record_exception).toast()
            PurpleLogger.current.d(TAG, "startRecord failed! ${it.message}")
            stopRecord("start exception")
        }
    }

    private fun getRecorder(context: Context): DesignRecorder {
        val outPut = DesignRecorder.OutPut()
        outPut.maxRecordSeconds = recorderState.value.maxRecordSeconds
        designRecorder.setOutPut(outPut)
        designRecorder.setUpdateCallback(object : DesignRecorder.UpdateCallBack {
            override fun onUpdate(startTime: Long, seconds: Int) {
                recorderState.value =
                    recorderState.value.copy(isStarted = true, isPaused = false, seconds = seconds)
            }

            override fun onStop() {
                recorderState.value = recorderState.value.copy(isStarted = false, isPaused = false)
            }

            override fun onPause() {
                recorderState.value = recorderState.value.copy(isPaused = true)
            }

        })
        val androidContextFileDir = LocalAndroidContextFileDir.current
        designRecorder.getOutPut()?.outDirFile =
            File(androidContextFileDir.tempAudiosCacheDir + "/audio_record/${System.currentTimeMillis()}/")
        return designRecorder
    }

    fun stopRecord(by: String) {
        designRecorder.stopRecord(by)
    }

    fun pauseRecord(by: String) {
        designRecorder.pauseRecord(by)
    }

    fun resumeRecord(by: String) {
        designRecorder.resumeRecord(by)
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(designRecorder)
    }

    fun getRecordFileUriProvider(): UriProvider? {
        val outPut = designRecorder.getOutPut()
        if (outPut is DesignRecorder.OutPut) {
            val uri = outPut.getOutPutFilePath().asFileOrNull()?.also {
                it.setReadable(true)
            }?.toUri()
            if (uri != null) {
                return object : UriProvider {
                    override fun provideUri(): Uri? {
                        return uri
                    }
                }
            }
        }
        return null
    }

    override fun onComposeDispose(by: String?) {

    }

    fun cleanUp(by: String) {
        designRecorder.cleanUp(by)
    }
}