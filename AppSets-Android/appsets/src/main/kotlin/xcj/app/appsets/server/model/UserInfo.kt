package xcj.app.appsets.server.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import xcj.app.appsets.im.Bio
import xcj.app.appsets.im.ImSessionHolder
import xcj.app.appsets.im.Session

@Entity(tableName = "UserInfo")
data class UserInfo(
    /**
     * @hidden
     */
    @PrimaryKey
    var uid: String,
    override var name: String? = null,
    var age: Int? = null,
    var sex: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var address: String? = null,
    @ColumnInfo(name = "avatar_url")
    var avatarUrl: String? = null,
    var introduction: String? = null,
    var company: String? = null,
    var profession: String? = null,
    var website: String? = null,
    var roles: String? = null,
    @Ignore
    var agreeToTheAgreement: Int? = null,
) : Bio, ImSessionHolder {

    override val id: String
        get() = uid

    @Ignore
    override var bioUrl: Any? = null

    @Ignore
    override var imSession: Session? = null

    constructor() : this(
        "",
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
        null
    )

    fun isDefault(): Boolean {
        return this == user0
    }

    companion object {
        private val user0 = UserInfo(
            "U0",
            "蒋开心",
            18,
            "female",
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

        fun default(): UserInfo {
            return user0
        }

        fun basic(uid: String, name: String?, avatarUrl: String?): UserInfo {
            return UserInfo(uid = uid, name = name, avatarUrl = avatarUrl)
        }

        fun isContentSame(a: UserInfo, other: UserInfo): Boolean {
            return a.agreeToTheAgreement == other.agreeToTheAgreement &&
                    a.uid == other.uid &&
                    a.name == other.name &&
                    a.age == other.age &&
                    a.sex == other.sex &&
                    a.email == other.email &&
                    a.phone == other.phone &&
                    a.address == other.address &&
                    a.avatarUrl == other.avatarUrl &&
                    a.introduction == other.introduction &&
                    a.company == other.company &&
                    a.profession == other.profession &&
                    a.website == other.website
        }
    }
}

class UserRole {
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_UNDEFINED = "undefined"
    }
}