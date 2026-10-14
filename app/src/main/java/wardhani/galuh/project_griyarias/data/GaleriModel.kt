package wardhani.galuh.project_griyarias.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GaleriModel(
    val id: Int? = null,
    @SerialName("image_url")
    val imageUrl: String = ""
)