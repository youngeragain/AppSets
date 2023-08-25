package xcj.app.a.config

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

class DynamicDataSource: AbstractRoutingDataSource(){
    override fun determineCurrentLookupKey(): Any? {
        return DynamicDataSourceRegister.instance.getDataSourceRouterKey()
    }
}