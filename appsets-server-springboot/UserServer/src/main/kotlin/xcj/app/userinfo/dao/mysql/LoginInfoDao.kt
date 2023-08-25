package xcj.app.userinfo.dao.mysql

import org.apache.ibatis.annotations.Mapper
import xcj.app.userinfo.model.table.mysql.LoginInfo

@Mapper
interface LoginInfoDao {
    fun addLoginInfo(loginInfo: LoginInfo):Int
}