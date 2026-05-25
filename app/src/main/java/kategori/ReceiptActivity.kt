package kategori

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
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
    private var showLogoInPrint = true
    
    private var namaToko = "TOKOKITA POS"
    private var headerStruk = ""
    private var currentTransaksi: modelTransaksi? = null

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
            currentTransaksi?.let { checkPermissionAndPrint(it) } ?: run {
                Toast.makeText(this, "Data transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
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
                    tvStoreHeader.text = if (headerStruk.isNotEmpty()) headerStruk else "Solusi Point of Sale Pintar"
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
        showLogoInPrint = prefs.getBoolean("showLogo", true)
    }

    private fun populateData(trx: modelTransaksi) {
        tvOrderId.text = "Order ID: ${trx.idTransaksi}"
        tvDate.text = trx.tanggal
        tvKasir.text = trx.namaPegawai
        tvPelanggan.text = trx.namaPelanggan ?: "Umum"
        tvSubtotal.text = "Rp %,d".format(trx.totalHarga)
        tvTotal.text = "Rp %,d".format(trx.totalHarga)
        tvMetodeLabel.text = "Metode Pembayaran (${trx.metodePembayaran})"
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
        if (printerAddress == null) {
            Toast.makeText(this, "Printer belum dipilih di menu Pengaturan Printer", Toast.LENGTH_LONG).show()
            return
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Bluetooth tidak aktif", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Menghubungkan ke $printerName...", Toast.LENGTH_SHORT).show()

        Thread {
            var socket: BluetoothSocket? = null
            try {
                val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(printerAddress)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return@Thread
                }

                // Attempt to connect (Try Secure first, then Insecure fallback)
                try {
                    socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID)
                    socket.connect()
                } catch (e: Exception) {
                    socket = device.createInsecureRfcommSocketToServiceRecord(PRINTER_UUID)
                    socket.connect()
                }

                val outputStream: OutputStream = socket.outputStream

                // ESC/POS Commands
                val escInit = byteArrayOf(0x1B, 0x40)
                val escCenter = byteArrayOf(0x1B, 0x61, 0x01)
                val escLeft = byteArrayOf(0x1B, 0x61, 0x00)
                val escBoldOn = byteArrayOf(0x1B, 0x45, 0x01)
                val escBoldOff = byteArrayOf(0x1B, 0x45, 0x00)

                outputStream.write(escInit)
                
                // Print Logo if enabled
                if (showLogoInPrint) {
                    try {
                        val logoBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                        if (logoBitmap != null) {
                            val scaledBitmap = Bitmap.createScaledBitmap(logoBitmap, 180, 180, true)
                            outputStream.write(escCenter)
                            outputStream.write(decodeBitmap(scaledBitmap))
                            outputStream.write("\n".toByteArray())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Header
                outputStream.write(escCenter)
                outputStream.write(escBoldOn)
                outputStream.write("$namaToko\n".toByteArray())
                outputStream.write(escBoldOff)
                if (headerStruk.isNotEmpty()) outputStream.write("$headerStruk\n".toByteArray())
                outputStream.write("--------------------------------\n".toByteArray())

                // Transaction Info
                outputStream.write(escLeft)
                outputStream.write("ID: ${trx.idTransaksi}\n".toByteArray())
                outputStream.write("Tgl: ${trx.tanggal}\n".toByteArray())
                outputStream.write("Kasir: ${trx.namaPegawai}\n".toByteArray())
                outputStream.write("Plg: ${trx.namaPelanggan ?: "Umum"}\n".toByteArray())
                outputStream.write("--------------------------------\n".toByteArray())

                // Items
                trx.listProduk?.forEach { item ->
                    outputStream.write("${item.produk?.namaProduk}\n".toByteArray())
                    val qtyPrice = "${item.jumlah} x Rp %,d".format(item.produk?.hargaJual ?: 0)
                    val total = "Rp %,d".format((item.produk?.hargaJual ?: 0) * item.jumlah)
                    val paddingCount = (32 - qtyPrice.length - total.length).coerceAtLeast(1)
                    val padding = " ".repeat(paddingCount)
                    outputStream.write("$qtyPrice$padding$total\n".toByteArray())
                }

                outputStream.write("--------------------------------\n".toByteArray())

                // Totals
                val totalValue = "Rp %,d".format(trx.totalHarga)
                outputStream.write(escBoldOn)
                outputStream.write("TOTAL${" ".repeat((32 - 5 - totalValue.length).coerceAtLeast(1))}$totalValue\n".toByteArray())
                outputStream.write(escBoldOff)

                if (trx.metodePembayaran == "Tunai") {
                    val bVal = "Rp %,d".format(trx.uangDiterima)
                    outputStream.write("Bayar${" ".repeat((32 - 5 - bVal.length).coerceAtLeast(1))}$bVal\n".toByteArray())
                    val kVal = "Rp %,d".format(trx.uangDiterima - trx.totalHarga)
                    outputStream.write("Kembali${" ".repeat((32 - 7 - kVal.length).coerceAtLeast(1))}$kVal\n".toByteArray())
                }
                
                outputStream.write("--------------------------------\n".toByteArray())
                outputStream.write(escCenter)
                val footerText = tvFooter.text.toString()
                outputStream.write("${footerText}\n".toByteArray())
                outputStream.write("\n\n\n".toByteArray())

                outputStream.flush()
                runOnUiThread { Toast.makeText(this, "Berhasil mencetak!", Toast.LENGTH_SHORT).show() }

            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show() }
            } finally {
                try { socket?.close() } catch (ex: Exception) {}
            }
        }.start()
    }

    private fun decodeBitmap(bmp: Bitmap): ByteArray {
        val width = bmp.width
        val height = bmp.height
        val bwWidth = (width + 7) / 8 * 8
        val bwHeight = height
        
        val data = ByteArray(bwWidth * bwHeight / 8 + 8)
        data[0] = 0x1D // GS
        data[1] = 0x76 // v
        data[2] = 0x30 // 0
        data[3] = 0x00 // m
        data[4] = (bwWidth / 8 % 256).toByte() // xL
        data[5] = (bwWidth / 8 / 256).toByte() // xH
        data[6] = (bwHeight % 256).toByte() // yL
        data[7] = (bwHeight / 256).toByte() // yH

        var k = 8
        for (i in 0 until bwHeight) {
            for (j in 0 until bwWidth / 8) {
                var temp = 0
                for (b in 0 until 8) {
                    val x = j * 8 + b
                    if (x < width) {
                        val pixel = bmp.getPixel(x, i)
                        val r = (pixel shr 16) and 0xff
                        val g = (pixel shr 8) and 0xff
                        val b_val = pixel and 0xff
                        val gray = (r * 0.299 + g * 0.587 + b_val * 0.114).toInt()
                        if (gray < 128) {
                            temp = temp or (1 shl (7 - b))
                        }
                    }
                }
                data[k++] = temp.toByte()
            }
        }
        return data
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
            Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
