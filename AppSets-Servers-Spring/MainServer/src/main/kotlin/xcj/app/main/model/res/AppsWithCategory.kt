package xcj.app.main.model.res

import xcj.app.main.model.table.mongo.Application

data class AppsWithCategory(
    val categoryName: String,
    val categoryNameZh: String,
    val applications: List<Application>
)