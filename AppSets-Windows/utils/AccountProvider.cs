using AA.rabbit;
using AppSets.models;
using AppSets.utils;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ZXing.Aztec.Internal;
using static AppSets.usecase.UserDataSyncUseCase;
using static AppSets.usecase.UserUseCase;

namespace AA
{
		internal class AccountProvider:BindableBase
		{

			public delegate void OnUserLoginEvent(bool logged, string? fromType);
			public delegate void OnAppTokenGottenEvent();
			public event OnUserLoginEvent? mOnUserLoginEvent;
			public event OnAppTokenGottenEvent? mOnAppTokenGottenEvent;


            private string? token;
			private string? appToken;
			private UserInfo? userInfo;
			private List<UserInfo>? relateUsers = null;
			private List<GroupInfo>? relateGroups = null;

			private const string userLoginDataJsonFileName = "userLoginData.json";
			private const string LocalSettingsKey_userLoginData = "LocalSettingsKey_userLoginData";
			private Windows.Storage.StorageFolder storageFolder = Windows.Storage.ApplicationData.Current.LocalFolder;
			private Windows.Storage.ApplicationDataContainer localSettings = Windows.Storage.ApplicationData.Current.LocalSettings;


			internal bool isLogged() {
				return !string.IsNullOrEmpty(token);
			}
			internal void onLogout()
			{
			    RabbitManager.close();
				token = null;
				userInfo = null;
				relateUsers = null;
				relateGroups = null;
				localSettings.Values.Clear();
				
				if (mOnUserLoginEvent != null) {
					mOnUserLoginEvent(false, null);
                }
			}
			private AccountProvider() {
					readDataFromFile();
			}

			public  void persistenceDataAsync(){
				if (mOnUserLoginEvent != null)
				{
					mOnUserLoginEvent(true, "from_server");
				}
				var task = Task.Run(() =>
					{
						try
						{
							var loginData = new LoginData()
							{
								Token = token,
								UserInfo = userInfo
							};
							var jsonDataStr = JsonConvert.SerializeObject(loginData);
                    
							localSettings.Values[LocalSettingsKey_userLoginData] = jsonDataStr;
							//Windows.Storage.StorageFile userLoginDataJsonFile = await storageFolder.CreateFileAsync("userLoginData.json", Windows.Storage.CreationCollisionOption.ReplaceExisting);
							//await Windows.Storage.FileIO.WriteTextAsync(userLoginDataJsonFile, jsonDataStr);
							Debug.WriteLine("持久化用户登录数据成功");
                           
                        }
						catch
						{
							Debug.WriteLine("持久化用户登录数据错误");
						}
					}); 
			}

			public void readDataFromFile() {
				try {


					//Windows.Storage.StorageFile jsonFile = await storageFolder.GetFileAsync(userLoginDataJsonFileName);

					//if (jsonFile != null && jsonFile.IsAvailable)
					//{
					//    string jsonInfo = await Windows.Storage.FileIO.ReadTextAsync(jsonFile);
					//    if (!string.IsNullOrEmpty(jsonInfo)) {
					//        var loginUserData = JsonConvert.DeserializeObject<LoginData>(jsonInfo);
					//        if (loginUserData != null) {
					//            this.loginUserData = loginUserData;
					//        }
					//    }
					//}
					var  str = localSettings.Values[LocalSettingsKey_userLoginData] as string;
					if (!string.IsNullOrEmpty(str)) {
						var restoredLoginData = JsonConvert.DeserializeObject<LoginData>(str);
						if (restoredLoginData != null) {
							token = restoredLoginData.Token;
							userInfo = restoredLoginData.UserInfo;
							Debug.WriteLine("读取的userData:"+str);
							if (mOnUserLoginEvent != null)
							{
								mOnUserLoginEvent(true, "from_local");
							}
                    }
					}


				}catch {
					Debug.WriteLine("获取持久化用户数据错误");
				}
		   
			}

       

			private static readonly Lazy<AccountProvider> InstanceLock = new Lazy<AccountProvider>(() => new AccountProvider());

			public static AccountProvider Instance {
					get { return InstanceLock.Value; }
		
			}

			public event OnRelatedUsersSyncSuccess? onRelatedUsersSyncSuccess;
			public event OnRelatedGroupsSyncSuccess? onRelatedGroupsSyncSuccess;

			public string? AppToken { 
				get {
					return appToken;
				} 
				internal set { 
					appToken = value;
					if (mOnAppTokenGottenEvent != null) {
						mOnAppTokenGottenEvent();
					}
				} 
			}
			public string? Token
			{
				get
				{
					return token;
				}
				set
				{
					token = value;
				}
			}

			public UserInfo? UserInfo
			{
				get
				{
					return userInfo;
				}
				set
				{
					SetProperty(ref userInfo, value);
				}
			}

        public List<UserInfo> RelatedUsers {
				get { 
					return relateUsers;
				}
				internal set { 
					relateUsers = value;
					if (onRelatedUsersSyncSuccess != null)
					{
						onRelatedUsersSyncSuccess();
					}
				} }

			public List<GroupInfo> RelatedGroups { get{
					return relateGroups;
				} internal set { 
					relateGroups = value;
					if (onRelatedGroupsSyncSuccess != null) {
						onRelatedGroupsSyncSuccess();
					}
				} }
    }
}
