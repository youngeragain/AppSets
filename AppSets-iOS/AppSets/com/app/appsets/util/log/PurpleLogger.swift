//
//  PurpleLogger.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/18.
//

import Foundation

let PurpleLogger: StaticProvider<PurpleLoggerForIOS> = staticProvider<Logger>{
    PurpleLoggerForIOS()
}
