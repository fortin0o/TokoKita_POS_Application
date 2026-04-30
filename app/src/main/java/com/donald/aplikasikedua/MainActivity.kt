package com.donald.aplikasikedua

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kategori.DataKategoriActivity
import kategori.DataProdukActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle padding system UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ==============================
        // 👉 KATEGORI (LinearLayout)
        // ==============================
        val cardKategori = findViewById<LinearLayout>(R.id.cardKategori)
        cardKategori.setOnClickListener {
            startActivity(Intent(this, DataKategoriActivity::class.java))
        }

        // ==============================
        // 👉 CABANG (CardView)
        // ==============================
        val cardCabang = findViewById<CardView>(R.id.cardCabang)
        cardCabang.setOnClickListener {
            startActivity(Intent(this, DataProdukActivity::class.java))
        }
    }
}
