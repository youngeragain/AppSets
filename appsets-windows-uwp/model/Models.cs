using System;
using System.Buffers.Text;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using ABI.Microsoft.UI.Xaml;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Newtonsoft.Json;
using Windows.Foundation.Metadata;

#nullable enable
namespace AppSets.models
{
    [Serializable]
    public class DeviceInfo
    {
        [JsonProperty("platform")]
        public string Platform { get; set; }

        [JsonProperty("screenResolution")]
        public string ScreenResolution { get; set; }

        [JsonProperty("screenSize")]
        public string ScreenSize { get; set; }

        [JsonProperty("version")]
        public string Version { get; set; }

        [JsonProperty("vendor")]
        public string Vendor { get; set; }

        [JsonProperty("model")]
        public string Model { get; set; }

        [JsonProperty("modelCode")]
        public string ModelCode { get; set; }

        [JsonProperty("ip")]
        public string Ip { get; set; }
    }

    [Serializable]
    public class LoginPasswordRequestModel
    {
        [JsonProperty("account")]
        public string Account { get; set; }

        [JsonProperty("password")]
        public string Password { get; set; }

        [JsonProperty("signInDeviceInfo")]
        public DeviceInfo SignInDeviceInfo { get; set; }

        [JsonProperty("signInLocation")]
        public string SignInLocation { get; set; }

    }

    [Serializable]
    public class BaseResponse<T>
    {
        
        [JsonProperty("info")]
        public string? Info { get; set; }

        [JsonProperty("data")]
        public T? Data { get; set; }

        [JsonProperty("code")]
        public int Code { get; set; }
    }


    [Serializable]
    public class LoginData
    {
      
        [JsonProperty("token")]
        public string? Token { get; set; }

        [JsonProperty("userInfo")]
        public UserInfo? UserInfo { get; set; }

    }

    [Serializable]
    public class UserInfo
    {

        [JsonProperty("sinnInLocation")]
        public String SinnInLocation { get; set; }


        [JsonProperty("signInIp")]
        public String SignInIp { get; set; }

        [JsonProperty("agreeToTheAgreement")]
        public int? AgreeToTheAgreement { get; set; }

        [JsonProperty("uid")]
        public string Uid { get; set; }

        [JsonProperty("name")]
        public string Name { get; set; }

        [JsonProperty("age")]
        public string Age { get; set; }

        [JsonProperty("sex")]
        public string Sex { get; set; }

        [JsonProperty("email")]
        public string Email { get; set; }

        [JsonProperty("phone")]
        public string Phone { get; set; }

        [JsonProperty("address")]
        public string Address { get; set; }

        [JsonProperty("avatarUrl")]
        public string AvatarUrl { get; set; }

        [JsonProperty("introduction")]
        public string Introduction { get; set; }


        [JsonProperty("company")]
        public string Company { get; set; }

        [JsonProperty("profession")]
        public string Profession { get; set; }

        [JsonProperty("website")]
        public string Website { get; set; }

        [JsonProperty("roles")]
        public string Roles { get; set; }

        public static UserInfo empty() { 
            return new UserInfo() {
                Uid = "0",
                Name = "登录",
                AvatarUrl = "ms-appx:///Assets/face_FILL0_wght400_GRAD0_opsz48.svg"
            };
        }

    }

    [Serializable]
    public class AppsWithCategory {

        [JsonProperty("categoryName")]
        public string? CategoryName { get; set; }

        [JsonProperty("categoryNameZh")]
        public string? CategoryNameZh { get; set; }

        [JsonProperty("applications")]
        public List<Application>? Applications { get; set; }
    }


    [Serializable]
    public class Application {


        [JsonProperty("iconUrl")]
        public string? IconUrl { get; set; }

        [JsonProperty("website")]
        public string? Website { get; set; }

        [JsonProperty("updateTime")]
        public string? UpdateTime { get; set; }

        [JsonProperty("createTime")]
        public string? CreateTime { get; set; }

        [JsonProperty("developerInfo")]
        public string? DeveloperInfo { get; set; }

        [JsonProperty("bannerUrl")]
        public string? BannerUrl { get; set; }

        [JsonProperty("createUid")]
        public string? CreateUid { get; set; }

        [JsonProperty("updateUid")]
        public string? UpdateUid { get; set; }

        [JsonProperty("name")]
        public string? Name { get; set; }

        [JsonProperty("category")]
        public string? Category { get; set; }

        [JsonProperty("appId")]
        public string? AppId { get; set; }

        [JsonProperty("platforms")]
        public List<PlatForm>? Platforms { get; set; }

    }


