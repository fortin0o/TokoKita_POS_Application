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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import model.modelPelanggan

class TambahPelangganActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var cgStatus: ChipGroup
    private lateinit var btnSimpan: Button

    private var existing: modelPelanggan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pelanggan)

        existing = intent.getParcelableExtra("pelanggan")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        fillData()
        
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        btnSimpan.setOnClickListener { savePelanggan() }
    }

    private fun initView() {
        etNama = findViewById(R.id.etNama)
        etPhone = findViewById(R.id.etPhone)
        etAlamat = findViewById(R.id.etAlamat)
        cgStatus = findViewById(R.id.cgStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun fillData() {
        existing?.let {
            findViewById<TextView>(R.id.tvJudul).text = "Ubah Pelanggan"
            etNama.setText(it.namaPelanggan)
            etPhone.setText(it.phonePelanggan)
            etAlamat.setText(it.alamatPelanggan)
            if (it.statusPelanggan == "Aktif") cgStatus.check(R.id.chipAktif)
            else cgStatus.check(R.id.chipTidakAktif)
        }
    }

    private fun savePelanggan() {
        val nama = etNama.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val status = if (cgStatus.checkedChipId == R.id.chipAktif) "Aktif" else "Tidak Aktif"

        if (nama.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Nama dan Telepon wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Pelanggan")
        val id = existing?.idPelanggan ?: ref.push().key ?: return

        val data = modelPelanggan(id, nama, phone, alamat, status)

        ref.child(id).setValue(data).addOnSuccessListener {
            Toast.makeText(this, "Data Pelanggan disimpan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
