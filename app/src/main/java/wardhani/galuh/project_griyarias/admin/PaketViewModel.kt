package wardhani.galuh.project_griyarias.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import wardhani.galuh.project_griyarias.data.Paket
import wardhani.galuh.project_griyarias.data.RetrofitClient

class PaketViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _pakets = MutableLiveData<List<Paket>>(emptyList())
    val pakets: LiveData<List<Paket>> = _pakets

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    init {
        loadPakets()
    }

    fun loadPakets() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _pakets.value = api.getPakets().data
            } catch (e: Exception) {
                _pakets.value = emptyList()
                _error.value = errorText(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun savePaket(paket: Paket) {
        viewModelScope.launch {
            try {
                val res = if (paket.id == null) api.createPaket(paket)
                else api.updatePaket(paket.id, paket)
                _message.value = res.message ?: "Berhasil disimpan"
                loadPakets()
            } catch (e: Exception) {
                _message.value = errorText(e)
            }
        }
    }

    fun deletePaket(id: Long) {
        viewModelScope.launch {
            try {
                val res = api.deletePaket(id)
                _message.value = res.message ?: "Berhasil dihapus"
                loadPakets()
            } catch (e: Exception) {
                _message.value = errorText(e)
            }
        }
    }

    fun messageShown() {
        _message.value = null
    }

    private fun errorText(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            422 -> "Data tidak valid, periksa kembali isian"
            404 -> "Data tidak ditemukan"
            else -> "Server error (${e.code()})"
        }
        else -> "Tidak bisa terhubung ke server. Cek koneksi & alamat API"
    }
}