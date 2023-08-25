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
using AppSets.pages.conversation;
using System.Collections.ObjectModel;
using AppSets.models;
using AA;
using AppSets.usecase;
using System.Diagnostics;
using AppSets.im;
using Windows.UI.Core;
using System.Threading;
// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace AppSets.pages
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class ConversationPage : Page
    {

        public ConversationViewModel conversationViewModel { get; set; }

        public ConversationPage()
        {
            
            this.InitializeComponent();
            Debug.WriteLine("ConversationPage InitializeComponent");
            conversationViewModel = new ConversationViewModel(SynchronizationContext.Current);
        }

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            Debug.WriteLine("ConversationPage OnNavigatedTo");
        }

        public void AppBarButton_Click(object? sender, RoutedEventArgs e) {
            if (sender == null) {
                return;
            }
            try {
                var btn = sender as Button;
                conversationViewModel.updateTabByTag(btn.Tag as String);

                Debug.WriteLine("AppBarButton_Click");
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex);
            }
            
        }
        public static void toThisPage(Frame frame, object args)
        {
            frame.Navigate(typeof(ConversationPage), args, new DrillInNavigationTransitionInfo());
        }

        private void ListView_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            try
            {
                var listView = sender as ListView;
                var selectedSession = listView.SelectedItem as Session;
                conversationViewModel.updateCurrentSession(selectedSession);
                ConversationDetailsPage.toThisPage(conversationFrame, selectedSession);
            }
            catch (Exception)
            {

                throw;
            }
            
        }
    }

    public class ConversationViewModel { 

        public ConversationUseCase mConversationUseCase { get; set; }
        
        public ObservableCollection<Session> currentSessions { get; set; }

        public Session? currentSession { get; set; }

        public string? currentTabTag = "default";//group, system

        
        public ConversationViewModel(SynchronizationContext context)
        {
            currentSessions = new ObservableCollection<Session>();
            mConversationUseCase = new ConversationUseCase(context);
            updateTabByTag("person");
        }



        internal void updateTabByTag(string tabTag)
        {
            if (string.Equals(currentTabTag, tabTag)) {
                return;
            }
            currentTabTag = tabTag;
            if (string.Equals(tabTag, "person"))
            {
                currentSessions.Clear();
                foreach (var item in mConversationUseCase.userSessions)
                {
                    currentSessions.Add(item);
                };
       
            }
            else if (string.Equals(tabTag, "group"))
            {
                currentSessions.Clear();
                foreach (var item in mConversationUseCase.groupSessions)
                {
                    currentSessions.Add(item);
                };
            }
            else {
                currentSessions.Clear();
                foreach (var item in mConversationUseCase.systemSessions)
                {
                    currentSessions.Add(item);
                };
            }
        }

        internal void updateCurrentSession(Session? session)
        {
            currentSession = session;
        }
    }
}
