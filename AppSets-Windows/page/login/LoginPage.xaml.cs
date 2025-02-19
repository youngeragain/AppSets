using AA;
using AppSets.models;
using AppSets.page.userinfo;
using AppSets.server;
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
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Security.Cryptography;
using System.Text;
using Windows.Foundation;
using Windows.Foundation.Collections;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace AppSets
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class LoginPage : Page
    {
        private LoginViewModel loginViewModel { get; set; }
        public LoginPage()
        {
            this.InitializeComponent();
            AccountProvider.Instance.mOnUserLoginEvent += OnUserLoginEvent;
            loginViewModel = new LoginViewModel();  
        }


        public void OnUserLoginEvent(bool logged, string? fromType)
        {

            if (logged)
            {
                var pFrame = Parent as Frame;
                UserInfoPage.toThisPage(pFrame, null);
            }
        }


        private async void Login_Click(object sender, RoutedEventArgs e)
        {

            var account = accountTextBox.Text.ToString();
            var password = passwordTextBox.Password;
            loginViewModel.loginByAccountPassword(account, password);

        }
        

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            Debug.WriteLine("LoginPage, OnNavigatedTo");
        }
        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            base.OnNavigatedFrom(e);
            AccountProvider.Instance.mOnUserLoginEvent -= OnUserLoginEvent;

        }
        public static void toThisPage(Frame frame, object args)
        {
            frame.Navigate(typeof(LoginPage), args, new DrillInNavigationTransitionInfo());
        }

    }
    class LoginViewModel { 
        private UserUseCase userUseCase { get; set; }
        public LoginViewModel()
        {
            
            userUseCase = new UserUseCase();
        }
        public void loginByAccountPassword(string account, string password) { 
            userUseCase.loginByAccountPassword(account, password);
        }

        
    }
}
