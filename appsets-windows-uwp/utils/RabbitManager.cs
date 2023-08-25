using AppSets.im;
using AppSets.models;
using Microsoft.Windows.ApplicationModel.DynamicDependency;
using Newtonsoft.Json;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Channels;
using System.Threading.Tasks;

namespace AA.rabbit
{
    public delegate void MsgCallback(ImMessage imMessage);

    public class RabbitMqBrokerProperty {
        public string rabbitHost;
        public int rabbitPort;
        public string rabbitAdminUsername;
        public string rabbitAdminPassword;
        public string rabbitVirtualHost;
        public string queuePrefix;
        public string routingKeyPrefix;
        public string userExchangeMain;
        public string userExchangeMainParent;
        public string userExchangeGroupPrefix;
        public string uid;
        public string? userExchangeGroups;

    }

    public class RabbitManager 
    {
        private event MsgCallback? msgCallbacks;
        private IConnection rabbitConnection;
        private IModel rabbitChannel;
        private bool userChanged = false;
        private bool groupChanged = false;
        private RabbitMqBrokerProperty rabbitMqBrokerProperty;
        private string? queueName;
        private string? routingKey;
        private ConnectionFactory connectionFactory;
        
        private RabbitManager()
        {
            
        }
        /*
         * 启动入口
         */
        public static void bootStrap(RabbitMqBrokerProperty property) {
            Debug.WriteLine("开始启动RabbitMq");
            Instance.updateRabbitMqBrokerProperty(property);
        }

       
        private void updateRabbitMqBrokerProperty(RabbitMqBrokerProperty property) {
            if (connectionFactory == null) {
                connectionFactory = new ConnectionFactory();
            }
            connectionFactory.HostName = property.rabbitHost;
            connectionFactory.Port = property.rabbitPort;
            connectionFactory.UserName = property.rabbitAdminUsername;
            connectionFactory.Password = property.rabbitAdminPassword;
            connectionFactory.VirtualHost = property.rabbitVirtualHost;
            if (rabbitMqBrokerProperty == null)
            {
                userChanged = true;
                groupChanged = true;
            }
            else {
                userChanged = !string.Equals(rabbitMqBrokerProperty.uid, property.uid);
                groupChanged = !string.Equals(rabbitMqBrokerProperty.userExchangeGroups, property.userExchangeGroups);
            }
            rabbitMqBrokerProperty = property;
            startRabbit();
        }
       

        private void startRabbit() {
            new Thread(new ThreadStart(run)).Start();
        }

        
        private void basicConnect()
        {
            try
            {
               if(rabbitConnection != null && rabbitConnection.IsOpen && !userChanged) {
                    return;
                }
                rabbitConnection = connectionFactory.CreateConnection();
                if (rabbitConnection == null || !rabbitConnection.IsOpen)
                {
                    return;
                }
                rabbitChannel = rabbitConnection.CreateModel();
            }
            catch (Exception ex)
            {

                Debug.WriteLine("RabbitManager basicConnect failed!" + ex);
            }
            
        }

        public void run() {
            
            try
            {
                basicConnect();
                if (rabbitConnection == null || !rabbitConnection.IsOpen || rabbitChannel == null || !rabbitChannel.IsOpen) {
                    Debug.WriteLine("RabbitManager basicConnect failed!");
                    return;
                }
                doUserChannelThings();
            }
            catch (Exception ex) { 
                Debug.WriteLine ("RabbitManager, 连接异常!");
            }
            
        }

        private void doUserChannelThings()
        {
            if (userChanged) {
                fullDeclare();
                return;
            }
            if (groupChanged) {
                someDeclare();
            }
        }

