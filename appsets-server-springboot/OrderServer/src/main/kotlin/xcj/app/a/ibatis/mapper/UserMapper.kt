package xcj.app.a.ibatis.mapper

import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Repository
import xcj.app.a.config.DataSourceUsed
import xcj.app.a.model.User

@Repository
@Mapper
interface UserMapper:IMapper<User> {
    @DataSourceUsed(0)
    fun getAll():List<User>
    @DataSourceUsed(1)
    fun saveUser(user:User)
    @DataSourceUsed(2)
    fun customQuery(querySql:String):List<HashMap<String, String?>>
}