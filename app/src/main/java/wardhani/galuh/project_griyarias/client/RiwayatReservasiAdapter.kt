package wardhani.galuh.project_griyarias.client

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import wardhani.galuh.project_griyarias.data.Reservasi
import wardhani.galuh.project_griyarias.databinding.ItemRiwayatReservasiBinding

class RiwayatReservasiAdapter(
    private var listReservasi: List<Reservasi>
) : RecyclerView.Adapter<RiwayatReservasiAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRiwayatReservasiBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatReservasiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listReservasi[position]
        with(holder.binding) {
            tvKodeReservasi.text = item.kodeReservasi ?: "RSV-UNKNOWN"
            tvTanggalAcara.text = "Tanggal Acara: ${item.tanggalAcara ?: "-"}"
            tvLokasi.text = "Lokasi: ${item.lokasi ?: "-"}"
            tvDetailAcara.text = item.detailAcara ?: "-"

            val status = item.statusReservasi ?: "Pending"
            tvStatusReservasi.text = status

            when (status.lowercase()) {
                "diterima", "disetujui" -> tvStatusReservasi.setBackgroundColor(Color.parseColor("#4CAF50"))
                "ditolak" -> tvStatusReservasi.setBackgroundColor(Color.parseColor("#F44336"))
                else -> tvStatusReservasi.setBackgroundColor(Color.parseColor("#FF9800"))
            }
        }
    }

    override fun getItemCount(): Int = listReservasi.size

    fun updateData(newList: List<Reservasi>) {
        listReservasi = newList
        notifyDataSetChanged()
    }
}