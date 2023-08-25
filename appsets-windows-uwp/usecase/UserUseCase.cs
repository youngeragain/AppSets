using AA;
using AA.rabbit;
using AppSets.im;
using AppSets.io;
using AppSets.models;
using AppSets.server;
using AppSets.utils;
using COSXML.Network;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Windows.Networking.NetworkOperators;
using Windows.System;
using Windows.UI.Core;
using ZXing.Aztec.Internal;
using static AppSets.usecase.UserUseCase;

namespace AppSets.usecase
{

    public class ConversationUseCase {


        public ObservableCollection<Session> userSessions = new ObservableCollection<Session>();
        public ObservableCollection<Session> groupSessions = new ObservableCollection<Session>();
        public ObservableCollection<Session> systemSessions = new ObservableCollection<Session>();

        private CoreDispatcher coreDispatcher;
        private SynchronizationContext context;
        public ConversationUseCase(SynchronizationContext context) {
            this.context = context;
            RabbitManager.addCallback(onRabbitMessageReceivedCallback);
            AccountProvider.Instance.onRelatedUsersSyncSuccess += onUserRelatedGroupsSyncSuccess;
            AccountProvider.Instance.onRelatedGroupsSyncSuccess += onUserRelatedGroupsSyncSuccess;
            onUserRelatedUsersSyncSuccess();
            onUserRelatedGroupsSyncSuccess();
        }

        private void updateSession(Session session) {
            if (session.imObj is ImSingle)
            {
                var imSingle = (ImSingle)session.imObj;
                if (!string.IsNullOrEmpty(imSingle.userRoles))
                {
                    var indexOfSession = systemSessions.IndexOf(session);
                    if (indexOfSession < 0)
                        return;
                    systemSessions.Remove(session);
                    systemSessions[indexOfSession] = session;
                }
                else
                {
                    var indexOfSession = userSessions.IndexOf(session);
                    if (indexOfSession < 0)
                        return;
                    userSessions.Remove(session);
                    userSessions[indexOfSession] = session;
                }
            }
            else {
                var indexOfSession = groupSessions.IndexOf(session);
                if (indexOfSession < 0)
                    return;
                groupSessions.Remove(session);
                groupSessions[indexOfSession] = session;
            }
        }

        public void onRabbitMessageReceivedCallback(ImMessage imMessage) {
            Session? session = null;
            ImObj? fromImObj = ImMessage.parseFromImObj(imMessage);
            if (fromImObj == null)
                return;
            if (fromImObj is ImSingle)
            {
                if (fromImObj.id == AccountProvider.Instance.UserInfo.Uid)
                {
                    ImObj? toImObj = ImMessage.parseToImObj(imMessage);
                    if (toImObj != null)
                    {
                        session = getSessionByImObj(toImObj);
                        context.Post(new SendOrPostCallback(postImMessage =>
                        {
                            session.conversationState.addMessage(postImMessage as ImMessage);
                            
                            //updateSession(session);

                        }), imMessage);
                        

                    }
                }
                else
                {
                    session = getSessionByImObj(fromImObj);
                    context.Post(new SendOrPostCallback(postImMessage =>
                    {
                        session.conversationState.addMessage(postImMessage as ImMessage);
                        //updateSession(session);

                    }), imMessage);
                    
                }
            }
            else if (fromImObj is ImGroup) {
                session = getSessionByImObj(fromImObj);
                context.Post(new SendOrPostCallback(postImMessage =>
                {
                    session.conversationState.addMessage(postImMessage as ImMessage);
                    //updateSession(session);

                }), imMessage);
                
            }
        }

        private Session getSessionByImObj(ImObj? imObj)
        {
            if (imObj == null)
                throw new ArgumentNullException("无效聊天对象!");
            if (imObj is ImSingle)
            {
                var imSingle = (ImSingle)imObj;
                var userRoles = imSingle.userRoles;
                if (!string.IsNullOrEmpty(userRoles))
                {
                    if (userRoles.Contains("admin"))
                    {
                        foreach (var systemSession in systemSessions)
                        {
                            if (imObj.id == systemSession.imObj.id)
                                return systemSession;
                        }
                        var newSystemSession = new Session()
                        {
                            imObj = imObj,
                            conversationState = new ConversationUiState()
                        };
                        systemSessions.Add(newSystemSession);
                        return newSystemSession;
                    }
                }
                foreach (var userSession in userSessions)
                {
                    if (imObj.id == userSession.imObj.id)
                        return userSession;
                }
                var newUserSession = new Session()
                {
                    imObj = imObj,
                    conversationState = new ConversationUiState()
                };
                userSessions.Add(newUserSession);
                return newUserSession;
            }
            else if (imObj is ImGroup) {
                foreach (var groupSession in groupSessions)
                {
                    if (imObj.id == groupSession.imObj.id)
                        return groupSession;
                }
                var newGroupSession = new Session()
                {
                    imObj = imObj,
                    conversationState = new ConversationUiState()
                };
                groupSessions.Add(newGroupSession);
                return newGroupSession;
            }
            throw new Exception("无效聊天对象!");
        }

