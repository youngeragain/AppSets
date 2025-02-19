//
//  ImObj.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

protocol ImObj {
    
    var bio: any Bio { get set }
    
    var isRelated:Bool { get }
    
    var id:String { get }
    
    var name:String { get }
    
    var avatarUrl:String? { get }
}

struct ImObjStatic {
    
    static func fromBio(_ bio: any Bio) -> any ImObj {
        switch bio{
        case is UserInfo:
            return ImSingle(bio)
            
        case is GroupInfo:
            return ImGroup(bio)
            
        case is Application:
            return ImGroup(bio)
            
        default:
            return ImSingle(bio)
        }
    }
    
}


class ImSingle : ImObj {
    
    var bio: any Bio
    var isRelated: Bool
    
    var id: String {
        return bio.id
    }
    
    var name: String {
        return bio.name ?? bio.id
    }
    
    var avatarUrl: String? {
        return bio.bioUrl
    }
    
    let userRoles: String?
    
    init(_ bio: any Bio, userRoles:String? = nil) {
        self.bio = bio
        self.userRoles = userRoles
        self.isRelated = RelationsUseCase.Instance.hasUserRelated(bio.id)
    }
}

class ImGroup : ImObj {
    
    var bio: any Bio
    
    var isRelated: Bool
    
    var id: String {
        return bio.id
    }
    
    var bios:[any Bio]? = nil
    
    var name: String {
        return bio.name ?? bio.id
    }
    
    var avatarUrl: String? {
        return bio.bioUrl
    }
    
    init(_ bio: any Bio) {
        self.bio = bio
        self.isRelated = RelationsUseCase.Instance.hasGroupRelated(bio.id)
    }
    
}
