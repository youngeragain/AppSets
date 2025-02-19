using AA;
using AppSets.models;
using AppSets.server;
using COSXML;
using COSXML.Auth;
using COSXML.Common;
using COSXML.Model.Object;
using COSXML.Model.Tag;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AppSets.io
{
    internal class SimpleFileIO
    {

        private CosXmlServer? cosXmlServer = null;
        private ITencentCosInfoProvider? cosInfoProvider = null;
        private SimpleFileIO() { }

        public void initThirdComponents(ITencentCosInfoProvider cosInfoProvider) {
            Debug.WriteLine("SimpleFileIO, initThirdComponents");
            
            if (cosXmlServer != null)
                return;
            this.cosInfoProvider = cosInfoProvider;
            //初始化 CosXmlConfig 
            QCloudCredentialProvider cosCredentialProvider = new CustomQCloudCredentialProvider(cosInfoProvider);
            var regionBucket = cosInfoProvider.getTencentCosRegionBucket();
            if (regionBucket == null)
                return;
            if (string.IsNullOrEmpty(regionBucket.BucketName) ||
                string.IsNullOrEmpty(regionBucket.Region) ||
                string.IsNullOrEmpty(regionBucket.FilePathPrefix)) {
                return;
            }
            string region = regionBucket.Region; //设置一个默认的存储桶地域
            CosXmlConfig  cosXmlConfig = new CosXmlConfig.Builder()
              .IsHttps(true)  //设置默认 HTTPS 请求
              .SetRegion(region)  //设置一个默认的存储桶地域
              .SetDebugLog(true)  //显示日志
              .Build();  //创建 CosXmlConfig 对象

            cosXmlServer = new CosXmlServer(cosXmlConfig, cosCredentialProvider);
        }


        public string? generatePreSign(string? contentUrlMarker) { 
            if(string.IsNullOrEmpty(contentUrlMarker))
                return null;
            if(cosInfoProvider==null)
                return null;
            if(cosXmlServer==null)
                return null;
            try
            {

                var regionBucket = cosInfoProvider.getTencentCosRegionBucket();
                if(string.IsNullOrEmpty(regionBucket.BucketName)||
                    string.IsNullOrEmpty(regionBucket.Region)||
                    string.IsNullOrEmpty(regionBucket.FilePathPrefix))
                    return null;
                var lastIndexOfMiddleHorizontalLine = regionBucket.BucketName.LastIndexOf('-');
                var appId = regionBucket.BucketName.Substring(lastIndexOfMiddleHorizontalLine + 1);
                var cosPath = regionBucket.FilePathPrefix + contentUrlMarker;
                PreSignatureStruct preSignatureStruct = new PreSignatureStruct();
                preSignatureStruct.appid = appId;//腾讯云账号 APPID
                preSignatureStruct.region = regionBucket.Region; //存储桶地域
                preSignatureStruct.bucket = regionBucket.BucketName; //存储桶
                preSignatureStruct.key = cosPath; //对象键
                preSignatureStruct.httpMethod = "GET"; //HTTP 请求方法
                preSignatureStruct.isHttps = true; //生成 HTTPS 请求 URL
                preSignatureStruct.signDurationSecond = 600; //请求签名时间为600s
                preSignatureStruct.headers = null;//签名中需要校验的 header
                preSignatureStruct.queryParameters = null; //签名中需要校验的 URL 中请求参数

                preSignatureStruct.signHost = false;

                string requestSignURL = cosXmlServer.GenerateSignURL(preSignatureStruct);
                return requestSignURL;

            }
            catch (COSXML.CosException.CosClientException clientEx)
            {
                //请求失败
                Console.WriteLine("CosClientException: " + clientEx);

            }
            catch (COSXML.CosException.CosServerException serverEx)
            {
                //请求失败
                Console.WriteLine("CosServerException: " + serverEx.GetInfo());
            }
            return null;
        }


        private static readonly Lazy<SimpleFileIO> InstanceLock = new Lazy<SimpleFileIO>(() => new SimpleFileIO());

        public static SimpleFileIO Instance
        {
            get { return InstanceLock.Value; }

        }
    }
    public class CustomQCloudCredentialProvider : DefaultSessionQCloudCredentialProvider
    {
        private ITencentCosInfoProvider CosInfoProvider { get; set; }
        // 这里假设开始没有密钥，也可以用初始的临时密钥来初始化
        public CustomQCloudCredentialProvider(ITencentCosInfoProvider cosInfoProvider) : base(null, null, 0L, null)
        {
            this.CosInfoProvider = cosInfoProvider;
        }

        public override void Refresh()
        {
            var cosSTS = CosInfoProvider.getTencentCosSTS();

            string tmpSecretId = cosSTS.TmpSecretId; 
            string tmpSecretKey = cosSTS.TmpSecretKey; 
            string tmpToken = cosSTS.SessionToken; 
            long tmpStartTime = (cosSTS.ServerTimeMills / 1000);
            long tmpExpiredTime = tmpStartTime + ((long)cosSTS.Duration);

            SetQCloudCredential(tmpSecretId, tmpSecretKey,
              String.Format("{0};{1}", tmpStartTime, tmpExpiredTime), tmpToken);
        }
    }

    public interface ITencentCosInfoProvider {

        public TencentCosSTS getTencentCosSTS();
        public TencentCosRegionBucket getTencentCosRegionBucket();
    }

    public class TencentCosInfoProvider: ITencentCosInfoProvider
    {

        private TencentCosSTS? mTencentCosSTS = null;
        private TencentCosRegionBucket? mTencentCosRegionBucket = null;


        public void updateSts(TencentCosSTS sts) {
            mTencentCosSTS = sts;
        }

        public void updateRegionBucket(TencentCosRegionBucket? regionBucket) {
            mTencentCosRegionBucket = regionBucket;
        }

        private TencentCosSTS? requestSts() {
            Task<BaseResponse<TencentCosSTS?>> stsResponseTask =  ApiProvider.Instance.thirdPartApi.getTencentCosSTS();
            var stsResponse = stsResponseTask.Result;    
            return stsResponse.Data;
        }

        public TencentCosSTS getTencentCosSTS()
        {
            var tempSts = mTencentCosSTS;
            if (tempSts == null) {
                var newSts = requestSts();
                updateSts(newSts);
                if (newSts == null)
                    throw new Exception("request sts exception!");
                return newSts;
            }
            if (tempSts.isOutOfDate()) {
                var newSts = requestSts();
                updateSts(newSts);
                if (newSts == null)
                    throw new Exception("request sts exception!");
                return newSts;
            }
            return tempSts;
        }

        public TencentCosRegionBucket getTencentCosRegionBucket()
        {
            if (mTencentCosRegionBucket == null) {
                throw new Exception(" TencentCosRegionBucket not provide");
            }
            return mTencentCosRegionBucket;
        }
    }




}