        private void someDeclare()
        {
            var exchanges = new List<KeyValuePair<String, String>>();
            var groupExchangePrefix = rabbitMqBrokerProperty.userExchangeGroupPrefix;
            if (!string.IsNullOrEmpty(rabbitMqBrokerProperty.userExchangeGroups))
            {
                var groupNames = rabbitMqBrokerProperty.userExchangeGroups.Split(',');
                foreach (var groupName in groupNames)
                {
                    var groupExchangeName = groupExchangePrefix + groupName;
                    var groupExchange = new KeyValuePair<String, String>(groupExchangeName, "fanout");
                    exchanges.Add(groupExchange);
                }
            }
            if (rabbitChannel == null || !rabbitChannel.IsOpen)
            {
                return;
            }
          
            foreach (var exchange in exchanges)
            {
                rabbitChannel.ExchangeDeclare(exchange: exchange.Key,
                                            type: exchange.Value,
                                            durable: true,
                                            autoDelete: true
                                           );
                rabbitChannel.QueueBind(queueName, exchange.Key, routingKey, null);
            }
        }
        
        private void fullDeclare()
        {
            Guid guid = Guid.NewGuid();
            string uuid = guid.ToString();
            queueName = rabbitMqBrokerProperty.queuePrefix + rabbitMqBrokerProperty.uid + "_CUUID_" + uuid;
            routingKey = rabbitMqBrokerProperty.routingKeyPrefix + rabbitMqBrokerProperty.queuePrefix + rabbitMqBrokerProperty.uid;
            var userTopicExchangeName = rabbitMqBrokerProperty.userExchangeMain;
            var userTopicExchange = new KeyValuePair<String, String>(userTopicExchangeName, "topic");
            var allExchanges = new List<KeyValuePair<String, String>>();
            allExchanges.Add(userTopicExchange);
            var groupExchangePrefix = rabbitMqBrokerProperty.userExchangeGroupPrefix;
            if (!string.IsNullOrEmpty(rabbitMqBrokerProperty.userExchangeGroups)) {
                var groupNames = rabbitMqBrokerProperty.userExchangeGroups.Split(',');
                foreach (var groupName in groupNames)
                {
                    var groupExchangeName = groupExchangePrefix + groupName;
                    var groupExchange = new KeyValuePair<String, String>(groupExchangeName, "fanout");
                    allExchanges.Add(groupExchange);
                }
            }
            if (rabbitChannel == null || !rabbitChannel.IsOpen) {
                return;
            }
            rabbitChannel.QueueDeclare(queue:queueName, durable:false, exclusive: true, autoDelete:false, arguments:null);
            foreach (var exchange in allExchanges)
            {
                rabbitChannel.ExchangeDeclare(exchange: exchange.Key,
                                            type: exchange.Value,
                                            durable: true,
                                            autoDelete: false
                                           );
                rabbitChannel.QueueBind(queueName, exchange.Key, routingKey, null);
            }
            var userFanExchangeName = rabbitMqBrokerProperty.userExchangeMainParent;
            rabbitChannel.ExchangeDeclare(userFanExchangeName, "fanout", true, false, null);
            rabbitChannel.ExchangeBind(userTopicExchangeName, userFanExchangeName, routingKey);
            var consumer = new EventingBasicConsumer(rabbitChannel);
            consumer.Received += transformToImMessage;
            rabbitChannel.BasicConsume(queue: queueName,
                                         autoAck: true,
                                         consumer: consumer);
        }

