using AA;
using AA.rabbit;
using AppSets.im;
using AppSets.models;
using AppSets.utils;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Controls.Primitives;
using Microsoft.UI.Xaml.Data;
using Microsoft.UI.Xaml.Input;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Media.Animation;
using Microsoft.UI.Xaml.Navigation;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace AppSets.pages.conversation
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class ConversationDetailsPage : Page
    {

        public ConversationDetailsViewModel conversationDetailsViewModel { get; set; }
        public ConversationDetailsPage()
        {
            this.InitializeComponent();
            conversationDetailsViewModel = new ConversationDetailsViewModel();
        }

        public static void toThisPage(Frame frame, object args)
        {
            var transitionInfo = new SlideNavigationTransitionInfo() { Effect = SlideNavigationTransitionEffect.FromRight };
            frame.Navigate(typeof(ConversationDetailsPage), args, transitionInfo);
        }

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            try
            {
                var count = 0;
                var p = Frame.Parent;
                do {
                    count++;
                    if (p is ConversationPage) {
                        var parentPage = p as ConversationPage;
                        var parentCurrentSession = parentPage.conversationViewModel.currentSession;
                        conversationDetailsViewModel.mCurrentSession = parentCurrentSession;
                        break;
                    }
                    try { p = (p as FrameworkElement).Parent; } catch { }
                    if (count > 6)
                        break;
                } while (true);
                //((Frame.Parent as Grid).Parent as Grid).Parent as ConversationPage;
            }
            catch (Exception)
            {

                throw;
            }
            
        }

        private void Send_Click(object sender, RoutedEventArgs e)
        {
           
            conversationDetailsViewModel.onSendMessage();
        }
    }
    public class ConversationDetailsViewModel:BindableBase{ 
           public InputSelector inputSelector = InputSelector.TEXT;
           public Session mCurrentSession { get; set; }
           private string userInputString = "";
           public string UserInputString { 
                get { 
                    return userInputString; 
                }
                set { 
                    SetProperty(ref userInputString, value);
                }
            }

        internal void onSendMessage()
        {
            var imObj = mCurrentSession.imObj;
            var userInfo = AccountProvider.Instance.UserInfo;
            Guid guid = Guid.NewGuid();
            string uuid = guid.ToString();
            ImMessage? imMessage = null;
            if (inputSelector == InputSelector.TEXT) {
                
                var messageFromInfo = new MessageFromInfo() { 
                    id = userInfo.Uid, name = userInfo.Name, avatarUrl = userInfo.AvatarUrl, roles = userInfo.Roles
                };
                var messageToInfo = ImObj.toToInfo(imObj);
                imMessage = new ImText(uuid, UserInputString, messageFromInfo, DateTime.Now, messageToInfo, null);
            }
            if (imMessage != null) {
                RabbitManager.sendMessage(imObj, imMessage);

            }
            if (inputSelector == InputSelector.TEXT) {
                UserInputString = "";
            }
        }
    }

    public enum InputSelector {
        NONE,
        TEXT,
        IMAGE,
        VIDEO,
        MUSIC,
        VOICE,
        LOCATION,
        HTML,
        AD,
        FILE
    }
}
