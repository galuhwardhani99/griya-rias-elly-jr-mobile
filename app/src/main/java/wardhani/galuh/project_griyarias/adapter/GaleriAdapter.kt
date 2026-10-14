package wardhani.galuh.project_griyarias.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import wardhani.galuh.project_griyarias.R
import wardhani.galuh.project_griyarias.data.GaleriModel
import wardhani.galuh.project_griyarias.databinding.ItemGaleriBinding

class GaleriAdapter(private var listFoto: List<GaleriModel>) :
    RecyclerView.Adapter<GaleriAdapter.GaleriViewHolder>() {

    class GaleriViewHolder(val binding: ItemGaleriBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GaleriViewHolder {
        val binding = ItemGaleriBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GaleriViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GaleriViewHolder, position: Int) {
        val item = listFoto[position]

        // Menggunakan binding.ivGaleri (sesuai ID di item_galeri.xml)
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.binding.ivGaleri)

        // Event klik gambar untuk menampilkan Pop-up Zoom
        holder.itemView.setOnClickListener {
            showZoomDialog(holder.itemView.context, item.imageUrl)
        }
    }

    override fun getItemCount(): Int = listFoto.size

    fun updateData(newList: List<GaleriModel>) {
        listFoto = newList
        notifyDataSetChanged()
    }

    // Pop-up dialog fullscreen untuk Zoom In & Zoom Out
    private fun showZoomDialog(context: Context, imageUrl: String) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_zoom_foto)

        val photoView = dialog.findViewById<PhotoView>(R.id.photoViewDetail)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnCloseZoom)

        Glide.with(context)
            .load(imageUrl)
            .into(photoView)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}