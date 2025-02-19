//
//  PurpleLogger.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/4.
//

import Foundation

class PurpleLoggerForIOS {
    
    private static let TAG = "PurpleLogger"
    
    static let LEVEL_VERBOSE = "VERBOSE"
    static let LEVEL_INFO = "INFO"
    static let LEVEL_DEBUG = "DEBUG"
    static let LEVEL_WARN = "WARN"
    static let LEVEL_ERROR = "ERROR"
    
    private let dateFormatter = DateFormatter()
    
    init() {
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss SSS"
    }
    
    func v(_ tag: String = TAG, _ message: String, error: Error? = nil) {
        logLevel(PurpleLoggerForIOS.LEVEL_VERBOSE, tag: tag, message: message, error: error)
    }
    
    func i(_ tag: String = TAG, _ message: String, error: Error? = nil) {
        logLevel(PurpleLoggerForIOS.LEVEL_INFO, tag: tag, message: message, error: error)
    }
    
    
    func d(_ tag: String = TAG, _ message: String, error: Error? = nil) {
        logLevel(PurpleLoggerForIOS.LEVEL_DEBUG, tag: tag, message: message, error: error)
    }
    
    func w(_ tag: String = TAG, _ message: String, error: Error? = nil) {
        logLevel(PurpleLoggerForIOS.LEVEL_WARN, tag: tag, message: message, error: error)
    }
    
    func e(_ tag: String = TAG, _ message: String, error: Error? = nil) {
        logLevel(PurpleLoggerForIOS.LEVEL_ERROR, tag: tag, message: message, error: error)
    }
    
    private func logLevel(_ level: String, tag: String, message: String, error: Error? = nil) {
        let currentDate = Date()
        let formattedDate = dateFormatter.string(from: currentDate)
        print("[\(formattedDate)] [\(level)] \(alignTagLength(tag)): \(message)")
    }
    
    private func alignTagLength(_ tag:String, align:Bool = true) -> String {
        if !align {
            return tag
        }
        return tag
    }
}

