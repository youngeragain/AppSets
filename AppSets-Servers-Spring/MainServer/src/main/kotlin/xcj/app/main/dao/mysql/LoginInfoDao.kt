package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Mapper
import xcj.app.main.model.table.mysql.LoginInfo

@Mapper
interface LoginInfoDao {
    fun addLoginInfo(loginInfo: LoginInfo): Int
}