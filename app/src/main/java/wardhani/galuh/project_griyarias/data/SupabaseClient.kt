package wardhani.galuh.project_griyarias.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    // Tempelkan URL dan API Key dari dashboard Supabase di sini
    private const val SUPABASE_URL = "https://nryaqgdflyhwypybhgxg.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5yeWFxZ2RmbHlod3lweWJoZ3hnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTE0MDIxMzcsImV4cCI6MjEwNjk3ODEzN30.mbR9_aL75Us8rTQygKEqB-u323odIYRIhk4i7xagK8I"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage)
        install(Postgrest)
    }
}