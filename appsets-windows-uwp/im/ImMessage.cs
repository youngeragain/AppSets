using AA;
using AppSets.utils;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Media;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Windows.Devices.Sms;
using Windows.Foundation.Metadata;
using Windows.UI.Core;
using Windows.UI.ViewManagement;

#nullable enable
namespace AppSets.im
{
    public class Session
    {
        public ImObj imObj;
        public ConversationUiState conversationState;
    }
    public interface ImObj
    {
        public string id { get; set; }
        public string name { get; set; }
        public string avatar { get; set; }
        public bool isRelated { get; set; }

        public static MessageToInfo toToInfo(ImObj imObj) {
            if (imObj is ImSingle)
            {
                return new MessageToInfo()
                {
                    toType = "one2one",
                    id = imObj.id,
                    name = imObj.name,
                    iconUrl = imObj.avatar,
                    roles = (imObj as ImSingle).userRoles
                };
            }
            else if (imObj is ImGroup)
            {
                return new MessageToInfo()
                {
                    toType = "one2many",
                    id = imObj.id,
                    name = imObj.name,
                    iconUrl = imObj.avatar,
                };
            }
            else throw new Exception();
        }
    }

    public class ImGroup : ImObj
    {
        public string groupId;
        public string groupName;
        public string? groupAvatarUrl;
        public bool mIsRelated;

        public string id { get => groupId; set => groupId = value; }
        public string name { get => groupName; set => groupName = value; }
        public string avatar
        {
            get
            {
                if (string.IsNullOrEmpty(groupAvatarUrl))
                {
                    return "https://locahost";
                }
                return groupAvatarUrl;
            }
            set => groupAvatarUrl = value;
        }
        public bool isRelated { get => mIsRelated; set => mIsRelated = value; }
    }


    public class ImSingle : ImObj
    {
        public string uid;
        public string userName;
        public string? userAvatarUrl;
        public string? userRoles;
        public bool mIsRelated;

        public string id { get => uid; set => uid = value; }
        public string name { get => userName; set => userName = value; }
        public string avatar
        {
            get
            {
                if (string.IsNullOrEmpty(userAvatarUrl))
                {
                    return "https://localhost";
                }
                return userAvatarUrl;
            }
            set => userAvatarUrl = value;
        }
        public bool isRelated { get => mIsRelated; set => mIsRelated = value; }
    }

    public class ConversationUiState: BindableBase
    {
        public ObservableCollection<ImMessage> messages = new ObservableCollection<ImMessage>();

        private string? mLastImMessage = null;
        public string? latestImMessage
        {
            get {
                if (mLastImMessage==null) { 
                    return "无消息";
                }
                return mLastImMessage;
            }
            set {
                this.SetProperty(ref this.mLastImMessage, value);
            }
        }
        public ConversationUiState() {
            
        }

        

        internal void addMessage(ImMessage imMessage)
        {
            Debug.WriteLine("ConversationUiState addMessage Thread:" + Thread.CurrentThread.ManagedThreadId);
            messages.Add(imMessage);
            latestImMessage = imMessage.content;
        }
    }



    public class MessageFromInfo {
        public string id;
        public string? name;
        public string? avatarUrl;
        public string? roles;
    }

    public class MessageToInfo
    {
        public string id;
        public string? name;
        public string? iconUrl;
        public string? roles;
        public string toType;

        public bool isImgGroupMessage { get => toType == "one2many"; set => throw new Exception(); }
        public bool isImSingleMessage { get => toType == "one2one"; set => throw new Exception(); }
    }

    public interface ImMessage {
        public string id { get; set; }
        public string content { get; set; }
        public MessageFromInfo msgFromInfo { get; set; }
        public DateTime date { get; set; }
        public MessageToInfo msgToInfo { get; set; }
        public string? groupMessageTag { get; set; }
        public string contentType { get; set; }

        public static ImObj? parseFromImObj(ImMessage imMessage)
        {
            if (imMessage.msgToInfo.isImgGroupMessage)
            {
                return new ImGroup()
                {
                    id = imMessage.msgToInfo.id,
                    name = imMessage.msgToInfo.name,
                    avatar = imMessage.msgToInfo.iconUrl
                };
            }
            else if (imMessage.msgToInfo.isImSingleMessage)
            {
                return new ImSingle()
                {
                    id = imMessage.msgFromInfo.id,
                    name = imMessage.msgFromInfo.name,
                    avatar = imMessage.msgFromInfo.avatarUrl,
                    userRoles = imMessage.msgFromInfo.roles
                };
            }
            return null;
        }

