package wardhani.galuh.project_griyarias.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

import wardhani.galuh.project_griyarias.admin.PaketViewModel
import wardhani.galuh.project_griyarias.admin.PaketAdapter
import wardhani.galuh.project_griyarias.data.Paket
import wardhani.galuh.project_griyarias.databinding.FragmentAdminPaketBinding
import wardhani.galuh.project_griyarias.databinding.DialogFormPaketBinding

class AdminPaketFragment : Fragment() {

    private var _binding: FragmentAdminPaketBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PaketViewModel
    private lateinit var adapter: PaketAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminPaketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PaketViewModel::class.java)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = PaketAdapter(
            onEdit = { showFormDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.rvPaketAdmin.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPaketAdmin.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAddPaket.setOnClickListener { showFormDialog(null) }
    }

    private fun observeViewModel() {
        viewModel.pakets.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarPaket.visibility = if (loading == true) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_INDEFINITE)
                    .setAnchorView(binding.fabAddPaket)
                    .setAction("Coba Lagi") { viewModel.loadPakets() }
                    .show()
            }
        }
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.fabAddPaket)
                    .show()
                viewModel.messageShown()
            }
        }
    }

    private fun showFormDialog(paket: Paket?) {
        val form = DialogFormPaketBinding.inflate(layoutInflater)

        // 1. Array opsi Kategori Paket
        val listKategori = arrayOf(
            "Pilih Kategori",
            "Rias Pengantin",
            "Rias Wisuda",
            "Rias Prewedding",
            "Sewa Busana",
            "Dekorasi & Makeup"
        )

        // 2. Buat Adapter untuk Spinner
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listKategori
        )
        form.spinnerKategori.adapter = spinnerAdapter

        // 3. Jika mode Edit: Set opsi Spinner sesuai kategori paket yang di-edit
        paket?.let {
            form.etNamaPaket.setText(it.namaPaket)
            form.etHarga.setText(it.harga.toString())
            form.etDeskripsi.setText(it.deskripsi)
            if (it.status == "Nonaktif") form.rbNonaktif.isChecked = true else form.rbAktif.isChecked = true

            val indexKategori = listKategori.indexOf(it.kategori)
            if (indexKategori >= 0) {
                form.spinnerKategori.setSelection(indexKategori)
            }
        }

        val dialog: AlertDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (paket == null) "Tambah Paket" else "Edit Paket")
            .setView(form.root)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nama = form.etNamaPaket.text.toString().trim()
                val kategori = form.spinnerKategori.selectedItem.toString()
                val harga = form.etHarga.text.toString().trim().toLongOrNull()
                val deskripsi = form.etDeskripsi.text.toString().trim()
                val status = if (form.rbNonaktif.isChecked) "Nonaktif" else "Aktif"
                var valid = true

                if (nama.isEmpty()) { form.tilNamaPaket.error = "Wajib diisi"; valid = false } else form.tilNamaPaket.error = null
                if (kategori == "Pilih Kategori") {
                    Toast.makeText(requireContext(), "Harap pilih kategori paket!", Toast.LENGTH_SHORT).show()
                    valid = false
                }
                if (harga == null) { form.tilHarga.error = "Wajib diisi"; valid = false } else form.tilHarga.error = null
                if (!valid) return@setOnClickListener

                viewModel.savePaket(Paket(paket?.id, nama, kategori, harga!!, deskripsi.ifBlank { null }, status))
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun confirmDelete(paket: Paket) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Paket")
            .setMessage("Yakin hapus ${paket.namaPaket}?")
            .setPositiveButton("Hapus") { _, _ -> paket.id?.let { viewModel.deletePaket(it) } }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}