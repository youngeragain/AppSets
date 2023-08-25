package xcj.app.userinfo.dao.mongo

import xcj.app.userinfo.model.table.mongo.AppToken


interface AppTokenDao {
    fun getAppTokenByKeySecret(appSetsAppId:String): AppToken?
    fun addToken(appToken: AppToken):AppToken?
}

