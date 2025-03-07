//
//  AppSetsShareService.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct AppSetsShareServiceImpl: AppSetsShareService {
    private static let TAG = "AppSetsShareServiceImpl"

    func greeting(context: any Context, clientHost: String) async -> StringResponse {
        PurpleLogger.current.d(AppSetsShareServiceImpl.TAG, "greeting")
        return StringResponse(code: 0, data: "Hello, AppSets Share!")
    }

    func ping(context: any Context, clientHost: String) async -> StringResponse {
        PurpleLogger.current.d(AppSetsShareServiceImpl.TAG, "ping")
        return StringResponse(code: 0, data: "pong")
    }

    func isNeedPin(context: any Context, clientHost: String) async -> BooleanResponse {
        return BooleanResponse(code: 0, data: true)
    }

    func pair(context: any Context, clientHost: String, pin: Int) async -> BooleanResponse {
        return BooleanResponse(code: 0, data: true)
    }

    func pairResponse(context: any Context, clientHost: String, shareToken: String) async -> BooleanResponse {
        return BooleanResponse(code: 0, data: true)
    }

    func prepareSend(context: any Context, clientHost: String, shareToken: String, uri: String) async -> BooleanResponse {
        return BooleanResponse(code: 0, data: true)
    }

    func prepareSendResponse(context: any Context, clientHost: String, shareToken: String, isAccept: Bool, preferDownloadSelf: Bool) async -> BooleanResponse {
        return BooleanResponse(code: 0, data: true)
    }

    func postText(context: any Context, clientHost: String, shareToken: String, text: String) async -> BooleanResponse {
        return BooleanResponse(code: 0, data: true)
    }

    func postFile(context: any Context, clientHost: String, shareToken: String, fileUploadN: FileUploadN) async -> BooleanResponse {
        return BooleanResponse(code: 0, data: true)
    }

    func postFileChunked(context: any Context, clientHost: String, shareToken: String, fileId: String, chunkCount: Int, chunk: Int, fileUploadN: FileUploadN) async -> BooleanResponse {
        return BooleanResponse(code: 0, data: true)
    }

    func getContent(context: any Context, clientHost: String, shareToken: String, contentId: String) async -> ContentDownloadNResponse {
        let contentDownloadN = ContentDownloadN(id: "", name: "")
        return ContentDownloadNResponse(code: 0, data: contentDownloadN)
    }

    func getContentList(context: any Context, clientHost: String, shareToken: String, uri: String) async -> ContentInfoListResponse {
        PurpleLogger.current.d(AppSetsShareServiceImpl.TAG, "getContentList")
        let uri = uri
        var infoList: [ContentInfo] = []
        if let str = "Are you ok?".data(using: .utf8)?.base64EncodedString() {
            let contentInfo = ContentInfo(id: "", name: str, size: 1, type: 0)
            infoList.append(contentInfo)
        }

        let contentInfoListWrapper = ContentInfoListWrapper(uri: uri, count: infoList.count, infoList: infoList)
        return ContentInfoListResponse(code: 0, data: contentInfoListWrapper)
    }

    func exchangeDeviceInfo(context: any Context, clientHost: String, device: HttpShareDevice) async -> DeviceInfoResponse {
        let deviceName = DeviceName.NONE
        let deviceAddress = DevcieAddress.NONE
        let device = HttpShareDevice(
             deviceName: deviceName,
             deviceAddress: deviceAddress,
             deviceType: ShareDevice.DEVICE_TYPE_PHONE
        )
        return DeviceInfoResponse(code: 0, data: device)
    }
}
