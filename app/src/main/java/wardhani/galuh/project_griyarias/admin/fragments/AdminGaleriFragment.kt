package wardhani.galuh.project_griyarias.admin.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wardhani.galuh.project_griyarias.data.SupabaseClient
import wardhani.galuh.project_griyarias.databinding.FragmentAdminGaleriBinding

class AdminGaleriFragment : Fragment() {

    private var _binding: FragmentAdminGaleriBinding? = null
    private val binding get() = _binding!!

    // Variabel untuk menyimpan URI/lokasi foto yang dipilih dari galeri HP
    private var imageUri: Uri? = null

    // Launcher untuk memilih foto dari galeri HP
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            // Tampilkan foto pada ImageView preview
            binding.ivPreview.setImageURI(uri)
            binding.ivPreview.imageTintList = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminGaleriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aksi tombol untuk memilih foto dari HP
        binding.btnPilihFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Aksi tombol untuk menyimpan/meng-upload foto ke Supabase
        binding.btnSimpanFoto.setOnClickListener {
            if (imageUri != null) {
                uploadImageToSupabase(imageUri!!)
            } else {
                Toast.makeText(requireContext(), "Pilih foto terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToSupabase(uri: Uri) {
        // Tampilkan status loading pada tombol
        binding.btnSimpanFoto.text = "Mengunggah..."
        binding.btnSimpanFoto.isEnabled = false

        // Jalankan proses unggah di background thread
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Baca data gambar menjadi ByteArray
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val byteArray = inputStream?.readBytes() ?: return@launch

                // Buat nama file unik berdasarkan timestamp
                val fileName = "foto_${System.currentTimeMillis()}.jpg"

                // 2. Upload gambar ke Supabase Storage (Bucket "galeri")
                val bucket = SupabaseClient.client.storage.from("galeri")
                bucket.upload(fileName, byteArray)

                // 3. Dapatkan URL Public gambar
                val publicUrl = bucket.publicUrl(fileName)

                // 4. Simpan URL tersebut ke Supabase Database (Tabel "tabel_galeri")
                val dataToInsert = mapOf("image_url" to publicUrl)
                SupabaseClient.client.postgrest["tabel_galeri"].insert(dataToInsert)

                // 5. Kembali ke UI thread untuk update status
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Foto berhasil diunggah!", Toast.LENGTH_SHORT).show()
                    binding.btnSimpanFoto.text = "Simpan Foto"
                    binding.btnSimpanFoto.isEnabled = true

                    // Reset tampilan preview dan URI
                    imageUri = null
                    binding.ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal mengunggah: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.btnSimpanFoto.text = "Simpan Foto"
                    binding.btnSimpanFoto.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}