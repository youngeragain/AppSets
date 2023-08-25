using AA;
using AppSets.models;
using AppSets.utils;
using Microsoft.Extensions.DependencyInjection;
using Refit;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

#nullable enable
namespace AppSets.server
{

    interface ThirdPartApi {

        [Get("/appsets/thirdpart/tencent/cos/sts")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<TencentCosSTS?>> getTencentCosSTS();


        [Get("/appsets/thirdpart/tencent/cos/regionbucket")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<TencentCosRegionBucket?>> getTencentCosRegionBucket();

    }


    interface UserApi
    {
        [Post("/user/login")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<string>> login([Body(BodySerializationMethod.Serialized)] LoginPasswordRequestModel body);


        [Get("/user/signout")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<bool?>> logout();



        [Get("/user/info/get")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<UserInfo>> getUserInfo();


        [Get("/user/friends")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<List<UserInfo>>> getFriends();


        [Get("/user/chatgroups")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<List<GroupInfo>>> getGroups();



        [Get("/user/screens/index/recommend")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<List<UserScreenInfo>>> getIndexRecommendScreens([AliasAs("page")] int page, [AliasAs("size")] int pageSize);


    }

    interface AppsApi {

        [Post("/appsets/apps/index/recommend")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<List<AppsWithCategory>>> getIndexApplications();


        [Post("/appsets/apptoken/get")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<string?>> getAppToken([Body(BodySerializationMethod.Serialized)] Dictionary<string, string> body);
    }

    interface AppSetsApi {

        [Get("/appsets/spotlight")]
        [Headers("Content-Type: application/json")]
        Task<BaseResponse<SpotLight>> getSpotLight();

        [Get("/appsets/spotlight")]
        [Headers("Content-Type: application/json")]
        Task<ApiResponse<string>> getSpotLight2();

    }


    class ApiProvider
    {
        private ServiceProvider serviceProvider;

        private ApiProvider() {
            IServiceCollection services = new ServiceCollection();
            services.AddTransient<RequestHeaderHandler>();
            Refit.HttpClientFactoryExtensions.AddRefitClient<ThirdPartApi>(services)
                    .ConfigureHttpClient(c => c.BaseAddress = new Uri(Constants.BASE_URL))
                    .ConfigurePrimaryHttpMessageHandler(() => new HttpClientHandler
                    {
                        ServerCertificateCustomValidationCallback = CheckValidationCallback
                    }).AddHttpMessageHandler<RequestHeaderHandler>(); 

            Refit.HttpClientFactoryExtensions.AddRefitClient<UserApi>(services)
                .ConfigureHttpClient(c => c.BaseAddress = new Uri(Constants.BASE_URL))
                .ConfigurePrimaryHttpMessageHandler(() => new HttpClientHandler
                {
                    ServerCertificateCustomValidationCallback = CheckValidationCallback
                }).AddHttpMessageHandler<RequestHeaderHandler>();
            Refit.HttpClientFactoryExtensions.AddRefitClient<AppsApi>(services)
                .ConfigureHttpClient(c => c.BaseAddress = new Uri(Constants.BASE_URL))
                .ConfigurePrimaryHttpMessageHandler(() => new HttpClientHandler
                {
                    ServerCertificateCustomValidationCallback = CheckValidationCallback
                }).AddHttpMessageHandler<RequestHeaderHandler>();
            Refit.HttpClientFactoryExtensions.AddRefitClient<AppSetsApi>(services)
                .ConfigureHttpClient(c => c.BaseAddress = new Uri(Constants.BASE_URL))
                .ConfigurePrimaryHttpMessageHandler(() => new HttpClientHandler
                {
                    ServerCertificateCustomValidationCallback = CheckValidationCallback
                }).AddHttpMessageHandler<RequestHeaderHandler>();
            serviceProvider = services.AddHttpClient().BuildServiceProvider();

        }

        public static bool CheckValidationCallback(object sender, X509Certificate? certificate, X509Chain? chain, SslPolicyErrors sslPolicyErrors)
        {
            return true;
        }

        public ThirdPartApi thirdPartApi { 
            get {
                return serviceProvider.GetRequiredService<ThirdPartApi>();
            }
            private set { } 
        }


        public UserApi userApi {
            get { 
                return serviceProvider.GetRequiredService<UserApi>();
            }
            private set { }
        }

        public AppsApi appsApi {
            get { 
                return serviceProvider.GetRequiredService<AppsApi>();
            }
            private set { }
        }

        public AppSetsApi appSetsApi
        {
            get
            {
                return serviceProvider.GetRequiredService<AppSetsApi>();
            }
            private set { }
        }

        private static readonly Lazy<ApiProvider> InstanceLock = new Lazy<ApiProvider>(() => {
            return new ApiProvider();
        });


        
       
        public static ApiProvider Instance
        {
            get { return InstanceLock.Value; }
        }
    }

    class RequestHeaderHandler : DelegatingHandler
    {
        

        public RequestHeaderHandler()
        {
           // InnerHandler = new HttpClientHandler();
        }

        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            
            string? token = AccountProvider.Instance.Token;
            if (!string.IsNullOrEmpty(token))
            {
                request.Headers.Add("fecbb6e8b4699053", token);

            }
            string? appToken = 
                AccountProvider.Instance.AppToken;
            if (!string.IsNullOrEmpty(appToken)) {
                request.Headers.Add("b3c6f3a2140f316c", appToken);
            }

            request.Headers.Add("4ade17ef4e51612c", "windows");
            request.Headers.Add("0c356273d46284f6", "200");
            var responseTask = await base.SendAsync(request, cancellationToken).ConfigureAwait(false);
           
            return responseTask;
        }
    }
}
