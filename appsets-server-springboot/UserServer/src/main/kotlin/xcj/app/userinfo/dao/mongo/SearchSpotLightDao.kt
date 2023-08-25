package xcj.app.userinfo.dao.mongo

import xcj.app.userinfo.model.table.mongo.SpotLight

interface SearchSpotLightDao {
    fun getSpotLightInfo():SpotLight?
}