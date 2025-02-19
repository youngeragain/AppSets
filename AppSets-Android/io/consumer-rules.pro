#-keep class xcj.app.io.**{ *; }
-keep class xcj.app.io.tencent.TencentCosRegionBucket{ *; }
-keep class xcj.app.io.tencent.TencentCosSTS{ *; }
-dontwarn com.tencent.**
-dontwarn org.slf4j.impl.StaticLoggerBinder