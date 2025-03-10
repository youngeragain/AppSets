//
//  PurpleHttp.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation
import Alamofire

class HttpEventMonitor: EventMonitor {
    
    private static let TAG = "HttpEventMonitor"
    
    func requestDidFinish(_ request: Request) {
        let url = request.request?.url?.absoluteString ?? "未知 URL"
        let duration = request.metrics?.taskInterval.duration ?? 0
        PurpleLogger.current.d(HttpEventMonitor.TAG, "请求完成：\(url)，耗时：\(duration) 秒")
    }

    func request(_ request: Request, didFailTask task: URLSessionTask, with error: Error) {
        let url = request.request?.url?.absoluteString ?? "未知 URL"
        PurpleLogger.current.d(HttpEventMonitor.TAG, "请求失败：\(url)，错误：\(error)")
    }

    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive data: Foundation.Data) {
        if(data.count>32){
            PurpleLogger.current.d(HttpEventMonitor.TAG, "收到数据：<数据过长，已折叠>")
            return
        }
        if let str = String(data:data, encoding: .utf8){
            PurpleLogger.current.d(HttpEventMonitor.TAG, "收到数据：\(str)")
        }
    }
}


struct DesignHttp {
    public static let TAG = "DesignHttp"
   
    public static let session: Alamofire.Session = setupSession()
    
    public static func setupSession() -> Alamofire.Session {
        let appConfig = AppConfig.Instance.appConfiguration
        let host = if(String.isNullOrEmpty(appConfig.apiUrl)){
            appConfig.apiHost
        }else{
            appConfig.apiUrl
        }
        let manager = ServerTrustManager(evaluators: [host: DisabledTrustEvaluator()])
        let httpEventMonitor = HttpEventMonitor()
        let session = Alamofire.Session(
            configuration: URLSessionConfiguration.default,
            serverTrustManager: manager,
            eventMonitors: [httpEventMonitor]
        )
        
        return session
    }
    
    public static func provideHeaders(_ url:String) -> HTTPHeaders {
        let appConfig = AppConfig.Instance.appConfiguration
        let apptoken = LocalAccountManager.Instance.provideAppToken() ?? ""
        PurpleLogger.current.d(TAG, "provideHeaders, url:\(url), apptoken:\(apptoken)")
        let headers: HTTPHeaders = [
            "appSetsAppId": appConfig.appSetsAppId,
            ApiDesignEncodeStr.platformStrToMd5: "ios",
            ApiDesignEncodeStr.versionStrToMd5: "300",
            ApiDesignEncodeStr.appTokenStrToMd5: apptoken,
            ApiDesignEncodeStr.tokenStrToMd5: LocalAccountManager.Instance.token ?? ""
        ]
        return headers
    }
    
}
