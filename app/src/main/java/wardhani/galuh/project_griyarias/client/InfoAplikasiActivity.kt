package wardhani.galuh.project_griyarias.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import wardhani.galuh.project_griyarias.R

class InfoAplikasiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_aplikasi)

        val toolbar = findViewById<Toolbar>(R.id.toolbarInfo)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val ivLogoInfo = findViewById<ImageView>(R.id.ivLogoInfo)

        ivLogoInfo?.setOnLongClickListener { view ->
            showLogoOptions(view)
            true
        }
    }

    private fun showLogoOptions(anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menu.add("Tentang Griya Wedding")
        popup.menu.add("Maps Griya Wedding")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Tentang Griya Wedding" -> {
                    startActivity(Intent(this, TentangActivity::class.java))
                    true
                }
                "Maps Griya Wedding" -> {
                    // Titik koordinat presisi Griya Rias Elly Jr (Doko, Ngasem, Kediri)
                    val latitude = "-7.8142841"
                    val longitude = "112.0432846"

                    val osmUrl = "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=18/$latitude/$longitude"
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(osmUrl))
                    startActivity(mapIntent)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}