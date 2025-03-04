//
//  AppSetsShareService.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

protocol AppSetsShareService {
    func greeting(
        context: Context,
        clientHost: String
    ) async -> StringResponse

    func ping(
        context: Context,
        clientHost: String
    ) async -> StringResponse

    func isNeedPin(
        context: Context,
        clientHost: String
    ) async -> BooleanResponse

    func pair(
        context: Context,
        clientHost: String,
        pin: Int
    ) async -> BooleanResponse

    func pairResponse(
        context: Context,
        clientHost: String,
        shareToken: String
    ) async -> BooleanResponse

    func prepareSend(
        context: Context,
        clientHost: String,
        shareToken: String,
        uri: String
    ) async -> BooleanResponse

    func prepareSendResponse(
        context: Context,
        clientHost: String,
        shareToken: String,
        isAccept: Bool,
        preferDownloadSelf: Bool
    ) async -> BooleanResponse

    func postText(
        context: Context,
        clientHost: String,
        shareToken: String,
        text: String
    ) async -> BooleanResponse

    func postFile(
        context: Context,
        clientHost: String,
        shareToken: String,
        fileUploadN: FileUploadN
    ) async -> BooleanResponse

    func postFileChunked(
        context: Context,
        clientHost: String,
        shareToken: String,
        fileId: String,
        chunkCount: Int,
        chunk: Int,
        fileUploadN: FileUploadN
    ) async -> BooleanResponse

    func getContent(
        context: Context,
        clientHost: String,
        shareToken: String,
        contentId: String
    ) async -> ContentDownloadNResponse

    func getContentList(
        context: Context,
        clientHost: String,
        shareToken: String,
        uri: String
    ) async -> ContentInfoListResponse

    func exchangeDeviceInfo(
        context: Context,
        clientHost: String,
        device: HttpShareDevice
    ) async -> DeviceInfoResponse
}
