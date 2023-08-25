package xcj.app.appsets.util

import xcj.app.appsets.server.model.AppsWithCategory
import xcj.app.core.android.ApplicationHelper

sealed class ApplicationCategory(val name: String, val nameZh: String) {
    object Social : ApplicationCategory("social", "社交")
    object Tool : ApplicationCategory("tool", "工具")
    object Game : ApplicationCategory("game", "游戏")
    object Media : ApplicationCategory("media", "媒体")
    object Music : ApplicationCategory("music", "音乐")
    object Video : ApplicationCategory("video", "视频")
    object Money : ApplicationCategory("money", "金融")
    object Camera : ApplicationCategory("camera", "相机")
    object Live : ApplicationCategory("live", "直播")
    object Shopping : ApplicationCategory("shopping", "购物")
    object Health : ApplicationCategory("health", "健康")

    object Others : ApplicationCategory("others", "其它")

    companion object {
        fun mapCategoryToLocale(appsWithCategory: AppsWithCategory) {
            if (ApplicationHelper.application.resources.configuration.locales.get(0)?.country == "China") {
                appsWithCategory.category = appsWithCategory.categoryNameZh
            } else {
                val initialUpperCase = appsWithCategory.categoryName.first().uppercase()
                val remaining = appsWithCategory.categoryName.substring(1)
                appsWithCategory.category = "${initialUpperCase}${remaining}"
            }
        }
    }

}