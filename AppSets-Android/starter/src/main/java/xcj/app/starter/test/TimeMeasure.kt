package xcj.app.starter.test

import android.os.SystemClock

class TimeMeasure(recordOnInit: Boolean = true) {
    private var index = 0
    private val snapshots: ArrayList<Long> = arrayListOf()

    init {
        if (recordOnInit) {
            snapshot()
        }
    }

    fun reset() {
        index = 0
        snapshots.clear()
    }

    fun snapshot(): TimeMeasure {
        snapshots.add(index++, SystemClock.elapsedRealtime())
        return this
    }

    fun latestDiff(): Long {
        if (snapshots.size < 2) {
            return 0
        }
        return snapshots[snapshots.size - 1] - snapshots[snapshots.size - 2]
    }
}