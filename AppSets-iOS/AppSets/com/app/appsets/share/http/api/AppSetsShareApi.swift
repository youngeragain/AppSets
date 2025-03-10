//
//  AppSetsShareApi.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/10.
//
import Foundation

protocol AppSetsShareApi {
    func greeting() async -> DesignResponse<String>
    func ping() async -> DesignResponse<String>
    func isNeedPin() async -> DesignResponse<Bool>
    func pair(pin: String) async -> DesignResponse<Bool>
    func pairResponse(shareToken: String) async -> DesignResponse<Bool>
    func postText(shareToken: String, text: String) async -> DesignResponse<Bool>
    func postFile(shareToken: String, url: URL) async -> DesignResponse<Bool>
    func prepareSend(shareToken: String, uri: String) async -> DesignResponse<Bool>
    func prepareSendResponse(shareToken: String, isAccept: Bool, preferDownloadSelf: Bool) async -> DesignResponse<Bool>
    func getContent(shareToken: String, contentId: String) -> HTTPURLResponse?
    func getCotnentList(shareToken: String, uri: String) async -> DesignResponse<ContentInfoList>
    func exchangeDeviceInfo(shareDevice: HttpShareDevice) async -> DesignResponse<HttpShareDevice>
}
