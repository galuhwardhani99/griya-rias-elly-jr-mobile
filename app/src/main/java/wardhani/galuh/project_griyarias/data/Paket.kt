package wardhani.galuh.project_griyarias.data

import com.google.gson.annotations.SerializedName

data class Paket(
    val id: Long? = null,
    @SerializedName("nama_paket") val namaPaket: String,
    val kategori: String,
    val harga: Long,
    val deskripsi: String? = null,
    val status: String = "Aktif"
)

data class PaketListResponse(
    val success: Boolean,
    val data: List<Paket>
)

data class PaketResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Paket? = null
)