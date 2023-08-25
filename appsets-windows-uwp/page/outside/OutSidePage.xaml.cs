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
using AppSets.usecase;
using static AppSets.usecase.ScreenUseCase;
using System.Diagnostics;
using AppSets.io;
using AppSets.utils;
using Windows.Media.Playback;
using Windows.Media.Core;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace AppSets.pages
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class OutSidePage : Page
    {
        public OutSideViewModel outSideViewModel { get; set; }
        public OutSidePage()
        {
            this.InitializeComponent();
            Debug.WriteLine("OutSidePage:InitializeComponent");
            outSideViewModel = new OutSideViewModel();
        }

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            Debug.WriteLine("OutSidePage:OnNavigatedTo");
            outSideViewModel.loadIndexScreens();
            
        }

        

        public static void toThisPage(Frame frame, object args)
        {
            frame.Navigate(typeof(OutSidePage), args, new DrillInNavigationTransitionInfo());
        }

        private void ScreenContent_Click(object sender, RoutedEventArgs e)
        {
            splitView.IsPaneOpen = !splitView.IsPaneOpen;
        }

        private void ScreenVideoPlay_Click(object sender, RoutedEventArgs e)
        {
           
            var userScreenInfo = (((sender as Button).Parent as StackPanel).DataContext) as UserScreenInfo;
            outSideViewModel.updatePlayVideo(userScreenInfo.VideoMediaFileUrls[0]);
            mediaPlayerElement.AutoPlay = true;
        }
    }

    public class OutSideViewModel :BindableBase{
        public System.Collections.ObjectModel.ObservableCollection<UserScreenInfo> userScreenInfosObservable { get; set; }
        public ScreenUseCase ScreenUseCase { get; set; }

        public Visibility videoPlaybackViewVisibility = Visibility.Collapsed;

        public Visibility VideoPlaybackViewVisibility {
            get {
                return videoPlaybackViewVisibility;
            }
            set { 
                SetProperty(ref videoPlaybackViewVisibility, value);
            }
        }

        public IMediaPlaybackSource? toPlayScreenVideoUrl = null;

        public IMediaPlaybackSource ToPlayScreenVideoUrl
        {
            get
            {
                return toPlayScreenVideoUrl;
            }
            set
            {
                SetProperty(ref toPlayScreenVideoUrl, value);
            }
        }
        public OutSideViewModel() {
            userScreenInfosObservable = new System.Collections.ObjectModel.ObservableCollection<UserScreenInfo>();
            ScreenUseCase = new ScreenUseCase();
        }

        internal void loadIndexScreens()
        {
            if (userScreenInfosObservable.Count == 0)
            {

                ScreenUseCase.loadIndexScreens(onScreensFetched);
            }
        }
        public void onScreensFetched(List<UserScreenInfo> userScreenInfos)
        {
            userScreenInfos.ForEach(userScreenInfo => {
                if (!string.IsNullOrEmpty(userScreenInfo.UserInfo.AvatarUrl)&&!Commons.isHttpUrl(userScreenInfo.UserInfo.AvatarUrl)){
                    var generatedUrl = SimpleFileIO.Instance.generatePreSign(userScreenInfo.UserInfo.AvatarUrl);
                    if (generatedUrl != null)
                    {
                        userScreenInfo.UserInfo.AvatarUrl = generatedUrl;
                    }
                }
                if (userScreenInfo.MediaFileUrls != null) {
                    userScreenInfo.MediaFileUrls.ForEach(fileUrl => {
                        if (fileUrl.mediaType == "image/*") {
                            if (!string.IsNullOrEmpty(fileUrl.mediaFileUrl) &&!Commons.isHttpUrl(fileUrl.mediaFileUrl))
                            {
                                var generatedUrl = SimpleFileIO.Instance.generatePreSign(fileUrl.mediaFileUrl);
                                if (generatedUrl != null)
                                {
                                    fileUrl.mediaFileUrl = generatedUrl;
                                }
                                
                            }
                        }
                        if (fileUrl.mediaType == "video/*") {
                            if (!string.IsNullOrEmpty(fileUrl.mediaFileUrl) && !Commons.isHttpUrl(fileUrl.mediaFileUrl))
                            {
                                var generatedUrl = SimpleFileIO.Instance.generatePreSign(fileUrl.mediaFileUrl);
                                if (generatedUrl != null)
                                {
                                    fileUrl.mediaFileUrl = generatedUrl;
                                }

                            }
                            if (!string.IsNullOrEmpty(fileUrl.mediaFileCompanionUrl) && !Commons.isHttpUrl(fileUrl.mediaFileCompanionUrl))
                            {
                                var generatedUrl = SimpleFileIO.Instance.generatePreSign(fileUrl.mediaFileCompanionUrl);
                                if (generatedUrl != null) {
                                    fileUrl.mediaFileCompanionUrl = generatedUrl;
                                }
                                
                            }
                        }
                    });
                }
                
                
                userScreenInfosObservable.Add(userScreenInfo);
            });
        }

        internal void updatePlayVideo(ScreenMediaFileUrl screenMediaFileUrl)
        {
            VideoPlaybackViewVisibility = Visibility.Visible;
            var uri = new Uri(screenMediaFileUrl.mediaFileUrl);
            var mediaSource = MediaSource.CreateFromUri(uri);

            ToPlayScreenVideoUrl = mediaSource;
            
        }
    }
}
