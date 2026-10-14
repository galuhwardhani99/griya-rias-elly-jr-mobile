package wardhani.galuh.project_griyarias.data

import retrofit2.http.*

interface ApiService {

    // --- PAKET ---
    @GET("pakets")
    suspend fun getPakets(@Query("search") search: String? = null): PaketListResponse

    @POST("pakets")
    suspend fun createPaket(@Body paket: Paket): PaketResponse

    @PUT("reservasi/{id}")
    suspend fun updateStatusReservasi(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): ReservasiResponse

    @PUT("pakets/{id}")
    suspend fun updatePaket(@Path("id") id: Long, @Body paket: Paket): PaketResponse

    @DELETE("pakets/{id}")
    suspend fun deletePaket(@Path("id") id: Long): PaketResponse

    // --- RESERVASI ---
    // Pastikan path-nya "reservasi" (tanpa awalan slash / atau kata api/)
    @GET("reservasi")
    suspend fun getReservasi(): ReservasiListResponse


    @POST("reservasi")
    suspend fun createReservasi(@Body reservasi: Reservasi): ReservasiResponse
}