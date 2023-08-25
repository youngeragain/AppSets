package xcj.app.userinfo.model.res

import xcj.app.userinfo.model.table.mongo.Application

data class AppsWithCategory(
    val categoryName:String,
    val categoryNameZh:String,
    val applications:List<Application>)