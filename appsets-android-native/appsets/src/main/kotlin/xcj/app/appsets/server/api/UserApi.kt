package xcj.app.appsets.server.api

import retrofit2.Call
import retrofit2.http.*
import xcj.app.appsets.server.model.AddUserScreenParams
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.core.foundation.http.DesignResponse

interface UserApi:URLApi {

    @POST("user/login")
    suspend fun login(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<String?>

    @POST("user/login2")
    suspend fun login2(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<String?>

    @POST("user/login")
    @Headers("Content-Type:application/json")
    fun loginCall(
        @Body body: HashMap<String, Any?>
    ): Call<String?>

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
    ): DesignResponse<String?>

    @POST("user/friend/request/feedback")
    suspend fun requestAddFriendFeedback(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<Boolean>

    @POST("user/chatgroup/requestjoin")
    suspend fun requestJoinGroup(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<String?>

    @POST("user/chatgroup/requestjoin/feedback")
    suspend fun requestJoinGroupFeedback(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<Boolean>


    @GET("user/screens")
    suspend fun getScreens(
        @Query(value = "page") page: Int?,
        @Query(value = "pageSize") pageSize: Int?
    ): DesignResponse<List<UserScreenInfo>>

    @GET("user/screens/index/recommend")
    suspend fun getIndexRecommendScreens(
        @Query(value = "page") page: Int?,
        @Query(value = "pageSize") pageSize: Int?
    ): DesignResponse<List<UserScreenInfo>>

    @GET("user/screens/{userId}")
    suspend fun getScreensByUid(
        @Path("userId") uid: String,
        @Query(value = "page") page: Int?,
        @Query(value = "pageSize") pageSize: Int?
    ): DesignResponse<List<UserScreenInfo>>


    @POST("user/screen")
    @Headers("Content-Type:application/json")
    suspend fun addScreen(
        @Body body: AddUserScreenParams
    ): DesignResponse<Boolean?>

    @GET("user/screen/reviews/{screenId}")
    @Headers("Content-Type:application/json")
    suspend fun getScreenReviews(
        @Path("screenId") screenId: String
    ): DesignResponse<List<ScreenReview>?>

    @POST("user/screen/review")
    @Headers("Content-Type:application/json")
    suspend fun addScreenReview(
        @Body body: HashMap<String, Any?>
    ): DesignResponse<Boolean?>

    @GET("user/chatgroup/{groupId}")
    @Headers("Content-Type:application/json")
    suspend fun getGroupInfoById(
        @Path("groupId") groupId: String
    ): DesignResponse<GroupInfo?>

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
    suspend fun getFollowersByUser(@Path("uid") uid: String): DesignResponse<Map<String, List<UserInfo>?>>

    @GET("user/follower/followed/{uid}")
    suspend fun getMyFollowedThisUser(@Path("uid") uid: String): DesignResponse<Boolean>


    /*  @GET("user/screens")
      @Headers("Content-Type:application/json")
      suspend fun userScreens(
          @Header("token") token:String,
          @Query("userid") userId:Int?=-1
      ): BaseResponse<List<Screen1Vo>?>

      @GET("user/screens")
      @Headers("Content-Type:application/json")
      suspend fun userScreensWithPage(
          @Header("token") token:String,
          @Query("page") page:Long,
          @Query("size") size:Long
      ): BaseResponse<UsersScreenResponseWithPage?>

      @GET("users/screens")
      @Headers("Content-Type:application/json")
      suspend fun allUserScreensWithPage(
          @Header("token") token:String,
          @Query("page") page:Long,
          @Query("size") size:Long
      ): BaseResponse<AllUsersScreenResponseWithPage?>


      @GET("user/recommend/user")
      @Headers("Content-Type:application/json")
      suspend fun recommendUser(
          @Header("token") token:String
      ): BaseResponse<List<RecommendUser>?>

      @GET("user/recommend/screen")
      @Headers("Content-Type:application/json")
      suspend fun recommendScreen(
          @Header("token") token:String
      ): BaseResponse<List<RecommendScreen>?>


      @GET("user/screen/review")
      @Headers("Content-Type:application/json")
      suspend fun screenComments(
          @Header("token") token:String,
          @Query("id") screenId:Int
      ): BaseResponse<List<ScreenComments>?>


      @POST("user/screen/review")
      @Headers("Content-Type:application/json")
      suspend fun addScreenComments(
          @Header("token") token:String,
          @Body body: List<AddScreenCommentsRequest>
      ): BaseResponse<Boolean?>

      @POST("user/screen/history")
      @Headers("Content-Type:application/json")
      suspend fun saveMyBrowseScreenHistory(
          @Header("token") token:String,
          @Body body: List<AddScreenBrowseHistoryRequest>
      ): BaseResponse<Boolean?>

      @POST("user/screen")
      @Headers("Content-Type:application/json")
      suspend fun addScreen(
          @Header("token") token: String,
          @Body body: AddScreenRequest
      ): BaseResponse<Boolean?>

      @GET("/user/friends/info/simplify")
      @Headers("Content-Type:application/json")
      suspend fun getFriendsInfoSimplify(
          @Header("token") token: String
      ): BaseResponse<List<FriendInfoSimplify>?>

      @POST("user/friend/sendrequest")
      @Headers("Content-Type:application/json")
      suspend fun sendAddFriendRequest(
              @Header("token") token: String,
              @Body body: AddFriendRequest
      ): BaseResponse<Boolean?>

      @POST("user/group/sendrequest")
      @Headers("Content-Type:application/json")
      suspend fun sendAddGroupRequest(
          @Header("token") token: String,
          @Body body: AddGroupRequest
      ): BaseResponse<Boolean?>


      @GET("/user/friend/finishadd/{addfriendrequestid}/{ispassed}")
      @Headers("Content-Type:application/json")
      suspend fun finishAddFriendRequest(
          @Header("token") token: String,
          @Path("addfriendrequestid") requestId:Int,
          @Path("ispassed") isPassed:Int
      ): BaseResponse<Boolean?>


      @GET("/user/group/finishaddusertogroup/{addgrouprequestid}/{ispassed}")
      @Headers("Content-Type:application/json")
      suspend fun finishAddGroupRequest(
          @Header("token") token: String,
          @Path("addgrouprequestid") requestId:Int,
          @Path("ispassed") isPassed:Int
      ): BaseResponse<Boolean?>


      @GET("/user/groups")
      @Headers("Content-Type:application/json")
      suspend fun getAllMyGroupsInfo(
          @Header("token") token: String
      ): BaseResponse<List<ChatGroupInfo>?>

      @GET("/user/group/userinfos/{groupid}")
      @Headers("Content-Type:application/json")
      suspend fun getAllUserInfoInGroup(
          @Header("token") token: String,
          @Path("groupid") groupId:String
      ): BaseResponse<List<UserInfo>?>


      @POST("appsets/checkupdate")
      @Headers("Content-Type:application/json")
      suspend fun checkAppSetsUpdate(
          @Header("appID") appID: String,
          @Body body: CheckUpdateRequest
      ): BaseResponse<AppSetsAppVersion?>

      @POST("/user/signup")
      @Headers("Content-Type:application/json")
      suspend fun signup(
          @Header("app_id") appID: String,
          @Body body: SignupRequest
      ): BaseResponse<Boolean?>


      @GET("/user/followers/info")
      @Headers("Content-Type:application/json")
      suspend fun getUserFollowersAmountInfo(
          @Header("token") token: String,
          @Query("userid") userId:Int?
      ): BaseResponse<UserFollowersAmountInfoResponse?>

      @PUT("/user/followers/update")
      suspend fun updateUserFollowers(
          @Header("token") token: String,
          @Body body: List<UpdatedUserFollower>
      ):BaseResponse<Boolean?>

      @PUT("/user/screen/thumb")
      suspend fun saveUserScreenThumb(
          @Header("token") token: String,
          @Body body: List<SaveThumbRequest>
      ):BaseResponse<Boolean?>

      @GET("/app/outside")
      suspend fun getOutsidePageData(
          @Header("token") token: String
      ):BaseResponse<OutSidePageData?>

      @POST("/user/friends")
      suspend fun deleteFriends(
          @Header("token") token: String,
          @Body body:List<Int>
      ):BaseResponse<Boolean?>

      @PUT("/user/group")
      suspend fun createChatGroup(
          @Header("token") token: String,
          @Body body:CreateGroupRequest
      ):BaseResponse<ChatGroupInfo?>*/

}