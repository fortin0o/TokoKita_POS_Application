package kategori

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import adapter.AdapterTransaksi
import com.donald.aplikasikedua.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import model.modelTransaksi

class LaporanActivity : AppCompatActivity() {

    private lateinit var rvTransaksi: RecyclerView
    private lateinit var adapter: AdapterTransaksi
    private val listTransaksi = mutableListOf<modelTransaksi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        rvTransaksi = findViewById(R.id.rvTransaksi)
        rvTransaksi.layoutManager = LinearLayoutManager(this)
        adapter = AdapterTransaksi(listTransaksi)
        rvTransaksi.adapter = adapter

        loadData()
    }

    private fun loadData() {
        val ref = FirebaseDatabase.getInstance().getReference("transaksi")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listTransaksi.clear()
                for (snap in snapshot.children) {
                    val trx = snap.getValue(modelTransaksi::class.java)
                    if (trx != null) {
                        listTransaksi.add(trx)
                    }
                }
                listTransaksi.sortByDescending { it.tanggal }
                adapter.updateData(listTransaksi)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
