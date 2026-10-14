package wardhani.galuh.project_griyarias.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import wardhani.galuh.project_griyarias.admin.AdminReservasiAdapter
import wardhani.galuh.project_griyarias.data.RetrofitClient
import wardhani.galuh.project_griyarias.databinding.FragmentAdminOverviewBinding

class AdminOverviewFragment : Fragment() {

    private var _binding: FragmentAdminOverviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminReservasiAdapter

    private val db = FirebaseFirestore.getInstance()
    private var firestoreListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up Adapter & RecyclerView
        adapter = AdminReservasiAdapter(emptyList()) { idReservasi, statusBaru ->
            updateStatusReservasi(idReservasi, statusBaru)
        }

        binding.rvReservasiAdmin.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservasiAdmin.adapter = adapter

        // 1. Muat data reservasi pertama kali dari server MySQL / Laravel
        loadReservasiData()

        // 2. Pasang Real-time Listener Firestore untuk memantau reservasi baru / perubahan status
        setupFirestoreRealtimeListener()
    }

    private fun loadReservasiData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getReservasi()
                if (response.success && !response.data.isNullOrEmpty()) {
                    adapter.updateData(response.data)
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFirestoreRealtimeListener() {
        // Mendengarkan perubahan data pada collection 'reservasi' secara real-time
        firestoreListener = db.collection("reservasi")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("FIRESTORE_ADMIN", "Listen error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    // Ketika ada dokumen baru / perubahan di Firestore, muat ulang list reservasi dari API
                    loadReservasiData()
                }
            }
    }

    private fun updateStatusReservasi(idReservasi: Long, status: String) {
        lifecycleScope.launch {
            try {
                val body = mapOf("status_reservasi" to status)
                val response = RetrofitClient.api.updateStatusReservasi(idReservasi, body)
                if (response.success) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Berhasil diubah menjadi $status", Toast.LENGTH_SHORT).show()
                    }
                    loadReservasiData() // Refresh list
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Gagal memperbarui status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hapus listener saat fragment ditutup agar hemat memori & baterai
        firestoreListener?.remove()
        _binding = null
    }
}