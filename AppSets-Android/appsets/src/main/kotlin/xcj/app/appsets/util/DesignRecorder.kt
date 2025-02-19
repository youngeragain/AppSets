package xcj.app.appsets.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import xcj.app.starter.android.util.PurpleLogger
import java.io.File
import java.util.Timer
import java.util.TimerTask

class DesignRecorder : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "DesignRecorder"
        private const val MAX_RECORD_SECONDS = 60
    }

    private var outPut: OutPut? = null

    private var mUpdateCallBack: UpdateCallBack? = null

    private var durationTimer: Timer? = null

    private var mMediaRecorder: MediaRecorder? = null

    private var isPrepared = false

    private var isStarted = false

    private var isPaused = false

    private var mCustomTimerTask: CustomTimerTask? = null


    private fun stopWhenReachMaxSeconds() {
        stopRecord("reach the max record seconds:${outPut?.maxRecordSeconds ?: 0}")
    }

    fun setUpdateCallback(updateCallBack: UpdateCallBack) {
        mUpdateCallBack = updateCallBack
    }

    fun setOutPut(outPut: OutPut) {
        this.outPut = outPut
    }

    private fun initMediaRecorder(context: Context) {
        if (mMediaRecorder == null) {
            mMediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }
        }
    }

    private fun prepareMediaRecorder(filePath: String) {
        mMediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
            setOutputFile(filePath)
            prepare()
            isPrepared = true
        }
    }

    fun release(by: String) {
        PurpleLogger.current.d(TAG, "release by:${by}")
        isPrepared = false
        isStarted = false
        isPaused = false
        durationTimer?.cancel()
        durationTimer = null
        mCustomTimerTask = null
        mUpdateCallBack = null
        runCatching {
            mMediaRecorder?.release()
        }.onFailure {
            PurpleLogger.current.d(TAG, "release by:${by}, failed! ${it.message}")
        }
    }

    fun stopRecord(by: String) {
        PurpleLogger.current.d(TAG, "stopRecord by:${by}")
        isPrepared = false
        isStarted = false
        isPaused = false
        durationTimer?.cancel()
        durationTimer = null
        mCustomTimerTask = null
        mUpdateCallBack?.onStop()
        mUpdateCallBack = null
        runCatching {
            mMediaRecorder?.stop()
        }.onFailure {
            PurpleLogger.current.d(TAG, "stopRecord by:${by}, failed! ${it.message}")
        }
    }

    fun pauseRecord(by: String) {
        PurpleLogger.current.d(TAG, "pauseRecord by:${by}")
        isPaused = false
        durationTimer?.cancel()
        durationTimer = null
        mUpdateCallBack?.onPause()
        runCatching {
            mMediaRecorder?.pause()
        }.onFailure {
            PurpleLogger.current.d(TAG, "pauseRecord by:${by}, failed! ${it.message}")
        }
    }

    fun resumeRecord(by: String) {
        PurpleLogger.current.d(TAG, "resumeRecord by:${by}")
        if (durationTimer == null) {
            durationTimer = Timer()
        }
        val startTimeMills = System.currentTimeMillis()
        val customTimerTask = CustomTimerTask(
            startTimeMills,
            mCustomTimerTask?.countedSeconds ?: 0,
            mCustomTimerTask?.lastSaveSeconds ?: 0
        )
        mCustomTimerTask = customTimerTask
        durationTimer?.schedule(customTimerTask, 0, 1000)
        runCatching {
            mMediaRecorder?.resume()
        }.onFailure {
            PurpleLogger.current.d(TAG, "resumeRecord by:${by}, failed! ${it.message}")
        }
    }

    fun startRecord(context: Context) {
        PurpleLogger.current.d(TAG, "startRecord")
        val theOutPut = getOutPut()
        if (theOutPut == null) {
            PurpleLogger.current.d(TAG, "startRecord, outPut is null, return")
            return
        }
        val outDirFile = theOutPut.outDirFile
        if (outDirFile == null) {
            PurpleLogger.current.d(TAG, "startRecord, outDirFile is null, return")
            return
        }
        if (!outDirFile.exists()) {
            PurpleLogger.current.d(
                TAG,
                "startRecord, outDirFile not exists, create it! path:${outDirFile.path}"
            )
            outDirFile.mkdirs()
        }
        outDirFile.setReadable(true)
        outDirFile.setWritable(true)
        if (!outDirFile.canWrite()) {
            PurpleLogger.current.d(TAG, "startRecord, output file dir can not write, return!")
            return
        }

        initMediaRecorder(context)

        val startTimeMills = System.currentTimeMillis()
        theOutPut.startTimeMills = startTimeMills

        prepareMediaRecorder(theOutPut.getOutPutFilePath())

        if (durationTimer == null) {
            durationTimer = Timer()
        }

        val customTimerTask = CustomTimerTask(startTimeMills)
        mCustomTimerTask = customTimerTask
        durationTimer?.schedule(customTimerTask, 0, 1000)
        mMediaRecorder?.start()
        isStarted = true
    }

    private fun resetMediaRecorderOutPutFile(filePath: String) {
        PurpleLogger.current.d(
            TAG,
            "resetMediaRecorderOutPutFile, filePath:$filePath"
        )
        isPrepared = false
        mMediaRecorder?.reset()
        prepareMediaRecorder(filePath)
        mMediaRecorder?.start()
    }

    fun isStart(): Boolean {
        return isStarted
    }

    fun getOutPut(): OutPut? {
        return outPut
    }

    fun cleanUp(by: String) {
        getOutPut()?.cleanUp(by)
    }

    override fun onStop(owner: LifecycleOwner) {
        stopRecord("lifecycle onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        release("lifecycle onDestroy")
        owner.lifecycle.removeObserver(this)
    }

    data class AudioRecorderState(
        val isStarted: Boolean = false,
        val isPaused: Boolean = false,
        val seconds: Int = 0,
        val file: String? = null,
        val maxRecordSeconds: Int = MAX_RECORD_SECONDS
    )

    interface UpdateCallBack {
        fun onUpdate(startTime: Long, seconds: Int)
        fun onPause()
        fun onStop()
    }

    open class OutPut {
        var startTimeMills: Long = 0L
        var outDirFile: File? = null
        var maxRecordSeconds: Int = MAX_RECORD_SECONDS
        var maxSize: Int = Byte.MAX_VALUE.toInt()//128kb

        open fun getOutPutFilePath(): String {
            val filePath = "${outDirFile?.path}/audio_${startTimeMills}_1.mp3"
            return filePath
        }

        fun cleanUp(by: String) {
            PurpleLogger.current.d(TAG, "cleanUp, by:$by")
            val dirFile = outDirFile
            if (dirFile == null) {
                return
            }
            if (!dirFile.exists()) {
                return
            }
            dirFile.deleteRecursively()
        }
    }

    class SegmentedOutput : OutPut() {
        var fragmentDuration: Int = 10//10s
        var fragmentSize: Int = 100//kb
        private var currentFragment: Int = 1
        private var outPutFilePaths: MutableList<String> = mutableListOf()
        override fun getOutPutFilePath(): String {
            val filePath = "${outDirFile?.path}/audio_${startTimeMills}_${currentFragment++}.mp3"
            outPutFilePaths.add(filePath)
            return filePath
        }
    }

    inner class CustomTimerTask(
        private val startTimeMills: Long,
        var countedSeconds: Int = 0,
        var lastSaveSeconds: Int = 0
    ) : TimerTask() {

        override fun run() {
            if (!isPrepared) {
                PurpleLogger.current.d(TAG, "CustomTimerTask, not prepared, return")
                return
            }
            if (!isStarted) {
                PurpleLogger.current.d(TAG, "CustomTimerTask, not start, return")
                return
            }
            if (isPaused) {
                PurpleLogger.current.d(TAG, "CustomTimerTask, paused, return")
                return
            }
            val theOutPut = outPut
            if (theOutPut == null) {
                PurpleLogger.current.d(TAG, "CustomTimerTask, outPut is null, return")
                return
            }
            if (countedSeconds > theOutPut.maxRecordSeconds) {
                PurpleLogger.current.d(
                    TAG,
                    "CustomTimerTask, stop record, because reach output max seconds:${theOutPut.maxRecordSeconds}"
                )
                stopWhenReachMaxSeconds()
                return
            }
            if (theOutPut is SegmentedOutput) {
                //分段输出
                val segmentedOutput = theOutPut
                if ((countedSeconds - lastSaveSeconds) == (segmentedOutput.fragmentDuration)) {
                    lastSaveSeconds = countedSeconds
                    val filePath = theOutPut.getOutPutFilePath()
                    resetMediaRecorderOutPutFile(filePath)
                }
            }
            mUpdateCallBack?.onUpdate(startTimeMills, countedSeconds)
            if (isPrepared) {
                countedSeconds++
            }
        }
    }
}