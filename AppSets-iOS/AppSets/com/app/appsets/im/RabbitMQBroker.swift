//
//  RabbitMQBroker.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/2.
//

import Foundation
import RMQClient


class SimpleRMQConnectionRecovery: NSObject, RMQConnectionRecovery{
    
    var interval: NSNumber!
    
    func recover(_ connection: (any RMQStarter)!, channelAllocator allocator: (any RMQChannelAllocator)!, error: (any Error)!) {
        
    }
    
}

class PrivateRMQConnectionDelegateLogger: RMQConnectionDelegateLogger{
    override func connection(_ connection: RMQConnection!, failedToConnectWithError error: (any Error)!) {
        BrokerTest.Instance.changeOnline(false)
    }
    override func connection(_ connection: RMQConnection!, disconnectedWithError error: (any Error)!) {
        BrokerTest.Instance.changeOnline(false)
    }
    override func recoveredConnection(_ connection: RMQConnection!) {
        BrokerTest.Instance.changeOnline(true)
    }
}


class RabbitMQBroker: MessageBroker {
    
    public static let TAG = "RabbitMQBroker"
    
    public static let MESSAGE_KEY_ON_IM_MESSAGE = "ON_IM_MESSAGE"
    
    typealias C = RabbitMQBrokerConfig
    
    private var connection: RMQConnection?
    
    private var channel: RMQChannel? = nil
    
    private var property: RabbitMqBrokerProperty? = nil
    
    private var queueName: String? = nil
    
    private var routingKey: String? = nil
    
    private var uid: String? = nil
    
    func bootstrap(_ config: RabbitMQBrokerConfig) {
        PurpleLogger.current.d(RabbitMQBroker.TAG, "bootstrap")
        updateUserChannel(config.property)
    }
    
    private func updateUserChannel(_ property: RabbitMqBrokerProperty) {
        PurpleLogger.current.d(RabbitMQBroker.TAG, "updateUserChannel")
        self.property = property
        let userGroupsChanged = true
        let loggedUserUid = LocalAccountManager.Instance.userInfo.uid
        let userChanged = self.uid != loggedUserUid
        self.uid = loggedUserUid
        basicConnect()
        doUserChannelThings(userChanged, userGroupsChanged)
    }
    
    private func basicConnect() {
        if checkConnection() {
            PurpleLogger.current.d(RabbitMQBroker.TAG,"basicConnect! connection is connected, return")
            return
        }
        guard let property = self.property else {
            return
        }
        do {
            PurpleLogger.current.d(RabbitMQBroker.TAG, "startConnection")
            let uri = "amqp://\(property.username):\(property.password )@\(property.host):\(property.port)/\(property.virtualHost)"
            connection = RMQConnection(uri: uri, delegate: PrivateRMQConnectionDelegateLogger())
            connection?.start()
            guard let channel = try connection?.createChannel() else {
                return
            }
            self.channel = channel
            BrokerTest.Instance.changeOnline(true)
            PurpleLogger.current.d(RabbitMQBroker.TAG, "startConnection, connected!")
        }catch let error {
            PurpleLogger.current.d(RabbitMQBroker.TAG, "startConnection, exception!")
        }
    }
    
    private func doUserChannelThings(_ userChanged: Bool, _ groupChanged: Bool) {
        if userChanged {
            fullDeclare()
        }else if groupChanged {
            someDeclare()
        }
    }
    
    private func someDeclare() {
        PurpleLogger.current.d(RabbitMQBroker.TAG, "someDeclare")
        if !checkConnection(){
            PurpleLogger.current.d(RabbitMQBroker.TAG, "someDeclare, connection not ready, return")
            return
        }
        guard let property = self.property else {
            return
        }
        guard let channel = self.channel else{
            return
        }
        guard let queueName = self.queueName else{
            return
        }
        let routingKey = "\(property.routingKeyPrefix)\(property.queuePrefix)\(uid ?? "")"
        var allExchanges:[Triple<String, String, String>] = []
        if let othersExchanges = fillExchanges(property) {
            allExchanges += othersExchanges
        }
        for exchange in allExchanges {
            PurpleLogger.current.d(RabbitMQBroker.TAG, "someDeclare, exchange:\(exchange)")
            channel.exchangeDeclare(exchange.second, type: exchange.third)
            channel.queueBind(queueName, exchange: exchange.second, routingKey: routingKey)
            if exchange.third == RMQConstants.EXCHNAGE_FANOUT {
                channel.exchangeBind(property.groupExchangeParent, destination: exchange.second, routingKey: "msg.to.group_\(exchange.first)")
            }
        }
        
    }
    
