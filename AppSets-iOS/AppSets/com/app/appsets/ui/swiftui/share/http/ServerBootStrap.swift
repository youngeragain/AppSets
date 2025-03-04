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

    func main(actionListenr: ActionListener?) {
        let thread = Thread {
            Task.detached {
                do {
                    let router = Router()
                    let appSetsShareController = AppSetsShareController<BasicRequestContext>()
                    appSetsShareController.addRoutes(to: router.group("/"))
                    // create application using router
                    let app = Hummingbird.Application(
                        router: router,
                        configuration: .init(address: .hostname("0.0.0.0", port: 11101))
                    )
                    // run hummingbird application
                    actionListenr?.onSuccess()
                    try await app.runService()
                } catch {
                    actionListenr?.onFailure(reason: nil)
                }
            }
        }
        thread.name = "humingBirdThread"
        thread.start()
    }

    func close(actionListenr: ActionListener?) {
        Task.detached(
            operation: {
                try await EventLoopGroupProvider.singleton.eventLoopGroup.shutdownGracefully()
                actionListenr?.onSuccess()
            }
        )
    }
}
