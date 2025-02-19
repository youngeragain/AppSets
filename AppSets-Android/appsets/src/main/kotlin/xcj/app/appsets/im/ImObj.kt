package xcj.app.appsets.im

import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.starter.android.util.PurpleLogger

sealed class ImObj(
    val bio: Bio,
    var isRelated: Boolean = false
) {
    val id: String
        get() = bio.id
    val name: String
        get() = bio.name ?: id
    val avatarUrl: Any?
        get() = bio.bioUrl

    class ImSingle(
        bio: Bio,
        val userRoles: String? = null
    ) : ImObj(bio, RelationsUseCase.getInstance().hasUserRelated(bio.id))

    class ImGroup(
        bio: Bio,
        val bios: List<Bio>? = null
    ) : ImObj(bio, RelationsUseCase.getInstance().hasGroupRelated(bio.id)) {

    }

    companion object {

        private const val TAG = "ImObj"

        fun fromBio(bio: Bio): ImObj {
            val imObj = when (bio) {
                is UserInfo -> {
                    ImSingle(bio)
                }

                is GroupInfo -> {
                    ImGroup(bio, bio.userInfoList)
                }

                is Application -> {
                    ImGroup(bio)
                }

                else -> ImSingle(bio)
            }
            PurpleLogger.current.d(TAG, "fromBio, bio:$bio, imgObj:${ImObj}")
            return imObj
        }
    }

}