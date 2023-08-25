package xcj.app.appsets.server.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.im.ImSessionHolder
import xcj.app.appsets.im.Member
import xcj.app.appsets.im.Session


@Entity(tableName = "UserInfo")
data class UserInfo(
    @Ignore
    var sinnInLocation: String? = null,
    @Ignore
    var signInIp: String? = null,
    @Ignore
    var agreeToTheAgreement: Int? = null,
    /**
     * 此uid在服务端应该数据脱敏
     * @hidden
     */
    @PrimaryKey
    override var uid: String,
    override var name: String? = null,
    var age: Int? = null,
    var sex: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var address: String? = null,
    @ColumnInfo(name = "avatar_url")
    override var avatarUrl: String? = null,
    var introduction: String? = null,
    var company: String? = null,
    var profession: String? = null,
    var website: String? = null,
    var roles: String? = null
) : Member, ImSessionHolder {
    @Ignore
    override var imSession: Session? = null
    override fun asImSingle(): ImObj.ImSingle {
        return ImObj.ImSingle(uid, name, avatarUrl, roles)
    }

    override fun asImGroup(): ImObj.ImGroup? = null

    constructor() : this(
        null,
        null,
        null,
        Int.MIN_VALUE.toString(),
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
        null
    )

    fun isContentSame(other: UserInfo): Boolean {
        return sinnInLocation == other.sinnInLocation &&
                signInIp == other.signInIp &&
                agreeToTheAgreement == other.agreeToTheAgreement &&
                uid == other.uid &&
                name == other.name &&
                age == other.age &&
                sex == other.sex &&
                email == other.email &&
                phone == other.phone &&
                address == other.address &&
                avatarUrl == other.avatarUrl &&
                introduction == other.introduction &&
                company == other.company &&
                profession == other.profession &&
                website == other.website

    }

    fun isDefault(): Boolean {
        return uid == "0" && name == "蒋开心" && age == 0
    }

    companion object {
        fun empty(): UserInfo {
            return UserInfo(
                null,
                null,
                null,
                "0",
                "蒋开心",
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
            )
        }

        fun basicInfo(uid: String, name: String?, avatarUrl: String?): UserInfo {
            return UserInfo(uid = uid, name = name, avatarUrl = avatarUrl)
        }
    }
}