package xcj.app.userinfo.model.res

import xcj.app.userinfo.model.table.mongo.Application

data class CombineSearchRes(
    val applications:List<Application>? = null,
    val screens:List<UserScreenInfoRes>? = null,
    val users:List<UserInfoRes>? = null,
    val groups:List<GroupInfoRes>? = null
)