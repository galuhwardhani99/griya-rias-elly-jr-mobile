package wardhani.galuh.project_griyarias.admin.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wardhani.galuh.project_griyarias.adapter.CalendarAdapter
import wardhani.galuh.project_griyarias.data.CalendarDay
import wardhani.galuh.project_griyarias.data.JadwalModel
import wardhani.galuh.project_griyarias.data.SupabaseClient
import wardhani.galuh.project_griyarias.databinding.FragmentAdminJadwalBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminJadwalFragment : Fragment() {

    private var _binding: FragmentAdminJadwalBinding? = null
    private val binding get() = _binding!!

    private val calendar = Calendar.getInstance()
    private lateinit var calendarAdapter: CalendarAdapter
    private var listJadwal = listOf<JadwalModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminJadwalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set awal ke tanggal 1 agar tidak melompati bulan pendek (seperti Februari)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        setupRecyclerView()
        setupListeners()
        loadJadwalFromSupabase()
    }

    private fun setupRecyclerView() {
        calendarAdapter = CalendarAdapter(emptyList()) { selectedDay ->
            handleDayClick(selectedDay)
        }
        binding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.rvCalendar.adapter = calendarAdapter
    }

    private fun setupListeners() {
        // Tombol Kembali: Otomatis mundur ke tahun sebelumnya jika melewati Januari
        binding.btnPrevMonth.setOnClickListener {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, -1)
            loadJadwalFromSupabase()
        }

        // Tombol Lanjut: Otomatis maju ke tahun berikutnya jika melewati Desember
        binding.btnNextMonth.setOnClickListener {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, 1)
            loadJadwalFromSupabase()
        }
    }

    private fun loadJadwalFromSupabase() {
        // Menampilkan nama bulan dan tahun secara dinamis (contoh: "Desember 2026", "Januari 2027")
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        binding.tvMonthYear.text = monthYearFormat.format(calendar.time)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                listJadwal = SupabaseClient.client.postgrest["tabel_jadwal"]
                    .select()
                    .decodeList<JadwalModel>()

                withContext(Dispatchers.Main) {
                    renderCalendar()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal memuat jadwal: ${e.message}", Toast.LENGTH_SHORT).show()
                    renderCalendar()
                }
            }
        }
    }

    private fun renderCalendar() {
        val daysList = mutableListOf<CalendarDay>()
        val tempCalendar = calendar.clone() as Calendar

        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1
        val maxDays = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Menambahkan kotak kosong sebelum tanggal 1
        for (i in 0 until firstDayOfWeek) {
            daysList.add(CalendarDay("", "", isCurrentMonth = false, isBooked = false))
        }

        // Menyusun tanggal sesuai bulan yang aktif
        for (day in 1..maxDays) {
            tempCalendar.set(Calendar.DAY_OF_MONTH, day)
            val fullDate = dateFormat.format(tempCalendar.time)

            val isBooked = listJadwal.any { it.tanggal == fullDate && it.status == "booked" }

            daysList.add(
                CalendarDay(
                    dayOfMonth = day.toString(),
                    fullDate = fullDate,
                    isCurrentMonth = true,
                    isBooked = isBooked
                )
            )
        }

        calendarAdapter.updateData(daysList)
    }

    private fun handleDayClick(day: CalendarDay) {
        val existingJadwal = listJadwal.find { it.tanggal == day.fullDate }

        if (day.isBooked && existingJadwal != null) {
            val options = arrayOf("Hapus / Kosongkan Jadwal", "Batal")
            AlertDialog.Builder(requireContext())
                .setTitle("Tanggal: ${day.fullDate}\nStatus: Terisi (${existingJadwal.namaAcara})")
                .setItems(options) { dialog, which ->
                    if (which == 0) {
                        deleteJadwal(existingJadwal.id!!)
                    }
                    dialog.dismiss()
                }
                .show()
        } else {
            val input = EditText(requireContext()).apply {
                hint = "Masukkan Nama Acara (mis: Rias Pengantin Rina)"
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Tambah Booking (${day.fullDate})")
                .setView(input)
                .setPositiveButton("Simpan") { _, _ ->
                    val namaAcara = input.text.toString().trim()
                    if (namaAcara.isNotEmpty()) {
                        saveJadwal(day.fullDate, namaAcara)
                    } else {
                        Toast.makeText(requireContext(), "Nama acara tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun saveJadwal(tanggal: String, namaAcara: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dataToInsert = mapOf(
                    "tanggal" to tanggal,
                    "nama_acara" to namaAcara,
                    "status" to "booked"
                )
                SupabaseClient.client.postgrest["tabel_jadwal"].insert(dataToInsert)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Jadwal berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    loadJadwalFromSupabase()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteJadwal(id: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["tabel_jadwal"].delete {
                    filter {
                        eq("id", id)
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Jadwal dikosongkan!", Toast.LENGTH_SHORT).show()
                    loadJadwalFromSupabase()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}