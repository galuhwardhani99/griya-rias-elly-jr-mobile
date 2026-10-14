package wardhani.galuh.project_griyarias.client

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import wardhani.galuh.project_griyarias.data.RetrofitClient
import wardhani.galuh.project_griyarias.databinding.ActivityRiwayatReservasiBinding

class RiwayatReservasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatReservasiBinding
    private lateinit var adapter: RiwayatReservasiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatReservasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRiwayat)
        binding.toolbarRiwayat.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadRiwayatReservasi()
    }

    private fun setupRecyclerView() {
        adapter = RiwayatReservasiAdapter(emptyList())
        binding.rvRiwayatReservasi.layoutManager = LinearLayoutManager(this)
        binding.rvRiwayatReservasi.adapter = adapter
    }

    private fun loadRiwayatReservasi() {
        binding.pbRiwayat.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getReservasi()
                binding.pbRiwayat.visibility = View.GONE

                val dataList = response.data
                if (response.success && !dataList.isNullOrEmpty()) {
                    binding.tvEmptyState.visibility = View.GONE
                    adapter.updateData(dataList)
                } else {
                    binding.tvEmptyState.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.pbRiwayat.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                Toast.makeText(this@RiwayatReservasiActivity, "Gagal memuat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}