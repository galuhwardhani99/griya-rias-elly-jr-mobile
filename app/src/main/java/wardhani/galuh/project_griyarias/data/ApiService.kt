package wardhani.galuh.project_griyarias.data

import retrofit2.http.*

interface ApiService {

    @GET("pakets")
    suspend fun getPakets(@Query("search") search: String? = null): PaketListResponse

    @POST("pakets")
    suspend fun createPaket(@Body paket: Paket): PaketResponse

    @PUT("pakets/{id}")
    suspend fun updatePaket(@Path("id") id: Long, @Body paket: Paket): PaketResponse

    @DELETE("pakets/{id}")
    suspend fun deletePaket(@Path("id") id: Long): PaketResponse
}