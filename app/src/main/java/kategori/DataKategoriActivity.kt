package kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.donald.aplikasikedua.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import model.modelKategori

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var fabData: FloatingActionButton
    private lateinit var rvKategori: RecyclerView

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    private lateinit var listKategori: ArrayList<modelKategori>
    private lateinit var adapter: KategoriAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fabData = findViewById(R.id.fabData)
        rvKategori = findViewById(R.id.rvKategori)
        val ivBack = findViewById<ImageView>(R.id.ivBack)

        ivBack.setOnClickListener { finish() }

        rvKategori.layoutManager = LinearLayoutManager(this)

        listKategori = ArrayList()
        adapter = KategoriAdapter(listKategori)
        rvKategori.adapter = adapter

        fabData.setOnClickListener {
            startActivity(Intent(this, ModKategoriActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                listKategori.clear()

                for (data in snapshot.children) {
                    val kategori = data.getValue(modelKategori::class.java)
                    if (kategori != null) {
                        listKategori.add(kategori)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataKategoriActivity,
                    "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}