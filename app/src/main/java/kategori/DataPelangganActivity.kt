package kategori

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import model.modelPelanggan

class DataPelangganActivity : AppCompatActivity() {

    private lateinit var rvPelanggan: RecyclerView
    private lateinit var svPelanggan: SearchView
    private lateinit var adapter: AdapterPelanggan
    private val listPelanggan = mutableListOf<modelPelanggan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pelanggan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        loadData()
    }

    private fun initView() {
        rvPelanggan = findViewById(R.id.rvPelanggan)
        svPelanggan = findViewById(R.id.svPelanggan)
        
        rvPelanggan.layoutManager = LinearLayoutManager(this)
        adapter = AdapterPelanggan(listPelanggan,
            onEdit = { p ->
                val intent = Intent(this, TambahPelangganActivity::class.java)
                intent.putExtra("pelanggan", p)
                startActivity(intent)
            },
            onDelete = { p -> deletePelanggan(p) }
        )
        rvPelanggan.adapter = adapter

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        findViewById<FloatingActionButton>(R.id.fabTambah).setOnClickListener {
            startActivity(Intent(this, TambahPelangganActivity::class.java))
        }

        svPelanggan.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun loadData() {
        FirebaseDatabase.getInstance().getReference("Pelanggan")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listPelanggan.clear()
                    for (snap in snapshot.children) {
                        snap.getValue(modelPelanggan::class.java)?.let { listPelanggan.add(it) }
                    }
                    adapter.updateData(listPelanggan)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun filterList(query: String?) {
        val filtered = if (query.isNullOrEmpty()) listPelanggan
        else listPelanggan.filter { 
            it.namaPelanggan?.lowercase()?.contains(query.lowercase()) == true ||
            it.phonePelanggan?.contains(query) == true
        }
        adapter.updateData(filtered)
    }

    private fun deletePelanggan(p: modelPelanggan) {
        FirebaseDatabase.getInstance().getReference("Pelanggan")
            .child(p.idPelanggan!!).removeValue().addOnSuccessListener {
                Toast.makeText(this, "Pelanggan dihapus", Toast.LENGTH_SHORT).show()
            }
    }
}
