# Add project specific ProGuard rules here.
-keep class com.flipcover.widgets.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
