package wardhani.galuh.project_griyarias.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import wardhani.galuh.project_griyarias.R
import wardhani.galuh.project_griyarias.admin.AdminDashboardActivity
import wardhani.galuh.project_griyarias.client.ClientDashboardActivity
import wardhani.galuh.project_griyarias.service.PromoIntentHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (savedInstanceState == null) {
            PromoIntentHelper.handleNotificationIntent(this, intent)
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmailLogin)
        val etPass = findViewById<EditText>(R.id.etPassLogin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvToRegister = findViewById<TextView>(R.id.tvToRegister)

        tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Harap isi email dan password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid

                        if (user != null && userId != null) {
                            // 1. Cek Data Role User di Firestore
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    val role = document.getString("role") ?: "client"

                                    // Jika Role Admin: Langsung masuk ke Admin Dashboard
                                    if (role.equals("admin", ignoreCase = true)) {
                                        Toast.makeText(this, "Login Admin Berhasil", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                                        finish()
                                    } else {
                                        // Jika Role Client: Wajib cek verifikasi email
                                        user.reload().addOnCompleteListener { reloadTask ->
                                            if (reloadTask.isSuccessful) {
                                                if (user.isEmailVerified) {
                                                    Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                                                    startActivity(Intent(this, ClientDashboardActivity::class.java))
                                                    finish()
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        "Email Anda belum diverifikasi! Silakan cek Inbox atau folder Spam.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    auth.signOut()
                                                }
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Gagal mengambil data user: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Gagal Login: Email atau Password salah",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PromoIntentHelper.handleNotificationIntent(this, intent)
    }
}