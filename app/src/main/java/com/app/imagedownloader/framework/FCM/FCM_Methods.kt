package com.app.imagedownloader.framework.FCM

import android.content.Context
import android.widget.Toast
import com.app.imagedownloader.framework.Utils.Logger
import com.google.firebase.messaging.FirebaseMessaging

object FCM_Methods {
    fun retreiveCurrentRegisterationToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Logger.log("Fetching FCM registration token failed = " + task.exception)
                return@addOnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            Logger.log("")
        }
    }


    fun unsubscribeToTopic(topic: String, baseContext: Context) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "UnSubscribed to $topic", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                Toast.makeText(baseContext, "Failed to Unsubscribing to $topic", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun subscribeToTopic(
        topic: String,
        baseContext: Context,
        set_FCM_COMMON_SUBSCRIBED: (boolean: Boolean) -> Unit
    ) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    set_FCM_COMMON_SUBSCRIBED(true)
                    return@addOnCompleteListener
                }
            }
    }
}