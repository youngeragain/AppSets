package xcj.app.appsets.server.model

import xcj.app.appsets.usecase.models.Application

data class CombineSearchRes(
    val applications: List<Application>? = null,
    val screens: List<UserScreenInfo>? = null,
    val users: List<UserInfo>? = null,
    val groups: List<GroupInfo>? = null
)