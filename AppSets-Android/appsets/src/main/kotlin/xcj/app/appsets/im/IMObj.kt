package xcj.app.appsets.im

import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.starter.android.util.PurpleLogger

sealed class IMObj(
    val bio: Bio,
    var isRelated: Boolean = false
) {
    val id: String
        get() = bio.bioId
    val name: String
        get() = bio.bioName ?: id
    val avatarUrl: Any?
        get() = bio.bioUrl

    class IMSingle(
        bio: Bio,
        val userRoles: String? = null
    ) : IMObj(bio, RelationsUseCase.getInstance().hasUserRelated(bio.bioId))

    class IMGroup(
        bio: Bio,
        val bios: List<Bio>? = null
    ) : IMObj(bio, RelationsUseCase.getInstance().hasGroupRelated(bio.bioId)) {

    }

    companion object {

        private const val TAG = "ImObj"

        fun fromBio(bio: Bio): IMObj {
            val imObj = when (bio) {
                is UserInfo -> {
                    IMSingle(bio)
                }

                is GroupInfo -> {
                    IMGroup(bio, bio.userInfoList)
                }

                is Application -> {
                    IMGroup(bio)
                }

                else -> IMSingle(bio)
            }
            PurpleLogger.current.d(TAG, "fromBio, bio:$bio, imgObj:${IMObj}")
            return imObj
        }
    }

}