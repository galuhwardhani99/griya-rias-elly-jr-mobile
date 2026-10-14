package wardhani.galuh.project_griyarias.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
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

    private var listPaketFull: List<Paket> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKatalogPaketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarKatalog)
        binding.toolbarKatalog.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupSearchAutoComplete()
        loadPaketData()
    }

    private fun setupRecyclerView() {
        adapter = PaketClientAdapter(emptyList()) { paket ->
            panggilFormReservasi(paket)
        }
        binding.rvKatalogPaket.layoutManager = LinearLayoutManager(this)
        binding.rvKatalogPaket.adapter = adapter
    }

    private fun setupSearchAutoComplete() {
        binding.actvSearchKatalog.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterKatalog(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.actvSearchKatalog.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String
            filterKatalog(selectedItem)
        }
    }

    private fun filterKatalog(query: String) {
        val filteredList = if (query.trim().isEmpty()) {
            listPaketFull
        } else {
            listPaketFull.filter { paket ->
                paket.namaPaket.contains(query, ignoreCase = true) ||
                        paket.kategori.contains(query, ignoreCase = true)
            }
        }

        if (filteredList.isEmpty()) {
            binding.tvEmptyKatalog.visibility = View.VISIBLE
            binding.tvEmptyKatalog.text = "Paket \"$query\" tidak ditemukan"
        } else {
            binding.tvEmptyKatalog.visibility = View.GONE
        }

        adapter.updateData(filteredList)
    }

    private fun loadPaketData() {
        binding.pbKatalog.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getPakets()
                binding.pbKatalog.visibility = View.GONE

                if (response.success && response.data.isNotEmpty()) {
                    binding.tvEmptyKatalog.visibility = View.GONE

                    listPaketFull = response.data
                    adapter.updateData(listPaketFull)

                    val searchSuggestions = listPaketFull.map { it.namaPaket }.distinct()
                    val autoCompleteAdapter = ArrayAdapter(
                        this@KatalogPaketActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        searchSuggestions
                    )
                    binding.actvSearchKatalog.setAdapter(autoCompleteAdapter)

                } else {
                    binding.tvEmptyKatalog.visibility = View.VISIBLE
                    binding.tvEmptyKatalog.text = "Belum ada paket layanan tersedia"
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

    private fun panggilFormReservasi(paket: Paket) {
        val intent = Intent(this, FormReservasiActivity::class.java).apply {
            putExtra("EXTRA_PAKET_ID", paket.id ?: -1L)
            putExtra("EXTRA_NAMA_PAKET", paket.namaPaket)
            putExtra("EXTRA_KATEGORI_PAKET", paket.kategori)
        }
        startActivity(intent)
    }

    private fun hubungiAdminWA(paket: Paket) {
        val nomorWA = "6287850875676"
        val pesan = "Halo Griya Rias ELLY JR, saya berminat dengan paket *${paket.namaPaket}* (${paket.kategori}). Bisakah saya minta informasi lebih lanjut?"
        val url = "https://api.whatsapp.com/send?phone=$nomorWA&text=${Uri.encode(pesan)}"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}