package kategori

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import model.modelKategori

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: TextInputEditText
    private lateinit var spStatusKategori: AutoCompleteTextView
    private lateinit var btnSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvJudul = findViewById(R.id.tvJudul)
        etNamaKategori = findViewById(R.id.etNama)
        spStatusKategori = findViewById(R.id.actStatus)
        btnSimpan = findViewById(R.id.btnSimpan)

        val statusList = resources.getStringArray(R.array.status_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusList)
        spStatusKategori.setAdapter(adapter)
        if (statusList.isNotEmpty()) spStatusKategori.setText(statusList[0], false)

        btnSimpan.setOnClickListener {

            val nama = etNamaKategori.text.toString().trim()
            val status = spStatusKategori.text.toString()

            if (nama.isEmpty()) {
                etNamaKategori.error = "Nama kategori wajib diisi"
                return@setOnClickListener
            }

            val key = myRef.push().key
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