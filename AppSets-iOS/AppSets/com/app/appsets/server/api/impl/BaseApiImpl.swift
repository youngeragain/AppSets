//
//  BaseApiImpl.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation
import Alamofire

class BaseApiImpl {
    
    private static let TAG = "BaseApiImpl"
    
    func getResponse<T:DesignResponse>(
        _ t: T.Type = T.self,
        url: String,
        method: HTTPMethod = .get,
        params: Parameters? = nil,
        headers: HTTPHeaders? = nil
    ) async -> T {
        do {
            let overrideHeaders = headers ?? DesignHttp.provideHeaders(url)
            let response = try await DesignHttp.session
                .request(
                    url,
                    method: method,
                    parameters: params,
                    encoding: JSONEncoding.default,
                    headers: overrideHeaders
                )
                .serializingDecodable(T.self)
                .value
            return response
        }catch let error {
            PurpleLogger.current.d(
                BaseApiImpl.TAG,
                """
                getResponse, error:\(error),
                url: \(url)
                """
            )
        }
        return BaseApiImpl.ErrorResponse(t) as! T
    }
    
    private static func ErrorResponse<T>(_ t: T.Type) -> any DesignResponse {
        switch t {
        case is StringResponse.Type:
            return StringResponse(code: -1)
        case is UserInfoResponse.Type:
            return UserInfoResponse(code: -1)
        case is UserFriendsReponse.Type:
            return UserFriendsReponse(code: -1)
        case is UserChatGroupInfosResponse.Type:
            return UserChatGroupInfosResponse(code: -1)
        case is ApplicationsResponse.Type:
            return ApplicationsResponse(code: -1)
        case is SpotLightResponse.Type:
            return SpotLightResponse(code: -1)
        case is ScreensResponse.Type:
            return ScreensResponse(code: -1)
        default:
            return NullReponse(code: -1)
        }
    }
}
