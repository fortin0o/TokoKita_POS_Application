package com.donald.aplikasikedua

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import kategori.*

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

        // --- Navigation Links ---

        // 1. Menu (Daftar Produk)
        findViewById<View>(R.id.cardMenu).setOnClickListener {
            startActivity(Intent(this, DataProdukActivity::class.java))
        }

        // 2. Kategori
        findViewById<View>(R.id.cardKategori).setOnClickListener {
            startActivity(Intent(this, DataKategoriActivity::class.java))
        }

        // 3. Pegawai
        findViewById<View>(R.id.cardPegawai).setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }

        // 4. Cabang
        findViewById<View>(R.id.cardCabang).setOnClickListener {
            startActivity(Intent(this, DataCabangActivity::class.java))
        }

        // 5. Layanan
        findViewById<View>(R.id.cardLayanan).setOnClickListener {
            startActivity(Intent(this, LayananActivity::class.java))
        }

        // 6. Printer
        findViewById<View>(R.id.cardPrinter).setOnClickListener {
            startActivity(Intent(this, PrinterActivity::class.java))
        }

        // --- Quick Actions ---
        
        // 1. Transaksi
        findViewById<View>(R.id.actionTransaksi).setOnClickListener {
            startActivity(Intent(this, DataTransaksiActivity::class.java))
        }

        // 2. Pelanggan
        findViewById<View>(R.id.actionPelanggan).setOnClickListener {
            startActivity(Intent(this, DataPelangganActivity::class.java))
        }

        // 3. Laporan
        findViewById<View>(R.id.actionLaporan).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }

        // --- Profile Bottom Sheet ---
        findViewById<View>(R.id.ivProfile).setOnClickListener {
            showProfileBottomSheet()
        }
    }

    private fun showProfileBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_profile_bottom_sheet, null)
        
        view.findViewById<View>(R.id.menuLogout).setOnClickListener {
            dialog.dismiss()
            // Logout logic
        }
        
        dialog.setContentView(view)
        dialog.show()
    }
}
