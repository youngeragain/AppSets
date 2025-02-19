//
//  MessageToInfo.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

struct MessageToInfo: Bio, Equatable {
    static func == (lhs: MessageToInfo, rhs: MessageToInfo) -> Bool {
        return lhs.toType == rhs.toType &&
        lhs.id == rhs.id &&
        lhs.name == rhs.name &&
        lhs.iconUrl == rhs.iconUrl &&
        lhs.roles == rhs.roles
    }
    
    
    let toType:String
    let id: String
    var name: String? = nil
    var iconUrl:String? = nil
    var roles:String? = nil
    
    var bioUrl: String? = nil
    
    init(toType:String, id:String, name:String?, iconUrl:String?, roles:String?){
        self.toType = toType
        self.id = id
        self.name = name
        self.iconUrl = iconUrl
        self.roles = roles
    }
    
    static func fromImObj(_ imObj: any ImObj) -> MessageToInfo {
        
        let bio = imObj.bio
        
        var iconUrl:String? = nil
        if bio is UserInfo {
            iconUrl = (bio as? UserInfo)?.avatarUrl
        }
        else if bio is GroupInfo {
            iconUrl = (bio as? GroupInfo)?.iconUrl
        }
        else if bio is Application {
            iconUrl = (bio as? Application)?.iconUrl
        }
        
        if imObj is ImSingle {
            return MessageToInfo(
                toType: ImMessageConstant.TYPE_O2O,
                id: imObj.id,
                name: bio.name,
                iconUrl: iconUrl,
                roles: (bio as? UserInfo)?.roles
            )
        }else {
            return MessageToInfo(
                toType: ImMessageConstant.TYPE_O2M,
                id: imObj.id,
                name: bio.name,
                iconUrl: iconUrl,
                roles: nil
            )
        }
    }
}
