package wardhani.galuh.project_griyarias.service

import android.app.Activity
import android.content.Intent
import wardhani.galuh.project_griyarias.client.DetailPromoActivity

object PromoIntentHelper {

    /** Return true kalau intent berasal dari klik notifikasi FCM / Promo dan sudah diteruskan. */
    fun handleNotificationIntent(activity: Activity, intent: Intent?): Boolean {
        val extras = intent?.extras ?: return false

        // Cek apakah ada indikasi notifikasi FCM atau payload promo
        val isFcmNotification = extras.containsKey("google.message_id")
        val isPromoPayload = extras.containsKey("promo") || extras.containsKey("is_promo") || extras.containsKey("title")

        if (!isFcmNotification && !isPromoPayload) return false

        // Ambil data dengan penanganan fallback key yang lebih fleksibel
        val title = extras.getString("title") ?: "Promo Spesial Griya Rias"
        val body = extras.getString("body") ?: extras.getString("promo") ?: "Klaim promo menarik ini sekarang!"
        val promoText = extras.getString("promo") ?: extras.getString("body") ?: ""
        val imageUrl = extras.getString("imageUrl") ?: extras.getString("image") ?: ""
        val promoUntil = extras.getString("promoUntil") ?: extras.getString("valid_until") ?: ""

        val detailIntent = Intent(activity, DetailPromoActivity::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
            putExtra("promo", promoText)
            putExtra("imageUrl", imageUrl)
            putExtra("promoUntil", promoUntil)
        }

        // Hapus tanda supaya tidak terproses berulang kali saat orientation change
        intent.removeExtra("google.message_id")
        intent.removeExtra("promo")
        intent.removeExtra("is_promo")

        activity.startActivity(detailIntent)
        return true
    }
}