    [Serializable]
    public class PlatForm
    {
        [JsonProperty("id")]
        public string? Id { get; set; }

        [JsonProperty("name")]
        public string? Name { get; set; }

        [JsonProperty("packageName")]
        public string? PackageName { get; set; }

        [JsonProperty("introduction")]
        public string? Introduction { get; set; }

        [JsonProperty("versionInfos")]
        public List<VersionInfo>? VersionInfos { get; set; }
    }

    [Serializable]
    public class VersionInfo
    {
        [JsonProperty("id")]
        public string? Id { get; set; }

        [JsonProperty("versionIconUrl")]
        public string? VersionIconUrl { get; set; }

        [JsonProperty("versionBannerUrl")]
        public string? VersionBannerUrl { get; set; }

        [JsonProperty("version")]
        public string? Version { get; set; }

        [JsonProperty("versionCode")]
        public string? VersionCode { get; set; }

        [JsonProperty("changes")]
        public string? Changes { get; set; }

        [JsonProperty("packageSize")]
        public string? PackageSize { get; set; }

        [JsonProperty("privacyUrl")]
        public string? PrivacyUrl { get; set; }

        [JsonProperty("screenshotInfos")]
        public List<ScreenshotInfo>? ScreenshotInfos { get; set; }

        [JsonProperty("downloadInfos")]
        public List<DownloadInfo>? DownloadInfos { get; set; }

    }

    [Serializable]
    public class ScreenshotInfo {
        [JsonProperty("id")]
        public string? Id { get; set; }

        [JsonProperty("createUid")]
        public string? CreateUid { get; set; }

        [JsonProperty("updateUid")]
        public string? UpdateUid { get; set; }

        [JsonProperty("createTime")]
        public string? CreateTime { get; set; }

        [JsonProperty("updateTime")]
        public string? UpdateTime { get; set; }

        [JsonProperty("type")]
        public string? Type { get; set; }

        [JsonProperty("contentType")]
        public string? ContentType { get; set; }


        [JsonProperty("url")]
        public string? Url { get; set; }
    }

    [Serializable]
    public class DownloadInfo
    {
        [JsonProperty("id")]
        public string? Id { get; set; }

        [JsonProperty("createUid")]
        public string? CreateUid { get; set; }

        [JsonProperty("updateUid")]
        public string? UpdateUid { get; set; }

        [JsonProperty("createTime")]
        public string? CreateTime { get; set; }

        [JsonProperty("updateTime")]
        public string? UpdateTime { get; set; }

        [JsonProperty("downloadTimes")]
        public int? DownloadTimes { get; set; }

        [JsonProperty("url")]
        public string? Url { get; set; }
    }


    [Serializable]
    public class UserScreenInfo {
        [JsonProperty("associateTopics")]
        public string? AssociateTopics { get; set; }

        [JsonProperty("associateUsers")]
        public string? AssociateUsers { get; set; }

        [JsonProperty("dislikeTimes")]
        public int? DislikeTimes { get; set; }

        [JsonProperty("editTime")]
        public string? EditTime { get; set; }

        [JsonProperty("editTimes")]
        public int? EditTimes { get; set; }

        [JsonProperty("likeTimes")]
        public int? LikeTimes { get; set; }

        [JsonProperty("mediaFileUrls")]
        public List<ScreenMediaFileUrl>? MediaFileUrls { get; set; }

        
        public List<ScreenMediaFileUrl>? PicMediaFileUrls { get; set; }

        public List<ScreenMediaFileUrl>? VideoMediaFileUrls { get; set; }

        [JsonProperty("postTime")]
        public string? PostTime { get; set; }

        [JsonProperty("screenContent")]
        public string? ScreenContent { get; set; }

        [JsonProperty("screenId")]
        public string? ScreenId { get; set; }

        [JsonProperty("isPublic")]
        public int? IsPublic { get; set; }

        [JsonProperty("systemReviewResultv")]
        public int? SystemReviewResult { get; set; }

        [JsonProperty("uid")]
        public string? Uid { get; set; }

        [JsonProperty("userInfo")]
        public UserInfo? UserInfo { get; set; }

        public Visibility TopicsViewVisibility() {
            if (string.IsNullOrEmpty(AssociateTopics))
            {
                return Visibility.Collapsed;
            }
            else { 
                return Visibility.Visible;
            }
        }

        public Visibility UsersViewVisibility()
        {
            if (string.IsNullOrEmpty(AssociateTopics))
            {
                return Visibility.Collapsed;
            }
            else
            {
                return Visibility.Visible;
            }
        }

