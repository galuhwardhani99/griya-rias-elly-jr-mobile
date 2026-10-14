package wardhani.galuh.project_griyarias.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import wardhani.galuh.project_griyarias.databinding.ActivityFirestoreTestBinding

class FirestoreTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirestoreTestBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirestoreTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.btnInsert.setOnClickListener {
            val id = binding.edId.text.toString().trim()
            val name = binding.edName.text.toString().trim()
            val address = binding.edAddress.text.toString().trim()
            val phone = binding.edPhone.text.toString().trim()

            if (id.isEmpty()) {
                Toast.makeText(this, "ID tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hm = HashMap<String, Any>()
            hm["id"] = id
            hm["name"] = name
            hm["address"] = address
            hm["phone"] = phone

            db.collection("students")
                .document(id)
                .set(hm)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data successfully added to Firestore!", Toast.LENGTH_SHORT).show()
                    binding.edId.setText("")
                    binding.edName.setText("")
                    binding.edAddress.setText("")
                    binding.edPhone.setText("")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal simpan: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}