        public void onUserRelatedUsersSyncSuccess()
        {
            var savedRelatedUsers = AccountProvider.Instance.RelatedUsers;
            if (savedRelatedUsers == null)
            {
                return;
            }
            savedRelatedUsers.ForEach(userInfo =>
            {
                var session = new Session();
                var imObj = new ImSingle() {
                    uid = userInfo.Uid,
                    userAvatarUrl = userInfo.AvatarUrl,
                    userName = userInfo.Name
                };
                session.imObj = imObj;
                session.conversationState = new ConversationUiState();
                userSessions.Add(session);
            });
        }

        public void onUserRelatedGroupsSyncSuccess()
        {
            var savedRelatedGroups = AccountProvider.Instance.RelatedGroups;
            if (savedRelatedGroups == null)
            {
                return;
            }
            savedRelatedGroups.ForEach(groupInfo =>
            {
                var session = new Session();
                var imObj = new ImGroup()
                {
                    groupId = groupInfo.GroupId,
                    groupAvatarUrl = groupInfo.IconUrl,
                    groupName = groupInfo.Name
                };
                session.imObj = imObj;
                session.conversationState = new ConversationUiState();
                groupSessions.Add(session);
            });
        }

        public void onReceivedMessage() {

        }

        public void onSendMessage() {

        }

        public void onMessage() {

        }

    }


    public class ScreenUseCase {
        public delegate void OnScreensFetched(List<UserScreenInfo> userScreenInfos);
        internal async void loadIndexScreens(OnScreensFetched onScreensFetched)
        {
            try
            {
                BaseResponse<List<UserScreenInfo>> userScreenInfosResponse = await ApiProvider.Instance.userApi.getIndexRecommendScreens(1, 20);
                if (userScreenInfosResponse.Data == null)
                {
                    Debug.WriteLine("获取Screen list失败");
                }
                else
                {

                    Debug.WriteLine("获取Screen list:" + userScreenInfosResponse.Data);
                    userScreenInfosResponse.Data.ForEach(userScreenInfo =>
                    {
                        userScreenInfo.categoryMediaFiles();
                    });
                    onScreensFetched(userScreenInfosResponse.Data);

                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine("");
                throw;
            }
        }
    }


    public class UserDataSyncUseCase{
        public delegate void OnRelatedUsersSyncSuccess();
        public delegate void OnRelatedGroupsSyncSuccess();

        public async void syncUserDataFromServer(string conditions)
        {
            if (!string.IsNullOrEmpty(conditions))
            {
                if (conditions.Contains("friends"))
                {
                    try
                    {
                        Debug.WriteLine($"{nameof(syncUserDataFromServer)}" + " 同步用户Friends数据开始");
                        BaseResponse<List<UserInfo>> friendsResponse = await ApiProvider.Instance.userApi.getFriends();
                        if (friendsResponse.Data == null)
                        {
                            Debug.WriteLine($"{nameof(syncUserDataFromServer)}" + " 同步用户Friends数据,空");
                        }
                        else
                        {
                            AccountProvider.Instance.RelatedUsers = friendsResponse.Data;

                        }
                    }
                    catch (Exception)
                    {

                        Debug.WriteLine($"{nameof(syncUserDataFromServer)}" + " 同步用户Friends数据,异常"); ;
                    }

                }
                if (conditions.Contains("groups"))
                {
                    Debug.WriteLine($"{nameof(syncUserDataFromServer)}" + " 同步用户Groups数据开始");
                    try
                    {
                        BaseResponse<List<GroupInfo>> groupsResponse = await ApiProvider.Instance.userApi.getGroups();
                        if (groupsResponse.Data == null)
                        {
                            Debug.WriteLine($"{nameof(syncUserDataFromServer)}" + " 同步用户Groups数据,空");
                        }
                        else
                        {
                            AccountProvider.Instance.RelatedGroups = groupsResponse.Data;

                        }
                        startImMessageBroker();
                    }
                    catch (Exception)
                    {

                        Debug.WriteLine($"{nameof(syncUserDataFromServer)}" + " 同步用户Groups数据,异常"); ;
                    }

                }

            }
           
        }
        private void startImMessageBroker() {
            var brokerProperty = Constants.RabbitMqBrokerProperty();
            
            RabbitManager.bootStrap(brokerProperty);
        }
    }
    public class UserUseCase
    {

