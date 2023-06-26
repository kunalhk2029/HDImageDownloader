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
        }
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