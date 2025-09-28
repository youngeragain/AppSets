package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import xcj.app.appsets.util.DesignRecorder
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.android.ktx.asFileOrNull
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File

class MediaAudioRecorderUseCase() : ComposeLifecycleAware {
    companion object {
        private const val TAG = "MediaAudioRecorderUseCase"
    }

    private val designRecorder: DesignRecorder = DesignRecorder()

    val recorderState: MutableState<DesignRecorder.AudioRecorderState> =
        mutableStateOf(DesignRecorder.AudioRecorderState())

    suspend fun startRecord(
        context: Context,
        nowSpaceContentUseCase: NowSpaceContentUseCase
    ) {
        val platformAudioRecordPermission =
            PlatformPermissionsUsage.provideAudioRecordPermission(context)
        if (!platformAudioRecordPermission.granted) {
            nowSpaceContentUseCase.showPlatformPermissionUsageTipsIfNeeded(
                context = context,
                platformPermissionsUsagesProvider = { context ->
                    listOf(platformAudioRecordPermission)
                }
            )
            return
        }
        runCatching {
            getRecorder(context).startRecord(context)
        }.onFailure {
            recorderState.value =
                recorderState.value.copy(isStarted = false)
            ContextCompat.getString(context, xcj.app.appsets.R.string.audio_record_exception)
                .toast()
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
        if (outPut !is DesignRecorder.OutPut) {
            return null
        }
        val fileOrNull = outPut.getOutPutFilePath().asFileOrNull()
        if (fileOrNull == null) {
            return null
        }
        fileOrNull.setReadable(true)
        val uri = fileOrNull.toUri()

        val uriProvider = UriProvider.fromUri(uri)
        return uriProvider
    }

    override fun onComposeDispose(by: String?) {

    }

    fun resetState() {
        recorderState.value = DesignRecorder.AudioRecorderState()
    }

    fun cleanUp(by: String) {
        resetState()
        designRecorder.cleanUp(by)
    }
}