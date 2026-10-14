package wardhani.galuh.project_griyarias.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import wardhani.galuh.project_griyarias.data.GaleriModel
import wardhani.galuh.project_griyarias.databinding.ItemGaleriBinding

class GaleriAdapter(private var listFoto: List<GaleriModel>) :
    RecyclerView.Adapter<GaleriAdapter.GaleriViewHolder>() {

    inner class GaleriViewHolder(val binding: ItemGaleriBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GaleriViewHolder {
        val binding = ItemGaleriBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GaleriViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GaleriViewHolder, position: Int) {
        val item = listFoto[position]
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.binding.ivGaleri)
    }

    override fun getItemCount(): Int = listFoto.size

    fun updateData(newList: List<GaleriModel>) {
        listFoto = newList
        notifyDataSetChanged()
    }
}