        public static ImObj? parseToImObj(ImMessage imMessage)
        {
            if (imMessage.msgToInfo.isImgGroupMessage)
            {
                return new ImGroup()
                {
                    id = imMessage.msgToInfo.id,
                    name = imMessage.msgToInfo.name,
                    avatar = imMessage.msgToInfo.iconUrl
                };
            }
            else if (imMessage.msgToInfo.isImSingleMessage)
            {
                return new ImSingle()
                {
                    id = imMessage.msgToInfo.id,
                    name = imMessage.msgToInfo.name,
                    avatar = imMessage.msgToInfo.iconUrl,
                    userRoles = imMessage.msgToInfo.roles
                };
            }
            return null;
        }
    }

    public abstract class AbsImMessage:ImMessage
    {
        public abstract string id { get; set; }
        public abstract string content { get; set; }
        public abstract MessageFromInfo msgFromInfo { get; set; }
        public abstract DateTime date { get; set; }
        public abstract MessageToInfo msgToInfo { get; set; }
        public abstract string? groupMessageTag { get; set; }
        public abstract string contentType { get; set; }

        public Brush bgColor()
        {
            if (string.Equals(AccountProvider.Instance.UserInfo.Uid, msgFromInfo.id))
                {
                var color = new UISettings().GetColorValue(UIColorType.Accent);
          
                return new SolidColorBrush(color);
                }
                else
                {
                
                var color = new UISettings().GetColorValue(UIColorType.Accent);
                return new SolidColorBrush(color);
            }
        }

        public HorizontalAlignment msgAlignment()
        {
            if (string.Equals(AccountProvider.Instance.UserInfo.Uid, msgFromInfo.id))
            {
                return HorizontalAlignment.Right;
            }
            else
            {
                return HorizontalAlignment.Left;
            }
        }

        public CornerRadius bgCornerRadius()
        {
            if (string.Equals(AccountProvider.Instance.UserInfo.Uid, msgFromInfo.id))
            {
                return new CornerRadius(16, 2, 16, 16);
            }
            else
            {
                return new CornerRadius(2, 16, 16, 16);
            }
        }



    }
    public class ImText : AbsImMessage
    {
        public string msgId;
        public string text;
        public MessageFromInfo fromInfo;
        public DateTime timestamp;
        public MessageToInfo toInfo;
        public string? msgGroupTag;
        public ImText(string msgId, string text, MessageFromInfo fromInfo,
            DateTime timestamp, MessageToInfo toInfo, string? msgGroupTag)
        {
            this.msgId = msgId;
            this.text = text;
            this.fromInfo = fromInfo;
            this.timestamp = timestamp;
            this.toInfo = toInfo;
            this.msgGroupTag = msgGroupTag;

        }

        public override string id { get => msgId; set => msgId = value; }
        public override string content { get => text; set => text = value; }
        public override MessageFromInfo msgFromInfo { get => fromInfo; set => fromInfo = value; }
        public override DateTime date { get => timestamp; set => timestamp = value; }
        public override MessageToInfo msgToInfo { get => toInfo; set => toInfo = value; }
        public override string? groupMessageTag { get => msgGroupTag; set => msgGroupTag = value; }
        public override string contentType { get => "application/*"; set => throw new NotImplementedException(); }
    }


    class RabbitMqBrokerPropertyDesignType {
        public static string TYPE_TEXT = "im.content.text";
        public static string TYPE_IMAGE = "im.content.image";
        public static string TYPE_VIDEO = "im.content.video";
        public static string TYPE_MUSIC = "im.content.music";
        public static string TYPE_VOICE = "im.content.voice";
        public static string TYPE_LOCATION = "im.content.location";
        public static string TYPE_FILE = "im.content.file";
        public static string TYPE_HTML = "im.content.html";
        public static string TYPE_AD = "im.content.ad";
        public static string TYPE_SYSTEM = "im.content.system";
        public static string TYPE_CUSTOM = "im.content.custom.*";

        public static string getTypeByImMessage(ImMessage imMessage) {
            if (imMessage is ImText) { 
                return TYPE_TEXT;
            }
            return TYPE_CUSTOM;
        }
    }
}
