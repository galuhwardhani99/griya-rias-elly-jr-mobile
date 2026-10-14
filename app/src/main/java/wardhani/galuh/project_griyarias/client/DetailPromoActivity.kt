package wardhani.galuh.project_griyarias.client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import wardhani.galuh.project_griyarias.R

class DetailPromoActivity : AppCompatActivity() {

    private lateinit var toolbarPromo: Toolbar
    private lateinit var tvJudulPromo: TextView
    private lateinit var tvDeskripsiPromo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_promo)

        // 1. Inisialisasi View berdasarkan ID di activity_detail_promo.xml
        toolbarPromo = findViewById(R.id.toolbarPromo)
        tvJudulPromo = findViewById(R.id.tvJudulPromo)
        tvDeskripsiPromo = findViewById(R.id.tvDeskripsiPromo)

        // 2. Setting Tombol Kembali pada Toolbar
        toolbarPromo.setNavigationOnClickListener {
            finish()
        }

        // 3. Tangkap Data dari FCM dan Tampilkan
        prosesDataIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        prosesDataIntent(intent)
    }

    private fun prosesDataIntent(intent: Intent?) {
        val bundle = intent?.extras ?: return

        val title = bundle.getString("title") ?: "Promo Spesial Griya Rias"
        val body = bundle.getString("body") ?: bundle.getString("promo") ?: "Detail promo tidak tersedia."
        val imageUrl = bundle.getString("imageUrl") ?: ""
        val promoUntil = bundle.getString("promoUntil") ?: ""

        // A. Tampilkan ke Halaman Detail (UI Utama activity_detail_promo.xml)
        tvJudulPromo.text = title
        tvDeskripsiPromo.text = if (promoUntil.isNotEmpty()) {
            "$body\n\n📌 Berlaku sampai: $promoUntil"
        } else {
            body
        }

        // B. Tampilkan Pop-up In-App Dialog Kustom Bergambar
        tampilkanInAppPromoDialog(title, body, imageUrl, promoUntil)
    }

    private fun tampilkanInAppPromoDialog(
        title: String,
        body: String,
        imageUrl: String,
        promoUntil: String
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_promo, null)
        val ivPromo = dialogView.findViewById<ImageView>(R.id.ivPromoImage)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvPromoTitle)
        val tvBody = dialogView.findViewById<TextView>(R.id.tvPromoBody)
        val btnKlaim = dialogView.findViewById<Button>(R.id.btnKlaim)

        tvTitle.text = title
        tvBody.text = if (promoUntil.isNotEmpty()) "$body\n\nS&K: $promoUntil" else body

        // Jika ada URL gambar dari payload FCM, tampilkan pakai Glide
        if (imageUrl.isNotEmpty()) {
            ivPromo.visibility = View.VISIBLE
            Glide.with(this)
                .load(imageUrl)
                .into(ivPromo)
        } else {
            ivPromo.visibility = View.GONE
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnKlaim.setOnClickListener {
            Toast.makeText(this, "Promo Berhasil Diklaim!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}