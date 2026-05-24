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
    private lateinit var fabCart: com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

    private lateinit var adapterProduk: ProdukTransaksiAdapter
    private lateinit var adapterCart: CartAdapter

    private val listProduk = mutableListOf<modelProduk>()
    private val listCart = mutableListOf<modelCartItem>()

    private val db = FirebaseDatabase.getInstance()
    private var selectedKategoriId: String = "Semua"
    private var activeCabangId: String = ""

    private var selectedKasirName: String = ""
    private var selectedPelangganId: String = ""
    private var selectedPelangganName: String = "Umum"

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
        fabCart = findViewById(R.id.fabCart)
        
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.ivHistory).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
    }

    private fun setupRecyclerViews() {
        adapterProduk = ProdukTransaksiAdapter(emptyList()) { produk ->
            addToCart(produk)
        }
        rvProdukSelection.layoutManager = GridLayoutManager(this, if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) 4 else 2)
        rvProdukSelection.adapter = adapterProduk
        
        adapterCart = CartAdapter(listCart) {
            updateCartUI()
        }
    }

    private fun loadKategori() {
        db.getReference("kategori").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cgKategori.removeAllViews()
                
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
            
            // Logic Fix: Product shows if it matches active branch OR if it is set to "Semua Cabang" (idCabang is empty)
            val matchCabang = activeCabangId.isEmpty() || produk.idCabang == activeCabangId || produk.idCabang.isNullOrEmpty()

            val matchSearch = produk.namaProduk?.lowercase()?.contains(query) == true || produk.barcode?.lowercase()?.contains(query) == true
            matchKategori && matchCabang && matchSearch
        }
        adapterProduk.updateData(filtered)
    }

    private fun addToCart(produk: modelProduk) {
        val existing = listCart.find { it.produk?.idProduk == produk.idProduk }
        val currentQty = existing?.jumlah ?: 0
        
        // Logic check for stock
        if (produk.tanpaBatas != true) {
            val stock = produk.stokProduk ?: 0
            if (currentQty + 1 > stock) {
                Toast.makeText(this, "Stok tidak mencukupi (Tersisa $stock)", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (existing != null) {
            existing.jumlah++
        } else {
            listCart.add(modelCartItem(produk, 1))
        }
        updateCartUI()
    }

    private fun updateCartUI() {
        var total = 0
        var count = 0
        for (item in listCart) {
            total += (item.produk?.hargaJual ?: 0) * item.jumlah
            count += item.jumlah
        }
        fabCart.text = "$count Items - Rp %,d".format(total)
        if (count > 0) fabCart.show() else fabCart.hide()
    }

    private fun setupActions() {
        fabCart.setOnClickListener {
            showCartBottomSheet()
        }
    }

    private fun showCartBottomSheet() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_cart_bottom_sheet, null)
        
        val rv = view.findViewById<RecyclerView>(R.id.rvCart)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotal)
        val btnCheckout = view.findViewById<Button>(R.id.btnCheckout)
        val btnHapus = view.findViewById<Button>(R.id.btnHapus)
        val actKasir = view.findViewById<AutoCompleteTextView>(R.id.actKasir)
        val actPelanggan = view.findViewById<AutoCompleteTextView>(R.id.actPelanggan)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapterCart

        // Load Kasir
        db.getReference("Pegawai").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val names = mutableListOf<String>()
                for (snap in snapshot.children) {
                    val p = snap.getValue(model.modelPegawai::class.java)
                    if (p != null && p.statusPegawai == "Aktif") names.add(p.namaPegawai ?: "")
                }
                val adapter = ArrayAdapter(this@DataTransaksiActivity, android.R.layout.simple_dropdown_item_1line, names)
                actKasir.setAdapter(adapter)
                if (selectedKasirName.isNotEmpty()) actKasir.setText(selectedKasirName, false)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Load Pelanggan
        db.getReference("Pelanggan").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listP = mutableListOf<model.modelPelanggan>()
                val names = mutableListOf<String>()
                names.add("Umum")
                for (snap in snapshot.children) {
                    val p = snap.getValue(model.modelPelanggan::class.java)
                    if (p != null && p.statusPelanggan == "Aktif") {
                        listP.add(p)
                        names.add(p.namaPelanggan ?: "")
                    }
                }
                val adapter = ArrayAdapter(this@DataTransaksiActivity, android.R.layout.simple_dropdown_item_1line, names)
                actPelanggan.setAdapter(adapter)
                actPelanggan.setText(selectedPelangganName, false)

                actPelanggan.setOnItemClickListener { _, _, pos, _ ->
                    if (pos == 0) {
                        selectedPelangganId = ""
                        selectedPelangganName = "Umum"
                    } else {
                        selectedPelangganId = listP[pos - 1].idPelanggan ?: ""
                        selectedPelangganName = listP[pos - 1].namaPelanggan ?: "Umum"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        actKasir.setOnItemClickListener { _, _, _, _ ->
            selectedKasirName = actKasir.text.toString()
        }
        
        fun refreshPopup() {
            var total = 0
            for (item in listCart) {
                total += (item.produk?.hargaJual ?: 0) * item.jumlah
            }
            tvTotal.text = "Rp %,d".format(total)
            if (listCart.isEmpty()) dialog.dismiss()
            updateCartUI()
        }
        
        refreshPopup()

        btnHapus.setOnClickListener {
            listCart.clear()
            adapterCart.notifyDataSetChanged()
            updateCartUI()
            dialog.dismiss()
        }

        btnCheckout.setOnClickListener {
            if (selectedKasirName.isEmpty() || selectedKasirName == "Kasir") {
                Toast.makeText(this, "Pilih Kasir terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putParcelableArrayListExtra("cart", ArrayList(listCart))
            var total = 0
            for (item in listCart) total += (item.produk?.hargaJual ?: 0) * item.jumlah
            intent.putExtra("total", total)
            intent.putExtra("cabangId", activeCabangId)
            intent.putExtra("kasirName", selectedKasirName)
            intent.putExtra("pelangganId", selectedPelangganId)
            intent.putExtra("pelangganName", selectedPelangganName)
            startActivityForResult(intent, 100)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            listCart.clear()
            adapterCart.notifyDataSetChanged()
            updateCartUI()
        }
    }
}
