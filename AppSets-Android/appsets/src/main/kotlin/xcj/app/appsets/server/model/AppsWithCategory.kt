package xcj.app.appsets.server.model

data class AppsWithCategory(
    val categoryName: String,
    val categoryNameZh: String,
    val applications: MutableList<Application>
) {
    lateinit var category: String
}