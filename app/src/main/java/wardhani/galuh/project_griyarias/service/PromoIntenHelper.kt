package wardhani.galuh.project_griyarias.service

import android.app.Activity
import android.content.Intent
import wardhani.galuh.project_griyarias.client.DetailPromoActivity

object PromoIntentHelper {

    /** Return true kalau intent berasal dari klik notifikasi FCM dan sudah diteruskan. */
    fun handleNotificationIntent(activity: Activity, intent: Intent?): Boolean {
        val extras = intent?.extras ?: return false
        if (!extras.containsKey("google.message_id")) return false

        val detail = Intent(activity, DetailPromoActivity::class.java).apply {
            putExtra("title", extras.getString("title") ?: "Promo Griya Rias")
            putExtra("body", extras.getString("body") ?: extras.getString("promo") ?: "")
            putExtra("promo", extras.getString("promo") ?: extras.getString("body") ?: "")
            putExtra("imageUrl", extras.getString("imageUrl") ?: "")
            putExtra("promoUntil", extras.getString("promoUntil") ?: "")
        }
        // Hapus tanda FCM supaya tidak terproses dua kali
        intent.removeExtra("google.message_id")
        activity.startActivity(detail)
        return true
    }
}