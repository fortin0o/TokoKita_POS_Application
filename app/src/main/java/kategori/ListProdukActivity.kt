package kategori

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import com.google.firebase.database.*
import adapter.ProdukAdapter
import model.modelProduk

class ListProdukActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var list: ArrayList<modelProduk>
    private lateinit var adapter: ProdukAdapter

    private val db = FirebaseDatabase.getInstance()
    private val ref = db.getReference("produk")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_produk)

        rv = findViewById(R.id.rvProduk)
        rv.layoutManager = LinearLayoutManager(this)

        list = ArrayList()
        adapter = ProdukAdapter(list)

        rv.adapter = adapter

        loadData()
    }

    private fun loadData() {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()

                for (snap in snapshot.children) {
                    val data = snap.getValue(modelProduk::class.java)
                    if (data != null) {
                        list.add(data)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
