package wardhani.galuh.project_griyarias.client

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wardhani.galuh.project_griyarias.R
import wardhani.galuh.project_griyarias.adapter.GaleriAdapter
import wardhani.galuh.project_griyarias.auth.LoginActivity
import wardhani.galuh.project_griyarias.data.GaleriModel
import wardhani.galuh.project_griyarias.data.SupabaseClient
import wardhani.galuh.project_griyarias.service.MyFirebaseMessagingService
import wardhani.galuh.project_griyarias.service.PromoIntentHelper

class ClientDashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvGaleri: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var galeriAdapter: GaleriAdapter

    companion object {
        // Nama event ini harus sama persis dengan trigger di Firebase Console
        private const val FIAM_EVENT = "client_dashboard_open"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Izin notifikasi ditolak, notifikasi tidak bisa masuk",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_dashboard)

        auth = FirebaseAuth.getInstance()

        // Kalau dibuka dari klik notifikasi (app background), teruskan ke detail promo
        if (savedInstanceState == null) {
            PromoIntentHelper.handleNotificationIntent(this, intent)
        }

        askNotificationPermission()
        subscribeToFirebaseTopic()

        // Ambil Installation ID untuk "Test on device" (FIAM)
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FIAM_ID", "INSTALLATION_ID: ${task.result}")
            } else {
                Log.e("FIAM_ID", "Gagal ambil installation ID: ${task.exception?.message}")
            }
        }

        FirebaseInAppMessaging.getInstance().isAutomaticDataCollectionEnabled = true

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        rvGaleri = findViewById(R.id.rvGaleriClient)
        progressBar = findViewById(R.id.progressBarClient)

        val btnLihatKatalogId = resources.getIdentifier("btnLihatKatalog", "id", packageName)
        if (btnLihatKatalogId != 0) {
            findViewById<View>(btnLihatKatalogId)?.setOnClickListener {
                startActivity(Intent(this, KatalogPaketActivity::class.java))
            }
        }

        setupRecyclerView()
        loadGaleriFromSupabase()

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        val headerView = navView.getHeaderView(0)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tvUserEmail)
        tvUserEmail?.text = auth.currentUser?.email ?: "client@email.com"

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Dashboard client tampil: buka blokir FIAM lalu picu event
        val fiam = FirebaseInAppMessaging.getInstance()
        fiam.setMessagesSuppressed(false)
        fiam.triggerEvent(FIAM_EVENT)
    }

    override fun onPause() {
        super.onPause()
        // Keluar dari dashboard: tekan lagi supaya tidak muncul di halaman lain
        FirebaseInAppMessaging.getInstance().setMessagesSuppressed(true)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PromoIntentHelper.handleNotificationIntent(this, intent)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun subscribeToFirebaseTopic() {
        val messaging = FirebaseMessaging.getInstance()

        // Berhenti dari topik lama supaya tidak bentrok dengan aplikasi sebelumnya
        messaging.unsubscribeFromTopic(MyFirebaseMessagingService.OLD_TOPIC)

        messaging.subscribeToTopic(MyFirebaseMessagingService.TOPIC)
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    "Berhasil subscribe ke topik ${MyFirebaseMessagingService.TOPIC}"
                } else {
                    "Gagal subscribe ke topik ${MyFirebaseMessagingService.TOPIC}: ${task.exception?.message}"
                }
                Log.d("FCM_TOKEN", msg)
            }

        messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCM_TOKEN", "TOKEN_HP_KAMU: ${task.result}")
            } else {
                Log.e("FCM_TOKEN", "Gagal mengambil token: ${task.exception?.message}")
            }
        }
    }

    private fun setupRecyclerView() {
        galeriAdapter = GaleriAdapter(emptyList())
        rvGaleri.layoutManager = GridLayoutManager(this, 2)
        rvGaleri.adapter = galeriAdapter
    }

    private fun loadGaleriFromSupabase() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val listFoto = SupabaseClient.client.postgrest["tabel_galeri"]
                    .select()
                    .decodeList<GaleriModel>()

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    galeriAdapter.updateData(listFoto)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ClientDashboardActivity,
                        "Gagal memuat galeri: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_jadwal -> startActivity(Intent(this, LihatJadwalActivity::class.java))
            R.id.nav_katalog -> startActivity(Intent(this, KatalogPaketActivity::class.java))
            R.id.nav_info_aplikasi -> startActivity(Intent(this, InfoAplikasiActivity::class.java))
            R.id.nav_logout -> logoutClient()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logoutClient() {
        FirebaseInAppMessaging.getInstance().setMessagesSuppressed(true)
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}