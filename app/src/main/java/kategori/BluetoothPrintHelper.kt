package kategori

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import model.modelTransaksi
import java.io.OutputStream
import java.util.*

class BluetoothPrintHelper(private val context: Context) {

    private val PRINTER_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    interface PrintCallback {
        fun onPrintStart()
        fun onPrintSuccess()
        fun onPrintFailure(message: String)
    }

    fun printReceipt(
        printerAddress: String,
        trx: modelTransaksi,
        namaToko: String,
        headerStruk: String,
        footerStruk: String,
        callback: PrintCallback
    ) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            callback.onPrintFailure("Bluetooth tidak aktif")
            return
        }

        callback.onPrintStart()

        Thread {
            var socket: BluetoothSocket? = null
            try {
                val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(printerAddress)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ) {
                    callback.onPrintFailure("Izin Bluetooth tidak diberikan")
                    return@Thread
                }

                // Advanced connection strategy: Secure -> Insecure -> Reflection
                try {
                    socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID)
                    socket.connect()
                } catch (e: Exception) {
                    Log.d("PRINTER", "Secure connection failed, trying insecure...")
                    try {
                        socket = device.createInsecureRfcommSocketToServiceRecord(PRINTER_UUID)
                        socket.connect()
                    } catch (e2: Exception) {
                        Log.d("PRINTER", "Insecure connection failed, trying reflection...")
                        val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                        socket = m.invoke(device, 1) as BluetoothSocket
                        socket.connect()
                    }
                }

                val out: OutputStream = socket!!.outputStream
                Thread.sleep(500) // Stabilization delay

                // Initialize printer
                out.write(byteArrayOf(0x1B, 0x40))

                // ESC/POS Commands
                val center = byteArrayOf(0x1B, 0x61, 0x01)
                val left = byteArrayOf(0x1B, 0x61, 0x00)
                val boldOn = byteArrayOf(0x1B, 0x45, 0x01)
                val boldOff = byteArrayOf(0x1B, 0x45, 0x00)

                // Header
                out.write(center)
                out.write(boldOn)
                out.write("$namaToko\n".toByteArray())
                out.write(boldOff)
                if (headerStruk.isNotEmpty()) out.write("$headerStruk\n".toByteArray())
                out.write("--------------------------------\n".toByteArray())

                // Transaction Info
                out.write(left)
                out.write("ID: ${trx.idTransaksi}\n".toByteArray())
                out.write("Tgl: ${trx.tanggal}\n".toByteArray())
                out.write("Kasir: ${trx.namaPegawai}\n".toByteArray())
                out.write("Plg: ${trx.namaPelanggan ?: "Umum"}\n".toByteArray())
                out.write("--------------------------------\n".toByteArray())

                // Items
                val localeID = Locale.forLanguageTag("id-ID")
                trx.listProduk?.forEach { item ->
                    val name = item.produk?.namaProduk ?: "Item"
                    out.write("$name\n".toByteArray())
                    
                    val q = String.format(localeID, "%d x Rp%,d", item.jumlah, item.produk?.hargaJual ?: 0)
                    val t = String.format(localeID, "Rp%,d", (item.produk?.hargaJual ?: 0) * item.jumlah)
                    val space = (32 - q.length - t.length).coerceAtLeast(1)
                    out.write((q + " ".repeat(space) + t + "\n").toByteArray())
                }

                out.write("--------------------------------\n".toByteArray())

                // Totals
                out.write(boldOn)
                val totalLabel = "TOTAL"
                val totalVal = String.format(localeID, "Rp%,d", trx.totalHarga)
                val tSpace = (32 - totalLabel.length - totalVal.length).coerceAtLeast(1)
                out.write((totalLabel + " ".repeat(tSpace) + totalVal + "\n").toByteArray())
                out.write(boldOff)

                if (trx.metodePembayaran == "Tunai") {
                    val bLabel = "Bayar"
                    val bVal = String.format(localeID, "Rp%,d", trx.uangDiterima)
                    val bSpace = (32 - bLabel.length - bVal.length).coerceAtLeast(1)
                    out.write((bLabel + " ".repeat(bSpace) + bVal + "\n").toByteArray())
                    
                    val kLabel = "Kembali"
                    val kVal = String.format(localeID, "Rp%,d", trx.uangDiterima - trx.totalHarga)
                    val kSpace = (32 - kLabel.length - kVal.length).coerceAtLeast(1)
                    out.write((kLabel + " ".repeat(kSpace) + kVal + "\n").toByteArray())
                }

                out.write("--------------------------------\n".toByteArray())
                out.write(center)
                out.write("${footerStruk}\n\n\n\n".toByteArray())

                out.flush()
                callback.onPrintSuccess()

            } catch (e: Exception) {
                Log.e("PRINTER", "Error: ${e.message}")
                callback.onPrintFailure(e.localizedMessage ?: "Gagal mencetak")
            } finally {
                try {
                    Thread.sleep(1000)
                    socket?.close()
                } catch (ex: Exception) {}
            }
        }.start()
    }
}
