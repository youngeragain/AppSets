using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Controls.Primitives;
using Microsoft.UI.Xaml.Data;
using Microsoft.UI.Xaml.Input;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Navigation;
using Microsoft.UI.Xaml.Media.Animation;
using System.Diagnostics;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace AppSets.pages
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class AppDetailsPage : Page
    {

        public AppDetailsViewModel appDetailsViewModel { get; private set; }
        public AppDetailsPage()
        {
            this.InitializeComponent();
            appDetailsViewModel = new AppDetailsViewModel();
        }

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            try {
                var application  = e.Parameter as models.Application;
                if (application != null)
                {
                    appDetailsViewModel.mApplication = application;
                }
            }catch (Exception ex) { 
                Debug.WriteLine(ex.ToString());
            }
        }

        public static void toThisPage(Frame frame, object args)
        {
            frame.Navigate(typeof(AppDetailsPage), args, new DrillInNavigationTransitionInfo());
        }

        private void Button_Back_Click(object sender, RoutedEventArgs e)
        {
            var pFrame = Parent as Frame;
            pFrame.GoBack();
        }
    }

    public class AppDetailsViewModel{
        public models.Application mApplication { get; set; }
    }
}
