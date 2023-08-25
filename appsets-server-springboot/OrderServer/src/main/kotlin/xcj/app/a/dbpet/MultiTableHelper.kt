package xcj.app.a.dbpet

import org.springframework.data.jpa.repository.Query

class MultiTableHelper(var tableCommonName:String) {
    private fun findAllDataBaseDefinition():List<DataBaseDefinition>{
        return emptyList()
    }
    fun findTablesInAllDB(){
        val tables = mutableListOf<TableDefinition>()
        findAllDataBaseDefinition().forEach{
            tables.addAll(it.findTablesInDbByLikeTableName(tableCommonName))
        }
    }
}

class MultiTableStrategy{

}
class DataBaseDefinition{
    val dbType:String="mysql"
    val dbName:String="dbName"
    val dbDriverClass:String = ""
    val dbConnectionUrl:String = ""
    val dbUserName:String = ""
    val dbPassword:String = ""
    val dbUseSSL:Boolean = false

    /**
     * @sample 1:
     * t_user_1
     * t_user_2
     * t_user_3
     *
     * @sample 2:
     * user_0-2
     * user_3_5
     * user_6_8
     * user_9_11
     *
     * @sample 3
     * 0_user
     * 1_user
     * 2-user
     * a_user
     * b_user
     * c_user
     *
     * far-fetched:
     * 0_user_1
     * t_user
     * user_t_1
     */
    fun findTablesInDbByLikeTableName(

        tableCommonName: String,
        hasPrefix:Boolean=false,
        hasSuffix:Boolean=false
    ):List<TableDefinition>{
        return emptyList()
    }

    @Query()
    fun query(value:String){

    }
}
class TableDefinition{
    val name:String="table_name"
    val tableSize:Int = 0
}

class DataBaseConnectionDefinition{
    val dataBaseDefinitions = mutableListOf<DataBaseDefinition>()
    fun getAllDataBases(optionalTableName:String?=null){
        val querySql = "select * from INFORMATION_SCHEMA.TABLES where table_schema = \"dbName\""
        val querySql1 = "select table_name from INFORMATION_SCHEMA.TABLES"+
                if(optionalTableName.isNullOrEmpty()){
                    ""
                }else{
                    " where table_name like $optionalTableName"
                }
    }
}