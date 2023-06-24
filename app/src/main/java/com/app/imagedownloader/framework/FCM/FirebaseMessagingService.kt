package com.app.imagedownloader.framework.FCM

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.imagedownloader.R
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity
import com.app.imagedownloader.Utils.Constants.Constants
import com.app.imagedownloader.framework.Utils.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingServicee() : FirebaseMessagingService() {

    lateinit var notificationManager: NotificationManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // ...


        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Logger.log("Debug FCM From: ${remoteMessage.from}")
        Logger.log("Debug FCM data: ${remoteMessage.data}")
        Logger.log("Debug FCM notifi: ${remoteMessage.notification}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {

            Logger.log("Debug FCM Message data payload: ${remoteMessage.data}")

            val packageName = remoteMessage.data.get("package")
            if (packageName != "com.app.imagedownloader") return

            val topic = remoteMessage.data.get("topic")

            if (topic == Constants.FCM_COMMON_TOPIC) {
                val title = remoteMessage.data.get("title")
                val content = remoteMessage.data.get("content")
                val bigtext = remoteMessage.data.get("bigtext")
                val imageurl = remoteMessage.data.get("imageurl")
                val bigimageurl = remoteMessage.data.get("bigimageurl")

                var bigIconBitmap: Bitmap? = null
                var largeImageBitmap: Bitmap? = null
                notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createchannel(notificationManager)
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val pi = PendingIntent.getActivities(
                    this,
                    210,
                    arrayOf(intent),
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val notification =
                    NotificationCompat.Builder(applicationContext, Constants.FCM_NOTIFICATION_ID)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pi)

                if (bigtext?.isNotEmpty() == true) {
                    notification.setStyle(NotificationCompat.BigTextStyle().bigText(bigtext))
                }
                if (imageurl?.isNotEmpty() == true) {
                    try {
                        val url = URL(imageurl).openConnection() as HttpURLConnection
                        bigIconBitmap = BitmapFactory.decodeStream(url.getInputStream())
                        url.disconnect()
                    } catch (e: Exception) {

                    }
                }

                if (bigimageurl?.isNotEmpty() == true) {
                    try {
                        val url = URL(bigimageurl).openConnection() as HttpURLConnection
                        url.disconnect()
                        largeImageBitmap = BitmapFactory.decodeStream(url.getInputStream())
                    } catch (e: Exception) {

                    }
                }
                bigIconBitmap?.let {
                    notification.setLargeIcon(it)
                }
                largeImageBitmap?.let {
                    notification.setStyle(
                        NotificationCompat.BigPictureStyle().bigPicture(it)
                            .setBigContentTitle(bigtext).setSummaryText(bigtext)
                    )
                }
                notificationManager.notify(
                    Constants.FCM_NOTIFICATION_ID.toInt(),
                    notification.build()
                )
            }

//            if (/* Check if data needs to be processed by long running job */ true) {
            // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob()
//            } else {
            // Handle message within 10 seconds
//                handleNow()
//            }
        }

        // Check if message contains a notification payload.
//        remoteMessage.notification?.let {
//             Logger.log("Debug FCM Message Notification Body: ${it.body}")
//        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }


    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Logger.log("Debug FCM Refreshed token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
//        sendRegistrationToServer(token)
    }


    private fun createchannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.FCM_NOTIFICATION_ID, Constants.FCM_NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }
}