package kategori

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var fabData: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // FAB klik -> buka ModKategoriActivity
        fabData = findViewById(R.id.fabData)
        fabData.setOnClickListener {
            val intent = Intent(this, ModKategoriActivity::class.java)
            startActivity(intent)
        }
    }
}
