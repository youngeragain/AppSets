package xcj.app.main.model.res

import xcj.app.main.model.table.mongo.Application

data class CombineSearchRes(
    val applications: List<Application>? = null,
    val screens: List<UserScreenRes>? = null,
    val users: List<UserInfoRes>? = null,
    val groups: List<GroupInfoRes>? = null
)