        private void transformToImMessage(object? sender, BasicDeliverEventArgs basicDeliverEventArgs)
        {
            Debug.WriteLine("收到从RabbitMq来的消息");
            var headers = basicDeliverEventArgs.BasicProperties.Headers;
            var imMessageType = basicDeliverEventArgs.BasicProperties.Type;

            var timeStamp = basicDeliverEventArgs.BasicProperties.Timestamp;
            var imMessageTimestamp = new DateTime(timeStamp.UnixTime);
            string fromUid;
            if (headers.ContainsKey("uid"))
            {
                
                fromUid = Encoding.UTF8.GetString((byte[])headers["uid"]);
            }
            else {
                return;
            }
            string msgId;
            if (headers.ContainsKey("msgId"))
            {
                msgId = Encoding.UTF8.GetString((byte[])headers["msgId"]);
            }
            else {
                return;
            }
            string? fromName = null;
            if (headers.ContainsKey("name"))
            {
                var nameFromHeader = headers["name"];
                if (nameFromHeader != null)
                {
                    fromName = Encoding.UTF8.GetString((byte[])nameFromHeader);
                }
                
            }

            string? fromAvatarUrl = null;
            if (headers.ContainsKey("avatarUrl"))
            {
                var avatarUrlFromHeader = headers["avatarUrl"];
                if (avatarUrlFromHeader != null)
                {
                    fromAvatarUrl = Encoding.UTF8.GetString((byte[])avatarUrlFromHeader);
                }
                
            }
            string? fromRoles = null;
            if (headers.ContainsKey("roles")) {
                var rolesFromHeader = headers["roles"];
                if (rolesFromHeader != null)
                {
                    fromRoles = Encoding.UTF8.GetString((byte[])rolesFromHeader);
                }
               
            }
            string? toRoles = null;
            if (headers.ContainsKey("toRoles")) {
                var toRolesFromHeader = headers["toRoles"];
                if (toRolesFromHeader != null) {

                    toRoles = Encoding.UTF8.GetString((byte[])toRolesFromHeader);
                }
                
            }
            string toType;
            if (headers.ContainsKey("toType"))
            {
                toType = Encoding.UTF8.GetString((byte[])headers["toType"]);
            }
            else {
                return;
            }

            string toId;
            if (headers.ContainsKey("toId"))
            {
                toId = Encoding.UTF8.GetString((byte[])headers["toId"]);
            }
            else {
                return;
            }
            string? toName = null;
            if(headers.ContainsKey("toName"))
            {
                var toNameFromHeader = headers["toName"];
                if (toNameFromHeader != null)
                {
                    toName = Encoding.UTF8.GetString((byte[])toNameFromHeader);
                }
                
            }
            string? toIconUrl = null;
            if (headers.ContainsKey("toIconUrl")) {
                var toIconUrlFromHeader = headers["toIconUrl"];
                if (toIconUrlFromHeader != null) {
                    toIconUrl = Encoding.UTF8.GetString((byte[])toIconUrlFromHeader);
                }
                
            }
            string? groupMessageTag = null;
            if (headers.ContainsKey("groupMessageTag")) {
                var groupMessageTagFromHeader = headers["groupMessageTag"];
                if (groupMessageTagFromHeader != null) {

                    groupMessageTag =  Encoding.UTF8.GetString((byte[])groupMessageTagFromHeader);
                }
                
            }
            var imUserFromInfo = new MessageFromInfo() { 
                id = fromUid, name = fromName,
                avatarUrl = fromAvatarUrl, roles = fromRoles,
            };
            var imToInfo = new MessageToInfo() { 
                id = toId, name = toName,
                iconUrl = toIconUrl, roles = toRoles,
                toType = toType,
            };


            var imContentBytes = basicDeliverEventArgs.Body.ToArray();
            var imContent = Encoding.UTF8.GetString(imContentBytes);
            ImMessage? imMessage = null;
            if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_TEXT)
            {
                imMessage = new ImText(msgId, imContent, imUserFromInfo, imMessageTimestamp, imToInfo, groupMessageTag);
            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_IMAGE)
            {

            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_VOICE) { 
            
            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_LOCATION)
            {

            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_VIDEO)
            {

            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_AD)
            {

            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_MUSIC)
            {

            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_FILE)
            {

            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_HTML)
            {

            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_SYSTEM)
            {

            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_SYSTEM)
            {

            }
            else if (imMessageType == RabbitMqBrokerPropertyDesignType.TYPE_CUSTOM)
            {

            }
            if (imMessage != null && msgCallbacks != null) { 
                msgCallbacks(imMessage);
            }
        }

        public static void sendMessage(ImObj imObj, ImMessage imMessage) {
            Instance.sendMessageInternal(imObj, imMessage);
        }

