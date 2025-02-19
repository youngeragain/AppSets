package xcj.app.main.service.mongo

import xcj.app.DesignResponse
import xcj.app.main.model.res.SpotLightRes

interface SpotLightService {
    fun getSpotLightInfo(): DesignResponse<SpotLightRes>
}