        public Visibility PicturesVisibility() {
            if (PicMediaFileUrls==null|| PicMediaFileUrls.Count == 0)
            {
                return Visibility.Collapsed;
            }
            else
            {
                return Visibility.Visible;

            }
        }
        public Visibility VideoViewVisibility()
        {
            if (VideoMediaFileUrls == null || VideoMediaFileUrls.Count == 0)
            {
                return Visibility.Collapsed;
            }
            else
            {
                return Visibility.Visible;

            }
        }

        internal void categoryMediaFiles()
        {
            if (MediaFileUrls==null||MediaFileUrls.Count == 0) {
                return;
            }
            MediaFileUrls.ForEach(url =>
            {
                if (url.mediaType!=null&&url.mediaType.Contains("image/*")) {
                    if (PicMediaFileUrls == null) { 
                        PicMediaFileUrls = new List<ScreenMediaFileUrl>();
                    }
                    PicMediaFileUrls.Add(url);
                }
                if (url.mediaType != null && url.mediaType.Contains("video/*"))
                {
                    if (VideoMediaFileUrls == null)
                    {
                        VideoMediaFileUrls = new List<ScreenMediaFileUrl>();
                    }
                    VideoMediaFileUrls.Add(url);
                }
            });
        }
    }

    [Serializable]
    public class ScreenMediaFileUrl {
        [JsonProperty("mediaFileUrl")]
        public string? mediaFileUrl { get; set; }

        [JsonProperty("mediaFileCompanionUrl")]
        public string? mediaFileCompanionUrl { get; set; }


        [JsonProperty("mediaType")]
        public string? mediaType { get; set; }


        [JsonProperty("mediaDescription")]
        public string? mediaDescription { get; set; }


        [JsonProperty("x18Content")]
        public int? x18Content { get; set; }
    }

    [Serializable]
    public class GroupInfo {
        [JsonProperty("name")]
        public string? Name { get; set; }

        [JsonProperty("groupId")]
        public string? GroupId { get; set; }

        [JsonProperty("currentOwnerUid")]
        public string? CurrentOwnerUid { get; set; }

        [JsonProperty("type")]
        public int? Type { get; set; }

        [JsonProperty("iconUrl")]
        public string? IconUrl { get; set; }

        [JsonProperty("introduction")]
        public string? Introduction { get; set; }

        [JsonProperty("public")]
        public int? Public { get; set; }

        [JsonProperty("maxMembers")]
        public int? MaxMembers { get; set; }


        [JsonProperty("userInfoList")]
        public List<UserInfo>? UserInfoList { get; set; }
    }

    [Serializable]
    public class TencentCosSTS {
        [JsonProperty("tmpSecretId")]
        public string TmpSecretId { get; set; }

        [JsonProperty("tmpSecretKey")]
        public string TmpSecretKey { get; set; }

        [JsonProperty("sessionToken")]
        public string SessionToken { get; set; }


        [JsonProperty("duration")]
        public int Duration { get; set; }


        [JsonProperty("serverTimeMills")]
        public long ServerTimeMills { get; set; }

        internal bool isOutOfDate()
        {

            return (ServerTimeMills / 1000 + Duration) < (DateTime.Now.Ticks / 1000);
        }
    }

    [Serializable]
    public class TencentCosRegionBucket {
        [JsonProperty("region")]
        public string Region { get; set; }

        [JsonProperty("bucketName")]
        public string BucketName { get; set; }

        [JsonProperty("filePathPrefix")]
        public string FilePathPrefix { get; set; }

        public void decode() {
            try
            {
                var regionBytes = Convert.FromBase64String(Region);
                Region = System.Text.Encoding.UTF8.GetString(regionBytes);

                var bucketNameBytes = Convert.FromBase64String(BucketName);
                BucketName = System.Text.Encoding.UTF8.GetString(bucketNameBytes);

                var filePathPrefixBytes = Convert.FromBase64String(FilePathPrefix);
                FilePathPrefix = System.Text.Encoding.UTF8.GetString(filePathPrefixBytes);
            }
            catch (Exception)
            {

                Debug.WriteLine("COS region bucket 解码失败!");
            }
            
        }

    }




    [Serializable]
    public class SpotLight {

        [JsonProperty("holiday")]
        public Holiday? Holiday { get; set; }

        [JsonProperty("popularSearches")]
        public PopularSearches? PopularSearches   { get; set; }

        [JsonProperty("todayInHistory")]
        public TodayInHistory? TodayInHistory { get; set; }

