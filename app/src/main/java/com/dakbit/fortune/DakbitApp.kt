package com.dakbit.fortune

import android.app.Application

class DakbitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        NotificationHelper.ensureFcmChannel(this)
        FortuneFirebaseMessagingService.subscribeDailyTopic()
    }
}
