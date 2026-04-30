package kategori

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import adapter.ProdukAdapter
import model.modelProduk

class DataProdukActivity : AppCompatActivity() {

    private lateinit var rvProduk: RecyclerView
    private lateinit var svProduk: SearchView
    private lateinit var fabTambah: FloatingActionButton

    private lateinit var listProduk: ArrayList<modelProduk>
    private lateinit var adapter: ProdukAdapter

    private val db = FirebaseDatabase.getInstance()
    private val ref = db.getReference("produk")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_produk)

        initView()
        setupRecycler()
        loadData()
        setupSearch()
        setupAction()
    }

    private fun initView() {
        rvProduk = findViewById(R.id.rvProduk)
        svProduk = findViewById(R.id.svProduk)
        fabTambah = findViewById(R.id.fabTambah)
    }

    private fun setupRecycler() {
        listProduk = ArrayList()
        adapter = ProdukAdapter(listProduk)

        rvProduk.layoutManager = LinearLayoutManager(this)
        rvProduk.adapter = adapter
    }

    // 🔥 LOAD DATA FIREBASE
    private fun loadData() {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()

                for (snap in snapshot.children) {
                    val data = snap.getValue(modelProduk::class.java)
                    if (data != null) {
                        listProduk.add(data)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 🔍 SEARCH
    private fun setupSearch() {
        svProduk.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        val filtered = ArrayList<modelProduk>()

        if (query.isNullOrEmpty()) {
            filtered.addAll(listProduk)
        } else {
            for (item in listProduk) {
                if (item.namaProduk
                        ?.lowercase()
                        ?.contains(query.lowercase()) == true
                ) {
                    filtered.add(item)
                }
            }
        }

        adapter = ProdukAdapter(filtered)
        rvProduk.adapter = adapter
    }

    // ➕ ACTION FAB
    private fun setupAction() {
        fabTambah.setOnClickListener {
            startActivity(Intent(this, TambahProdukActivity::class.java))
        }
    }
}
