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
using AppSets.models;
using System.Diagnostics;
using System.ComponentModel;
using static System.Net.Mime.MediaTypeNames;
using System.Threading;
using Refit;
using AppSets.usecase;
using AppSets.utils;
using AppSets.io;
using FFImageLoading;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

#nullable enable
namespace AppSets.pages
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class AppsPage : Page
    {
        
        public AppsViewModel appsViewModel { get; set; }


        public AppsPage()
        {

            
            this.InitializeComponent();
            Debug.WriteLine("AppsPage InitializeComponent");
            appsViewModel = new AppsViewModel();


        }
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            Debug.WriteLine("AppsPage OnNavigatedTo");
            appsViewModel.loadIndexApplications();
            
        }



        public static void toThisPage(Frame frame, object args)
        {
            frame.Navigate(typeof(AppsPage), args, new DrillInNavigationTransitionInfo());
        }

        public class AppsViewModel: BindableBase
        {
            private models.Application? headerApplication;
            public models.Application? HeaderApplication {
                get {
                    return headerApplication;
                }
                set {
                    SetProperty(ref headerApplication, value);
                }
             }

            public System.Collections.ObjectModel.ObservableCollection<AppsWithCategory> AppsWithCategoryObservable { get; set; }

            private AppSetsUseCase AppSetsUseCase { get; set; }

            public AppsViewModel()
            {
             
                AppSetsUseCase = new AppSetsUseCase();
                AppsWithCategoryObservable = new System.Collections.ObjectModel.ObservableCollection<AppsWithCategory>();
            }

            internal void updateApplications(List<AppsWithCategory> apps)
            {

                if (apps.Count == 0)
                {
                    return;
                }
                
                
                var random = new Random();
                var randomIndex = random.Next(apps.Count - 1);
                var appWithCategory = apps[randomIndex];
                if (appWithCategory.Applications != null && appWithCategory.Applications.Count != 0)
                {
                    randomIndex = random.Next(appWithCategory.Applications.Count - 1); 
                    apps.ForEach(appsWithCategory =>
                    {

                        if (appsWithCategory.Applications != null) {
                            appsWithCategory.Applications.ForEach(application =>
                            {
                                if (!string.IsNullOrEmpty(application.IconUrl) && !Commons.isHttpUrl(application.IconUrl)) {
                                    var generatedUrl = SimpleFileIO.Instance.generatePreSign(application.IconUrl);
                                    if (generatedUrl != null) { 
                                        application.IconUrl = generatedUrl;
                                    }
                                }
                                if (!string.IsNullOrEmpty(application.BannerUrl) && !Commons.isHttpUrl(application.BannerUrl))
                                {
                                    var generatedUrl = SimpleFileIO.Instance.generatePreSign(application.BannerUrl);
                                    if (generatedUrl != null)
                                    {
                                        application.BannerUrl = generatedUrl;
                                    }
                                }
                            });
                        
                        }
                        AppsWithCategoryObservable.Add(appsWithCategory);
                    });
                    var application = appWithCategory.Applications[randomIndex];
                    if (application != null)
                    {
                        if (string.IsNullOrEmpty(application.IconUrl))
                        {
                            application.IconUrl = "https://i.loli.net/2021/05/16/BGC5IMwrSKm72v4.png";
                        }
                        if (string.IsNullOrEmpty(application.BannerUrl))
                        {
                            application.BannerUrl = "https://img1.baidu.com/it/u=1157252718,2208155279&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800";
                        }
                        if (string.IsNullOrEmpty(application.Name))
                        {
                            application.Name = "Application";
                        }
                    }
                    HeaderApplication = application;
                    Debug.WriteLine("onAppFetched");
                }
            }

            internal void loadIndexApplications()
            {
               if(AppsWithCategoryObservable.Count==0)
                    AppSetsUseCase.loadIndexApplications(updateApplications);
            }
        }

        private void GridView_ItemClick(object sender, ItemClickEventArgs e)
        {
            Debug.WriteLine("sender:" + sender);
            try {
                var pFrame = Parent as Frame;
                if (pFrame != null)
                {
                    AppDetailsPage.toThisPage(pFrame, e.ClickedItem);
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"{ex.Message}", ex);
            }
            
            
        }
    }
}
