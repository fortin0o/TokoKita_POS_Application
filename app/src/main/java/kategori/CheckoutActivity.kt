package kategori

import android.content.Intent
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
    private lateinit var scrollQuickPay: View
    private lateinit var cgMetode: ChipGroup
    private lateinit var tvTotal: TextView
    private lateinit var btnBayar: Button

    private lateinit var btnUangPas: Button
    private lateinit var btnQuick50: Button
    private lateinit var btnQuick100: Button

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
        scrollQuickPay = findViewById(R.id.scrollQuickPay)
        cgMetode = findViewById(R.id.cgMetode)
        tvTotal = findViewById(R.id.tvTotalCheckout)
        btnBayar = findViewById(R.id.btnBayarFinal)

        btnUangPas = findViewById(R.id.btnUangPas)
        btnQuick50 = findViewById(R.id.btnQuick50)
        btnQuick100 = findViewById(R.id.btnQuick100)
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
                scrollQuickPay.visibility = View.GONE
            } else {
                etBayar.visibility = View.VISIBLE
                scrollQuickPay.visibility = View.VISIBLE
                if ((etBayar.text.toString().toIntOrNull() ?: 0) >= totalHarga) {
                    layoutKembalian.visibility = View.VISIBLE
                }
            }
        }

        btnUangPas.setOnClickListener {
            etBayar.setText(totalHarga.toString())
        }

        btnQuick50.setOnClickListener {
            etBayar.setText("50000")
        }

        btnQuick100.setOnClickListener {
            etBayar.setText("100000")
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
        val bayar = if (metode == "Tunai") etBayar.text.toString().toIntOrNull() ?: 0 else totalHarga
        
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
            metodePembayaran = metode,
            uangDiterima = bayar
        )

        ref.child(id).setValue(transaksi).addOnSuccessListener {
            updateStock()
            
            // Pass the transaction data back to DataTransaksiActivity
            val resultIntent = Intent()
            resultIntent.putExtra("transaksi", transaksi)
            setResult(RESULT_OK, resultIntent)
            finish()

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
}
