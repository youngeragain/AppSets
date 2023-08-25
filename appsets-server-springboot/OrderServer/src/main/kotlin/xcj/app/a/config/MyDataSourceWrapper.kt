package xcj.app.a.config

import jakarta.sql.DataSource

data class MyDataSourceWrapper(
    val dataSource: DataSource?,
    val id:Int?,
    var isDefault:Boolean = false)
