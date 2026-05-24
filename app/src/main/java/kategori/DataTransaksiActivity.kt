package kategori

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import adapter.CartAdapter
import adapter.ProdukTransaksiAdapter
import com.donald.aplikasikedua.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.*
import model.modelCartItem
import model.modelKategori
import model.modelProduk
import model.modelTransaksi
import java.text.SimpleDateFormat
import java.util.*

class DataTransaksiActivity : AppCompatActivity() {

    private lateinit var svProduk: SearchView
    private lateinit var cgKategori: ChipGroup
    private lateinit var rvProdukSelection: RecyclerView
    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var tvCountItem: TextView
    private lateinit var btnBayar: Button
    private lateinit var btnHapus: Button

    private lateinit var adapterProduk: ProdukTransaksiAdapter
    private lateinit var adapterCart: CartAdapter

    private val listProduk = mutableListOf<modelProduk>()
    private val listCart = mutableListOf<modelCartItem>()

    private val db = FirebaseDatabase.getInstance()
    private var selectedKategoriId: String = "Semua"
    private var activeCabangId: String = ""

    private var namaToko: String = "TokoKita"
    private var headerStruk: String = ""
    private var footerStruk: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        activeCabangId = getSharedPreferences("TokoKita", MODE_PRIVATE).getString("cabangId", "") ?: ""

        initView()
        loadSettings()
        setupRecyclerViews()
        loadKategori()
        loadProduk()
        setupSearch()
        setupActions()
    }

    private fun initView() {
        svProduk = findViewById(R.id.svProduk)
        cgKategori = findViewById(R.id.cgKategori)
        rvProdukSelection = findViewById(R.id.rvProdukSelection)
        rvCart = findViewById(R.id.rvCart)
        tvTotal = findViewById(R.id.tvTotal)
        tvCountItem = findViewById(R.id.tvCountItem)
        
        btnBayar = findViewById(R.id.btnBayar)
        btnHapus = findViewById(R.id.btnHapus)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.ivHistory).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
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

    private fun setupRecyclerViews() {
        adapterProduk = ProdukTransaksiAdapter(emptyList()) { produk ->
            addToCart(produk)
        }
        rvProdukSelection.layoutManager = GridLayoutManager(this, 2)
        rvProdukSelection.adapter = adapterProduk

        adapterCart = CartAdapter(listCart) {
            updateSummary()
        }
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapterCart
    }

    private fun loadKategori() {
        db.getReference("kategori").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cgKategori.removeAllViews()
                
                // Add "Semua" chip
                val chipSemua = Chip(this@DataTransaksiActivity)
                chipSemua.text = "Semua"
                chipSemua.isCheckable = true
                chipSemua.isChecked = true
                chipSemua.setOnClickListener { 
                    selectedKategoriId = "Semua"
                    filterProduk()
                }
                cgKategori.addView(chipSemua)

                for (snap in snapshot.children) {
                    val kategori = snap.getValue(modelKategori::class.java)
                    if (kategori != null && kategori.statusKategori == "Aktif") {
                        val chip = Chip(this@DataTransaksiActivity)
                        chip.text = kategori.namaKategori
                        chip.isCheckable = true
                        chip.setOnClickListener {
                            selectedKategoriId = kategori.idKategori ?: "Semua"
                            filterProduk()
                        }
                        cgKategori.addView(chip)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadProduk() {
        db.getReference("produk").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()
                for (snap in snapshot.children) {
                    val produk = snap.getValue(modelProduk::class.java)
                    if (produk != null && produk.statusProduk == "Tersedia (Ready)") {
                        listProduk.add(produk)
                    }
                }
                filterProduk()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupSearch() {
        svProduk.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterProduk()
                return true
            }
        })
    }

    private fun filterProduk() {
        val query = svProduk.query.toString().lowercase()
        val filtered = listProduk.filter { produk ->
            val matchKategori = selectedKategoriId == "Semua" || produk.idKategori == selectedKategoriId
            val matchCabang = activeCabangId.isEmpty() || produk.idCabang == activeCabangId
            val matchSearch = produk.namaProduk?.lowercase()?.contains(query) == true || produk.barcode?.lowercase()?.contains(query) == true
            matchKategori && matchCabang && matchSearch
        }
        adapterProduk.updateData(filtered)
    }

    private fun addToCart(produk: modelProduk) {
        val existing = listCart.find { it.produk?.idProduk == produk.idProduk }
        if (existing != null) {
            existing.jumlah++
        } else {
            listCart.add(modelCartItem(produk, 1))
        }
        adapterCart.notifyDataSetChanged()
        updateSummary()
    }

    private fun updateSummary() {
        var subtotalVal = 0
        var count = 0
        for (item in listCart) {
            subtotalVal += (item.produk?.hargaJual ?: 0) * item.jumlah
            count += item.jumlah
        }

        tvTotal.text = "Rp %,d".format(subtotalVal)
        tvCountItem.text = "$count item"
    }

    private fun setupActions() {
        btnHapus.setOnClickListener {
            listCart.clear()
            adapterCart.notifyDataSetChanged()
            updateSummary()
        }

        btnBayar.setOnClickListener {
            if (listCart.isEmpty()) {
                Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val ref = db.getReference("transaksi")
        val id = ref.push().key ?: return
        
        var subtotal = 0
        for (item in listCart) {
            subtotal += (item.produk?.hargaJual ?: 0) * item.jumlah
        }

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        val transaksi = modelTransaksi(
            idTransaksi = id,
            listProduk = listCart.toList(),
            subtotal = subtotal,
            totalHarga = subtotal,
            diskon = 0,
            tanggal = date,
            idCabang = activeCabangId,
            namaPegawai = "Kasir"
        )

        ref.child(id).setValue(transaksi).addOnSuccessListener {
            updateStock()
            showReceipt(transaksi)
            
            listCart.clear()
            adapterCart.notifyDataSetChanged()
            updateSummary()
        }.addOnFailureListener {
            Toast.makeText(this, "Transaksi Gagal", Toast.LENGTH_SHORT).show()
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
        }
        
        btnTutup.setOnClickListener { dialog.dismiss() }
        
        dialog.show()
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
