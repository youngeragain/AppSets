package xcj.app.userinfo.dao.mysql

import org.apache.ibatis.annotations.Mapper
import xcj.app.userinfo.model.table.mysql.ScreenMediaFileUrl
@Mapper
interface ScreenMediaFileUrlDao {
    fun addScreenMediaFileUrl(
        screenMediaFileUrl: ScreenMediaFileUrl):Int
    fun addScreenMediaFileUrls(
        screenId:String,
        screenMediaFileUrls: List<ScreenMediaFileUrl>):Int
}