package wardhani.galuh.project_griyarias.client

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import wardhani.galuh.project_griyarias.data.Paket
import wardhani.galuh.project_griyarias.databinding.ItemPaketClientBinding
import java.text.NumberFormat
import java.util.Locale

class PaketClientAdapter(
    private var listPaket: List<Paket>,
    private val onPesanClick: (Paket) -> Unit
) : RecyclerView.Adapter<PaketClientAdapter.PaketViewHolder>() {

    inner class PaketViewHolder(val binding: ItemPaketClientBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaketViewHolder {
        val binding = ItemPaketClientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PaketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaketViewHolder, position: Int) {
        val item = listPaket[position]

        holder.binding.tvNamaPaket.text = item.namaPaket
        holder.binding.tvKategoriPaket.text = item.kategori.uppercase()
        holder.binding.tvDeskripsiPaket.text = item.deskripsi ?: "Tidak ada deskripsi"

        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        holder.binding.tvHargaPaket.text = formatRupiah.format(item.harga)

        holder.binding.btnPesanPaket.setOnClickListener {
            onPesanClick(item)
        }
    }

    override fun getItemCount(): Int = listPaket.size

    fun updateData(newList: List<Paket>) {
        listPaket = newList
        notifyDataSetChanged()
    }
}