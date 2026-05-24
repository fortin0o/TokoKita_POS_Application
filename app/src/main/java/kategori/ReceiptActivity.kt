package kategori

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.firebase.database.*
import model.modelTransaksi
import java.io.File
import java.io.FileOutputStream

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
    private lateinit var btnCetak: Button

    private var autoPrint = false
    private var printerAddress: String? = null
    private var showLogoInPrint = true
    
    private var namaToko = "TOKOKITA POS"
    private var headerStruk = ""

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
        loadPrinterSettings()

        if (transaksi != null) {
            populateData(transaksi)
            if (autoPrint) {
                printReceipt(transaksi)
            }
        }

        btnSelesai.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
        
        btnBagikan.setOnClickListener {
            if (transaksi != null) {
                shareReceipt(transaksi)
            }
        }

        btnCetak.setOnClickListener {
            if (transaksi != null) {
                printReceipt(transaksi)
            }
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
        btnCetak = findViewById(R.id.btnCetak)
    }

    private fun loadSettings() {
        FirebaseDatabase.getInstance().getReference("Settings").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    namaToko = snapshot.child("namaToko").value?.toString() ?: "TOKOKITA POS"
                    headerStruk = snapshot.child("headerStruk").value?.toString() ?: ""
                    
                    tvStoreName.text = namaToko
                    tvStoreHeader.text = if (headerStruk.isNotEmpty()) headerStruk else "Solusi Point of Sale Pintar"
                    
                    val footer = snapshot.child("footerStruk").value?.toString() ?: ""
                    if (footer.isNotEmpty()) tvFooter.text = footer
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadPrinterSettings() {
        val prefs = getSharedPreferences("PrinterSettings", android.content.Context.MODE_PRIVATE)
        autoPrint = prefs.getBoolean("autoPrint", false)
        printerAddress = prefs.getString("printerAddress", null)
        showLogoInPrint = prefs.getBoolean("showLogo", true)
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

    private fun shareReceipt(trx: modelTransaksi) {
        val sb = StringBuilder()
        sb.append("===== $namaToko =====\n")
        if (headerStruk.isNotEmpty()) sb.append("$headerStruk\n")
        sb.append("ID: ${trx.idTransaksi}\n")
        sb.append("Tgl: ${trx.tanggal}\n")
        sb.append("Kasir: ${trx.namaPegawai}\n")
        sb.append("Plg: ${trx.namaPelanggan ?: "Umum"}\n")
        sb.append("--------------------------------\n")
        
        trx.listProduk?.forEach { item ->
            val subtotal = (item.produk?.hargaJual ?: 0) * item.jumlah
            sb.append("${item.produk?.namaProduk}\n")
            sb.append("${item.jumlah} x ${item.produk?.hargaJual} = Rp %,d\n".format(subtotal))
        }
        
        sb.append("--------------------------------\n")
        sb.append("TOTAL: Rp %,d\n".format(trx.totalHarga))
        
        if (trx.metodePembayaran == "Tunai") {
            sb.append("Bayar: Rp %,d\n".format(trx.uangDiterima))
            sb.append("Kembali: Rp %,d\n".format(trx.uangDiterima - trx.totalHarga))
        }
        
        sb.append("--------------------------------\n")
        val footer = tvFooter.text.toString()
        sb.append("$footer\n")

        try {
            val file = File(getExternalFilesDir(null), "Struk_${trx.idTransaksi}.txt")
            val out = FileOutputStream(file)
            out.write(sb.toString().toByteArray())
            out.close()

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(Intent.EXTRA_TEXT, sb.toString())
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Bagikan Struk"))
            
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membagikan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun printReceipt(trx: modelTransaksi) {
        if (printerAddress == null) {
            Toast.makeText(this, "Printer belum diatur. Silakan atur di Pengaturan Printer.", Toast.LENGTH_LONG).show()
            return
        }

        val sb = StringBuilder()
        if (showLogoInPrint) sb.append("[LOGO]\n")
        sb.append("$namaToko\n")
        sb.append("$headerStruk\n")
        sb.append("--------------------------------\n")
        sb.append("ID: ${trx.idTransaksi}\n")
        sb.append("Tgl: ${trx.tanggal}\n")
        sb.append("Kasir: ${trx.namaPegawai}\n")
        sb.append("Plg: ${trx.namaPelanggan ?: "Umum"}\n")
        sb.append("--------------------------------\n")
        
        trx.listProduk?.forEach { item ->
            sb.append("${item.produk?.namaProduk}\n")
            val qtyPrice = "${item.jumlah} x ${item.produk?.hargaJual}"
            val total = "Rp %,d".format((item.produk?.hargaJual ?: 0) * item.jumlah)
            sb.append(qtyPrice.padEnd(32 - total.length) + total + "\n")
        }
        
        sb.append("--------------------------------\n")
        val totalLabel = "TOTAL"
        val totalValue = "Rp %,d".format(trx.totalHarga)
        sb.append(totalLabel.padEnd(32 - totalValue.length) + totalValue + "\n")
        
        if (trx.metodePembayaran == "Tunai") {
            val bayarLabel = "Bayar"
            val bayarValue = "Rp %,d".format(trx.uangDiterima)
            sb.append(bayarLabel.padEnd(32 - bayarValue.length) + bayarValue + "\n")
            
            val kembaliLabel = "Kembali"
            val kembaliValue = "Rp %,d".format(trx.uangDiterima - trx.totalHarga)
            sb.append(kembaliLabel.padEnd(32 - kembaliValue.length) + kembaliValue + "\n")
        }
        
        sb.append("--------------------------------\n")
        sb.append("${tvFooter.text}\n\n\n")

        Toast.makeText(this, "Mengirim data ke printer: ${getSharedPreferences("PrinterSettings", android.content.Context.MODE_PRIVATE).getString("printerName", "Printer")}", Toast.LENGTH_SHORT).show()
    }
}
