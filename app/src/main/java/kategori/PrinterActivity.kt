package kategori

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.android.material.switchmaterial.SwitchMaterial

class PrinterActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnSearch: Button
    private lateinit var swAutoPrint: SwitchMaterial
    private lateinit var swShowLogo: SwitchMaterial

    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_printer)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        loadSettings()
        setupListeners()
    }

    private fun initView() {
        tvStatus = findViewById(R.id.tvPrinterStatus)
        btnSearch = findViewById(R.id.btnSearchPrinter)
        swAutoPrint = findViewById(R.id.swAutoPrint)
        swShowLogo = findViewById(R.id.swShowLogo)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("PrinterSettings", Context.MODE_PRIVATE)
        swAutoPrint.isChecked = prefs.getBoolean("autoPrint", false)
        swShowLogo.isChecked = prefs.getBoolean("showLogo", true)
        
        val printerName = prefs.getString("printerName", null)
        if (printerName != null) {
            tvStatus.text = "Terhubung ke: $printerName"
            tvStatus.setTextColor(getColor(R.color.primary_teal))
        }
    }

    private fun setupListeners() {
        btnSearch.setOnClickListener {
            checkPermissionsAndSearch()
        }

        swAutoPrint.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("PrinterSettings", Context.MODE_PRIVATE).edit()
                .putBoolean("autoPrint", isChecked).apply()
        }

        swShowLogo.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("PrinterSettings", Context.MODE_PRIVATE).edit()
                .putBoolean("showLogo", isChecked).apply()
        }
    }

    private fun checkPermissionsAndSearch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), 101)
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
                return
            }
        }
        showDevicePicker()
    }

    private fun showDevicePicker() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth tidak didukung di perangkat ini", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Silakan aktifkan Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Final permission check before accessing Bluetooth hardware
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 101)
                return
            }
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
        val deviceList = mutableListOf<BluetoothDevice>()
        val deviceNames = mutableListOf<String>()

        pairedDevices.forEach { device ->
            deviceList.add(device)
            val name = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                device.name ?: device.address
            } else {
                device.address
            }
            deviceNames.add(name)
        }

        if (deviceNames.isEmpty()) {
            Toast.makeText(this, "Tidak ada perangkat Bluetooth tersambung. Pasangkan printer Anda di Pengaturan Android.", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Pilih Printer Thermal")
            .setItems(deviceNames.toTypedArray()) { _, which ->
                val selectedDevice = deviceList[which]
                savePrinter(selectedDevice)
            }
            .show()
    }

    private fun savePrinter(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        getSharedPreferences("PrinterSettings", Context.MODE_PRIVATE).edit()
            .putString("printerName", device.name)
            .putString("printerAddress", device.address)
            .apply()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        tvStatus.text = "Terhubung ke: ${device.name}"
        tvStatus.setTextColor(getColor(R.color.primary_teal))
        Toast.makeText(this, "Printer berhasil dipilih", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showDevicePicker()
        }
    }
}
