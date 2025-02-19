package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Mapper
import xcj.app.main.model.table.mysql.ScreenMediaFileUrl

@Mapper
interface ScreenMediaFileUrlDao {

    fun addScreenMediaFileUrl(
        screenMediaFileUrl: ScreenMediaFileUrl
    ): Int

    fun addScreenMediaFileUrls(
        screenId: String,
        screenMediaFileUrls: List<ScreenMediaFileUrl>
    ): Int

}