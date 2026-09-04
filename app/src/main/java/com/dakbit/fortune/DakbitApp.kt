package com.dakbit.fortune

import android.app.Application
import com.google.android.gms.ads.MobileAds

class DakbitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        NotificationHelper.ensureFcmChannel(this)
        MobileAds.initialize(this)
        FortuneFirebaseMessagingService.subscribeDailyTopic()
    }
}
