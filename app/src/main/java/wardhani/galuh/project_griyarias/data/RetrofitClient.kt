package wardhani.galuh.project_griyarias.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Emulator Android  -> http://10.0.2.2:8000/api/
    // HP asli (satu WiFi) -> http://IP_LAPTOP_KAMU:8000/api/  (contoh 192.168.1.10)
    private const val BASE_URL = "http://192.168.40.108:8080/api/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // wajib supaya Laravel membalas error validasi dalam bentuk JSON
            val request = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}