package wardhani.galuh.project_griyarias.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import wardhani.galuh.project_griyarias.R
import wardhani.galuh.project_griyarias.data.Paket
import wardhani.galuh.project_griyarias.databinding.ItemPaketBinding
import java.util.Locale

class PaketAdapter(
    private val onEdit: (Paket) -> Unit,
    private val onDelete: (Paket) -> Unit
) : ListAdapter<Paket, PaketAdapter.PaketViewHolder>(DiffCallback) {

    inner class PaketViewHolder(val binding: ItemPaketBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaketViewHolder {
        val binding = ItemPaketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaketViewHolder, position: Int) {
        val paket = getItem(position)
        with(holder.binding) {
            tvNamaPaket.text = paket.namaPaket
            tvKategori.text = paket.kategori
            tvHarga.text = String.Companion.format(Locale.forLanguageTag("id-ID"), "Rp %,d", paket.harga)

            tvDeskripsi.text = paket.deskripsi
            tvDeskripsi.visibility =
                if (paket.deskripsi.isNullOrBlank()) View.GONE else View.VISIBLE

            tvStatus.text = paket.status
            tvStatus.setBackgroundResource(
                if (paket.status == "Aktif") R.drawable.bg_status_aktif
                else R.drawable.bg_status_nonaktif
            )

            btnEdit.setOnClickListener { onEdit(paket) }
            btnDelete.setOnClickListener { onDelete(paket) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Paket>() {
        override fun areItemsTheSame(oldItem: Paket, newItem: Paket) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Paket, newItem: Paket) = oldItem == newItem
    }
}