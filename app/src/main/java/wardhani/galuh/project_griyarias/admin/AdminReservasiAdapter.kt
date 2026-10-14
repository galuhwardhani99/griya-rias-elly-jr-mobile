package wardhani.galuh.project_griyarias.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import wardhani.galuh.project_griyarias.data.Reservasi
import wardhani.galuh.project_griyarias.databinding.ItemAdminReservasiBinding

class AdminReservasiAdapter(
    private var listReservasi: List<Reservasi>,
    private val onStatusChanged: (Long, String) -> Unit
) : RecyclerView.Adapter<AdminReservasiAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminReservasiBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminReservasiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listReservasi[position]

        with(holder.binding) {
            tvKodeReservasi.text = item.kodeReservasi ?: "RSV-UNKNOWN"
            tvTanggalAcara.text = "Tanggal: ${item.tanggalAcara ?: "-"}"
            tvLokasi.text = "Lokasi: ${item.lokasi ?: "-"}"
            tvDetailAcara.text = item.detailAcara ?: "-"

            val status = item.statusReservasi ?: "Pending"
            tvStatus.text = "Status: $status"

            // 1. Tampilkan tombol aksi jika status Pending atau Menunggu Konfirmasi Admin
            if (status.equals("Pending", ignoreCase = true) ||
                status.equals("Menunggu Konfirmasi Admin", ignoreCase = true)
            ) {
                layoutAksiAdmin.visibility = View.VISIBLE
            } else {
                layoutAksiAdmin.visibility = View.GONE
            }

            // 2. Tampilkan bukti transfer menggunakan Glide
            val urlBukti = item.buktiTransfer
            if (!urlBukti.isNullOrEmpty()) {
                imgBuktiTransferAdmin.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(urlBukti)
                    .into(imgBuktiTransferAdmin)
            } else {
                imgBuktiTransferAdmin.visibility = View.GONE
            }

            btnSetujui.setOnClickListener {
                onStatusChanged(item.idReservasi ?: 0, "Diterima")
            }

            btnTolak.setOnClickListener {
                onStatusChanged(item.idReservasi ?: 0, "Ditolak")
            }
        }
    }

    override fun getItemCount(): Int = listReservasi.size

    fun updateData(newList: List<Reservasi>) {
        listReservasi = newList
        notifyDataSetChanged()
    }
}