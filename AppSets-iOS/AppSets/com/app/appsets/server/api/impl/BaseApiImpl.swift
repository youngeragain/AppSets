//
//  BaseApiImpl.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Alamofire
import Foundation

class BaseApiImpl {
    private static let TAG = "BaseApiImpl"

    let defaultReponseProvider: DefaultResponseProvider
    let baseUrlProvider: BaseUrlProvider

    init(_ defaultReponseProvider: DefaultResponseProvider, _ baseUrlProvider: BaseUrlProvider) {
        self.defaultReponseProvider = defaultReponseProvider
        self.baseUrlProvider = baseUrlProvider
    }

    private func contactApi(_ api: String) -> String {
        return "\(baseUrlProvider.provideUrl())\(api)"
    }

    func getResponse<D:Codable, T: DesignResponse<D>>(
        _ t: T.Type = T.self,
        _ d: D.Type = D.self,
        api: String,
        method: HTTPMethod = .get,
        params: Parameters? = nil,
        headers: HTTPHeaders? = nil
    ) async -> T {
        let url = contactApi(api)
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
        } catch let error {
            PurpleLogger.current.d(
                BaseApiImpl.TAG,
                """
                getResponse, error:\(error),
                url: \(url)
                """
            )
        }
        return defaultReponseProvider.provideResponse(d) as! T
    }

    func getDownloadResponse(
        api: String,
        method: HTTPMethod = .get,
        params: Parameters? = nil,
        headers: HTTPHeaders? = nil
    ) -> HTTPURLResponse? {
        let url = contactApi(api)
        let overrideHeaders = headers ?? DesignHttp.provideHeaders(url)

        let response = DesignHttp.session
            .download(
                url,
                method: method,
                parameters: params,
                encoding: JSONEncoding.default,
                headers: overrideHeaders
            )
            .response
        return response
    }
}
