package xcj.app.appsets.server.api

import retrofit2.Call
import retrofit2.http.*
import xcj.app.appsets.server.model.AddUserScreenParams
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.server.model.UserInfo
import xcj.app.starter.foundation.http.DesignResponse

interface UserApi {

    @POST("user/login")
    suspend fun login(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<String>

    @POST("user/login2")
    suspend fun login2(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<String>

    @POST("user/login")
    @Headers("Content-Type:application/json")
    fun loginCall(
        @Body body: HashMap<String, Any?>
    ): Call<String>

    @POST("user/signup")
    suspend fun signUp(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<Boolean>

    @POST("user/signup/pre")
    suspend fun preSignUp(
        @Query("ac") account: String
    ): DesignResponse<Boolean>

    @GET("user/info/get")
    suspend fun getLoggedUserInfo(): DesignResponse<UserInfo>

    @PUT("user/info/update")
    suspend fun updateUserInfo(
        @Body updateUserInfoParams: HashMap<String, String?>
    ): DesignResponse<Boolean>

    @GET("user/info/get/{uid}")
    suspend fun getUserInfoByUid(
        @Path("uid") uid: String
    ): DesignResponse<UserInfo>

    @GET("user/signout")
    suspend fun signOut(): DesignResponse<Boolean>

    @GET("user/friends")
    suspend fun getFriends(): DesignResponse<List<UserInfo>>

    @GET("user/chatgroups")
    suspend fun getChatGroupInfoList(): DesignResponse<List<GroupInfo>>

    @POST("user/chatgroup")
    suspend fun createChatGroup(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<Boolean>

    @GET("user/chatgroup/precheck")
    suspend fun createChatGroupPreCheck(
        @Query("name") groupName: String
    ): DesignResponse<Boolean>

    @POST("user/friend/request")
    suspend fun requestAddFriend(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<String>

    @POST("user/friend/request/feedback")
    suspend fun requestAddFriendFeedback(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<Boolean>

    @POST("user/chatgroup/requestjoin")
    suspend fun requestJoinGroup(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<String>

    @POST("user/chatgroup/requestjoin/feedback")
    suspend fun requestJoinGroupFeedback(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<Boolean>


    @GET("user/screens")
    suspend fun getScreens(
        @Query(value = "page") page: Int?,
        @Query(value = "pageSize") pageSize: Int?
    ): DesignResponse<List<ScreenInfo>>

    @GET("user/screens/index/recommend")
    suspend fun getIndexRecommendScreens(
        @Query(value = "page") page: Int?,
        @Query(value = "pageSize") pageSize: Int?
    ): DesignResponse<List<ScreenInfo>>

    @GET("user/screens/{userId}")
    suspend fun getScreensByUid(
        @Path("userId") uid: String,
        @Query(value = "page") page: Int?,
        @Query(value = "pageSize") pageSize: Int?
    ): DesignResponse<List<ScreenInfo>>


    @POST("user/screen")
    @Headers("Content-Type:application/json")
    suspend fun addScreen(
        @Body body: AddUserScreenParams
    ): DesignResponse<Boolean>

    @GET("user/screen/reviews/{screenId}")
    @Headers("Content-Type:application/json")
    suspend fun getScreenReviews(
        @Path("screenId") screenId: String
    ): DesignResponse<List<ScreenReview>>

    @POST("user/screen/review")
    @Headers("Content-Type:application/json")
    suspend fun addScreenReview(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<Boolean>

    @GET("user/chatgroup/{groupId}")
    @Headers("Content-Type:application/json")
    suspend fun getGroupInfoById(
        @Path("groupId") groupId: String
    ): DesignResponse<GroupInfo>

    @GET("user/follower/state/flip/{uid}")
    @Headers("Content-Type:application/json")
    suspend fun flipFollowToUserState(
        @Path("uid") uid: String
    ): DesignResponse<Boolean>

    @POST("user/screen/viewedbyuser/{screenId}")
    suspend fun screenViewedByUser(
        @Path("screenId") screenId: String
    ): DesignResponse<Boolean>

    @GET("user/screen/viewcount/{screenId}")
    suspend fun getScreenViewCount(
        @Path("screenId") screenId: String
    ): DesignResponse<Int>

    @POST("user/screen/likeitbyuser/{screenId}")
    suspend fun screenLikeItByUser(
        @Path("screenId") screenId: String,
        @Query("count") count: Int
    ): DesignResponse<Boolean>

    @GET("user/screen/likecount/{screenId}")
    suspend fun getScreenLikedCount(
        @Path("screenId") screenId: String
    ): DesignResponse<Int>

    @POST("user/screen/collectbyuser/{screenId}")
    suspend fun screenCollectByUser(
        @Path("screenId") screenId: String,
        @Query("category") category: String?
    ): DesignResponse<Boolean>

    @POST("user/screen/collectbyuser/remove/{screenId}")
    suspend fun removeCollectedScreen(
        @Path("screenId") screenId: String
    ): DesignResponse<Boolean>


    @GET("user/screen/collectbyuser/{screenId}")
    suspend fun screenIsCollectByUser(
        @Path("screenId") screenId: String
    ): DesignResponse<Boolean>

    @PUT("user/screen/publicstate/{screenId}")
    suspend fun changeScreenPublicState(
        @Path("screenId") screenId: String,
        @Query("public") isPublic: Boolean
    ): DesignResponse<Boolean>

    @GET("user/follower/active_passive/{uid}")
    suspend fun getFollowersByUser(
        @Path("uid") uid: String
    ): DesignResponse<Map<String, List<UserInfo>?>>

    @GET("user/follower/followed/{uid}")
    suspend fun getMyFollowedThisUser(
        @Path("uid") uid: String
    ): DesignResponse<Boolean>

}