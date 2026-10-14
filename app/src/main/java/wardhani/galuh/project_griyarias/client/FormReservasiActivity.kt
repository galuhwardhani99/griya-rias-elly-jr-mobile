package wardhani.galuh.project_griyarias.client

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import wardhani.galuh.project_griyarias.data.Reservasi
import wardhani.galuh.project_griyarias.data.RetrofitClient
import wardhani.galuh.project_griyarias.databinding.ActivityFormReservasiBinding
import java.util.Calendar
import android.content.Intent

class FormReservasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormReservasiBinding
    private var paketId: Long = -1
    private var namaPaket: String = ""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormReservasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarReservasi)
        binding.toolbarReservasi.setNavigationOnClickListener { finish() }

        paketId = intent.getLongExtra("EXTRA_PAKET_ID", -1)
        namaPaket = intent.getStringExtra("EXTRA_NAMA_PAKET") ?: ""

        binding.tvNamaPaketReservasi.text = "Paket: $namaPaket"

        setupDatePicker()
        setupTimePicker()
        setupSpinnerSesi()

        binding.btnKirimReservasi.setOnClickListener {
            prosesReservasi()
        }
    }

    private fun setupDatePicker() {
        binding.etTanggalAcara.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etTanggalAcara.setText(formattedDate)
            }, year, month, day)

            datePicker.show()
        }
    }

    private fun setupTimePicker() {
        binding.etJamMulai.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val formattedTime = String.format("%02d:%02d WIB", selectedHour, selectedMinute)
                    binding.etJamMulai.setText(formattedTime)
                },
                hour,
                minute,
                true
            )

            timePicker.show()
        }
    }

    private fun setupSpinnerSesi() {
        val listSesiWaktu = arrayOf(
            "Pilih Sesi Waktu",
            "Pagi (08:00 - 12:00 WIB)",
            "Siang (13:00 - 17:00 WIB)",
            "Malam (18:00 - 22:00 WIB)",
            "Full Day (08:00 - 21:00 WIB)"
        )

        val adapterSesi = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listSesiWaktu
        )
        binding.spinnerSesiWaktu.adapter = adapterSesi
    }

    private fun prosesReservasi() {
        val nama = binding.etNamaClient.text.toString().trim()
        val hp = binding.etNomorHp.text.toString().trim()
        val tgl = binding.etTanggalAcara.text.toString().trim()
        val jamMulai = binding.etJamMulai.text.toString().trim()
        val lokasi = binding.etLokasi.text.toString().trim()
        val catatanInput = binding.etCatatan.text.toString().trim()
        val sesiTerpilih = binding.spinnerSesiWaktu.selectedItem.toString()

        if (nama.isEmpty() || hp.isEmpty() || tgl.isEmpty() || jamMulai.isEmpty() || lokasi.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua field utama!", Toast.LENGTH_SHORT).show()
            return
        }

        if (sesiTerpilih == "Pilih Sesi Waktu") {
            Toast.makeText(this, "Harap pilih sesi waktu acara!", Toast.LENGTH_SHORT).show()
            return
        }

        val catatanLengkap = "Sesi: $sesiTerpilih | Jam Acara Dimulai: $jamMulai | Catatan: $catatanInput"

        binding.pbReservasi.visibility = View.VISIBLE
        binding.btnKirimReservasi.isEnabled = false

        val request = Reservasi(
            namaClient = nama,
            nomorHp = hp,
            tanggalAcara = tgl,
            lokasi = lokasi,
            catatan = catatanLengkap
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.createReservasi(request)

                if (response.success && response.data != null) {
                    // 1. Ambil ID Reservasi asli hasil generate dari database MySQL / Laravel
                    val newIdReservasi = response.data.idReservasi ?: 0L

                    // 2. Simpan data ke Firestore (sertakan id_reservasi dari MySQL)
                    val firestoreData = hashMapOf(
                        "id_reservasi" to newIdReservasi,
                        "nama_client" to nama,
                        "nomor_hp" to hp,
                        "tanggal_acara" to tgl,
                        "lokasi" to lokasi,
                        "paket_layanan" to namaPaket,
                        "catatan" to catatanLengkap,
                        "status_reservasi" to "Pending",
                        "created_at" to System.currentTimeMillis()
                    )

                    db.collection("reservasi")
                        .add(firestoreData)
                        .addOnSuccessListener {
                            binding.pbReservasi.visibility = View.GONE
                            binding.btnKirimReservasi.isEnabled = true
                            Toast.makeText(this@FormReservasiActivity, "Reservasi Berhasil!", Toast.LENGTH_SHORT).show()

                            // 3. Pindah langsung ke PembayaranActivity membawa ID_RESERVASI
                            val intent = Intent(this@FormReservasiActivity, PembayaranActivity::class.java)
                            intent.putExtra("ID_RESERVASI", newIdReservasi)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            binding.pbReservasi.visibility = View.GONE
                            binding.btnKirimReservasi.isEnabled = true
                            Toast.makeText(this@FormReservasiActivity, "Gagal Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    binding.pbReservasi.visibility = View.GONE
                    binding.btnKirimReservasi.isEnabled = true
                    Toast.makeText(this@FormReservasiActivity, response.message ?: "Gagal membuat reservasi", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.pbReservasi.visibility = View.GONE
                binding.btnKirimReservasi.isEnabled = true
                Toast.makeText(this@FormReservasiActivity, "Error koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}