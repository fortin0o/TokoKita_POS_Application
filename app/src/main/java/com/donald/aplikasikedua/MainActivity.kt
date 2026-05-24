package com.donald.aplikasikedua

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import kategori.*
import model.modelCabang
import model.modelTransaksi
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvRevenue: TextView
    private lateinit var tvCurrentCabang: TextView
    
    private var selectedCabangId: String = ""
    private var selectedCabangNama: String = "Semua Cabang"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvRevenue = findViewById(R.id.tvRevenue)
        tvCurrentCabang = findViewById(R.id.tvCurrentCabang)

        val prefs = getSharedPreferences("TokoKita", MODE_PRIVATE)
        selectedCabangId = prefs.getString("cabangId", "") ?: ""
        selectedCabangNama = prefs.getString("cabangNama", "Semua Cabang") ?: "Semua Cabang"
        tvCurrentCabang.text = selectedCabangNama

        // Set Dynamic Greeting
        updateGreeting()
        loadTodayRevenue()
        
        findViewById<View>(R.id.cardCabangSelector).setOnClickListener { showCabangSelector() }

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

    private fun updateGreeting() {
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greetingText = when (hour) {
            in 0..11 -> "Selamat Pagi"
            in 12..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }

        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvDate.text = sdf.format(Date())

        FirebaseDatabase.getInstance().getReference("Settings").child("namaToko")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.value?.toString() ?: "TokoKita"
                    tvGreeting.text = "$greetingText, $userName!"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadTodayRevenue() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ref = FirebaseDatabase.getInstance().getReference("transaksi")
        
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0
                for (snap in snapshot.children) {
                    val trx = snap.getValue(modelTransaksi::class.java)
                    if (trx != null && trx.tanggal?.startsWith(today) == true) {
                        if (selectedCabangId.isEmpty() || trx.idCabang == selectedCabangId) {
                            total += trx.totalHarga
                        }
                    }
                }
                tvRevenue.text = "Rp %,d".format(total)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showProfileBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_profile_bottom_sheet, null)
        
        view.findViewById<View>(R.id.menuSettings).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        view.findViewById<View>(R.id.menuLogout).setOnClickListener {
            dialog.dismiss()
            // Logout logic
        }
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun showCabangSelector() {
        val ref = FirebaseDatabase.getInstance().getReference("Cabang")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<modelCabang>()
                list.add(modelCabang("", "Semua Cabang", "", ""))
                for (snap in snapshot.children) {
                    snap.getValue(modelCabang::class.java)?.let { list.add(it) }
                }
                
                val names = list.map { it.namaCabang ?: "" }.toTypedArray()
                
                android.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("Pilih Cabang Kerja")
                    .setItems(names) { _, which ->
                        val selected = list[which]
                        selectedCabangId = selected.idCabang ?: ""
                        selectedCabangNama = selected.namaCabang ?: "Semua Cabang"
                        
                        tvCurrentCabang.text = selectedCabangNama
                        
                        getSharedPreferences("TokoKita", MODE_PRIVATE).edit()
                            .putString("cabangId", selectedCabangId)
                            .putString("cabangNama", selectedCabangNama)
                            .apply()
                            
                        loadTodayRevenue()
                    }
                    .show()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
