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
import com.google.firebase.database.*
import model.modelCabang
import model.modelTransaksi

class LaporanActivity : AppCompatActivity() {

    private lateinit var rvTransaksi: RecyclerView
    private lateinit var adapter: AdapterTransaksi
    private lateinit var tvIncome: TextView
    private lateinit var tvProfit: TextView
    private lateinit var tvCurrentCabang: TextView
    
    private val listTransaksi = mutableListOf<modelTransaksi>()
    
    private var selectedCabangId: String = ""
    private var selectedCabangNama: String = "Semua Cabang"

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
        tvCurrentCabang = findViewById(R.id.tvCurrentCabang)
        rvTransaksi = findViewById(R.id.rvTransaksi)
        
        findViewById<android.view.View>(R.id.cardCabangFilter).setOnClickListener { showCabangSelector() }

        rvTransaksi.layoutManager = LinearLayoutManager(this)
        adapter = AdapterTransaksi(listTransaksi)
        rvTransaksi.adapter = adapter

        loadData()
    }

    private fun showCabangSelector() {
        val ref = FirebaseDatabase.getInstance().getReference("Cabang")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<modelCabang>()
                list.add(modelCabang("", "Semua Cabang", "", ""))
                for (snap in snapshot.children) {
                    snap.getValue(modelCabang::class.java)?.let { list.add(it) }
                }
                
                val names = list.map { it.namaCabang ?: "" }.toTypedArray()
                
                android.app.AlertDialog.Builder(this@LaporanActivity)
                    .setTitle("Pilih Cabang Laporan")
                    .setItems(names) { _, which ->
                        val selected = list[which]
                        selectedCabangId = selected.idCabang ?: ""
                        selectedCabangNama = selected.namaCabang ?: "Semua Cabang"
                        tvCurrentCabang.text = selectedCabangNama
                        loadData()
                    }
                    .show()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
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
                        // Filter by branch
                        if (selectedCabangId.isEmpty() || trx.idCabang == selectedCabangId) {
                            listTransaksi.add(trx)
                            income += trx.totalHarga
                            trx.listProduk?.forEach { item ->
                                val jual = item.produk?.hargaJual ?: 0
                                val beli = item.produk?.hargaBeli ?: 0
                                profit += (jual - beli) * item.jumlah
                            }
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