        [JsonProperty("wordOfTheDay")]
        public WordOfTheDay? WordOfTheDay { get; set; }

        [JsonProperty("baiduHotData")]
        public BaiduHotData? BaiduHotData { get; set; }

        [JsonProperty("bingWallpaperJson")]
        public MicrosoftBingWallpaperJson? BingWallpaperJson { get; set; }
    }

    [Serializable]
    public class Holiday
    {
        [JsonProperty("infoUrl")]
        public string? InfoUrl { get; set; }

        [JsonProperty("moreUrl")]
        public string? MoreUrl { get; set; }

        [JsonProperty("name")]
        public string? Name { get; set; }

        [JsonProperty("picUrl")]
        public string? PicUrl { get; set; }
    }

    [Serializable]
    public class PopularSearches
    {
        [JsonProperty("keywords")]
        public List<string>? Keywords { get; set; }

        [JsonProperty("url")]
        public string? Url { get; set; }

    }


    [Serializable]
    public class TodayInHistory
    {
        [JsonProperty("date")]
        public string? Date { get; set; }

        [JsonProperty("event")]
        public string? Event { get; set; }

        [JsonProperty("infoUrl")]
        public string? InfoUrl { get; set; }

        [JsonProperty("picUrl")]
        public string? PicUrl { get; set; }

        [JsonProperty("title")]
        public string? Title { get; set; }

    }

    [Serializable]
    public class WordOfTheDay
    {
        [JsonProperty("author")]
        public string? Author { get; set; }

        [JsonProperty("authorInfo")]
        public string? AuthorInfo { get; set; }

        [JsonProperty("infoUrl")]
        public string? InfoUrl { get; set; }

        [JsonProperty("word")]
        public string? Word { get; set; }

        [JsonProperty("picUrl")]
        public string? PicUrl { get; set; }

    }

    [Serializable]
    public class BaiduHotData {

        [JsonProperty("hotsearch")]
        public List<Hotsearch>? Hotsearch { get; set; }
    }


    [Serializable]
    public class Hotsearch
    {
        [JsonProperty("cardTitle")]
        public string? CardTitle { get; set; }

        [JsonProperty("heatScore")]
        public string? HeatScore { get; set; }

        [JsonProperty("hotTags")]
        public string? HotTags { get; set; }

        [JsonProperty("index")]
        public string? Index { get; set; }

        [JsonProperty("isNew")]
        public string? IsNew { get; set; }

        [JsonProperty("isViewed")]
        public string? IsViewed { get; set; }

        [JsonProperty("linkurl")]
        public string? Linkurl { get; set; }

        [JsonProperty("preTag")]
        public string? PreTag { get; set; }

        [JsonProperty("views")]
        public string? Views { get; set; }

    }


    [Serializable]
    public class MicrosoftBingWallpaperJson
    {
        [JsonProperty("images")]
        public List<Image>? Images { get; set; }

        [JsonProperty("tooltips")]
        public Tooltips? Tooltips { get; set; }

    }


    [Serializable]
    public class Tooltips
    {
        [JsonProperty("loading")]
        public string? Loading { get; set; }

        [JsonProperty("next")]
        public string? Next { get; set; }

        [JsonProperty("previous")]
        public string? Previous { get; set; }

        [JsonProperty("walle")]
        public string? Walle { get; set; }

        [JsonProperty("walls")]
        public string? Walls { get; set; }

    }




    [Serializable]
    public class Image
    {
        [JsonProperty("bot")]
        public int? Bot { get; set; }

        [JsonProperty("copyright")]
        public string? Copyright { get; set; }

        [JsonProperty("copyrightlink")]
        public string? Copyrightlink { get; set; }

        [JsonProperty("drk")]
        public int? Drk { get; set; }

        [JsonProperty("enddate")]
        public string? EndDate { get; set; }

        [JsonProperty("fullstartdate")]
        public string? FullStartDate { get; set; }

        [JsonProperty("hs")]
        public List<object>? Hs { get; set; }

        [JsonProperty("hsh")]
        public string? Hsh { get; set; }

        [JsonProperty("quiz")]
        public string? Quiz { get; set; }

        [JsonProperty("startdate")]
        public string? StartDate { get; set; }


        [JsonProperty("title")]
        public string? Title { get; set; }


        [JsonProperty("top")]
        public int? Top { get; set; }


        [JsonProperty("url")]
        public string? Url { get; set; }

        [JsonProperty("urlbase")]
        public string? UrlBase { get; set; }

        [JsonProperty("wp")]
        public bool? Wp { get; set; }

    }
}
