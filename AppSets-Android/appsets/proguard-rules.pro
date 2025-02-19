# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
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