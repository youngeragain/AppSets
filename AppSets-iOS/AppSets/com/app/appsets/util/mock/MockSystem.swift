//
//  MockSystem.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

class MockSystem: NSObject, Mockable {
    
    private static let TAG = "MockSystem"
    
    private static let Instance = MockIOS()
    
    private var mThread: Thread? = nil
    
    private func setThread(_ thread: Thread) {
        self.mThread = thread
    }
    
    private func getThread() -> Thread {
        let rThread = mThread ?? Thread.current
        PurpleLogger.current.d(MockSystem.TAG, "getThread, mThread:\(mThread.debugDescription)")
        return rThread
    }
    
    private func getMainThread()-> Thread{
        return Thread.main
    }

    public static func startMock() {
        let hanlder:@convention(c) (NSException)->Void = { e in
            PurpleLogger.current.d(MockSystem.TAG, "Athread:\(Thread.current.description) UncaughtException, e:\(e)")
        }
        NSSetUncaughtExceptionHandler(hanlder)
        
        let currentThread = Thread.current
        PurpleLogger.current.d(MockSystem.TAG, "startMock, start thread:\(currentThread.debugDescription)")
        let mockThread = Thread {
            let currentThread1 = Thread.current
            PurpleLogger.current.d(MockSystem.TAG, "startMock with new thread:\(currentThread1.debugDescription)")
            
            let runLoop:RunLoop = RunLoop.current
            runLoop.run()
            PurpleLogger.current.d(MockSystem.TAG, "startMock Mock thread:\(currentThread1.debugDescription) finished!")
        }
        mockThread.name = "Thread-\(MockSystem.TAG)"
        mockThread.start()
        MockSystem.Instance.setThread(mockThread)
        MockSystem.Instance.perform(
            #selector(MockIOS.bootstrap),
            on: mockThread,
            with: nil,
            waitUntilDone: false
        )
    }
    
    public static func startService(_ intent: Intent) {
        let currentThread = Thread.current
        
        PurpleLogger.current.d(MockSystem.TAG, "startService, intent:\(intent), thread:\(currentThread.debugDescription)")
        
        let clientCommand = ClientCommand(intent: intent, commandRunnable: {})
        
        MockSystem.Instance.perform(
            #selector(MockIOS.onClientCommand),
            on: MockSystem.Instance.getMainThread(),
            with: clientCommand,
            waitUntilDone: false
        )
      
    }
    
    public static func execute(_ what: @escaping () -> Void) {
        let currentThread = Thread.current
        
        PurpleLogger.current.d(MockSystem.TAG, "execute, thread:\(currentThread.debugDescription)")
        let clientCommand = ClientCommand(intent: Intent.Empty, commandRunnable: {
            what()
        })
        MockIOS.Instance.perform(
            #selector(MockIOS.exec),
            on: MockSystem.Instance.getMainThread(),
            with: clientCommand,
            waitUntilDone: false
        )
    }
    
    public static func execute(name:String, _ what: @escaping () -> Void) {
        PurpleLogger.current.d(MockSystem.TAG, "execute, with name:\(name)")
        execute(what)
    }
    
}
