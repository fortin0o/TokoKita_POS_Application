package kategori

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R

class ModKategoriActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ===== DROPDOWN STATUS =====
        // Fixed ID from R.id.spinnerStatus to R.id.actStatus to match activity_mod_kategori.xml
        val dropdownStatus = findViewById<AutoCompleteTextView>(R.id.actStatus)

        // ambil data dari strings.xml
        val statusList = resources.getStringArray(R.array.status_array)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            statusList
        )

        dropdownStatus.setAdapter(adapter)

        // optional: set default value
        if (statusList.isNotEmpty()) {
            dropdownStatus.setText(statusList[0], false) // Aktif
        }
    }
}
