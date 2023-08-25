using AA;
using AppSets.models;
using AppSets.page.userinfo;
using AppSets.pages;
using AppSets.pages.settings;
using AppSets.utils;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Controls.Primitives;
using Microsoft.UI.Xaml.Data;
using Microsoft.UI.Xaml.Input;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Navigation;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.ApplicationSettings;
using static AA.AccountProvider;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace AppSets
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {


        public MainPageViewModel mainPageViewModel { get; set; }
        public MainPage()
        {
            this.InitializeComponent();
            mainPageViewModel = new MainPageViewModel();
        }

        /*
         * 
         * ×ó±ßtabÇÐ»»»Øµ÷
         */
        private void OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            var listView = sender as ListView;
            if (listView.SelectedItem == null) {
                return;
            }
            var itemName = (listView.SelectedItem as ListViewItem).Name;
            if (string.Equals(itemName, "itemStart"))
            {
                if (MainPageFrame.CurrentSourcePageType != typeof(SpotLightPage))
                {
                    listViewBottom.SelectedIndex = -1;
                    SpotLightPage.toThisPage(MainPageFrame, null);
                }
            } else if (string.Equals(itemName, "itemApps"))
            {
                if (MainPageFrame.CurrentSourcePageType != typeof(AppsPage))
                {
                    listViewBottom.SelectedIndex = -1;
                    AppsPage.toThisPage(MainPageFrame, null);
                }
            }
            else if (string.Equals(itemName, "itemOutside"))
            {
                if (MainPageFrame.CurrentSourcePageType != typeof(OutSidePage))
                {
                    listViewBottom.SelectedIndex = -1;
                    OutSidePage.toThisPage(MainPageFrame, null);
                }
            }
            else if (string.Equals(itemName, "itemConversation"))
            {
                if (AccountProvider.Instance.isLogged())
                {
                    if (MainPageFrame.CurrentSourcePageType != typeof(ConversationPage))
                    {
                        listViewBottom.SelectedIndex = -1;
                        ConversationPage.toThisPage(MainPageFrame, null);
                    }
                }
                else {
                    listViewTop.SelectedIndex = -1;
                    listViewBottom.SelectedIndex = -1;
                    if (MainPageFrame.CurrentSourcePageType != typeof(LoginPage))
                    {       
                        LoginPage.toThisPage(MainPageFrame, null);
                    }                   
                }
                
            }
            else if (string.Equals(itemName, "itemSettings"))
            {
                if (MainPageFrame.CurrentSourcePageType != typeof(SettingsPage))
                {
                    listViewTop.SelectedIndex = -1;
                    SettingsPage.toThisPage(MainPageFrame, null);
                    //PlatformAppsInflater.Main1(null);

                }
            } else if (string.Equals(itemName, "itemLogin"))
            {
                if (AccountProvider.Instance.isLogged())
                {
                    if (MainPageFrame.CurrentSourcePageType != typeof(UserInfoPage))
                    {
                        listViewTop.SelectedIndex = -1;
                        UserInfoPage.toThisPage(MainPageFrame, null);
                    }
                }
                else {
                    listViewTop.SelectedIndex = -1;
                    listViewBottom.SelectedIndex = -1;
                    if (MainPageFrame.CurrentSourcePageType != typeof(LoginPage))
                    {         
                        LoginPage.toThisPage(MainPageFrame, null);
                    }
                }
                
            }
        }

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            Debug.WriteLine("MainPage onNavigatedTo");
            var selectedPosition = listViewTop.SelectedIndex;
            if (selectedPosition == -1)
            {
                listViewTop.SelectedIndex = 0;
            }
        }

        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            base.OnNavigatedFrom(e);
            mainPageViewModel.onClear();
            
        }


        public static void toThisPage(Frame frame, object args)
        {
            frame.Navigate(typeof(MainPage), args);
        }
    }
    public class MainPageViewModel : BindableBase {
        private UserInfo? activeUserInfo = null;
        public UserInfo ActiveUserInfo {
            get {
                return activeUserInfo;
            }
            set {
                SetProperty(ref activeUserInfo, value);
            }
        }
        
        public MainPageViewModel(){
            if (AccountProvider.Instance.UserInfo == null)
            {
                
                activeUserInfo = UserInfo.empty();
            }
            else {
                activeUserInfo = AccountProvider.Instance.UserInfo;
            }
           
            AccountProvider.Instance.mOnUserLoginEvent += OnUserLoginEvent;
        }
        public void OnUserLoginEvent(bool logged, string? fromType) {

            if (logged)
            {
                ActiveUserInfo = AccountProvider.Instance.UserInfo;
            }
            else {
                ActiveUserInfo = UserInfo.empty();
            }
            
        }

        internal void onClear()
        {
            AccountProvider.Instance.mOnUserLoginEvent -= OnUserLoginEvent;
        }
    }
}
