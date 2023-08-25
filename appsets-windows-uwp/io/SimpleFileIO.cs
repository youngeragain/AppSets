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
           
            QCloudCredentialProvider cosCredentialProvider = new CustomQCloudCredentialProvider(cosInfoProvider);
            var regionBucket = cosInfoProvider.getTencentCosRegionBucket();
            if (regionBucket == null)
                return;
            if (string.IsNullOrEmpty(regionBucket.BucketName) ||
                string.IsNullOrEmpty(regionBucket.Region) ||
                string.IsNullOrEmpty(regionBucket.FilePathPrefix)) {
                return;
            }
            string region = regionBucket.Region; 
            CosXmlConfig  cosXmlConfig = new CosXmlConfig.Builder()
              .IsHttps(true) 
              .SetRegion(region)
              .SetDebugLog(true) 
              .Build();

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
                preSignatureStruct.appid = appId;
                preSignatureStruct.region = regionBucket.Region; 
                preSignatureStruct.bucket = regionBucket.BucketName; 
                preSignatureStruct.key = cosPath; 
                preSignatureStruct.httpMethod = "GET"; 
                preSignatureStruct.isHttps = true; 
                preSignatureStruct.signDurationSecond = 600;
                preSignatureStruct.headers = null;
                preSignatureStruct.queryParameters = null; 

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
