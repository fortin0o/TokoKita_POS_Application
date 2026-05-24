package kategori

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SettingsActivity : AppCompatActivity() {

    private lateinit var etNamaToko: TextInputEditText
    private lateinit var etHeader: TextInputEditText
    private lateinit var etFooter: TextInputEditText
    private lateinit var btnSimpan: Button

    private val db = FirebaseDatabase.getInstance()
    private val ref = db.getReference("Settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNamaToko = findViewById(R.id.etNamaToko)
        etHeader = findViewById(R.id.etHeaderStruk)
        etFooter = findViewById(R.id.etFooterStruk)
        btnSimpan = findViewById(R.id.btnSimpan)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        loadSettings()

        btnSimpan.setOnClickListener { saveSettings() }
    }

    private fun loadSettings() {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    etNamaToko.setText(snapshot.child("namaToko").value?.toString() ?: "")
                    etHeader.setText(snapshot.child("headerStruk").value?.toString() ?: "")
                    etFooter.setText(snapshot.child("footerStruk").value?.toString() ?: "")
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun saveSettings() {
        val nama = etNamaToko.text.toString().trim()
        val header = etHeader.text.toString().trim()
        val footer = etFooter.text.toString().trim()

        val data = mapOf(
            "namaToko" to nama,
            "headerStruk" to header,
            "footerStruk" to footer
        )

        ref.setValue(data).addOnSuccessListener {
            Toast.makeText(this, "Pengaturan disimpan", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
        }
    }
}
