package com.dakbit.fortune

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FortuneFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        subscribeDailyTopic()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "달빛 운세"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: return

        NotificationHelper.showFcmFortune(applicationContext, title, body)
    }

    companion object {
        fun subscribeDailyTopic() {
            FirebaseMessaging.getInstance()
                .subscribeToTopic(FcmConfig.TOPIC_DAILY_FORTUNE)
        }
    }
}
