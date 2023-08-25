package xcj.app.appsets.server.model

import xcj.app.appsets.usecase.models.Application

data class AppsWithCategory(
    val categoryName: String,
    val categoryNameZh: String,
    val applications: List<Application>
) {
    lateinit var category: String
}