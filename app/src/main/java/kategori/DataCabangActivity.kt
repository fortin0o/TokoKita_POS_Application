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
import model.modelCabang

class DataCabangActivity : AppCompatActivity() {

    private lateinit var rvCabang: RecyclerView
    private lateinit var adapter: AdapterCabang
    private val listCabang = mutableListOf<modelCabang>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_cabang)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvCabang = findViewById(R.id.rvCabang)
        val spanCount = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) 2 else 1
        rvCabang.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, spanCount)
        
        adapter = AdapterCabang(listCabang,
            onEdit = { cabang ->
                val intent = Intent(this, TambahCabangActivity::class.java)
                intent.putExtra("cabang", cabang)
                startActivity(intent)
            },
            onDelete = { cabang ->
                deleteCabang(cabang)
            }
        )
        rvCabang.adapter = adapter

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        findViewById<FloatingActionButton>(R.id.fabTambah).setOnClickListener {
            startActivity(Intent(this, TambahCabangActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Cabang")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                for (item in snapshot.children) {
                    val cabang = item.getValue(modelCabang::class.java)
                    if (cabang != null) {
                        listCabang.add(cabang)
                    }
                }
                adapter.updateData(listCabang)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataCabangActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteCabang(cabang: modelCabang) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Cabang")
        cabang.idCabang?.let { id ->
            myRef.child(id).removeValue().addOnSuccessListener {
                Toast.makeText(this, "Cabang dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
