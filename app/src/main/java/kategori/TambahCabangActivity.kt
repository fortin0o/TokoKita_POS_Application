package kategori

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import model.modelCabang

class TambahCabangActivity : AppCompatActivity() {

    private lateinit var etNamaCabang: TextInputEditText
    private lateinit var etAlamatCabang: TextInputEditText
    private lateinit var actStatus: AutoCompleteTextView
    private lateinit var btnSimpan: Button
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_cabang)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNamaCabang = findViewById(R.id.etNamaCabang)
        etAlamatCabang = findViewById(R.id.etAlamatCabang)
        actStatus = findViewById(R.id.actStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        ivBack = findViewById(R.id.ivBack)

        // Set up status dropdown
        val statusList = arrayOf("Pusat", "Cabang")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusList)
        actStatus.setAdapter(adapter)

        ivBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            saveCabang()
        }
    }

    private fun saveCabang() {
        val nama = etNamaCabang.text.toString().trim()
        val alamat = etAlamatCabang.text.toString().trim()
        val status = actStatus.text.toString().trim()

        if (nama.isEmpty() || alamat.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Cabang")
        val id = myRef.push().key

        val cabang = modelCabang(id, nama, alamat, status)

        if (id != null) {
            myRef.child(id).setValue(cabang).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cabang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menambahkan cabang", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
