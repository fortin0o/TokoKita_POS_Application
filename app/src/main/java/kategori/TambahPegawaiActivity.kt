package kategori

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import model.modelPegawai

class TambahPegawaiActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var actRole: AutoCompleteTextView
    private lateinit var cgStatus: ChipGroup
    private lateinit var btnSimpan: Button
    private lateinit var ivBack: ImageView

    private var pegawaiExisting: modelPegawai? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pegawai)

        pegawaiExisting = intent.getParcelableExtra("pegawai")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNama = findViewById(R.id.etNama)
        etPhone = findViewById(R.id.etPhone)
        actRole = findViewById(R.id.actRole)
        cgStatus = findViewById(R.id.cgStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        ivBack = findViewById(R.id.ivBack)

        if (pegawaiExisting != null) {
            findViewById<TextView>(R.id.tvJudul).text = "Ubah Pegawai"
            etNama.setText(pegawaiExisting?.namaPegawai)
            etPhone.setText(pegawaiExisting?.phonePegawai)
            actRole.setText(pegawaiExisting?.rolePegawai, false)
            if (pegawaiExisting?.statusPegawai == "Aktif") {
                cgStatus.check(R.id.chipAktif)
            } else {
                cgStatus.check(R.id.chipTidakAktif)
            }
        }

        // Set up role dropdown
        val roles = arrayOf("Staff", "Manager", "Admin", "Kasir")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        actRole.setAdapter(adapter)

        ivBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            savePegawai()
        }
    }

    private fun savePegawai() {
        val nama = etNama.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val role = actRole.text.toString().trim()
        
        val selectedChipId = cgStatus.checkedChipId
        val status = if (selectedChipId != -1) {
            findViewById<Chip>(selectedChipId).text.toString()
        } else {
            "Aktif"
        }

        if (nama.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Pegawai")
        val id = pegawaiExisting?.idPegawai ?: myRef.push().key

        val pegawai = modelPegawai(id, nama, phone, role, status)

        if (id != null) {
            myRef.child(id).setValue(pegawai).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Pegawai berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menambahkan pegawai", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
