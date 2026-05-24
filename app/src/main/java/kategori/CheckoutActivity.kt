package kategori

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.*
import model.modelCartItem
import model.modelPegawai
import model.modelTransaksi
import java.text.SimpleDateFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {

    private lateinit var actKasir: AutoCompleteTextView
    private lateinit var actPelanggan: AutoCompleteTextView
    private lateinit var cgMetode: ChipGroup
    private lateinit var tvTotal: TextView
    private lateinit var btnBayar: Button

    private var listCart = mutableListOf<modelCartItem>()
    private var totalHarga = 0
    private var activeCabangId = ""

    private val db = FirebaseDatabase.getInstance()
    private val listPegawai = mutableListOf<modelPegawai>()
    
    private var namaToko = "TokoKita"
    private var headerStruk = ""
    private var footerStruk = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listCart = intent.getParcelableArrayListExtra<modelCartItem>("cart") ?: mutableListOf()
        totalHarga = intent.getIntExtra("total", 0)
        activeCabangId = intent.getStringExtra("cabangId") ?: ""

        initView()
        loadSettings()
        loadPegawai()
        // Pelanggan logic could be added here similarly
        
        tvTotal.text = "Rp %,d".format(totalHarga)
        
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        btnBayar.setOnClickListener { saveTransaction() }
    }

    private fun initView() {
        actKasir = findViewById(R.id.actKasir)
        actPelanggan = findViewById(R.id.actPelanggan)
        cgMetode = findViewById(R.id.cgMetode)
        tvTotal = findViewById(R.id.tvTotalCheckout)
        btnBayar = findViewById(R.id.btnBayarFinal)
    }

    private fun loadSettings() {
        db.getReference("Settings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    namaToko = snapshot.child("namaToko").value?.toString() ?: "TokoKita"
                    headerStruk = snapshot.child("headerStruk").value?.toString() ?: ""
                    footerStruk = snapshot.child("footerStruk").value?.toString() ?: ""
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadPegawai() {
        db.getReference("Pegawai").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPegawai.clear()
                val names = mutableListOf<String>()
                for (snap in snapshot.children) {
                    val p = snap.getValue(modelPegawai::class.java)
                    if (p != null && p.statusPegawai == "Aktif") {
                        listPegawai.add(p)
                        names.add(p.namaPegawai ?: "")
                    }
                }
                val adapter = ArrayAdapter(this@CheckoutActivity, android.R.layout.simple_dropdown_item_1line, names)
                actKasir.setAdapter(adapter)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun saveTransaction() {
        val kasirName = actKasir.text.toString()
        if (kasirName == "Pilih Kasir" || kasirName.isEmpty()) {
            Toast.makeText(this, "Pilih Kasir terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedMetodeId = cgMetode.checkedChipId
        val metode = findViewById<Chip>(selectedMetodeId).text.toString()

        val ref = db.getReference("transaksi")
        val id = ref.push().key ?: return
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        val transaksi = modelTransaksi(
            idTransaksi = id,
            listProduk = listCart.toList(),
            subtotal = totalHarga,
            totalHarga = totalHarga,
            diskon = 0,
            tanggal = date,
            idCabang = activeCabangId,
            namaPegawai = kasirName,
            metodePembayaran = metode
        )

        ref.child(id).setValue(transaksi).addOnSuccessListener {
            updateStock()
            showReceipt(transaksi)
        }.addOnFailureListener {
            Toast.makeText(this, "Transaksi Gagal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStock() {
        for (item in listCart) {
            val produk = item.produk ?: continue
            if (produk.tanpaBatas == true) continue
            val newStok = (produk.stokProduk ?: 0) - item.jumlah
            db.getReference("produk").child(produk.idProduk!!).child("stokProduk").setValue(newStok)
        }
    }

    private fun showReceipt(transaksi: modelTransaksi) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_receipt)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val tvContent = dialog.findViewById<TextView>(R.id.tvReceiptContent)
        val btnCetak = dialog.findViewById<Button>(R.id.btnCetak)
        val btnTutup = dialog.findViewById<Button>(R.id.btnTutup)
        
        val sb = StringBuilder()
        sb.append("$namaToko\n")
        if (headerStruk.isNotEmpty()) sb.append("$headerStruk\n")
        sb.append("Tanggal: ${transaksi.tanggal}\n")
        sb.append("Kasir: ${transaksi.namaPegawai}\n")
        sb.append("Metode: ${transaksi.metodePembayaran}\n")
        sb.append("----------------------------\n")
        transaksi.listProduk?.forEach {
            sb.append("${it.produk?.namaProduk?.padEnd(15)} x${it.jumlah}  Rp %,d\n".format((it.produk?.hargaJual ?: 0) * it.jumlah))
        }
        sb.append("----------------------------\n")
        sb.append("Total:           Rp %,d\n".format(transaksi.totalHarga))
        sb.append("----------------------------\n")
        if (footerStruk.isNotEmpty()) sb.append("$footerStruk\n")
        else sb.append("Terima Kasih!\n")
        
        tvContent.text = sb.toString()
        
        btnCetak.setOnClickListener {
            Toast.makeText(this, "Mencetak struk...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            finishWithResult()
        }
        
        btnTutup.setOnClickListener { 
            dialog.dismiss()
            finishWithResult()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun finishWithResult() {
        setResult(RESULT_OK)
        finish()
    }
}
