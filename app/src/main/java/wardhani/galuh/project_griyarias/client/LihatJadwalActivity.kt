package wardhani.galuh.project_griyarias.client

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wardhani.galuh.project_griyarias.R
import wardhani.galuh.project_griyarias.adapter.CalendarAdapter
import wardhani.galuh.project_griyarias.data.CalendarDay
import wardhani.galuh.project_griyarias.data.JadwalModel
import wardhani.galuh.project_griyarias.data.SupabaseClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LihatJadwalActivity : AppCompatActivity() {

    private val calendar = Calendar.getInstance()
    private lateinit var calendarAdapter: CalendarAdapter
    private var listJadwal = listOf<JadwalModel>()

    private lateinit var tvMonthYear: TextView
    private lateinit var rvCalendar: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lihat_jadwal)

        val toolbar = findViewById<Toolbar>(R.id.toolbarJadwal)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        calendar.set(Calendar.DAY_OF_MONTH, 1)

        tvMonthYear = findViewById(R.id.tvMonthYearClient)
        rvCalendar = findViewById(R.id.rvCalendarClient)
        progressBar = findViewById(R.id.progressBarJadwal)

        val btnPrev = findViewById<ImageButton>(R.id.btnPrevMonthClient)
        val btnNext = findViewById<ImageButton>(R.id.btnNextMonthClient)

        setupRecyclerView()

        btnPrev.setOnClickListener {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, -1)
            loadJadwalFromSupabase()
        }

        btnNext.setOnClickListener {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, 1)
            loadJadwalFromSupabase()
        }

        loadJadwalFromSupabase()
    }

    private fun setupRecyclerView() {
        calendarAdapter = CalendarAdapter(emptyList()) { selectedDay ->
            handleDayClick(selectedDay)
        }
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvCalendar.adapter = calendarAdapter
    }

    private fun loadJadwalFromSupabase() {
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        tvMonthYear.text = monthYearFormat.format(calendar.time)

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                listJadwal = SupabaseClient.client.postgrest["tabel_jadwal"]
                    .select()
                    .decodeList<JadwalModel>()

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    renderCalendar()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@LihatJadwalActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
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

        for (i in 0 until firstDayOfWeek) {
            daysList.add(CalendarDay("", "", isCurrentMonth = false, isBooked = false))
        }

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
        if (day.isBooked) {
            AlertDialog.Builder(this)
                .setTitle("Tanggal Terisi")
                .setMessage("Maaf, tanggal ${day.fullDate} sudah dibooking oleh klien lain.")
                .setPositiveButton("Tutup", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Tanggal Tersedia")
                .setMessage("Tanggal ${day.fullDate} masih kosong! Silakan lakukan reservasi paket layanan kami.")
                .setPositiveButton("Tutup", null)
                .show()
        }
    }
}