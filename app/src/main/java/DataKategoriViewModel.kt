import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

import model.modelKategori

class DataKategoriViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    val kategoriList = MutableLiveData<List<modelKategori>>()
    private var originalKategoriList = ArrayList<modelKategori>()

    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    init {
        getData()
    }

    fun getData() {
        isLoading.value = true

        val query = myRef.orderByChild("namaKategori").limitToLast(100)

        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false

                val list = ArrayList<modelKategori>()

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val kategori = data.getValue(modelKategori::class.java)
                        if (kategori == null) {
                            Log.e("DataKategoriViewModel", "Kategori data is null")
                        } else {
                            list.add(kategori)
                        }
                    }
                }

                originalKategoriList.clear()
                originalKategoriList.addAll(list)
                kategoriList.value = list
                isSearchEmpty.value = list.isEmpty()
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("DataKategoriViewModel", "Error: ${error.message}")
            }
        })
    }
}