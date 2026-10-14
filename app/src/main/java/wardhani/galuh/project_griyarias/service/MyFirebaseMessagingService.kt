package wardhani.galuh.project_griyarias.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import wardhani.galuh.project_griyarias.client.DetailPromoActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "griya_promo_v2"
        const val TOPIC = "GRIYA_RIAS_PROMO_V2"
        const val OLD_TOPIC = "INFO_PAKET_GRIYA"
        private const val TAG = "FCM_SERVICE"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token baru: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Pesan diterima. data=${message.data}, notif=${message.notification?.title}")

        val title = message.notification?.title
            ?: message.data["title"] ?: "Promo Griya Rias"
        val body = message.notification?.body
            ?: message.data["body"] ?: message.data["promo"] ?: "Ada penawaran menarik!"
        val imageUrl = message.data["imageUrl"] ?: ""
        val promoUntil = message.data["promoUntil"] ?: ""

        val intent = Intent(this, DetailPromoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("title", title)
            putExtra("body", body)
            putExtra("promo", body)
            putExtra("imageUrl", imageUrl)
            putExtra("promoUntil", promoUntil)
        }

        sendNotification(title, body, intent)
    }

    private fun sendNotification(title: String, messageBody: String, intent: Intent) {
        val notificationId = System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Informasi Paket & Promo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi promo dan paket Griya Rias"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        manager.notify(notificationId, builder.build())
    }
}