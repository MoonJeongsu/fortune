# Cauly SDK
-keep class com.fsn.cauly.** {
    public *;
    protected *;
}
-keep class com.trid.tridad.** {
    public *;
    protected *;
}
-dontwarn android.webkit.**
-dontwarn com.fsn.cauly.**

-keep class com.bytedance.sdk.** { *; }
-keep class com.pangle.global.** { *; }
-keep class com.unity3d.ads.** { *; }
-dontwarn com.bytedance.sdk.**
-dontwarn com.pangle.global.**
-dontwarn com.unity3d.ads.**

# Keep Parcelable / Serializable used by system intents
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Kotlin / Compose
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }

# App receivers / entry points (already kept by default, reinforce)
-keep class com.dakbit.fortune.FortuneAlarmReceiver { *; }
-keep class com.dakbit.fortune.BootReceiver { *; }
-keep class com.dakbit.fortune.DakbitApp { *; }
-keep class com.dakbit.fortune.MainActivity { *; }
-keep class com.dakbit.fortune.FortuneFirebaseMessagingService { *; }

# Firebase Messaging
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
