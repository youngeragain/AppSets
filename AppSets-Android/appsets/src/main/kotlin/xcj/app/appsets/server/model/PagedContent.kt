package xcj.app.appsets.server.model

data class PagedContent<T>(
    val page: Int,
    val pageSize: Int,
    var content: T? = null
)
