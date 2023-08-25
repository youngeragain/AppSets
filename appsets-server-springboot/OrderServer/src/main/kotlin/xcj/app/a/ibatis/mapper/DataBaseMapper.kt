package xcj.app.a.ibatis.mapper

import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Repository
import xcj.app.a.dbpet.DataBaseDefinition
import xcj.app.a.model.User

@Repository
@Mapper
interface DataBaseMapper:IMapper<DataBaseDefinition> {
    fun getAll():List<User>
    fun saveUser(user:User)

    fun customQuery(querySql:String):List<HashMap<String, String?>>
}