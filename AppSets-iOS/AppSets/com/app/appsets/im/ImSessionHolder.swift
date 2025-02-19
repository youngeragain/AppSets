//
//  ImSessionHolder.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation
import RealmSwift

protocol ImSessionHolder {

    var session: Session? { get set }
    
}

struct ImSessionHolderStatic {
    
    public static func updateSession(_ h:ImSessionHolder, _ s:Session){
        if let t = h as? UserInfo {
            t.session = s
            return
        }
        if let t = h as? GroupInfo {
            t.session = s
            return
        }
        if let t = h as? Application {
            t.session = s
            return
        }
    }
}
