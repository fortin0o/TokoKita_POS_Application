package kategori

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.donald.aplikasikedua.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
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
    private lateinit var etHargaBeli: TextInputEditText
    private lateinit var etNilaiProfit: TextInputEditText
    private lateinit var etHargaJual: TextInputEditText
    private lateinit var etStok: TextInputEditText

    private lateinit var spinnerProfit: AutoCompleteTextView
    private lateinit var actCabang: AutoCompleteTextView
    private lateinit var actKategori: AutoCompleteTextView
    private lateinit var cbTanpaBatas: MaterialCheckBox
    private lateinit var btnSimpan: MaterialButton

    private var idKategori: String = ""
    private var idCabang: String = ""

    private val listKategori = mutableListOf<modelKategori>()
    private val listCabang = mutableListOf<modelCabang>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_produk)

        initView()
        setupDropdown()
        setupAutoHitung()
        loadKategori()
        loadCabang()
        setupAction()
    }

    private fun initView() {
        etNama = findViewById(R.id.et_nama_produk)
        etBarcode = findViewById(R.id.et_barcode)
        etHargaBeli = findViewById(R.id.et_harga_beli)
        etNilaiProfit = findViewById(R.id.et_nilai_profit)
        etHargaJual = findViewById(R.id.et_harga_jual)
        etStok = findViewById(R.id.et_stok)

        spinnerProfit = findViewById(R.id.spinner_tipe_keuntungan)
        actCabang = findViewById(R.id.actCabang)
        actKategori = findViewById(R.id.actKategori)
        cbTanpaBatas = findViewById(R.id.cb_stok_tanpa_batas)
        btnSimpan = findViewById(R.id.btn_simpan)
        
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
    }

    private fun loadKategori() {
        db.getReference("Kategori").addValueEventListener(object : ValueEventListener {
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
                for (item in snapshot.children) {
                    val cabang = item.getValue(modelCabang::class.java)
                    if (cabang != null) {
                        listCabang.add(cabang)
                        names.add(cabang.namaCabang ?: "")
                    }
                }
                val adapter = ArrayAdapter(this@TambahProdukActivity, android.R.layout.simple_dropdown_item_1line, names)
                actCabang.setAdapter(adapter)
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
            val hargaBeli = etHargaBeli.text.toString().toIntOrNull() ?: 0
            val nilaiProfit = etNilaiProfit.text.toString().toDoubleOrNull() ?: 0.0
            val hargaJual = etHargaJual.text.toString().toIntOrNull() ?: 0
            val stok = etStok.text.toString().toIntOrNull() ?: 0
            val tipe = spinnerProfit.text.toString()
            val tanpaBatas = cbTanpaBatas.isChecked

            // VALIDASI
            if (nama.isEmpty()) {
                etNama.error = "Nama wajib diisi"
                return@setOnClickListener
            }

            if (hargaBeli == 0) {
                etHargaBeli.error = "Harga beli wajib diisi"
                return@setOnClickListener
            }

            val key = ref.push().key

            if (key != null) {

                val time = System.currentTimeMillis().toString()

                val data = modelProduk(
                    idProduk = key,
                    namaProduk = nama,
                    deskripsiProduk = "",
                    hargaBeli = hargaBeli,
                    tipeKeuntungan = tipe,
                    nilaiProfit = nilaiProfit,
                    hargaJual = hargaJual,
                    idKategori = idKategori,
                    statusProduk = "Aktif",
                    stokProduk = if (tanpaBatas) -1 else stok,
                    tanpaBatas = tanpaBatas,
                    barcode = barcode,
                    fotoProduk = "",
                    createdAt = time,
                    updatedAt = time
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