        public async void loginByAccountPassword(string account, string password) {
         
            var encodeAccount = AppSets.utils.Commons.EncryptByMD5(account);
            var encodePassword = AppSets.utils.Commons.EncryptByMD5(password);
            var deviceInfo = new DeviceInfo()
            {
                Platform = "Windows 11",
                ScreenResolution = "1920*1080",
                ScreenSize = "27 in",
                Version = "3.0",
                Vendor = "windows",
                Model = "windows",
                ModelCode = "windows",
                Ip = "0.0.0.0",
            };
            var body = new LoginPasswordRequestModel() { 
                Account = encodeAccount, 
                Password = encodePassword,
                SignInDeviceInfo = deviceInfo,
                SignInLocation = "Si Chuan" 
            };

            try
            {
                BaseResponse<string> loginResponse = await ApiProvider.Instance.userApi.login(body);
                Debug.WriteLine("登录开始");
                if (loginResponse.Data != null)
                {
                    Debug.WriteLine("登录获取Token");
                    AccountProvider.Instance.Token = loginResponse.Data;
                    BaseResponse<UserInfo> userInfoResponse = await ApiProvider.Instance.userApi.getUserInfo();
                    if (userInfoResponse.Data != null)
                    {
                        Debug.WriteLine("登录获取用户信息");
                        AccountProvider.Instance.UserInfo = userInfoResponse.Data;
                        AccountProvider.Instance.persistenceDataAsync();
                        var userDataSyncUseCase = new UserDataSyncUseCase();
                        userDataSyncUseCase.syncUserDataFromServer("friends, groups");

                    }
                    else
                    {
                        Debug.WriteLine("登录失败2");
                    }
                }
                else
                {
                    Debug.WriteLine("登录失败1：" + loginResponse.Info);
                }

            }
            catch (Exception e1)
            {

                Debug.WriteLine("login Exception:\n" + "message:\n" + e1.ToString());
            }
        }

        public async void logout() {
           
            if (!AccountProvider.Instance.isLogged()) {
                return;
            }
            try
            {
               
                BaseResponse<bool?> logoutResponse = await ApiProvider.Instance.userApi.logout();
                if (logoutResponse.Data != true)
                {
                    Debug.WriteLine("请求退出登录时返回False或其它值");
                }
            }
            catch (Exception)
            {
                Debug.WriteLine("请求退出登录时返回False或其它值, 可能是返回数据解析异常");
            }finally {
                AccountProvider.Instance.onLogout();
            }
        }

        
    }
    public class AppSetsUseCase
    {

        public async void getAppToken(Action action) {

            var dictionary = new Dictionary<string, string>();
            dictionary.Add("appSetsAppId", "APPSETS2023071579019880338529");
            try
            {
                Debug.WriteLine("开始获取AppToken");
                BaseResponse<string?> appTokenResponse = await ApiProvider.Instance.appsApi.getAppToken(dictionary);
                if (appTokenResponse.Data == null)
                {
                    Debug.WriteLine("获取AppToken失败");
                }
                else
                {
                    Debug.WriteLine("获取AppToken:" + appTokenResponse.Data);
                    AccountProvider.Instance.AppToken = appTokenResponse.Data;
                    action();
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine("获取AppToken失败, 异常");
            }
        }

        public async void getOrRestoreToken()
        {
            
            
            var restoreToken = AccountProvider.Instance.Token;
            if (!string.IsNullOrEmpty(restoreToken)) {
                Debug.WriteLine("从本地获取到Token成功");
                var userDataSyncUseCase = new UserDataSyncUseCase();
                userDataSyncUseCase.syncUserDataFromServer("friends, groups");
            }
            
        }
        public delegate void OnAppsFetched(List<AppsWithCategory> appsWithCategories);

        public async void loadIndexApplications(OnAppsFetched onAppsFetched)
        {
            
            try
            {
                BaseResponse<List<AppsWithCategory>> appsResponse = await ApiProvider.Instance.appsApi.getIndexApplications();
                if (appsResponse.Data == null)
                {
                    Debug.WriteLine("获取Apps失败");
                }
                else
                {

                    Debug.WriteLine("获取Apps:" + appsResponse.Data);
                    onAppsFetched(appsResponse.Data);


                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine("获取Apps失败, 异常\n" + "message:" + ex.Message + "\nstack:" + ex.StackTrace);
            }

        }

        internal void init()
        {
            
            getOrRestoreToken();
            getAppToken(()=>{
                initSimpleFileIO();
            });
            
        }


        private async void initSimpleFileIO()
        {
            try
            {
                BaseResponse<TencentCosRegionBucket> regionBucketResponse = await ApiProvider.Instance.thirdPartApi.getTencentCosRegionBucket();
                if (regionBucketResponse.Data == null) {
                    Debug.WriteLine("initSimpleFileIO fail regionBucketResponse data isNullOrEmpty");
                    return;
                }
                
                TencentCosInfoProvider cosInfoProvider = new TencentCosInfoProvider();
                var regionBucket = regionBucketResponse.Data;
                regionBucket.decode();
                cosInfoProvider.updateRegionBucket(regionBucket);

                SimpleFileIO.Instance.initThirdComponents(cosInfoProvider);
            }
            catch (Exception ex)
            {

                Debug.WriteLine("initSimpleFileIO 异常"+ex);
            }
            
        }

        internal async void loadSpotLight(Action<SpotLight> onSpotLightLoad)
        {
            try
            {
                BaseResponse<SpotLight> spotLightResponse = await ApiProvider.Instance.appSetsApi.getSpotLight();
                if (spotLightResponse.Data != null)
                {
                    onSpotLightLoad(spotLightResponse.Data);
                }
            }
            catch (Exception ex)
            {

                Debug.WriteLine("加载SpotLight时异常"+ex);
            }
            
        }
    }
}
