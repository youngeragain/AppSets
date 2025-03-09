//
//  AppSettings.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/14.
//

import Foundation

class AppSettings {
    
    public static let IM_BUBBLE_ALIGNMENT_ALL_START = "all_start"
    public static let IM_BUBBLE_ALIGNMENT_ALL_END = "all_end"
    public static let IM_BUBBLE_ALIGNMENT_START_END = "start_end"

    public static let IM_MESSAGE_DELIVERY_TYPE_DI = "send_directly"
    public static let IM_MESSAGE_DELIVERY_TYPE_RT = "relay_transmission"
    
    private static let TAG = "AppSettings"
    
    public static let Instance = AppSettings()
    
    
    var imMessageDeliveryType: String = IM_MESSAGE_DELIVERY_TYPE_RT
    var imBubbleAlignment: String = IM_BUBBLE_ALIGNMENT_START_END

    var isImMessageShowDate: Bool = true
    var isImMessageDateShowSeconds: Bool = false
    
    
}
