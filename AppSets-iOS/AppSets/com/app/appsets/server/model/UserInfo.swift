//
//  UserInfo.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/4.
//

import Foundation
import RealmSwift

final class UserInfo : Object, ImSessionHolder, Bio, Codable {

    @Persisted(primaryKey: true)
    var uid: String
    @Persisted
    var name: String? = nil
    @Persisted
    var avatarUrl: String? = nil
    @Persisted
    var agreeToTheAgreement: Int? = nil
    @Persisted
    var age: Int? = nil
    @Persisted
    var sex: String? = nil
    @Persisted
    var email: String? = nil
    @Persisted
    var phone: String? = nil
    @Persisted
    var address: String? = nil
    @Persisted
    var introduction: String? = nil
    @Persisted
    var company: String? = nil
    @Persisted
    var profession: String? = nil
    @Persisted
    var website: String? = nil
    @Persisted
    var roles: String? = nil
    
    var bioUrl: String? {
        get{
            return avatarUrl
        }
        set{
            
        }
    }
    
    var id: String {
        get {
            return uid
        }
    }
    
    var session: Session? = nil
    
    
    enum CodingKeys: String, CodingKey {
        case uid
        case name
        case avatarUrl
        case agreeToTheAgreement
        case age
        case sex
        case email
        case phone
        case address
        case introduction
        case company
        case profession
        case website
    }
    
    override init() {
        
    }
    
    init(uid:String) {
        super.init()
        self.uid = uid
    }
    
    
    init(
        uid:String,
        name:String? = nil,
        age:Int? = nil,
        sex:String? = nil
    ) {
        super.init()
        self.uid = uid
        self.name = name
        self.age = age
        self.sex = sex
    }
    
    init(from decoder: any Decoder) throws {
        super.init()
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.uid = try container.decode(String.self, forKey: .uid)
        self.name = try container.decodeIfPresent(String.self, forKey: .name)
        self.avatarUrl = try container.decodeIfPresent(String.self, forKey: .avatarUrl)
        self.agreeToTheAgreement = try container.decodeIfPresent(Int.self, forKey: .agreeToTheAgreement)
        let ageString = try container.decodeIfPresent(String.self, forKey: .age)
        if ageString != nil {
            if let ageInt = Int(ageString!) {
                self.age = ageInt
            }
        }
        
        self.sex = try container.decodeIfPresent(String.self, forKey: .sex)
        self.email = try container.decodeIfPresent(String.self, forKey: .email)
        self.phone = try container.decodeIfPresent(String.self, forKey: .phone)
        self.address = try container.decodeIfPresent(String.self, forKey: .address)
        self.introduction = try container.decodeIfPresent(String.self, forKey: .introduction)
        self.company = try container.decodeIfPresent(String.self, forKey: .company)
        self.profession = try container.decodeIfPresent(String.self, forKey: .profession)
        self.website = try container.decodeIfPresent(String.self, forKey: .website)
    }
    
    func encode(to encoder: any Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(self.uid, forKey: .uid)
        try container.encodeIfPresent(self.name, forKey: .name)
        try container.encodeIfPresent(self.avatarUrl, forKey: .avatarUrl)
        try container.encodeIfPresent(self.agreeToTheAgreement, forKey: .agreeToTheAgreement)
        try container.encodeIfPresent("\(String(describing: self.age))", forKey: .age)
        try container.encodeIfPresent(self.sex, forKey: .sex)
        try container.encodeIfPresent(self.email, forKey: .email)
        try container.encodeIfPresent(self.phone, forKey: .phone)
        try container.encodeIfPresent(self.address, forKey: .address)
        try container.encodeIfPresent(self.introduction, forKey: .introduction)
        try container.encodeIfPresent(self.company, forKey: .company)
        try container.encodeIfPresent(self.profession, forKey: .profession)
        try container.encodeIfPresent(self.website, forKey: .website)
    }
    
    private static let user0 = UserInfo(uid: "0", name: "蒋开心", age: 0, sex: "female")
    
    static func defaultUser() -> UserInfo {
        return user0
    }
}
