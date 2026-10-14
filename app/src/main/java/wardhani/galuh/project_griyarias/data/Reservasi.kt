package wardhani.galuh.project_griyarias.data

import com.google.gson.annotations.SerializedName

data class Reservasi(
    @SerializedName("id_reservasi")
    val idReservasi: Long? = null,

    @SerializedName("kode_reservasi")
    val kodeReservasi: String? = null,

    @SerializedName("jenis_acara")
    val jenisAcara: String? = null,

    @SerializedName("detail_acara")
    val detailAcara: String? = null,

    @SerializedName("lokasi")
    val lokasi: String? = null,

    @SerializedName("tanggal_acara")
    val tanggalAcara: String? = null,

    @SerializedName("tanggal_pengajuan")
    val tanggalPengajuan: String? = null,

    @SerializedName("status_reservasi")
    val statusReservasi: String? = null,

    @SerializedName("id_akun")
    val idAkun: Long? = null,

    // Properti tambahan untuk pengiriman data dari FormReservasi (jika dibutuhkan)
    @SerializedName("nama_client")
    val namaClient: String? = null,

    @SerializedName("nomor_hp")
    val nomorHp: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null,

    @SerializedName("bukti_transfer")
    val buktiTransfer: String? = null
)

data class ReservasiResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: Reservasi? = null
)

data class ReservasiListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<Reservasi> = emptyList()
)