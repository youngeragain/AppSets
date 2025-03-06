//
//  ServerBootStrap.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/2.
//
import Foundation
import Hummingbird

class ServerBootStrap {
    protocol ActionListener {
        func onSuccess()
        func onFailure(reason: String?)
    }

    private static let TAG = "ServerBootStrap"

    private var app: (any Hummingbird.ApplicationProtocol)? = nil

    func main(actionListener: ActionListener?) {
        PurpleLogger.current.d(ServerBootStrap.TAG, "main")
        Task.detached {
            do {
                self.close(actionListener: nil)

                let router = Router()
                let appSetsShareController = AppSetsShareController<BasicRequestContext>()
                appSetsShareController.addRoutes(to: router.group("/"))
                // create application using router
                let app = Hummingbird.Application(
                    router: router,
                    configuration: .init(address: .hostname("0.0.0.0", port: 11101))
                )
                self.app = app
                // run hummingbird application
                actionListener?.onSuccess()
                try await app.runService()
            } catch let e {
                actionListener?.onFailure(reason: e.localizedDescription)
            }
        }
    }

    func close(actionListener: ActionListener?) {
        PurpleLogger.current.d(ServerBootStrap.TAG, "close")
        if app == nil {
            return
        }
        Task.detached(
            operation: {
                do {
                    try await self.app?.eventLoopGroup.shutdownGracefully()
                    self.app = nil
                    actionListener?.onSuccess()
                } catch let e {
                    actionListener?.onFailure(reason: e.localizedDescription)
                }
            }
        )
    }
}
