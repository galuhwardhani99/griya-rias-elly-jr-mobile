package wardhani.galuh.project_griyarias.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import wardhani.galuh.project_griyarias.data.Paket
import wardhani.galuh.project_griyarias.data.RetrofitClient
import wardhani.galuh.project_griyarias.databinding.ActivityKatalogPaketBinding

class KatalogPaketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKatalogPaketBinding
    private lateinit var adapter: PaketClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKatalogPaketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarKatalog)
        binding.toolbarKatalog.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadPaketData()
    }

    private fun setupRecyclerView() {
        adapter = PaketClientAdapter(emptyList()) { paket ->
            hubungiAdminWA(paket)
        }
        binding.rvKatalogPaket.layoutManager = LinearLayoutManager(this)
        binding.rvKatalogPaket.adapter = adapter
    }

    private fun loadPaketData() {
        binding.pbKatalog.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Menggunakan RetrofitClient.api sesuai variabel di file RetrofitClient.kt
                val response = RetrofitClient.api.getPakets()
                binding.pbKatalog.visibility = View.GONE

                if (response.success && response.data.isNotEmpty()) {
                    binding.tvEmptyKatalog.visibility = View.GONE
                    adapter.updateData(response.data)
                } else {
                    binding.tvEmptyKatalog.visibility = View.VISIBLE
                    adapter.updateData(emptyList())
                }
            } catch (e: Exception) {
                binding.pbKatalog.visibility = View.GONE
                Toast.makeText(
                    this@KatalogPaketActivity,
                    "Error koneksi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun hubungiAdminWA(paket: Paket) {
        val nomorWA = "6287850875676"
        val pesan = "Halo Griya Rias ELLY JR, saya berminat dengan paket *${paket.namaPaket}* (${paket.kategori}). Bisakah saya minta informasi lebih lanjut?"
        val url = "https://api.whatsapp.com/send?phone=$nomorWA&text=${Uri.encode(pesan)}"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}