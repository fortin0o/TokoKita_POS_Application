package kategori

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
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
    private lateinit var tvIncome: TextView
    private lateinit var tvProfit: TextView
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

        tvIncome = findViewById(R.id.tvIncome)
        tvProfit = findViewById(R.id.tvProfit)
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
                var income = 0
                var profit = 0

                for (snap in snapshot.children) {
                    val trx = snap.getValue(modelTransaksi::class.java)
                    if (trx != null) {
                        listTransaksi.add(trx)
                        income += trx.totalHarga
                        trx.listProduk?.forEach { item ->
                            val jual = item.produk?.hargaJual ?: 0
                            val beli = item.produk?.hargaBeli ?: 0
                            profit += (jual - beli) * item.jumlah
                        }
                    }
                }
                listTransaksi.sortByDescending { it.tanggal }
                adapter.updateData(listTransaksi)
                
                tvIncome.text = "Rp %,d".format(income)
                tvProfit.text = "Rp %,d".format(profit)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
