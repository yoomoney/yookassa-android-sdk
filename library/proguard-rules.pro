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

-keep class com.threatmetrix.TrustDefender.** { *; }
# Required to suppress warning messages about ThreatMetrix SDK
-dontwarn com.threatmetrix.TrustDefender.**

# ThreatMetrix SDK wants OkHttp to be available like this
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class org.threeten.bp.** { *; }
-keep class ru.yoo.sdk.auth.model.** { *; }
-keep class ru.yoo.sdk.auth.ProcessType** { *; }
-keep class ru.yoo.sdk.kassa.payments.Checkout { *; }
-keep class ru.yoo.sdk.kassa.payments.ui.** { *; }

-dontwarn javax.annotation.Nullable
-dontwarn org.conscrypt.OpenSSLProvider
-dontwarn org.conscrypt.Conscrypt
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
