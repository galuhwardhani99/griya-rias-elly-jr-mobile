package wardhani.galuh.project_griyarias

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import wardhani.galuh.project_griyarias.admin.AdminDashboardActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Karena semua fitur sudah dipindah, MainActivity hanya bertugas
        // mengarahkan (melempar) pengguna langsung ke halaman Admin Dashboard.
        val intent = Intent(this, AdminDashboardActivity::class.java)
        startActivity(intent)
        finish() // Menutup MainActivity agar pengguna tidak kembali ke layar kosong saat menekan tombol Back
    }
}