using AA;
using AA.rabbit;
using AppSets.models;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace AppSets.utils
{

    internal class Constants {

        internal static string RABBIT_HOST = "192.168.0.104";
        internal static int RABBIT_PORT = 5672;
        internal static string BASE_URL = "https://localhost:8084/";
        internal static RabbitMqBrokerProperty RabbitMqBrokerProperty() {
            var property = new RabbitMqBrokerProperty()
            {
                rabbitHost = RABBIT_HOST,
                rabbitPort = RABBIT_PORT,
                rabbitAdminUsername = "testuser1",
                rabbitAdminPassword = "testuser1pwd",
                rabbitVirtualHost = "/",
                queuePrefix = "user_",
                routingKeyPrefix = "msg.",
                userExchangeMain = "one2one-topic",
                userExchangeMainParent = "one2one-fanout",
                userExchangeGroupPrefix = "one2many-fanout-",
                uid = AccountProvider.Instance.UserInfo.Uid
            };
            var relatedGroups = AccountProvider.Instance.RelatedGroups;
            if (relatedGroups == null || relatedGroups.Count == 0)
                return property;
            var sb = new StringBuilder();
            for (int i = 0; i < relatedGroups.Count; i++)
            {
                var groupInfo = relatedGroups[i];
                sb.Append(groupInfo.GroupId);

                if (i < relatedGroups.Count - 1)
                {
                    sb.Append(",");
                }
            }
            property.userExchangeGroups = sb.ToString();
            return property;
        }
    }


    internal static class Extentions {

        public static bool isHttpUrlExt(this string str)
        {
            return Commons.isHttpUrl(str);
        }
       
    }

    internal class Commons
    {

        

        public static string EncryptByMD5(string cleartext)
        {

            var md5 = MD5.Create();
            var bs = md5.ComputeHash(Encoding.UTF8.GetBytes(cleartext));
            var sb = new StringBuilder();
            foreach (byte b in bs)
            {
                sb.Append(b.ToString("x2"));
            }
            //16位小写
            return sb.ToString();

        }
        public static bool isHttpUrl(string? any) { 
            if(string.IsNullOrEmpty(any))
                return false;
            if(any.StartsWith("https://")||any.StartsWith("http://"))
                return true; 
            return false;
        }
    }

    [Windows.Foundation.Metadata.WebHostHidden]
    public abstract class BindableBase : INotifyPropertyChanged
    {
        /// <summary>
        /// Multicast event for property change notifications.
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary>
        /// Checks if a property already matches a desired value.  Sets the property and
        /// notifies listeners only when necessary.
        /// </summary>
        /// <typeparam name="T">Type of the property.</typeparam>
        /// <param name="storage">Reference to a property with both getter and setter.</param>
        /// <param name="value">Desired value for the property.</param>
        /// <param name="propertyName">Name of the property used to notify listeners.  This
        /// value is optional and can be provided automatically when invoked from compilers that
        /// support CallerMemberName.</param>
        /// <returns>True if the value was changed, false if the existing value matched the
        /// desired value.</returns>
        protected bool SetProperty<T>(ref T storage, T value, [CallerMemberName] String propertyName = null)
        {
            if (object.Equals(storage, value)) return false;

            storage = value;
            OnPropertyChanged(propertyName);
            return true;
        }

        /// <summary>
        /// Notifies listeners that a property value has changed.
        /// </summary>
        /// <param name="propertyName">Name of the property used to notify listeners.  This
        /// value is optional and can be provided automatically when invoked from compilers
        /// that support <see cref="CallerMemberNameAttribute"/>.</param>
        protected void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}






