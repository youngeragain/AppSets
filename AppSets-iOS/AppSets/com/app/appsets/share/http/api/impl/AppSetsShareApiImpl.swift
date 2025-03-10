//
//  AppSetsShareApiImpl.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/10.
//
import Alamofire

class AppSetsShareApiImpl: BaseApiImpl, AppSetsShareApi {
    private static let TAG = "AppSetsShareApiImpl"

    override init(_ defaultReponseProvider: any DefaultResponseProvider, _ baseUrlProvider: any BaseUrlProvider) {
        super.init(defaultReponseProvider, baseUrlProvider)
    }

    func greeting() async -> DesignResponse<String> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "greeting")
        let appConfig = AppConfig.Instance.appConfiguration
        return await getResponse(
            api: ShareApiSuffix.API_GREETING,
            method: .get
        )
    }

    func ping() async -> DesignResponse<String> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "ping")
        let appConfig = AppConfig.Instance.appConfiguration
        return await getResponse(
            api: ShareApiSuffix.API_PING,
            method: .get
        )
    }

    func isNeedPin() async -> DesignResponse<Bool> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "isNeedPin")
        let appConfig = AppConfig.Instance.appConfiguration
        let headers: HTTPHeaders = [:]
        return await getResponse(
            api: ShareApiSuffix.API_IS_NEED_PIN,
            method: .get,
            headers: headers
        )
    }

    func pair(pin: String) async -> DesignResponse<Bool> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "pair")
        let appConfig = AppConfig.Instance.appConfiguration
        let headers: HTTPHeaders = ["pin": pin]
        return await getResponse(
            api: ShareApiSuffix.API_PAIR,
            method: .post,
            headers: headers
        )
    }

    func pairResponse(shareToken: String) async -> DesignResponse<Bool> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "pairResponse")
        let appConfig = AppConfig.Instance.appConfiguration
        let headers: HTTPHeaders = ["share_token": shareToken]
        return await getResponse(
            api: ShareApiSuffix.API_PAIR,
            method: .post,
            headers: headers
        )
    }

    func postText(shareToken: String, text: String) async -> DesignResponse<Bool> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "postText")
        let appConfig = AppConfig.Instance.appConfiguration
        let headers: HTTPHeaders = ["share_token": shareToken]
        return await getResponse(
            api: ShareApiSuffix.API_POST_TEXT,
            method: .post
        )
    }

    func postFile(shareToken: String, url: URL) async -> DesignResponse<Bool> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "postFile")
        let appConfig = AppConfig.Instance.appConfiguration
        let headers: HTTPHeaders = ["share_token": shareToken]
        return await getResponse(
            api: ShareApiSuffix.API_POST_FILE,
            method: .post
        )
    }

    func prepareSend(shareToken: String, uri: String) async -> DesignResponse<Bool> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "prepareSend")
        let appConfig = AppConfig.Instance.appConfiguration
        let headers: HTTPHeaders = ["share_token": shareToken, "uri": uri]
        return await getResponse(
            api: ShareApiSuffix.API_PREPARE_SEND,
            method: .post,
            headers: headers
        )
    }

    func prepareSendResponse(shareToken: String, isAccept: Bool, preferDownloadSelf: Bool) async -> DesignResponse<Bool> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "prepareSendResponse")
        let appConfig = AppConfig.Instance.appConfiguration
        let headers: HTTPHeaders = ["share_token": shareToken, "is_accept": isAccept.description, "prefer_download_self": preferDownloadSelf.description]
        return await getResponse(
            api: ShareApiSuffix.API_PREPARE_SEND,
            method: .post,
            headers: headers
        )
    }

    func getContent(shareToken: String, contentId: String) -> HTTPURLResponse? {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "getContent")
        let appConfig = AppConfig.Instance.appConfiguration
        let headers: HTTPHeaders = ["share_token": shareToken, "content_id": contentId]
        return getDownloadResponse(
            api: ShareApiSuffix.API_GET_CONTENT_LIST,
            method: .post,
            headers: headers
        )
    }

    func getCotnentList(shareToken: String, uri: String) async -> DesignResponse<ContentInfoList> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "getCotnentList")
        let appConfig = AppConfig.Instance.appConfiguration
        let headers: HTTPHeaders = ["share_token": shareToken, "uri": uri]
        return await getResponse(
            api: ShareApiSuffix.API_GET_CONTENT_LIST,
            method: .post,
            headers: headers
        )
    }

    func exchangeDeviceInfo(shareDevice: HttpShareDevice) async -> DesignResponse<HttpShareDevice> {
        PurpleLogger.current.d(AppSetsShareApiImpl.TAG, "exchangeDeviceInfo")
        let appConfig = AppConfig.Instance.appConfiguration

        let params: [String: Any] = [
            "id": shareDevice.id,
            "deviceName": ["rawName": shareDevice.deviceName.rawName, "nickName": shareDevice.deviceName.nickName],
            "deviceAddress": ["ips": shareDevice.deviceAddress.ips],
            "deviceType": shareDevice.deviceType,
        ]
        return await getResponse(
            api: ShareApiSuffix.API_EXCHANGE_DEVICE_INFO,
            method: .post,
            params: params
        )
    }
}
