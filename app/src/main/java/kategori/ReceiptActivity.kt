package kategori

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.firebase.database.*
import model.modelTransaksi
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class ReceiptActivity : AppCompatActivity() {

    private lateinit var tvOrderId: TextView
    private lateinit var tvStoreName: TextView
    private lateinit var tvStoreHeader: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvKasir: TextView
    private lateinit var tvPelanggan: TextView
    private lateinit var itemContainer: LinearLayout
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvMetodeLabel: TextView
    private lateinit var tvMetodeValue: TextView
    private lateinit var layoutTunai: View
    private lateinit var tvUangDiterima: TextView
    private lateinit var tvKembalian: TextView
    private lateinit var tvFooter: TextView
    private lateinit var btnSelesai: Button
    private lateinit var btnBagikan: Button
    private lateinit var btnCetak: Button

    private var autoPrint = false
    private var printerAddress: String? = null
    private var printerName: String? = "Printer"
    
    private var namaToko = "TOKOKITA POS"
    private var headerStruk = ""
    private var currentTransaksi: modelTransaksi? = null

    // UUID SPP Standar untuk hampir semua Printer Bluetooth Thermal
    private val PRINTER_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_receipt)

        currentTransaksi = intent.getParcelableExtra<modelTransaksi>("transaksi")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        loadSettings()
        loadPrinterSettings()

        currentTransaksi?.let {
            populateData(it)
            // Cetak otomatis jika diaktifkan di pengaturan
            if (autoPrint) {
                checkPermissionAndPrint(it)
            }
        }

        btnSelesai.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
        
        btnBagikan.setOnClickListener {
            currentTransaksi?.let { shareReceipt(it) }
        }

        btnCetak.setOnClickListener {
            currentTransaksi?.let { 
                checkPermissionAndPrint(it) 
            } ?: run {
                Toast.makeText(this, "Data transaksi tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initView() {
        tvOrderId = findViewById(R.id.tvOrderId)
        tvStoreName = findViewById(R.id.tvStoreName)
        tvStoreHeader = findViewById(R.id.tvStoreHeader)
        tvDate = findViewById(R.id.tvDate)
        tvKasir = findViewById(R.id.tvKasir)
        tvPelanggan = findViewById(R.id.tvPelanggan)
        itemContainer = findViewById(R.id.itemContainer)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotal = findViewById(R.id.tvTotal)
        tvMetodeLabel = findViewById(R.id.tvMetodeLabel)
        tvMetodeValue = findViewById(R.id.tvMetodeValue)
        layoutTunai = findViewById(R.id.layoutTunaiDetails)
        tvUangDiterima = findViewById(R.id.tvUangDiterima)
        tvKembalian = findViewById(R.id.tvKembalian)
        tvFooter = findViewById(R.id.tvFooter)
        btnSelesai = findViewById(R.id.btnSelesai)
        btnBagikan = findViewById(R.id.btnBagikan)
        btnCetak = findViewById(R.id.btnCetak)
    }

    private fun loadSettings() {
        FirebaseDatabase.getInstance().getReference("Settings").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    namaToko = snapshot.child("namaToko").value?.toString() ?: "TOKOKITA POS"
                    headerStruk = snapshot.child("headerStruk").value?.toString() ?: ""
                    tvStoreName.text = namaToko
                    tvStoreHeader.text = if (headerStruk.isNotEmpty()) headerStruk else "Solusi Point of Sale"
                    val footer = snapshot.child("footerStruk").value?.toString() ?: ""
                    if (footer.isNotEmpty()) tvFooter.text = footer
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadPrinterSettings() {
        val prefs = getSharedPreferences("PrinterSettings", Context.MODE_PRIVATE)
        autoPrint = prefs.getBoolean("autoPrint", false)
        printerAddress = prefs.getString("printerAddress", null)
        printerName = prefs.getString("printerName", "Printer")
    }

    private fun populateData(trx: modelTransaksi) {
        tvOrderId.text = "Order ID: ${trx.idTransaksi}"
        tvDate.text = trx.tanggal
        tvKasir.text = trx.namaPegawai
        tvPelanggan.text = trx.namaPelanggan ?: "Umum"
        tvSubtotal.text = "Rp %,d".format(trx.totalHarga)
        tvTotal.text = "Rp %,d".format(trx.totalHarga)
        tvMetodeValue.text = trx.metodePembayaran
        
        if (trx.metodePembayaran == "Tunai") {
            layoutTunai.visibility = View.VISIBLE
            tvUangDiterima.text = "Rp %,d".format(trx.uangDiterima)
            tvKembalian.text = "Rp %,d".format(trx.uangDiterima - trx.totalHarga)
        } else {
            layoutTunai.visibility = View.GONE
        }

        itemContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        trx.listProduk?.forEach { item ->
            val itemView = inflater.inflate(R.layout.item_receipt_row, itemContainer, false)
            itemView.findViewById<TextView>(R.id.tvItemName).text = item.produk?.namaProduk
            itemView.findViewById<TextView>(R.id.tvItemTotal).text = "Rp %,d".format((item.produk?.hargaJual ?: 0) * item.jumlah)
            itemView.findViewById<TextView>(R.id.tvItemSubDetail).text = "${item.jumlah} x Rp %,d".format(item.produk?.hargaJual ?: 0)
            itemContainer.addView(itemView)
        }
    }

    private fun checkPermissionAndPrint(trx: modelTransaksi) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 200)
                return
            }
        }
        printReceipt(trx)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            currentTransaksi?.let { printReceipt(it) }
        }
    }

    private fun printReceipt(trx: modelTransaksi) {
        if (printerAddress.isNullOrEmpty()) {
            Toast.makeText(this, "Printer belum dipilih di Pengaturan", Toast.LENGTH_LONG).show()
            return
        }

        val bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Silakan aktifkan Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Mencetak ke $printerName...", Toast.LENGTH_SHORT).show()

        Thread {
            var socket: BluetoothSocket? = null
            try {
                val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(printerAddress)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return@Thread
                }

                // Mencoba koneksi dengan 2 cara: Secure dan Insecure (beberapa printer butuh Insecure)
                try {
                    socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID)
                    socket.connect()
                } catch (e: Exception) {
                    Log.d("PRINTER", "Coba koneksi Insecure...")
                    socket = device.createInsecureRfcommSocketToServiceRecord(PRINTER_UUID)
                    socket.connect()
                }

                val out: OutputStream = socket.outputStream

                // Perintah Dasar ESC/POS
                val init = byteArrayOf(0x1B, 0x40)        // Reset printer
                val center = byteArrayOf(0x1B, 0x61, 0x01) // Rata tengah
                val left = byteArrayOf(0x1B, 0x61, 0x00)   // Rata kiri
                val boldOn = byteArrayOf(0x1B, 0x45, 0x01) // Tebal ON
                val boldOff = byteArrayOf(0x1B, 0x45, 0x00)// Tebal OFF
                val newLine = "\n".toByteArray()

                out.write(init)
                Thread.sleep(100) // Jeda stabilisasi

                // HEADER
                out.write(center)
                out.write(boldOn)
                out.write("${namaToko}\n".toByteArray())
                out.write(boldOff)
                if (headerStruk.isNotEmpty()) out.write("${headerStruk}\n".toByteArray())
                out.write("--------------------------------\n".toByteArray())

                // INFO TRANSAKSI
                out.write(left)
                out.write("ID   : ${trx.idTransaksi}\n".toByteArray())
                out.write("Tgl  : ${trx.tanggal}\n".toByteArray())
                out.write("Kasir: ${trx.namaPegawai}\n".toByteArray())
                out.write("Plg  : ${trx.namaPelanggan ?: "Umum"}\n".toByteArray())
                out.write("--------------------------------\n".toByteArray())

                // DAFTAR PRODUK
                trx.listProduk?.forEach { item ->
                    val nama = item.produk?.namaProduk ?: "Item"
                    out.write("${nama}\n".toByteArray())
                    
                    val qty = "${item.jumlah} x Rp%,d".format(item.produk?.hargaJual ?: 0)
                    val sub = "Rp%,d".format((item.produk?.hargaJual ?: 0) * item.jumlah)
                    
                    // Padding spasi (asumsi lebar kertas 32 karakter)
                    val spasiCount = 32 - qty.length - sub.length
                    val spasi = " ".repeat(if (spasiCount > 0) spasiCount else 1)
                    
                    out.write("${qty}${spasi}${sub}\n".toByteArray())
                }

                out.write("--------------------------------\n".toByteArray())

                // TOTAL & PEMBAYARAN
                out.write(boldOn)
                val totalLabel = "TOTAL"
                val totalVal = "Rp%,d".format(trx.totalHarga)
                val tSpasi = 32 - totalLabel.length - totalVal.length
                out.write("${totalLabel}${" ".repeat(if (tSpasi > 0) tSpasi else 1)}${totalVal}\n".toByteArray())
                out.write(boldOff)

                if (trx.metodePembayaran == "Tunai") {
                    val bayarLabel = "Bayar"
                    val bayarVal = "Rp%,d".format(trx.uangDiterima)
                    val bSpasi = 32 - bayarLabel.length - bayarVal.length
                    out.write("${bayarLabel}${" ".repeat(if (bSpasi > 0) bSpasi else 1)}${bayarVal}\n".toByteArray())
                    
                    val kembaliLabel = "Kembali"
                    val kembaliVal = "Rp%,d".format(trx.uangDiterima - trx.totalHarga)
                    val kSpasi = 32 - kembaliLabel.length - kembaliVal.length
                    out.write("${kembaliLabel}${" ".repeat(if (kSpasi > 0) kSpasi else 1)}${kembaliVal}\n".toByteArray())
                }
                
                out.write("--------------------------------\n".toByteArray())

                // FOOTER
                out.write(center)
                out.write("${tvFooter.text}\n".toByteArray())
                out.write("\n\n\n".toByteArray()) // Spasi untuk sobek kertas

                out.flush()
                runOnUiThread { Toast.makeText(this, "Berhasil mencetak!", Toast.LENGTH_SHORT).show() }

            } catch (e: Exception) {
                Log.e("PRINTER", "Gagal cetak", e)
                runOnUiThread { Toast.makeText(this, "Gagal Hubungkan Printer: ${e.message}", Toast.LENGTH_LONG).show() }
            } finally {
                try {
                    // Beri jeda sedikit sebelum menutup socket agar data terkirim semua
                    Thread.sleep(500)
                    socket?.close()
                } catch (ex: Exception) { }
            }
        }.start()
    }

    private fun shareReceipt(trx: modelTransaksi) {
        val sb = StringBuilder()
        sb.append("===== $namaToko =====\n")
        sb.append("ID: ${trx.idTransaksi}\n")
        sb.append("TOTAL: Rp %,d\n".format(trx.totalHarga))
        
        try {
            val file = File(getExternalFilesDir(null), "Struk.txt")
            FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Bagikan Struk"))
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membagikan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
