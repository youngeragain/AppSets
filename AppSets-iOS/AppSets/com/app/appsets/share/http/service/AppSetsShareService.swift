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
    ) async -> DesignResponse<String>

    func ping(
        context: Context,
        clientHost: String
    ) async -> DesignResponse<String>

    func isNeedPin(
        context: Context,
        clientHost: String
    ) async -> DesignResponse<Bool>

    func pair(
        context: Context,
        clientHost: String,
        pin: Int
    ) async -> DesignResponse<Bool>

    func pairResponse(
        context: Context,
        clientHost: String,
        shareToken: String
    ) async -> DesignResponse<Bool>

    func prepareSend(
        context: Context,
        clientHost: String,
        shareToken: String,
        uri: String
    ) async -> DesignResponse<Bool>

    func prepareSendResponse(
        context: Context,
        clientHost: String,
        shareToken: String,
        isAccept: Bool,
        preferDownloadSelf: Bool
    ) async -> DesignResponse<Bool>

    func postText(
        context: Context,
        clientHost: String,
        shareToken: String,
        text: String
    ) async -> DesignResponse<Bool>

    func postFile(
        context: Context,
        clientHost: String,
        shareToken: String,
        fileUploadN: FileUploadN
    ) async -> DesignResponse<Bool>

    func postFileChunked(
        context: Context,
        clientHost: String,
        shareToken: String,
        fileId: String,
        chunkCount: Int,
        chunk: Int,
        fileUploadN: FileUploadN
    ) async -> DesignResponse<Bool>

    func getContent(
        context: Context,
        clientHost: String,
        shareToken: String,
        contentId: String
    ) async -> DesignResponse<ContentDownloadN>

    func getContentList(
        context: Context,
        clientHost: String,
        shareToken: String,
        uri: String
    ) async -> DesignResponse<ContentInfoList>

    func exchangeDeviceInfo(
        context: Context,
        clientHost: String,
        device: HttpShareDevice
    ) async -> DesignResponse<HttpShareDevice>
}
