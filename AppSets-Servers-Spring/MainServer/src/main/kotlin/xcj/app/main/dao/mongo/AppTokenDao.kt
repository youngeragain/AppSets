package xcj.app.main.dao.mongo

import xcj.app.main.model.table.mongo.AppToken


interface AppTokenDao {

    fun getAppTokenByKeySecret(appSetsAppId: String): AppToken?

    fun addToken(appToken: AppToken): AppToken?

}

