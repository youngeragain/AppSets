using AA;
using AppSets.models;
using AppSets.pages;
using AppSets.usecase;
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
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace AppSets.page.userinfo
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class UserInfoPage : Page
    {

        public UserInfoViewModel userInfoViewModel { get; set; }
        public UserInfoPage()
        {
            this.InitializeComponent();
            AccountProvider.Instance.mOnUserLoginEvent += OnUserLoginEvent;
            userInfoViewModel = new UserInfoViewModel();
        }

        private void OnUserLoginEvent(bool logged, string fromType)
        {
            if (!logged) {
                var pFrame = Parent as Frame;
                LoginPage.toThisPage(pFrame, null);
            }
        }

        public static void toThisPage(Frame frame, object args)
        {
            frame.Navigate(typeof(UserInfoPage), args, new DrillInNavigationTransitionInfo());
        }

        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            base.OnNavigatedFrom(e);
            AccountProvider.Instance.mOnUserLoginEvent -= OnUserLoginEvent;
        }

        private void Logout_Click(object sender, RoutedEventArgs e)
        {
            userInfoViewModel.logOut();
        }
    }

    public class UserInfoViewModel {
        public void logOut() {
            var userUseCase = new UserUseCase();
            userUseCase.logout();
        }
    }
}
