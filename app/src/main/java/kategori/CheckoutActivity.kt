package kategori

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import model.modelPelanggan
import model.modelTransaksi
import java.text.SimpleDateFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {

    private lateinit var etBayar: EditText
    private lateinit var tvKembalian: TextView
    private lateinit var layoutKembalian: View
    private lateinit var cgMetode: ChipGroup
    private lateinit var tvTotal: TextView
    private lateinit var btnBayar: Button

    private var listCart = mutableListOf<modelCartItem>()
    private var totalHarga = 0
    private var activeCabangId = ""
    private var kasirName = ""
    private var pelangganId = ""
    private var pelangganName = ""

    private val db = FirebaseDatabase.getInstance()
    
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
        kasirName = intent.getStringExtra("kasirName") ?: "Kasir"
        pelangganId = intent.getStringExtra("pelangganId") ?: ""
        pelangganName = intent.getStringExtra("pelangganName") ?: "Umum"

        initView()
        loadSettings()
        setupBayar()
        
        tvTotal.text = "Rp %,d".format(totalHarga)
        
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        btnBayar.setOnClickListener { saveTransaction() }
    }

    private fun initView() {
        etBayar = findViewById(R.id.etBayar)
        tvKembalian = findViewById(R.id.tvKembalian)
        layoutKembalian = findViewById(R.id.layoutKembalian)
        cgMetode = findViewById(R.id.cgMetode)
        tvTotal = findViewById(R.id.tvTotalCheckout)
        btnBayar = findViewById(R.id.btnBayarFinal)
    }

    private fun setupBayar() {
        etBayar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val bayar = s.toString().toIntOrNull() ?: 0
                if (bayar >= totalHarga && totalHarga > 0) {
                    layoutKembalian.visibility = View.VISIBLE
                    tvKembalian.text = "Rp %,d".format(bayar - totalHarga)
                } else {
                    layoutKembalian.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cgMetode.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != R.id.chipTunai) {
                etBayar.visibility = View.GONE
                layoutKembalian.visibility = View.GONE
            } else {
                etBayar.visibility = View.VISIBLE
                if ((etBayar.text.toString().toIntOrNull() ?: 0) >= totalHarga) {
                    layoutKembalian.visibility = View.VISIBLE
                }
            }
        }
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

    private fun saveTransaction() {
        val selectedMetodeId = cgMetode.checkedChipId
        if (selectedMetodeId == -1) {
            Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        val metode = findViewById<Chip>(selectedMetodeId).text.toString()
        
        if (metode == "Tunai") {
            val bayar = etBayar.text.toString().toIntOrNull() ?: 0
            if (bayar < totalHarga) {
                Toast.makeText(this, "Uang pembayaran kurang", Toast.LENGTH_SHORT).show()
                return
            }
        }

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
            idPelanggan = pelangganId,
            namaPelanggan = pelangganName,
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
        if (transaksi.namaPelanggan != "Umum / Walk-in") {
            sb.append("Pelanggan: ${transaksi.namaPelanggan}\n")
        }
        sb.append("Metode: ${transaksi.metodePembayaran}\n")
        sb.append("----------------------------\n")
        transaksi.listProduk?.forEach {
            sb.append("${it.produk?.namaProduk?.padEnd(15)} x${it.jumlah}  Rp %,d\n".format((it.produk?.hargaJual ?: 0) * it.jumlah))
        }
        sb.append("----------------------------\n")
        sb.append("Total:           Rp %,d\n".format(transaksi.totalHarga))
        
        if (transaksi.metodePembayaran == "Tunai") {
            val bayar = etBayar.text.toString().toIntOrNull() ?: 0
            sb.append("Bayar:           Rp %,d\n".format(bayar))
            sb.append("Kembalian:       Rp %,d\n".format(bayar - transaksi.totalHarga))
        }

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
