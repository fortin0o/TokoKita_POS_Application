package kategori

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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
import model.modelPegawai

class DataPegawaiActivity : AppCompatActivity() {

    private lateinit var rvPegawai: RecyclerView
    private lateinit var adapter: AdapterPegawai
    private val listPegawai = mutableListOf<modelPegawai>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pegawai)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvPegawai = findViewById(R.id.rvPegawai)
        rvPegawai.layoutManager = LinearLayoutManager(this)
        
        adapter = AdapterPegawai(listPegawai, 
            onEdit = { pegawai ->
                val intent = Intent(this, TambahPegawaiActivity::class.java)
                intent.putExtra("pegawai", pegawai)
                startActivity(intent)
            },
            onDelete = { pegawai ->
                // Handle Delete
                deletePegawai(pegawai)
            }
        )
        rvPegawai.adapter = adapter

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        findViewById<FloatingActionButton>(R.id.fabTambah).setOnClickListener {
            startActivity(Intent(this, TambahPegawaiActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Pegawai")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPegawai.clear()
                for (item in snapshot.children) {
                    val pegawai = item.getValue(modelPegawai::class.java)
                    if (pegawai != null) {
                        listPegawai.add(pegawai)
                    }
                }
                adapter.updateData(listPegawai)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPegawaiActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deletePegawai(pegawai: modelPegawai) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Pegawai")
        
        pegawai.idPegawai?.let { id ->
            myRef.child(id).removeValue().addOnSuccessListener {
                Toast.makeText(this, "Pegawai dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
