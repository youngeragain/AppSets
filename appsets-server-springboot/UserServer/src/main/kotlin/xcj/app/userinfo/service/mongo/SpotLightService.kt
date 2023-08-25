package xcj.app.userinfo.service.mongo

import xcj.app.DesignResponse
import xcj.app.userinfo.model.table.mongo.SpotLight

interface SpotLightService {

    fun getWin11SpotLightInfo():DesignResponse<SpotLight>
}