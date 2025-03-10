//
//  DevcieAddress.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct DevcieAddress: Codable {
    var ips: [DeviceIP] = []
    
    var ip4:String?{
        get{
            
            return ips.first { ip in
                ip.type==DeviceIP.IP_4
            }?.ip
        }
    }
    
    var ip6:String?{
        get{
            
            return ips.first { ip in
                ip.type==DeviceIP.IP_6
            }?.ip
        }
    }
    
    func containsIp(inIp: String)-> Bool {
        for ip in ips {
            if(inIp == ip.ip){
                return true
            }
        }
        return false
    }
    
    public static let NONE = DevcieAddress()
}
