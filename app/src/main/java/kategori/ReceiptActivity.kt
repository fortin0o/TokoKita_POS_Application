package kategori

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.firebase.database.*
import model.modelTransaksi

class ReceiptActivity : AppCompatActivity() {

    private lateinit var tvOrderId: TextView
    private lateinit var tvStoreName: TextView
    private lateinit var tvStoreHeader: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvKasir: TextView
    private lateinit var tvPelanggan: TextView
    private lateinit var itemContainer: LinearLayout
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvMetodeLabel: TextView
    private lateinit var tvMetodeValue: TextView
    private lateinit var layoutTunai: View
    private lateinit var tvUangDiterima: TextView
    private lateinit var tvKembalian: TextView
    private lateinit var tvFooter: TextView
    private lateinit var btnSelesai: Button
    private lateinit var btnBagikan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_receipt)

        val transaksi = intent.getParcelableExtra<modelTransaksi>("transaksi")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        loadSettings()

        if (transaksi != null) {
            populateData(transaksi)
        }

        btnSelesai.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
        
        btnBagikan.setOnClickListener {
            Toast.makeText(this, "Membagikan struk...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        tvOrderId = findViewById(R.id.tvOrderId)
        tvStoreName = findViewById(R.id.tvStoreName)
        tvStoreHeader = findViewById(R.id.tvStoreHeader)
        tvDate = findViewById(R.id.tvDate)
        tvKasir = findViewById(R.id.tvKasir)
        tvPelanggan = findViewById(R.id.tvPelanggan)
        itemContainer = findViewById(R.id.itemContainer)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotal = findViewById(R.id.tvTotal)
        tvMetodeLabel = findViewById(R.id.tvMetodeLabel)
        tvMetodeValue = findViewById(R.id.tvMetodeValue)
        layoutTunai = findViewById(R.id.layoutTunaiDetails)
        tvUangDiterima = findViewById(R.id.tvUangDiterima)
        tvKembalian = findViewById(R.id.tvKembalian)
        tvFooter = findViewById(R.id.tvFooter)
        btnSelesai = findViewById(R.id.btnSelesai)
        btnBagikan = findViewById(R.id.btnBagikan)
    }

    private fun loadSettings() {
        FirebaseDatabase.getInstance().getReference("Settings").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    tvStoreName.text = snapshot.child("namaToko").value?.toString() ?: "TOKOKITA POS"
                    tvStoreHeader.text = snapshot.child("headerStruk").value?.toString() ?: "Solusi Point of Sale Pintar"
                    val footer = snapshot.child("footerStruk").value?.toString() ?: ""
                    if (footer.isNotEmpty()) tvFooter.text = footer
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun populateData(trx: modelTransaksi) {
        tvOrderId.text = "Order ID: ${trx.idTransaksi}"
        tvDate.text = trx.tanggal
        tvKasir.text = trx.namaPegawai
        tvPelanggan.text = trx.namaPelanggan ?: "Umum"
        
        tvSubtotal.text = "Rp %,d".format(trx.totalHarga)
        tvTotal.text = "Rp %,d".format(trx.totalHarga)
        
        tvMetodeLabel.text = "Metode Pembayaran (${trx.metodePembayaran})"
        tvMetodeValue.text = trx.metodePembayaran
        
        if (trx.metodePembayaran == "Tunai") {
            layoutTunai.visibility = View.VISIBLE
            tvUangDiterima.text = "Rp %,d".format(trx.uangDiterima)
            tvKembalian.text = "Rp %,d".format(trx.uangDiterima - trx.totalHarga)
        } else {
            layoutTunai.visibility = View.GONE
        }

        itemContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        trx.listProduk?.forEach { item ->
            val itemView = inflater.inflate(R.layout.item_receipt_row, itemContainer, false)
            itemView.findViewById<TextView>(R.id.tvItemName).text = item.produk?.namaProduk
            itemView.findViewById<TextView>(R.id.tvItemTotal).text = "Rp %,d".format((item.produk?.hargaJual ?: 0) * item.jumlah)
            itemView.findViewById<TextView>(R.id.tvItemSubDetail).text = "${item.jumlah} x Rp %,d".format(item.produk?.hargaJual ?: 0)
            itemContainer.addView(itemView)
        }
    }
}