    func fullDeclare() {
        PurpleLogger.current.d(RabbitMQBroker.TAG, "fullDeclare")
        if !checkConnection(){
            PurpleLogger.current.d(RabbitMQBroker.TAG, "fullDeclare, connection not ready, return")
            return
        }
        guard let property = self.property else {
            return
        }
        
        guard let channel = self.channel else {
            PurpleLogger.current.d(RabbitMQBroker.TAG, "fullDeclare, channel is null, return")
            return
        }
        
        guard let uid = self.uid else {
            return
        }
        
        let queueName = "\(property.queuePrefix)\(uid)_CUUID_\(UUID().uuidString.lowercased())"
        
        self.queueName = queueName
        
        let routingKey = "\(property.routingKeyPrefix)\(property.queuePrefix)\(uid)"
        
        let mQueue = channel.queue(queueName, options: [.exclusive])
        
        channel.exchangeDeclare(property.groupExchangeParent, type: RMQConstants.EXCHNAGE_TOPIC, options: [.durable])
        channel.exchangeDeclare(property.groupSubRootExchange, type: RMQConstants.EXCHNAGE_FANOUT, options: [.durable])
        channel.exchangeBind(property.groupSubRootExchange, destination: property.groupExchangeParent, routingKey: "")
        
        
        var allExchanges: [Triple<String, String, String>] = []
        allExchanges.append(Triple(uid, "\(property.groupExchangePrefix)\(uid)", RMQConstants.EXCHNAGE_FANOUT))
        if let othersExchanges = fillExchanges(property) {
            allExchanges += othersExchanges
        }
        for exchange in allExchanges {
            PurpleLogger.current.d(RabbitMQBroker.TAG, "fullDeclare, exchange:\(exchange)")
            channel.exchangeDeclare(exchange.second, type: exchange.third)
            channel.queueBind(queueName, exchange: exchange.second, routingKey: routingKey)
            if exchange.third == RMQConstants.EXCHNAGE_FANOUT {
                channel.exchangeBind(property.groupExchangeParent, destination: exchange.second, routingKey: "msg.to.group_\(exchange.first)")
            }
        }
        
        mQueue.subscribe([], handler: { message in
            PurpleLogger.current.d(RabbitMQBroker.TAG, "message received")
            guard message.body != nil else {
                PurpleLogger.current.d(RabbitMQBroker.TAG, "message received, content is null, return")
                return
            }
            
            if let imMessage = ImMessageGenerator.Instance.generateByReceived(message) {
                LocalMessager.post(RabbitMQBroker.MESSAGE_KEY_ON_IM_MESSAGE, imMessage)
            }
            
        })
    }
    
    
    func fillExchanges(_ property: RabbitMqBrokerProperty) -> [Triple<String, String, String>]? {
        let groupIds = RelationsUseCase.Instance.getGroupIds()
        if groupIds.isEmpty {
            return nil
        }
        
        var exchanges:[Triple<String, String, String>] = []
        
        for groupId in groupIds {
            let exchange = Triple(groupId, "\(property.groupExchangePrefix)\(groupId)", RMQConstants.EXCHNAGE_FANOUT)
            exchanges.append(exchange)
        }
        
        PurpleLogger.current.d(RabbitMQBroker.TAG, "fillExchanges, exchanges:\(exchanges)")
        
        return exchanges
    }
    
    
    func retry() {
    
    }
    
    func close() {
        
    }
    
    func checkConnection() -> Bool {
        guard let connection = self.connection else {
            PurpleLogger.current.d(RabbitMQBroker.TAG, "checkConnection, broker connection is null, return false")
            return false
        }
        PurpleLogger.current.d(RabbitMQBroker.TAG, "checkConnection, broker connection isOpen:\(connection.isOpen())")
        //if !connection.isOpen() {
        //    PurpleLogger.current.d(RabbitMQBroker.TAG, "checkConnection, broker connection is not open, return false")
        //    return false
        //}
        
        guard let channel = self.channel else {
            PurpleLogger.current.d(RabbitMQBroker.TAG, "checkConnection, broker connection channel is null, return false")
            return false
        }
        let isChannelOpen = channel.isOpen()
        PurpleLogger.current.d(RabbitMQBroker.TAG, "checkConnection, broker connection channel isOpen:\(isChannelOpen)")
        return isChannelOpen
    }
    
