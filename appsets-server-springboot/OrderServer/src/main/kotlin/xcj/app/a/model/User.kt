package xcj.app.a.model

import java.util.*
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class User(
    @Id
    val id:Long?,
    val account:String?,
    val password:String?,
    val iToken:String?,
    val userInfoId:Long?,
    val signUpTime: Date?,
    val signInTime:Date?,
    val signInTimes:Int?,
    val signInDeviceInfo:String?,
    val signInLocation:String?,
    val signInIp:String?,
    val randomCode:String?,
    val uid:String?
) {
    constructor():this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null)
}