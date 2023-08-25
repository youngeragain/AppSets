package xcj.app.userinfo

import org.springframework.stereotype.Component
import xcj.app.userinfo.dao.mysql.UserDao

@Component
class UserDataCleaner(private val userDao: UserDao){
    fun cleanUpUserData(uid:String) {
        userDao.deleteUser(uid)
        //Clean Linked Data
    }
}