    private var brokerDeliveryMode = 2
    
    func sendMessage(_ imObj: any ImObj, _ imMessage: any ImMessage)-> Bool {
        if !checkConnection() {
            PurpleLogger.current.d(RabbitMQBroker.TAG, "sendMessage, broker connection not ready! return")
            return false
        }
        PurpleLogger.current.d(RabbitMQBroker.TAG, "sendMessage")
        guard let channel = self.channel else {
            return false
        }
        guard let property = self.property else {
            return false
        }
        
        guard let data = ImMessageGenerator.Instance.makeMessageMetadataAsJsonStringData(message: imMessage) else {
            return false
        }
        
        var properties: [RMQValue] = []
        let timestamp = RMQBasicTimestamp(imMessage.timestamp)
        let type = RMQBasicType(ImMessageDesignType.getType(imMessage))
        let deliveryMode = RMQBasicDeliveryMode(CChar(brokerDeliveryMode))
        properties.append(timestamp)
        properties.append(type)
        properties.append(deliveryMode)
        var headers: [String: RMQValue & RMQFieldValue] = [String: RMQValue & RMQFieldValue]()
        headers[ImMessageConstant.HEADER_MESSAGE_MESSAGE_DELIVERY_TYPE] = RMQLongstr(AppSettings.Instance.imMessageDeliveryType)
        headers[ImMessageConstant.HEADER_MESSAGE_ID] = RMQLongstr(imMessage.id)
        headers[ImMessageConstant.HEADER_MESSAGE_UID] = RMQLongstr(imMessage.fromInfo.uid)
        headers[ImMessageConstant.HEADER_MESSAGE_NAME] = RMQLongstr(imMessage.fromInfo.name ?? "")
        let fromUserNameBase64 = (imMessage.fromInfo.name ?? "").data(using: .utf8)?.base64EncodedString() ?? ""
        headers[ImMessageConstant.HEADER_MESSAGE_NAME_BASE64] = RMQLongstr(fromUserNameBase64)
        headers[ImMessageConstant.HEADER_MESSAGE_AVATAR_URL] = RMQLongstr(imMessage.fromInfo.avatarUrl ?? "")
        headers[ImMessageConstant.HEADER_MESSAGE_ROLES] = RMQLongstr(imMessage.fromInfo.roles ?? "")
        headers[ImMessageConstant.HEADER_MESSAGE_MESSAGE_GROUP_TAG] = RMQLongstr(imMessage.messageGroupTag ?? "")
        headers[ImMessageConstant.HEADER_MESSAGE_TO_ID] = RMQLongstr(imMessage.toInfo.id)
        headers[ImMessageConstant.HEADER_MESSAGE_TO_NAME] = RMQLongstr(imMessage.toInfo.name ?? "")
        let toNameBase64 = (imMessage.toInfo.name ?? "").data(using: .utf8)?.base64EncodedString() ?? ""
        headers[ImMessageConstant.HEADER_MESSAGE_TO_NAME_BASE64] = RMQLongstr(toNameBase64)
        headers[ImMessageConstant.HEADER_MESSAGE_TO_TYPE] = RMQLongstr(imMessage.toInfo.toType)
        headers[ImMessageConstant.HEADER_MESSAGE_TO_ICON_URL] = RMQLongstr(imMessage.toInfo.iconUrl ?? "")
        headers[ImMessageConstant.HEADER_MESSAGE_TO_ROLES] = RMQLongstr(imMessage.toInfo.roles ?? "")
        let basicHeaders = RMQBasicHeaders(headers)
        properties.append(basicHeaders)
    
        let exchange: String = property.groupSubRootExchange
        
        let routingKey: String = "msg.to.group_\(imObj.id)"
        
        do{
            try channel.basicPublish(data, routingKey: routingKey, exchange: exchange, properties: properties)
            PurpleLogger.current.d(RabbitMQBroker.TAG, "sendMessage, exchange:\(exchange), routingKey:\(routingKey)")
        }catch let error {
            PurpleLogger.current.d(RabbitMQBroker.TAG, "sendMessage, exception! error\(error)")
            return false
        }
        
        
        
        return true
    }
    
    func updateImGroupBindIfNeeded() {
        PurpleLogger.current.d(RabbitMQBroker.TAG, "updateImGroupBindIfNeeded")
        guard let property = property else {
            return
        }
        updateUserChannel(property)
    }
}
