using AA;
using AppSets.models;
using AppSets.server;
using AppSets.usecase;
using Microsoft.UI.Windowing;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Controls.Primitives;
using Microsoft.UI.Xaml.Data;
using Microsoft.UI.Xaml.Input;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Navigation;
using Newtonsoft.Json;
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

namespace AppSets.pages
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class SplashPage : Page
    {
        private SplashViewModel splashViewModel { get; set; }
        public SplashPage()
        {
            this.InitializeComponent();
           splashViewModel = new SplashViewModel(); 
           
        }
        public static void toThisPage(Frame frame, object args)
        {
            frame.Navigate(typeof(SplashPage), args);
        }
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            splashViewModel.init();
            MainPage.toThisPage(splashFrame, null);
        }
    }
    public class SplashViewModel { 
    
        public AppSetsUseCase appSetsUseCase { get; set; }

        public SplashViewModel()
        {

           
            appSetsUseCase = new AppSetsUseCase();
            
        }

        internal void init()
        {
            appSetsUseCase.init();
          
        }
    }
    
}
