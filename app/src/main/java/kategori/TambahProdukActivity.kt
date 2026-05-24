package kategori

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.donald.aplikasikedua.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import model.modelCabang
import model.modelKategori
import model.modelProduk

class TambahProdukActivity : AppCompatActivity() {

    private val db = FirebaseDatabase.getInstance()
    private val ref = db.getReference("produk")

    private lateinit var etNama: TextInputEditText
    private lateinit var etBarcode: TextInputEditText
    private lateinit var etFoto: TextInputEditText
    private lateinit var etHargaBeli: TextInputEditText
    private lateinit var etNilaiProfit: TextInputEditText
    private lateinit var etHargaJual: TextInputEditText
    private lateinit var etStok: TextInputEditText

    private lateinit var spinnerProfit: AutoCompleteTextView
    private lateinit var actCabang: AutoCompleteTextView
    private lateinit var actKategori: AutoCompleteTextView
    private lateinit var cbTanpaBatas: MaterialCheckBox
    private lateinit var cgStatus: ChipGroup
    private lateinit var btnSimpan: MaterialButton

    private var idKategori: String = ""
    private var idCabang: String = ""

    private var produkExisting: modelProduk? = null

    private val listKategori = mutableListOf<modelKategori>()
    private val listCabang = mutableListOf<modelCabang>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_produk)

        produkExisting = intent.getParcelableExtra("produk")

        initView()
        setupDropdown()
        setupAutoHitung()
        loadKategori()
        loadCabang()
        setupAction()
        
        fillData()
    }

    private fun fillData() {
        produkExisting?.let {
            findViewById<TextView>(R.id.tvJudul).text = "Ubah Menu"
            etNama.setText(it.namaProduk)
            etBarcode.setText(it.barcode)
            etFoto.setText(it.fotoProduk)
            etHargaBeli.setText(it.hargaBeli.toString())
            etNilaiProfit.setText(it.nilaiProfit.toString())
            etHargaJual.setText(it.hargaJual.toString())
            etStok.setText(it.stokProduk.toString())
            cbTanpaBatas.isChecked = it.tanpaBatas ?: false
            
            spinnerProfit.setText(it.tipeKeuntungan, false)
            
            idKategori = it.idKategori ?: ""
            idCabang = it.idCabang ?: ""
            
            if (it.statusProduk == "Tersedia (Ready)") {
                cgStatus.check(R.id.chipReady)
            } else {
                cgStatus.check(R.id.chipHabis)
            }
        }
    }

    private fun initView() {
        etNama = findViewById(R.id.et_nama_produk)
        etBarcode = findViewById(R.id.et_barcode)
        etFoto = findViewById(R.id.et_foto_produk)
        etHargaBeli = findViewById(R.id.et_harga_beli)
        etNilaiProfit = findViewById(R.id.et_nilai_profit)
        etHargaJual = findViewById(R.id.et_harga_jual)
        etStok = findViewById(R.id.et_stok)

        spinnerProfit = findViewById(R.id.spinner_tipe_keuntungan)
        actCabang = findViewById(R.id.actCabang)
        actKategori = findViewById(R.id.actKategori)
        cbTanpaBatas = findViewById(R.id.cb_stok_tanpa_batas)
        cgStatus = findViewById(R.id.cgStatus)
        btnSimpan = findViewById(R.id.btn_simpan)
        
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
    }

    // 🔽 Dropdown kategori
    private fun loadKategori() {
        db.getReference("kategori").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKategori.clear()
                val names = mutableListOf<String>()
                for (item in snapshot.children) {
                    val kategori = item.getValue(modelKategori::class.java)
                    if (kategori != null) {
                        listKategori.add(kategori)
                        names.add(kategori.namaKategori ?: "")
                    }
                }
                val adapter = ArrayAdapter(this@TambahProdukActivity, android.R.layout.simple_dropdown_item_1line, names)
                actKategori.setAdapter(adapter)
                
                if (listKategori.isNotEmpty()) {
                    if (idKategori.isEmpty()) {
                        actKategori.setText(listKategori[0].namaKategori, false)
                        idKategori = listKategori[0].idKategori ?: ""
                    } else {
                        val current = listKategori.find { it.idKategori == idKategori }
                        current?.let { actKategori.setText(it.namaKategori, false) }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        actKategori.setOnItemClickListener { _, _, position, _ ->
            idKategori = listKategori[position].idKategori ?: ""
        }
    }

    private fun loadCabang() {
        db.getReference("Cabang").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                val names = mutableListOf<String>()
                
                // Add "Semua Cabang" option
                names.add("Semua Cabang")
                listCabang.add(modelCabang("", "Semua Cabang", "", ""))

                for (item in snapshot.children) {
                    val cabang = item.getValue(modelCabang::class.java)
                    if (cabang != null) {
                        listCabang.add(cabang)
                        names.add(cabang.namaCabang ?: "")
                    }
                }
                val adapter = ArrayAdapter(this@TambahProdukActivity, android.R.layout.simple_dropdown_item_1line, names)
                actCabang.setAdapter(adapter)
                
                if (listCabang.isNotEmpty()) {
                    if (idCabang.isEmpty()) {
                        actCabang.setText(listCabang[0].namaCabang, false)
                        idCabang = listCabang[0].idCabang ?: ""
                    } else {
                        val current = listCabang.find { it.idCabang == idCabang }
                        current?.let { actCabang.setText(it.namaCabang, false) }
                        // If not found (e.g. was "Semua Cabang" but model stored empty string), default to first
                        if (current == null) {
                             actCabang.setText(listCabang[0].namaCabang, false)
                             idCabang = listCabang[0].idCabang ?: ""
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        actCabang.setOnItemClickListener { _, _, position, _ ->
            idCabang = listCabang[position].idCabang ?: ""
        }
    }

    private fun setupDropdown() {
        val list = arrayOf("Persentase (%)", "Nominal (Rp)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        spinnerProfit.setAdapter(adapter)
        spinnerProfit.setText(list[0], false)
    }

    private fun setupAutoHitung() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                hitungHargaJual()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etHargaBeli.addTextChangedListener(watcher)
        etNilaiProfit.addTextChangedListener(watcher)
        spinnerProfit.setOnItemClickListener { _, _, _, _ -> hitungHargaJual() }
    }

    private fun hitungHargaJual() {
        val hargaBeli = etHargaBeli.text.toString().toDoubleOrNull() ?: 0.0
        val profit = etNilaiProfit.text.toString().toDoubleOrNull() ?: 0.0
        val tipe = spinnerProfit.text.toString()

        val hasil = if (tipe.contains("%")) {
            hargaBeli + (hargaBeli * profit / 100)
        } else {
            hargaBeli + profit
        }

        etHargaJual.setText(hasil.toInt().toString())
    }

    // 💾 Simpan ke Firebase
    private fun setupAction() {
        btnSimpan.setOnClickListener {

            val nama = etNama.text.toString().trim()
            val barcode = etBarcode.text.toString().trim()
            val foto = etFoto.text.toString().trim()
            val hargaBeli = etHargaBeli.text.toString().toIntOrNull() ?: 0
            val nilaiProfit = etNilaiProfit.text.toString().toDoubleOrNull() ?: 0.0
            val hargaJual = etHargaJual.text.toString().toIntOrNull() ?: 0
            val stok = etStok.text.toString().toIntOrNull() ?: 0
            val tipe = spinnerProfit.text.toString()
            val tanpaBatas = cbTanpaBatas.isChecked
            
            val selectedChipId = cgStatus.checkedChipId
            val status = if (selectedChipId == R.id.chipReady) "Tersedia (Ready)" else "Habis / Nonaktif"

            // VALIDASI
            if (nama.isEmpty()) {
                etNama.error = "Nama wajib diisi"
                return@setOnClickListener
            }

            if (hargaBeli == 0) {
                etHargaBeli.error = "Harga beli wajib diisi"
                return@setOnClickListener
            }

            val key = produkExisting?.idProduk ?: ref.push().key

            if (key != null) {

                val time = produkExisting?.createdAt ?: System.currentTimeMillis().toString()
                val updatedTime = System.currentTimeMillis().toString()

                val data = modelProduk(
                    idProduk = key,
                    namaProduk = nama,
                    deskripsiProduk = "",
                    hargaBeli = hargaBeli,
                    tipeKeuntungan = tipe,
                    nilaiProfit = nilaiProfit,
                    hargaJual = hargaJual,
                    idKategori = idKategori,
                    idCabang = idCabang,
                    statusProduk = status,
                    stokProduk = if (tanpaBatas) -1 else stok,
                    tanpaBatas = tanpaBatas,
                    barcode = barcode,
                    fotoProduk = foto,
                    createdAt = time,
                    updatedAt = updatedTime
                )

                ref.child(key).setValue(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Produk berhasil disimpan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
