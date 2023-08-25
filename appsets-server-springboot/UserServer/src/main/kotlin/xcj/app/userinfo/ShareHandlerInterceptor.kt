package xcj.app.userinfo

import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import xcj.app.*
import xcj.app.userinfo.dao.mysql.RoleDao
import xcj.app.userinfo.service.SimpleUserRoleServiceImpl
import xcj.app.userinfo.service.UserRoleService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class ShareHandlerInterceptor(
    private val tokenHelper: TokenHelper,
    private val userRoleService: UserRoleService,
    ): HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        val r_host = request.getHeader("r_host")
        val r_port = request.getIntHeader("r_port")
        val r_address = request.getHeader("r_address")

        (handler as? HandlerMethod)?.let {
            if(it.method.isAnnotationPresent(ApiDesignPermission.CombineRequired::class.java)){
                throw ApiDesignPermissionException(
                    "Please add processing logic for Annotation Class of 'CombineRequired'!")
            }else{
                if(it.method.isAnnotationPresent(ApiDesignPermission.VersionRequired::class.java)){
                    try {
                        val version = request.getIntHeader(ApiDesignEncodeStr.versionStrToMd5)
                        if(version==-1)
                            throw ApiDesignPermissionException("Version is null or empty or blank!")
                        else{
                            val versionRequired = it.method.getAnnotation(ApiDesignPermission.VersionRequired::class.java)
                            val versionDefinition = versionRequired.leastVersionCode
                            if(version<versionDefinition)
                                throw ApiDesignPermissionException(
                                    "Your version is less than the server definition's version! Least version:${versionDefinition}")
                        }
                    }catch (e:NumberFormatException){
                        e.printStackTrace()
                        throw ApiDesignPermissionException("Version should be a number!")
                    }
                }
                var token:String? = null
                if(it.method.isAnnotationPresent(ApiDesignPermission.LoginRequired::class.java)){
                    token = request.getHeader(ApiDesignEncodeStr.tokenStrToMd5)
                    if(token.isNullOrEmpty()||token.isBlank())
                        throw ApiDesignPermissionException("Token is null or empty or blank!", ApiDesignCode.ERROR_CODE_TOKEN_MISSING)
                    val tokenWrapper =
                        tokenHelper.getTokenInRedisWrapper(token) ?: throw ApiDesignPermissionException("Token not found!", ApiDesignCode.ERROR_CODE_TOKEN_NOT_FOUND)
                    when (it.method.getAnnotation(ApiDesignPermission.LoginRequired::class.java).tokenState) {
                        ApiDesignPermission.LoginRequired.TOKEN_STATE_VALID_BUT_NOT_EXPIRED -> {
                            if(tokenWrapper.isExpire)
                                throw ApiDesignPermissionException("Token expired!", ApiDesignCode.ERROR_CODE_TOKEN_EXPIRED)
                        }
                        ApiDesignPermission.LoginRequired.TOKEN_STATE_VALID_HAS_EXPIRED -> {
                            if (!tokenWrapper.isExpire)
                               throw ApiDesignPermissionException("Token not expired!")
                        }
                    }
                }
                if(it.method.isAnnotationPresent(ApiDesignPermission.SpecialRequired::class.java)){
                    val specialRequired = it.method.getAnnotation(ApiDesignPermission.SpecialRequired::class.java)
                    val specialHeader = request.getHeader(specialRequired.keyCode)
                    if(specialHeader.isNullOrEmpty())
                        throw ApiDesignPermissionException("SpecialRequired, but no value founded!")
                }
                if(it.method.isAnnotationPresent(ApiDesignPermission.AdministratorRequired::class.java)){
                    if(!it.method.isAnnotationPresent(ApiDesignPermission.LoginRequired::class.java))
                        throw ApiDesignPermissionException("You are not logged!")
                    if(token.isNullOrEmpty()||token.isBlank())
                        throw ApiDesignPermissionException("Token is null or empty or blank!", ApiDesignCode.ERROR_CODE_TOKEN_MISSING)
                    val userRoleIsAdminByToken = userRoleService.userRoleIsAdminByToken(token)
                    if(!userRoleIsAdminByToken)
                        throw ApiDesignPermissionException("You does not have role:Administrator!")
                }
                if(it.method.isAnnotationPresent(ApiDesignPermission.UserRoleRequired::class.java)){
                    if(!it.method.isAnnotationPresent(ApiDesignPermission.UserRoleRequired::class.java))
                        throw ApiDesignPermissionException("You are not logged!")
                    if(token.isNullOrEmpty()||token.isBlank())
                        throw ApiDesignPermissionException("Token is null or empty or blank!", ApiDesignCode.ERROR_CODE_TOKEN_MISSING)
                    val userRoleRequired = it.method.getAnnotation(ApiDesignPermission.UserRoleRequired::class.java)
                    if(userRoleRequired.andRoles.isNotEmpty() &&userRoleRequired.orRoles.isEmpty()){
                        //hand andRoles
                        val andRolesNeeded = userRoleRequired.andRoles
                        val userHasThoseRoles = userRoleService.userHasThoseRolesStrict(token, andRolesNeeded)
                        if(!userHasThoseRoles)
                            throw ApiDesignPermissionException("[strict mode] You does not have roles:${andRolesNeeded.toList()}!")
                    }else if(userRoleRequired.andRoles.isEmpty()&&userRoleRequired.orRoles.isNotEmpty()){
                        //handle orRoles
                        val orRolesNeeded = userRoleRequired.orRoles
                        val userHasThoseRoles = userRoleService.userHasThoseRolesNotStrict(token, orRolesNeeded)
                        if(!userHasThoseRoles)
                            throw ApiDesignPermissionException("[not strict mode] You does not have roles:${orRolesNeeded}!")
                    }else if(userRoleRequired.andRoles.isNotEmpty()&&userRoleRequired.orRoles.isNotEmpty()){
                        //hand andRoles and or roles
                    }else{
                        return@let
                    }
                }
            }
        }
        return true
    }
}



