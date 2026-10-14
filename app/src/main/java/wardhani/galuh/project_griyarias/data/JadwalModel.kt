package wardhani.galuh.project_griyarias.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JadwalModel(
    val id: Int? = null,
    val tanggal: String = "", // Format: YYYY-MM-DD
    @SerialName("nama_acara")
    val namaAcara: String = "",
    val status: String = "booked",
    val catatan: String? = ""
)

data class CalendarDay(
    val dayOfMonth: String,
    val fullDate: String, // Format YYYY-MM-DD
    val isCurrentMonth: Boolean,
    val isBooked: Boolean
)