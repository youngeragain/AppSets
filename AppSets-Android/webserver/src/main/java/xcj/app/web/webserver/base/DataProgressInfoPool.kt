package xcj.app.web.webserver.base

import java.util.UUID
import kotlin.apply
import kotlin.collections.first

object DataProgressInfoPool {
    //key is UUID
    private val pool: LinkedHashMap<String, DataProgressInfo> =
        LinkedHashMap<String, DataProgressInfo>(16, 0.75f, true)

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
        }
    }

    fun makeEnd(uuid: String): DataProgressInfo {
        return obtainById(uuid).apply {
            total = 1
            current = 1
        }
    }

    fun clear() {
        pool.clear()
    }
}