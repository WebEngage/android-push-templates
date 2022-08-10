package com.webengage.pushtemplates

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.webengage.sdk.android.WebEngage

class FireBaseService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        WebEngage.get().receive(message.data)

    }

    override fun onNewToken(token: String) {
        WebEngage.get().setRegistrationID(token)
        super.onNewToken(token)
    }
}