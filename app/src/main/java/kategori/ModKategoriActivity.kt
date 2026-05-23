package kategori

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import model.modelKategori

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: TextInputEditText
    private lateinit var cgStatus: ChipGroup
    private lateinit var btnSimpan: Button
    private lateinit var ivBack: ImageView

    private var kategoriExisting: modelKategori? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        kategoriExisting = intent.getParcelableExtra("kategori")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvJudul = findViewById(R.id.tvJudul)
        etNamaKategori = findViewById(R.id.etNama)
        cgStatus = findViewById(R.id.cgStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        ivBack = findViewById(R.id.ivBack)

        if (kategoriExisting != null) {
            tvJudul.text = "Ubah Kategori"
            etNamaKategori.setText(kategoriExisting?.namaKategori)
            if (kategoriExisting?.statusKategori == "Aktif") {
                cgStatus.check(R.id.chipAktif)
            } else {
                cgStatus.check(R.id.chipTidakAktif)
            }
        }

        ivBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {

            val nama = etNamaKategori.text.toString().trim()
            val status = if (cgStatus.checkedChipId == R.id.chipAktif) "Aktif" else "Tidak Aktif"

            if (nama.isEmpty()) {
                etNamaKategori.error = "Nama kategori wajib diisi"
                return@setOnClickListener
            }

            val key = kategoriExisting?.idKategori ?: myRef.push().key
            if (key != null) {

                val kategoriData = modelKategori(
                    idKategori = key,
                    namaKategori = nama,
                    statusKategori = status
                )

                myRef.child(key).setValue(kategoriData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Kategori berhasil disimpan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menyimpan kategori", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}