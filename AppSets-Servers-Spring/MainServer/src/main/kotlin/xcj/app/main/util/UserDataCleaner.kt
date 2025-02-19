package xcj.app.main.util

import org.springframework.stereotype.Component
import xcj.app.main.dao.mysql.UserDao

@Component
class UserDataCleaner(private val userDao: UserDao){
    fun cleanUpUserData(uid:String) {
        userDao.deleteUser(uid)
        //Clean Linked Data
    }
}