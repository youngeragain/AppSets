package xcj.app.main

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper

class ExtendedRequestWrapper(request: HttpServletRequest?) : HttpServletRequestWrapper(request),
    ApiDesignHeaderProvider {
    private val apiDesignHeaders: MutableMap<String, Any?> = mutableMapOf()
    override fun getDesignHeader(name: String): String? {
        return apiDesignHeaders[name] as? String
    }

    override fun getDesignHeaderInt(name: String): Int? {
        return apiDesignHeaders[name] as? Int
    }

    override fun addDesignHeader(name: String, value: Any) {
        apiDesignHeaders[name] = value
    }

    override fun getDesignHeaders(): Map<String, Any?> {
        return apiDesignHeaders
    }

    /**
     * 兼容原始获取模式
     */
    override fun getHeader(name: String): String? {
        val header = super.getHeader(name)
        if (header.isNullOrEmpty()) {
            return apiDesignHeaders[name] as? String
        }
        return header
    }

    override fun getIntHeader(name: String): Int {
        val header = super.getIntHeader(name)
        if (header == -1) {
            return (apiDesignHeaders[name] as? Int) ?: -1
        }
        return header
    }
}