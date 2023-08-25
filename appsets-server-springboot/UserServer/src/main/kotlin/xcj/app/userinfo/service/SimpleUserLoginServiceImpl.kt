package xcj.app.userinfo.service

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xcj.app.ApiDesignCode
import xcj.app.CoreLogger
import xcj.app.DesignResponse
import xcj.app.userinfo.ktx.jsonStr
import xcj.app.userinfo.Helpers
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.UserDataCleaner
import xcj.app.userinfo.dao.mysql.LoginInfoDao
import xcj.app.userinfo.dao.mysql.UserDao
import xcj.app.userinfo.model.req.LoginParams
import xcj.app.userinfo.model.req.SignupParams
import xcj.app.userinfo.model.req.TokenTradeInParams
import xcj.app.userinfo.model.table.mongo.User
import xcj.app.userinfo.model.table.mysql.LoginInfo
import xcj.app.userinfo.service.external.sha256Hex
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Service
class SimpleUserLoginServiceImpl(
    private val tokenHelper: TokenHelper,
    private val userDao: UserDao,
    private val loginInfoDao: LoginInfoDao,
    private val userDataCleaner: UserDataCleaner,
    private val mongoTemplate: MongoTemplate
    ):UserLoginService, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext + Job()
    private val gson:Gson = Gson()
    override fun logIn(loginParams: LoginParams): DesignResponse<String> {
        return logInV2(loginParams)
    }

    private fun logInV1(loginParams: LoginParams): DesignResponse<String> {
        if(loginParams.account.isNullOrEmpty())
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Please provide account!")
        if(loginParams.password.isNullOrEmpty())
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Please provide password!")
        loginParams.signInDeviceInfo?:return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Please provide a signInDeviceInfo!")
        val user = userDao.getUserByAccountAndPassword(loginParams.account!!, loginParams.password!!)?: return DesignResponse(ApiDesignCode.ERROR_CODE_ACCOUNT_DOES_NOT_EXIST, "Account does not exist")
        launch(Dispatchers.IO) {
            val deviceInfo = if(loginParams.signInDeviceInfo.allEmpty){
                null
            }else{
                loginParams.signInDeviceInfo.jsonStr(gson)
            }
            loginInfoDao.addLoginInfo(LoginInfo(user.uid, deviceInfo, loginParams.signInLocation, loginParams.signInDeviceInfo.ip))
        }
        loginParams.account = null
        loginParams.password = null
        val generatedToken = tokenHelper.generateTokenAndSaveToRedis(user.uid, loginParams, user.canMultiOnline==1)
        return DesignResponse(data = generatedToken)
    }

    @Transactional
    private fun logInV2(loginParams: LoginParams): DesignResponse<String> {
        if(loginParams.account.isNullOrEmpty())
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Please provide account!")
        if(loginParams.password.isNullOrEmpty())
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Please provide password!")
        loginParams.signInDeviceInfo?:return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Please provide a signInDeviceInfo!")
        val user = userDao.getUserByAccount(loginParams.account!!) ?: return DesignResponse(ApiDesignCode.ERROR_CODE_ACCOUNT_DOES_NOT_EXIST, "Account does not exist")
        if(user.salt.isNullOrEmpty())
            throw Exception("User salt is null or empty when on call loginV2")
        val hash = Helpers.sha256Hex(Helpers.sha256Hex(loginParams.password!!) + Helpers.sha256Hex(user.salt!!))
        if(hash!=user.hash)
            return DesignResponse(ApiDesignCode.ERROR_CODE_ACCOUNT_DOES_NOT_EXIST, "Password does not correct for this account!")
        launch(Dispatchers.IO) {
            val newSalt = UUID.randomUUID().toString()
            val newHash = Helpers.sha256Hex(user.password + sha256Hex(newSalt))
            user.salt = newSalt
            user.hash = newHash
            userDao.updateUserSaltHash(user)
            val deviceInfo = if(loginParams.signInDeviceInfo.allEmpty){
                null
            }else{
                loginParams.signInDeviceInfo.jsonStr(gson)
            }
            loginInfoDao.addLoginInfo(LoginInfo(user.uid, deviceInfo, loginParams.signInLocation, loginParams.signInDeviceInfo.ip))
        }
        loginParams.account = null
        loginParams.password = null
        val generatedToken = tokenHelper.generateTokenAndSaveToRedis(user.uid, loginParams, user.canMultiOnline==1)
        return DesignResponse(data = generatedToken)

    }


    override fun logInByOtherDevice(token: String, loginParams: LoginParams): DesignResponse<String> {
        val tokenInRedisWrapper = tokenHelper.getTokenInRedisWrapper(token)
        if(tokenInRedisWrapper?.loginParams==null)
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Can't find loginParams with this token!")
        loginParams.account = tokenInRedisWrapper.loginParams.account
        loginParams.password = tokenInRedisWrapper.loginParams.password
        return logIn(loginParams)
    }

    override fun signUp(signupParams: SignupParams): DesignResponse<Boolean> {
       return signUpV2(signupParams)
    }

    /**
     * 注册时检测是否存在相同的账号
     * @return
     */
    override fun preSignUp(account: String): DesignResponse<Boolean> {
        val accountExist = userDao.isUserAccountExist(account)
        return DesignResponse(data = !accountExist)
    }

    fun signUpV1(signupParams: SignupParams): DesignResponse<Boolean>{
         val userAccountExist = userDao.isUserAccountExist(signupParams.account)
        if(userAccountExist){
           return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL,
               "This account has already been registered!", data = false)
        }
        val uid = Helpers.generateUserId()

        val addUserResult = userDao.addUser(uid, signupParams.account, signupParams.password, null, null,
            signupParams.name, signupParams.avatarUrl, signupParams.introduction, signupParams.tags,
            signupParams.sex, signupParams.age, signupParams.phone, signupParams.email,
            signupParams.area, signupParams.address, signupParams.website)
        return if(addUserResult==1){
            DesignResponse(info = "You have successfully registered, congratulations!", data = true)
        }else{
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Registration failed!", data = false)
        }
    }
    @Transactional
    fun signUpV2(signupParams: SignupParams): DesignResponse<Boolean>{
        val userAccountExist = userDao.isUserAccountExist(signupParams.account)
        if(userAccountExist){
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL,
                "This account has already been registered!", data = false)
        }
        val uid = Helpers.generateUserId()
        val salt = UUID.randomUUID().toString()
        val newPassword = Helpers.sha256Hex(signupParams.password)
        val hash = Helpers.sha256Hex(newPassword + sha256Hex(salt))
        val addUserResult = userDao.addUser(uid, signupParams.account, newPassword,salt, hash,
            signupParams.name, signupParams.avatarUrl, signupParams.introduction, signupParams.tags,
            signupParams.sex, signupParams.age, signupParams.phone, signupParams.email,
            signupParams.area, signupParams.address, signupParams.website)
        return if(addUserResult==1){
            DesignResponse(info = "You have successfully registered, congratulations!", data = true)
        }else{
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Registration failed!", data = false)
        }
    }
    override fun signOut(token:String): DesignResponse<Boolean> {
        tokenHelper.deleteTokenInRedis(token)
        return DesignResponse(info = "Sign out!", data = true)
    }


    override fun logOut(token: String, deleteData: Boolean): DesignResponse<String> {
        val uid = tokenHelper.getUidByToken(token)
        if(!deleteData){
            userDao.addDeleteUser(uid)
        }else{
            userDataCleaner.cleanUpUserData(uid)
        }
        tokenHelper.deleteTokenInRedis(token)
        val suffix = if(deleteData){
            "Your personal data is deleted from server!"
        }else{
            "Your personal data will saved!"
        }
        return DesignResponse(info = "Log out!, $suffix")
    }

    override fun logOutByUserId(userId: String, deleteData: Boolean): DesignResponse<String> {
        if(!deleteData){
            userDao.addDeleteUser(userId)
        }else{
            userDataCleaner.cleanUpUserData(userId)
        }
        val suffix = if(deleteData){
            "This person with userId${userId} data is deleted from server!"
        }else{
            "This person with userId${userId} data data will saved!"
        }
        return DesignResponse(info = "User Log out!, $suffix")
    }

    override fun tokenTradeIn(token: String, tokenTradeInParams: TokenTradeInParams): DesignResponse<String> {
        val newToken = tokenHelper.tokenTradeIn(token, tokenTradeInParams)
        return DesignResponse(data = newToken)
    }

    override fun addUserToMongo(user: User): DesignResponse<String> {
        val insert = mongoTemplate.insert(user)
        CoreLogger.d(message = "Insert:$insert")
        return DesignResponse(info = "Insert mongo successful!")

    }
}