        private  void sendMessageInternal(ImObj imObj, ImMessage imMessage) {
            if (rabbitConnection == null || !rabbitConnection.IsOpen|| rabbitChannel == null || !rabbitChannel.IsOpen)
            {
                Debug.WriteLine("发送消息失败,请检查连接!");
                return;
            }
          
            var basicProperties = rabbitChannel.CreateBasicProperties();
            basicProperties.Timestamp = new AmqpTimestamp(DateTime.Now.Ticks);
            basicProperties.Type = RabbitMqBrokerPropertyDesignType.getTypeByImMessage(imMessage);
            basicProperties.ContentType = imMessage.contentType;
            var headers = new Dictionary<string, object>();
            headers.Add("messageDeliveryType", "RelayTransmission");
            headers.Add("msgId", imMessage.id);
            headers.Add("uid", imMessage.msgFromInfo.id);
            headers.Add("name", imMessage.msgFromInfo.name);
            headers.Add("avatarUrl", imMessage.msgFromInfo.avatarUrl);
            headers.Add("roles", imMessage.msgFromInfo.roles);
            if (!string.IsNullOrEmpty(imMessage.groupMessageTag)) {
                headers.Add("groupMessageTag", imMessage.groupMessageTag);
            }

            if (!string.IsNullOrEmpty(imMessage.msgToInfo.id))
            {
                headers.Add("toId", imMessage.msgToInfo.id);
            }
            if (!string.IsNullOrEmpty(imMessage.msgToInfo.name))
            {
                headers.Add("toName", imMessage.msgToInfo.name);
            }

            if (!string.IsNullOrEmpty(imMessage.msgToInfo.toType))
            {
                headers.Add("toType", imMessage.msgToInfo.toType);
            }

            if (!string.IsNullOrEmpty(imMessage.msgToInfo.iconUrl))
            {
                headers.Add("iconUrl", imMessage.msgToInfo.iconUrl);
            }

            if (!string.IsNullOrEmpty(imMessage.msgToInfo.roles))
            {
                headers.Add("toRoles", imMessage.msgToInfo.roles);
            }
            basicProperties.Headers = headers;
            string? exchange = null;
            if (imObj is ImSingle)
            {
                exchange = rabbitMqBrokerProperty.userExchangeMainParent;
            }
            else if (imObj is ImGroup) {
                exchange = rabbitMqBrokerProperty.userExchangeGroupPrefix + imObj.id;
            }
            string? routingKey = null;
            if (imObj is ImSingle)
            {
                routingKey = rabbitMqBrokerProperty.routingKeyPrefix + rabbitMqBrokerProperty.queuePrefix + imObj.id;
            }
            else if (imObj is ImGroup) { 
                routingKey = rabbitMqBrokerProperty.routingKeyPrefix +"to.group_"+imObj.id;
            }
            if (string.IsNullOrEmpty(exchange)||string.IsNullOrEmpty(routingKey)) {
                return;
            }
            var contentBytes = Encoding.UTF8.GetBytes(imMessage.content);
            rabbitChannel.BasicPublish(exchange, routingKey, false, basicProperties,  contentBytes);
            if (imObj is ImSingle) {
                var selfClientRoutingKey = rabbitMqBrokerProperty.routingKeyPrefix + rabbitMqBrokerProperty.queuePrefix + rabbitMqBrokerProperty.uid;
                rabbitChannel.BasicPublish(exchange, selfClientRoutingKey, false, basicProperties, contentBytes);
            }
        }

      

        public static void addCallback(MsgCallback msgCallback)
        {
            Instance.msgCallbacks -= msgCallback;
            Instance.msgCallbacks+=msgCallback;
        }

        private static readonly Lazy<RabbitManager> InstanceLock = new Lazy<RabbitManager>(() => new RabbitManager());

        private static RabbitManager Instance
        {
            get { return InstanceLock.Value; }
        }


        public static void close() {
            Instance.closeInternal();
        }

        private void closeInternal() {
            deleteUserQueue();
            if (rabbitChannel!=null&&rabbitChannel.IsOpen) { 
                rabbitChannel.Close();
            }
            if (rabbitConnection != null && rabbitConnection.IsOpen) { 
                rabbitConnection.Close();
            }
            rabbitChannel = null;
            rabbitConnection = null;
        }
        private void deleteUserQueue()
        {

            if (rabbitConnection != null && rabbitConnection.IsOpen && rabbitChannel != null && rabbitChannel.IsOpen) {
                if (queueName != null) { 
                    rabbitChannel.QueueDelete(queueName);
                }
            }
        }
    }
}
