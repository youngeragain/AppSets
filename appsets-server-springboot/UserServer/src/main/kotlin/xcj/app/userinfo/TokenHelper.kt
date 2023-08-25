package xcj.app.userinfo

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.springframework.data.redis.core.StringRedisTemplate
import xcj.app.ApiDesignPermissionException
import xcj.app.CoreLogger
import xcj.app.userinfo.ktx.jsonStr
import xcj.app.userinfo.model.redis.TokenInRedisWrapper
import xcj.app.userinfo.model.req.LoginParams
import xcj.app.userinfo.model.req.TokenTradeInParams
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * 由Configuration提供单例
 */
class TokenHelper(private val stringRedisTemplate: StringRedisTemplate) {

    private lateinit var jwtVerifier: JWTVerifier
    private val gson:Gson = Gson()
    private val valueOps = stringRedisTemplate.opsForValue()
    private val hashOps = stringRedisTemplate.opsForHash<String, String>()
    private val zSetOps = stringRedisTemplate.opsForZSet()
    private val hmaC256 = Algorithm.HMAC256("${System.currentTimeMillis()}")

    fun generateTokenAndSaveToRedis(uid:String, loginParams: LoginParams, canMultiOnline:Boolean):String{
        val tokenExpireDuration = Duration.ofDays(3)
        //token在过期后存在时间，由于定时任务扫描redis key和设置redis key 过期时间有时间间隔，所以此值存在误差
        val tokenAfterExpireDuration = Duration.ofMinutes(15)
        val calendar = Calendar.getInstance()
        val now = calendar.time
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)+tokenExpireDuration.toDays().toInt())
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)+tokenAfterExpireDuration.toMinutes().toInt())
        val tokenInRedisWrapper = TokenInRedisWrapper(
            createTime = now.time,
            expireAt = calendar.time.time,
            lastToken = null,
            canMultiOnline = canMultiOnline,
            loginParams = loginParams
        )
        return generateTokenAndSaveToRedis(uid, tokenInRedisWrapper, now, calendar.time)
    }
    private fun generateTokenAndSaveToRedis(
        uid:String,
        tokenInRedisWrapper: TokenInRedisWrapper,
        genDate: Date,
        withExpiresAt:Date
    ):String{
        tokenInRedisWrapper.loginParams?:throw ApiDesignPermissionException("tokenInRedisWrapper.loginParams is null!")
        //如果不能多端登录，且已经有相同登录信息的设备登录了，flag就还是-1
        var tokenFoundedInRedis:String?=null
        val count = zSetOps.count(uid, 0.0, Byte.MAX_VALUE.toDouble())
        if(tokenInRedisWrapper.canMultiOnline==false){
            if(count==1L){
                val tokens = zSetOps.range(uid, 0, 1)
                if(tokens?.size==1){
                    val tokenOfUid = tokens.first()
                    val tokenInRedisWrapper1 = getTokenInRedisWrapper(unWrapperTokenKeyInRedis(tokenOfUid))
                    val compareToOtherLoginParams =
                        tokenInRedisWrapper1?.loginParams?.compareToOtherLoginParams(tokenInRedisWrapper.loginParams)
                    if(compareToOtherLoginParams==true){
                        throw ApiDesignPermissionException("uid:${uid} Your account can't multi device online! only one device can login at the same time! this time, your login info is same as last time!")
                    }else
                        tokenFoundedInRedis = tokenOfUid
                }
            }else if(count!=null&&count>1L){
                throw ApiDesignPermissionException("uid:${uid} Design rules are broken! Developers please self-check.")
            }
        }
        if(tokenInRedisWrapper.canMultiOnline==false&&tokenFoundedInRedis!=null){
            CoreLogger.d("blue", "uid:$uid Your current login info is not same as last login info, your last login will become invalid! because your account can't multi device online!")
            stringRedisTemplate.delete(tokenFoundedInRedis)
        }
        val privatePayload = mapOf("uid" to uid)
        val token = JWT.create()
            .withPayload(privatePayload)
            .withExpiresAt(withExpiresAt)
            .sign(hmaC256)
        val realTokenInRedis = tokenKeyInRedis(token)
        valueOps.set(realTokenInRedis, tokenInRedisWrapper.jsonStr(gson), withExpiresAt.time-genDate.time, TimeUnit.MILLISECONDS)
        //多设备可同时登录[在线]
        if(tokenInRedisWrapper.canMultiOnline==true){
            CoreLogger.d("blue", "uid:${uid} Your account can multi device online!")
            if(count!=null){
                if(count==0L){
                    zSetOps.add(uid, realTokenInRedis, 0.0)
                }else if(count>0L){
                    zSetOps.add(uid, realTokenInRedis, (count+1.0))
                }
            }
        }else{
            if(count==0L){
                CoreLogger.d("blue", "uid:${uid} Your account can only login on one device at the same time!")
                zSetOps.add(uid, realTokenInRedis, 0.0)
            }else if(count==1L){
                if(tokenFoundedInRedis!=null){
                    zSetOps.remove(uid, tokenFoundedInRedis)
                    zSetOps.add(uid, realTokenInRedis, 0.0)
                }
            }
        }
        return token
    }
    fun getUidByToken(token:String):String{
        val decodedJWT = JWT.decode(token)//maybe throw JWTDecodeException
        val uidClaim = decodedJWT.getClaim("uid")
        val accountClaim = decodedJWT.getClaim("account")
        if(uidClaim.isMissing||uidClaim.isNull){
            throw Exception()
        }
        val uidString = uidClaim.asString()
        if(uidString.isNullOrEmpty())
            throw Exception()
        return uidString
    }
    fun verify(token: String): DecodedJWT {
        if(!::jwtVerifier.isInitialized){
            jwtVerifier = JWT.require(hmaC256).build()
        }
        return jwtVerifier.verify(token)

    }

    fun deleteTokenInRedis(token: String) {
        val uid = getUidByToken(token)
        zSetOps.remove(uid, tokenKeyInRedis(token))
        stringRedisTemplate.delete(tokenKeyInRedis(token))
    }

    fun tokenExistInRedis(token: String): Boolean {
        return stringRedisTemplate.hasKey(tokenKeyInRedis(token))
    }

    fun getTokenInRedisWrapper(token: String): TokenInRedisWrapper? {
        if (!tokenExistInRedis(token))
            return null
        val wrapperJson = valueOps.get(tokenKeyInRedis(token))
        return gson.fromJson(wrapperJson, TokenInRedisWrapper::class.java)

    }
    fun countToken():Int{
        return stringRedisTemplate.keys("${TOKEN_IN_REDIS_PREFIX}*").size
    }

    fun countNotExpireToken():Int{
        val keys = stringRedisTemplate.keys("${TOKEN_IN_REDIS_PREFIX}*")
        return keys.mapNotNull {
            getTokenInRedisWrapper(it.substringAfter(TOKEN_IN_REDIS_PREFIX))?.isExpire==false
        }.size
    }


    fun testRedisHash(){
        GlobalScope.launch {
            hashOps.put("user_1234", "name", "leijun")
            hashOps.put("user_1234", "age", "12")
            hashOps.put("user_1234", "sex", "female")
        }
    }

    /**
     * 更新用户对应的token集合，当token过期自动删除后删除用户下的token
     */
    fun updateAllTokenInRedis(){
        GlobalScope.launch(Dispatchers.Default) {
            val uids = stringRedisTemplate.keys("U*")
            if(uids.isEmpty())
                return@launch
            val tempTokenKeysRemove = mutableListOf<String>()
            val tempUidsRemove = mutableListOf<String>()
            for (uid in uids) {
                if(!stringRedisTemplate.hasKey(uid)){
                   continue
                }
                val tokensForUid = zSetOps.range(uid, 0, Byte.MAX_VALUE.toLong())
                if(tokensForUid==null){
                    continue
                }
                if(tokensForUid.isEmpty()){
                    tempUidsRemove.add(uid)
                    continue
                }
                tempTokenKeysRemove.clear()
                tokensForUid.forEach { tokenKey->
                    if (!stringRedisTemplate.hasKey(tokenKey)) {
                        tempTokenKeysRemove.add(tokenKey)
                    }
                }
                if(tempTokenKeysRemove.isNotEmpty())
                    zSetOps.remove(uid, *tempTokenKeysRemove.toTypedArray())
            }
            if(tempUidsRemove.isNotEmpty())
                stringRedisTemplate.delete(tempUidsRemove)
        }
    }

    fun isTokenExpiredFlag(token: String): Boolean {
        return getTokenInRedisWrapper(tokenKeyInRedis(token))?.isExpire==true
    }

    fun tokenTradeIn(token: String, tokenTradeInParams: TokenTradeInParams): String {
        val hasKey = stringRedisTemplate.hasKey(tokenKeyInRedis(token))
        if (!hasKey)
            throw ApiDesignPermissionException("Token not exist when trade-in!")
        val tokenInRedisWrapper = getTokenInRedisWrapper(token)
        if (tokenInRedisWrapper?.loginParams == null)//cannot be null
            throw ApiDesignPermissionException("The login information corresponding to the token does not exist [this is rare]!")
        val isAllFieldSame = tokenTradeInParams.compareToLoginParams(tokenInRedisWrapper.loginParams)
        if(!isAllFieldSame)
            throw ApiDesignPermissionException("The information does not correspond when changing a new token!")
        val tokenExpireDuration = Duration.ofDays(3)
        //token在过期后存在时间，由于定时任务扫描redis key和设置redis key 过期时间有时间间隔，所以此值存在误差
        val tokenAfterExpireDuration = Duration.ofMinutes(15)
        val calendar = Calendar.getInstance()
        val now = calendar.time
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)+tokenExpireDuration.toDays().toInt())
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)+tokenAfterExpireDuration.toMinutes().toInt())
        tokenInRedisWrapper.apply {
            lastToken = token
            createTime = now.time
            expireAt = calendar.time.time
        }
        val newToken =
            generateTokenAndSaveToRedis(getUidByToken(token), tokenInRedisWrapper, now, calendar.time)
        stringRedisTemplate.delete(tokenKeyInRedis(token))
        return newToken
    }

    fun getTokenInRedisIsExistByUids(uids: List<String>):Map<String, Boolean> {
        return uids.associate { uid ->
            val uidMappedTokenInRedis = zSetOps.range(uid, 0, -1)
            if (uidMappedTokenInRedis.isNullOrEmpty())
                return@associate uid to false
            for (tokenInRedis in uidMappedTokenInRedis) {
                if (stringRedisTemplate.hasKey(tokenInRedis)) {
                    return@associate uid to true
                }
            }
            uid to false
        }
    }

    fun generateAppToken(publicKey: String): String {
        val privatePayload = mapOf("key" to publicKey)
        return JWT.create()
            .withPayload(privatePayload)
            .sign(hmaC256)
    }
    companion object{
        const val TOKEN_IN_REDIS_PREFIX = "T"
        fun tokenKeyInRedis(token: String):String{
            return "${TOKEN_IN_REDIS_PREFIX}$token"
        }
        fun unWrapperTokenKeyInRedis(tokenKey:String):String{
            return tokenKey.substringAfter(TOKEN_IN_REDIS_PREFIX)
        }
    }
}
