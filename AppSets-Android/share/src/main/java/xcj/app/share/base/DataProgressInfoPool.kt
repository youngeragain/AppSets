package xcj.app.share.base

import java.util.UUID

object DataProgressInfoPool {
    //key is UUID
    private val pool: LinkedHashMap<String, DataProgressInfo> =
        LinkedHashMap<String, DataProgressInfo>(16, 0.75f, true)

    fun cleanup() {
        pool.clear()
    }

    fun obtain(): DataProgressInfo {
        if (pool.isEmpty) {
            val uuid = UUID.randomUUID().toString()
            return obtainById(uuid)
        }
        val firstKey = pool.keys.first()
        return obtainById(firstKey)
    }

    fun obtainById(uuid: String): DataProgressInfo {
        if (pool.containsKey(uuid)) {
            val dataProgressInfo = pool.get(uuid)
            if (dataProgressInfo != null) {
                return dataProgressInfo
            }
        }
        synchronized(pool) {
            val new = DataProgressInfo(uuid, null)
            pool.put(uuid, new)
            return new
        }
    }

    fun makeStart(uuid: String): DataProgressInfo {
        return obtainById(uuid).apply {
            total = 1
            current = 0
            percentage = 0.00
        }
    }

    fun makeEnd(uuid: String): DataProgressInfo {
        return obtainById(uuid).apply {
            total = 1
            current = 1
            percentage = 100.00
        }
    }
}