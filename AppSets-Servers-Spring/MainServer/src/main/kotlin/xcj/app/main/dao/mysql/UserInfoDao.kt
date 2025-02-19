package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Mapper
import xcj.app.main.model.table.mysql.UserInfo

/**
 * 用户信息无法单独删除增加，增删依赖于user表
 */
@Mapper
interface UserInfoDao {

    fun updateUserInfo(userInfo: UserInfo): Int

    fun getUserInfoByUid(uid: String): UserInfo

    fun getUserInfoByUids(uids: List<String>): List<UserInfo>

}