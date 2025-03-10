//
//  AppSetsShareRepository.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/10.
//

class AppSetsShareRepository {
    private static let TAG = "AppSetsShareRepository"

    func buildApi(baseUrlProvider: BaseUrlProvider) -> AppSetsShareApi? {
        PurpleLogger.current.d(AppSetsShareRepository.TAG, "buildApi, baseUrlProvider:\(baseUrlProvider)")
        let baseUrl = baseUrlProvider.provideUrl()
        if baseUrl.isEmpty {
            return nil
        }
        return AppSetsShareApiImpl(ShareResponseProvider.INSTANCE, baseUrlProvider)
    }

    func isNeedPin(shareDevice: HttpShareDevice) async -> DesignResponse<Bool> {
        let baseUrlProvider = ShareDeviceApiBaseUrlProvider(shareDevice: shareDevice)
        guard let api = buildApi(baseUrlProvider: baseUrlProvider) else {
            return DesignResponseStatic.notFound()
        }
        return await api.isNeedPin()
    }

    func pair(shareDevice: HttpShareDevice, pin:String) async {
        let baseUrlProvider = ShareDeviceApiBaseUrlProvider(shareDevice: shareDevice)
        guard let api = buildApi(baseUrlProvider: baseUrlProvider) else {
            return
        }
        _ = await api.pair(pin: pin)
        shareDevice.pin = pin
    }

    func pairResponse(shareDevice: HttpShareDevice, shareToken:String) async -> DesignResponse<Bool> {
        let baseUrlProvider = ShareDeviceApiBaseUrlProvider(shareDevice: shareDevice)
        guard let api = buildApi(baseUrlProvider: baseUrlProvider) else {
            return DesignResponseStatic.notFound()
        }
        return await api.pairResponse(shareToken: shareToken)
    }

    func postText(dataContent: StringDataContent) async {
    }

    func postFile(dataContent: UriDataContent) async {
    }

    func prepareSend(shareDevice: HttpShareDevice, uri: String) async -> DesignResponse<Bool> {
        let baseUrlProvider = ShareDeviceApiBaseUrlProvider(shareDevice: shareDevice)
        guard let api = buildApi(baseUrlProvider: baseUrlProvider) else {
            return DesignResponseStatic.notFound()
        }
        return await api.prepareSend(shareToken: shareDevice.token ?? "", uri: uri)
    }

    func prepareSendReponse(shareDevice: HttpShareDevice, isAccept: Bool, isPreferDownlaodSelf: Bool) async -> DesignResponse<Bool> {
        let baseUrlProvider = ShareDeviceApiBaseUrlProvider(shareDevice: shareDevice)
        guard let api = buildApi(baseUrlProvider: baseUrlProvider) else {
            return DesignResponseStatic.notFound()
        }
        return await api.prepareSendResponse(shareToken: shareDevice.token ?? "", isAccept: isAccept, preferDownloadSelf: isPreferDownlaodSelf)
    }

    func exchangeDeviceInfo(
        shareMethod: HttpShareMethod,
        address: String
    ) async {
        guard let currentShareDevice = shareMethod.getCurrentShareDevice() else {
            return
        }
        
        let baseUrlProvider = ShareDeviceApiBaseUrlProvider(shareDevice: currentShareDevice)
        guard let api = buildApi(baseUrlProvider: baseUrlProvider) else {
            return
        }
        
        let shareDevcieResponse = await api.exchangeDeviceInfo(shareDevice: currentShareDevice)
        if let httpShareDevice = shareDevcieResponse.data {
            _ = shareMethod.exchangeDeviceInfo(shareDevice: httpShareDevice)
        }
    }

    func handleSend(uri: String) async {
    }

    func resumeHandleSend(uri: String) async {
    }

    func handleSendForDevice(shareDevice: HttpShareDevice, uri: String) async {
    }

    func handleDownload(shareDevice: HttpShareDevice, uri: String) async {
    }

    func getContentList(shareDevice:HttpShareDevice, uri:String) async -> DesignResponse<ContentInfoList> {
        let baseUrlProvider = ShareDeviceApiBaseUrlProvider(shareDevice: shareDevice)
        guard let api = buildApi(baseUrlProvider: baseUrlProvider) else {
            return DesignResponseStatic.notFound()
        }
        return await api.getCotnentList(shareToken: shareDevice.token ?? "", uri: uri)
    }

    func getContent(shareDevice: HttpShareDevice, contentInfo: ContentInfo) async {
    }
}
