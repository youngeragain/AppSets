package xcj.app.main.model.common

data class PagedContent<T>(
    val page: Int,
    val pageSize: Int,
    var content: T? = null
)
