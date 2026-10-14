package wardhani.galuh.project_griyarias.client

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import wardhani.galuh.project_griyarias.R
import wardhani.galuh.project_griyarias.data.RetrofitClient
import wardhani.galuh.project_griyarias.data.SupabaseClient
import java.io.ByteArrayOutputStream
import java.util.UUID

class PembayaranActivity : AppCompatActivity() {

    private lateinit var imgBuktiPreview: ImageView
    private lateinit var btnPilihBukti: Button
    private lateinit var btnKameraBukti: Button
    private lateinit var btnKirimBukti: Button
    private lateinit var progressBar: ProgressBar

    private var imageBytes: ByteArray? = null
    private var idReservasi: Long = 0
    private val db = FirebaseFirestore.getInstance()

    // Launcher untuk Galeri
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val inputStream = contentResolver.openInputStream(uri)
            imageBytes = inputStream?.readBytes()
            inputStream?.close()

            imgBuktiPreview.setImageURI(uri)
            imgBuktiPreview.visibility = View.VISIBLE
            btnKirimBukti.isEnabled = true
        }
    }

    // Launcher untuk Kamera (diperbaiki agar bebas dari warning deprecated)
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val extras = result.data?.extras
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras?.getParcelable("data", Bitmap::class.java)
            } else {
                @Suppress("DEPRECATION")
                extras?.get("data") as? Bitmap
            }

            if (bitmap != null) {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                imageBytes = stream.toByteArray()

                imgBuktiPreview.setImageBitmap(bitmap)
                imgBuktiPreview.visibility = View.VISIBLE
                btnKirimBukti.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pembayaran)

        // 1. Ambil ID Reservasi dari Intent
        idReservasi = intent.getLongExtra("ID_RESERVASI", 0)

        imgBuktiPreview = findViewById(R.id.imgBuktiPreview)
        btnPilihBukti = findViewById(R.id.btnPilihBukti)
        btnKameraBukti = findViewById(R.id.btnKameraBukti)
        btnKirimBukti = findViewById(R.id.btnKirimBukti)
        progressBar = findViewById(R.id.progressBarPembayaran)

        btnPilihBukti.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        btnKameraBukti.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        }

        btnKirimBukti.setOnClickListener {
            uploadBuktiDanUpdateStatus()
        }
    }

    private fun uploadBuktiDanUpdateStatus() {
        val bytes = imageBytes
        if (bytes == null) {
            Toast.makeText(this, "Pilih atau ambil foto bukti transfer terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnKirimBukti.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Fallback: Jika ID Reservasi bernilai 0, ambil ID reservasi terbaru dari API
                if (idReservasi == 0L) {
                    val latestList = RetrofitClient.api.getReservasi()
                    if (latestList.success && latestList.data.isNotEmpty()) {
                        idReservasi = latestList.data.first().idReservasi ?: 0L
                    }
                }

                if (idReservasi == 0L) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        btnKirimBukti.isEnabled = true
                        Toast.makeText(this@PembayaranActivity, "Error: Data reservasi tidak ditemukan di database!", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                // 1. Upload foto ke Supabase Storage
                val fileName = "bukti_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
                val bucket = SupabaseClient.client.storage.from("bukti_pembayaran")

                bucket.upload(fileName, bytes)
                val imageUrl = bucket.publicUrl(fileName)

                // 2. Update status reservasi di API MySQL / Laravel
                val body = mapOf(
                    "status_reservasi" to "Menunggu Konfirmasi Admin",
                    "bukti_transfer" to imageUrl
                )
                val response = RetrofitClient.api.updateStatusReservasi(idReservasi, body)

                // 3. Simpan log pembayaran ke Firestore
                val paymentLog = hashMapOf(
                    "id_reservasi" to idReservasi,
                    "bukti_transfer" to imageUrl,
                    "status_reservasi" to "Menunggu Konfirmasi Admin",
                    "updated_at" to System.currentTimeMillis()
                )

                db.collection("pembayaran").add(paymentLog)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.success) {
                        Toast.makeText(
                            this@PembayaranActivity,
                            "Bukti berhasil dikirim! Status: Menunggu Konfirmasi Admin",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        btnKirimBukti.isEnabled = true
                        Toast.makeText(this@PembayaranActivity, "Gagal update status: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnKirimBukti.isEnabled = true
                    Toast.makeText(this@PembayaranActivity, "Validasi Backend Gagal: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnKirimBukti.isEnabled = true
                    Toast.makeText(this@PembayaranActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}