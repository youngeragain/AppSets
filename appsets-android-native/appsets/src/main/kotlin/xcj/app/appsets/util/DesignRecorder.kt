package xcj.app.appsets.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.util.Timer
import java.util.TimerTask

class DesignRecorder {
    interface UpdateCallBack {
        fun onUpdate(startTime: Long, seconds: Int)
        fun onStop(type: Int)
    }

    open class OutPut {
        var startTimeMills: Long = 0L
        var outDirFile: File? = null
        var maxSeconds: Int = 60//60s
        var maxSize: Int = Byte.MAX_VALUE.toInt()//128kb
        var outPutFilePaths: MutableList<String> = mutableListOf()
        open fun getOutPutFilePath(): String {
            val filePath = "${outDirFile?.path}/audio_${startTimeMills}_1.mp3"
            outPutFilePaths.add(filePath)
            return filePath
        }
    }

    class SegmentedOutput : OutPut() {
        var fragmentDuration: Int = 10//10s
        var fragmentSize: Int = 100//kb
        private var currentFragment: Int = 1
        override fun getOutPutFilePath(): String {
            val filePath = "${outDirFile?.path}/audio_${startTimeMills}_${currentFragment++}.mp3"
            outPutFilePaths.add(filePath)
            return filePath
        }
    }

    private var outPut: OutPut? = null


    private var mUpdateCallBack: UpdateCallBack? = null

    private var durationTimer: Timer? = null

    private var mMediaRecorder: MediaRecorder? = null

    private var isPrepared = false

    private var start = false


    private fun stopWhenReachMaxSeconds() {
        stopRecord()
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

    fun releaseMediaRecorder() {
        stopRecord()
        mMediaRecorder?.release()
        mUpdateCallBack = null
        outPut = null
        mMediaRecorder = null
    }

    fun stopRecord() {
        start = false
        isPrepared = false
        mMediaRecorder?.stop()
        durationTimer?.cancel()
        durationTimer = null
        mUpdateCallBack?.onStop(10)
    }

    fun startRecord(context: Context) {
        if (outPut == null)
            return
        if (outPut!!.outDirFile?.exists() == false) {
            outPut!!.outDirFile?.mkdir()
        } else if (outPut!!.outDirFile?.canWrite() == false) {
            Log.e("DesignRecorder", "output file can not write!")
            return
        }
        initMediaRecorder(context)
        prepareMediaRecorder(outPut!!.getOutPutFilePath())
        if (durationTimer == null) {
            durationTimer = Timer()
        }
        val startTimeMills = System.currentTimeMillis()
        outPut!!.startTimeMills = startTimeMills
        start = true
        durationTimer?.scheduleAtFixedRate(CustomTimerTask(startTimeMills), 0, 1000)
        mMediaRecorder?.start()
    }

    private fun resetMediaRecorderOutPutFile(filePath: String) {
        isPrepared = false
        mMediaRecorder?.reset()
        prepareMediaRecorder(filePath)
        mMediaRecorder?.start()
    }

    fun isStart(): Boolean {
        return start
    }

    fun getRecordFiles(): List<String>? {
        return outPut?.outPutFilePaths
    }

    inner class CustomTimerTask(private val startTimeMills: Long) : TimerTask() {

        private var seconds = 0

        private var lastSaveSeconds = 0

        override fun run() {
            if (!isPrepared)
                return
            if (!start) {
                mUpdateCallBack?.onStop(10)
                Log.e("CustomTimerTask", "stop record, manual")
                return
            }
            if (seconds > outPut!!.maxSeconds) {
                mUpdateCallBack?.onStop(0)
                Log.e(
                    "CustomTimerTask",
                    "stop record, because reach output max seconds:${outPut!!.maxSeconds}"
                )
                stopWhenReachMaxSeconds()
                return
            }
            if (outPut is SegmentedOutput) {
                //分段输出
                val segmentedOutput = outPut as SegmentedOutput
                if ((seconds - lastSaveSeconds) == (segmentedOutput.fragmentDuration)) {
                    lastSaveSeconds = seconds
                    val filePath = outPut!!.getOutPutFilePath()
                    Log.e("CustomTimerTask", "resetMediaRecorderOutPutFile:$filePath")
                    resetMediaRecorderOutPutFile(filePath)
                }
            }
            mUpdateCallBack?.onUpdate(startTimeMills, seconds)
            if (isPrepared) {
                seconds++
            }
        }
    }
}