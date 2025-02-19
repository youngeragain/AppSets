-keepattributes Signature

-keep class xcj.app.appsets.server.**{ *; }
-keep class xcj.app.appsets.purple_module.AndroidInitEventListener{ *; }
-keep class xcj.app.appsets.purple_module.PurpleContextAware{ *; }
-keep class xcj.app.appsets.db.room.entity.**{ *; }
-keep class xcj.app.appsets.im.message.**{ *; }
-keep class xcj.app.appsets.im.model.**{ *; }

-keepclassmembers class * implements android.os.Parcelable {
   public static final android.os.Parcelable$Creator CREATOR;
}

-keep class androidx.media3.ui.PlayerView {
    private final androidx.media3.ui.PlayerControlView controller;
    private final android.view.View bufferingView;
}

-keep class androidx.media3.ui.PlayerControlViewLayoutManager {
      private final android.view.View timeBar;
      private final android.animation.AnimatorSet showAllBarsAnimator;
      private final android.animation.AnimatorSet hideAllBarsAnimator;
      private final android.animation.AnimatorSet hideProgressBarAnimator;

}

-keep class androidx.media3.ui.PlayerControlView {
     private final androidx.media3.ui.TimeBar timeBar;
     private final androidx.media3.ui.PlayerControlViewLayoutManager controlViewLayoutManager;
}