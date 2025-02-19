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
using AppSets.server;
using AA;
using AppSets.usecase;
using AppSets.models;
using Newtonsoft.Json;
using AppSets.utils;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace AppSets.pages
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class SpotLightPage : Page
    {

        public SpotLightViewModel spotLightViewModel { get; set; }
        public SpotLightPage()
        {
            this.InitializeComponent();
            Debug.WriteLine("SpotLightPage:InitializeComponent");
            spotLightViewModel = new SpotLightViewModel();
        }
        ~SpotLightPage()
        {
            spotLightViewModel.onClear();
        }

        public static void toThisPage(Frame frame, object args)
        {
            frame.Navigate(typeof(SpotLightPage), args, new DrillInNavigationTransitionInfo());
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {

        }

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            Debug.WriteLine("SpotLightPage:OnNavigatedTo");
            
        }

        public class SpotLightViewModel:BindableBase{

            private SpotLightUIState? spotLightUIState;

            public SpotLightUIState? SpotLightUIState {
                get=>spotLightUIState; 
                set{
                    SetProperty(ref spotLightUIState, value);
                } 
            }

            public SpotLightViewModel()
            {
                AccountProvider.Instance.mOnAppTokenGottenEvent += OnAppTokenGotten;
            }

            private void OnAppTokenGotten()
            {
                if (SpotLightUIState != null)
                    return;
                Debug.WriteLine("SpotLightViewModel, OnAppTokenGotten, loadSpotLight");
                var appSetsUseCase = new AppSetsUseCase();
                appSetsUseCase.loadSpotLight(onSpotLightLoad);
            }

            public void onSpotLightLoad(SpotLight spotLight) { 
                
                SpotLightUIState = new SpotLightUIState(spotLight);
                
            }

            internal void onClear()
            {
                AccountProvider.Instance.mOnAppTokenGottenEvent -= OnAppTokenGotten;
            }
        }
        #nullable enable
        public class SpotLightUIState {

            public string todayDataString;

            public string? bingWallpaperUrl;
            public string? bingWallpaperWhere;
            public string? bingWallpaperWhereBelowText;

            public string? wordOfTheDayContent;
            public string? wordOfTheDayContentAuthor;

            public string? todayInHistoryPicUrl;
            public string? todayInHistoryTitle;
            public string? todayInHistoryEvent;

            public string popularSearchTitle="热门搜索";
            public List<string>? popularSearchKeywords;

            public string baiduHotDataTitle = "百度热搜";
            public List<Hotsearch>? baiduHotDataHotSearch;



            public SpotLightUIState(SpotLight spotLight)
            {
                try
                {
                    var dateTime = DateTime.Now;
                    todayDataString = string.Format("今天 • {0}月{1}日", dateTime.Month, dateTime.Day);
                    if (spotLight.BingWallpaperJson != null) {
                        var bingImage = spotLight.BingWallpaperJson.Images[0];
                        bingWallpaperUrl = "https://www.bing.com" + bingImage.Url;
                        bingWallpaperWhere = bingImage.Copyright;
                        bingWallpaperWhereBelowText = bingImage.Title;
                    }

                    if (spotLight.WordOfTheDay != null) {
                        wordOfTheDayContent = spotLight.WordOfTheDay.Word;
                        wordOfTheDayContentAuthor = spotLight.WordOfTheDay.Author;
                    }

                    if (spotLight.TodayInHistory != null) {
                        todayInHistoryPicUrl = spotLight.TodayInHistory.PicUrl;
                        todayInHistoryTitle = spotLight.TodayInHistory.Title;
                        todayInHistoryEvent = spotLight.TodayInHistory.Event;
                    }
                  
                    if (spotLight.PopularSearches != null) {
                        popularSearchKeywords = spotLight.PopularSearches.Keywords;
                    }

                    if (spotLight.BaiduHotData != null) { 
                   
                        baiduHotDataHotSearch = spotLight.BaiduHotData.Hotsearch;
                    }
                    
                }
                catch (Exception ex)
                {

                    Debug.WriteLine("解析SpotLightUIState异常："+ex);
                }
                
            }
        }
    }
}
