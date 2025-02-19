package xcj.app.main.dao.mongo

import xcj.app.main.model.table.mongo.SpotLight

interface SpotLightDao {
    fun getSpotLight(): SpotLight?
}