//
//  AppSetsShareController.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

import Foundation
import HTTPTypes
import Hummingbird

struct AppSetsShareController<ReqContext: RequestContext> {
    private let service: AppSetsShareService = AppSetsShareServiceImpl()

    func addRoutes(to group: RouterGroup<ReqContext>) {
        group.get("/appsets/share", use: greeting)
        group.get("/appsets/share/ping", use: ping)
        group.get("/appsets/share/pin/isneed", use: isNeedPin)
        group.post("/appsets/share/pin/pair", use: pair)
        group.post("/appsets/share/pin/pair_response", use: pairResponse)
        group.post("/appsets/share/prepare", use: prepareSend)
        group.post("/appsets/share/prepare_response", use: prepareSendResponse)
        group.post("/appsets/share/text", use: postText)
        group.post("/appsets/share/file", use: postFile)
        group.post("/appsets/share/file/chunked", use: postFileChunked)
        group.get("/appsets/share/content/get", use: getContent)
        group.post("/appsets/share/contents/get", use: getContentList)
        group.post("/appsets/share/device/info/exchange", use: exchangeDeviceInfo)
    }

    private func findContext() -> any Context {
        return LocalContext.current
    }

    private func findClientHost(_ request: Request, reqContext: ReqContext) -> String {
        return request.uri.host ?? ""
    }

    @Sendable func greeting(_ request: Request, reqContext: ReqContext) async -> StringResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        return await service.greeting(context: context, clientHost: clientHost)
    }

    @Sendable func ping(_ request: Request, reqContext: ReqContext) async -> StringResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        return await service.ping(context: context, clientHost: clientHost)
    }

    @Sendable func isNeedPin(_ request: Request, reqContext: ReqContext) async -> BooleanResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        return await service.isNeedPin(context: context, clientHost: clientHost)
    }

    @Sendable func pair(_ request: Request, reqContext: ReqContext) async -> BooleanResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let pin = 0
        return await service.pair(context: context, clientHost: clientHost, pin: pin)
    }

    @Sendable func pairResponse(_ request: Request, reqContext: ReqContext) async -> BooleanResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let shareToken = ""
        return await service.pairResponse(context: context, clientHost: clientHost, shareToken: shareToken)
    }

    @Sendable func prepareSend(_ request: Request, reqContext: ReqContext) async -> BooleanResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let shareToken = ""
        let uri = ""
        return await service.prepareSend(context: context, clientHost: clientHost, shareToken: shareToken, uri: uri)
    }

    @Sendable func prepareSendResponse(_ request: Request, reqContext: ReqContext) async -> BooleanResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let shareToken = ""
        let isAccept = false
        let preferDownloadSelf = true
        return await service.prepareSendResponse(context: context, clientHost: clientHost, shareToken: shareToken, isAccept: isAccept, preferDownloadSelf: preferDownloadSelf)
    }

    @Sendable func postText(request: Request, reqContext: ReqContext) async -> BooleanResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let shareToken = ""
        let text = ""
        return await service.postText(context: context, clientHost: clientHost, shareToken: shareToken, text: text)
    }

    @Sendable func postFile(request: Request, reqContext: ReqContext) async -> BooleanResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let shareToken = ""
        let fileUploadN = FileUploadN(id: "")
        return await service.postFile(context: context, clientHost: clientHost, shareToken: shareToken, fileUploadN: fileUploadN)
    }

    @Sendable func postFileChunked(request: Request, reqContext: ReqContext) async -> BooleanResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let shareToken = ""
        let fileId = ""
        let chunkCount = 1
        let chunk = 1
        let fileUploadN = FileUploadN(id: "")
        return await service.postFileChunked(context: context, clientHost: clientHost, shareToken: shareToken,
                                             fileId: fileId, chunkCount: chunkCount, chunk: chunk, fileUploadN: fileUploadN)
    }

    @Sendable func getContent(request: Request, reqContext: ReqContext) async -> ContentDownloadNResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let shareToken = ""
        let contentId = ""
        return await service.getContent(context: context, clientHost: clientHost, shareToken: shareToken, contentId: contentId)
    }

    @Sendable func getContentList(_ request: Request, reqContext: ReqContext) async -> ContentInfoListResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let shareToken = ""
        let uri = request.headers[HTTPField.Name("uri")!] ?? "/"
        return await service.getContentList(context: context, clientHost: clientHost, shareToken: shareToken, uri: uri)
    }

    @Sendable func exchangeDeviceInfo(request: Request, reqContext: ReqContext) async -> DeviceInfoResponse {
        let context = findContext()
        let clientHost = findClientHost(request, reqContext: reqContext)
        let deviceName = DeviceName.NONE
        let deviceAddress = DevcieAddress.NONE
        let device = HttpShareDevice(
            deviceName: deviceName,
            deviceAddress: deviceAddress,
            deviceType: ShareDevice.DEVICE_TYPE_PHONE
        )
        return await service.exchangeDeviceInfo(context: context, clientHost: clientHost, device: device)
    }
}
