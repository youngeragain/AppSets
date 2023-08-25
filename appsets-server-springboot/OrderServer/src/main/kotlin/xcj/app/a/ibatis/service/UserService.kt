package xcj.app.a.ibatis.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xcj.app.a.annotation.WithPermission
import xcj.app.a.ibatis.IUserService
import xcj.app.a.ibatis.mapper.UserMapper
import xcj.app.a.model.User

fun common(queryString: String, userMapper: UserMapper):List<HashMap<String, String?>>{
    val invalidSql = mutableListOf("insert", "update", "delete", "drop", "create")
    val has = invalidSql.any {
        queryString.contains(it, true)
    }
    if(has)
        return emptyList()
    return userMapper.customQuery(queryString)
}

@Service("ibatisUserService")
class UserService : IUserService {
    @Autowired
    lateinit var userMapper: UserMapper

    override fun getAll(): List<User> {

        return userMapper.getAll()
    }
    @WithPermission(["admin"], [])
    fun customQuery(queryString:String):List<HashMap<String, String?>>{
        return common(queryString, userMapper)
    }
}

@Service("ibatisUserService1")
class UserService1 : IUserService {
    @Autowired
    lateinit var userMapper: UserMapper

    override fun getAll(): List<User> {
        return userMapper.getAll()
    }
    @WithPermission(["admin"], [])
    fun customQuery(queryString:String):List<HashMap<String, String?>>{
        return common(queryString, userMapper)
    }
}

