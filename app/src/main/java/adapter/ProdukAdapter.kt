package adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import com.google.android.material.chip.Chip
import com.google.firebase.database.FirebaseDatabase
import kategori.TambahProdukActivity
import model.modelProduk

class ProdukAdapter(private val list: ArrayList<modelProduk>) :
    RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img_produk)
        val nama: TextView = itemView.findViewById(R.id.tv_nama_produk)
        val harga: TextView = itemView.findViewById(R.id.tv_harga_produk)
        val status: Chip = itemView.findViewById(R.id.chip_status)
        val btnDelete: View? = itemView.findViewById(R.id.ivDelete) // Not currently in layout, let's check
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_produk, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        val tvCabang: TextView? = holder.itemView.findViewById(R.id.tvCabangBadge)
        if (data.idCabang.isNullOrEmpty()) {
            tvCabang?.visibility = View.VISIBLE
            tvCabang?.text = "Tersedia di Semua Cabang"
        } else {
            tvCabang?.visibility = View.GONE
        }

        // Nama Produk
        holder.nama.text = data.namaProduk ?: "-"

        // Harga Produk (fix dari hargaJual -> hargaProduk)
        val harga = data.hargaJual ?: 0
        holder.harga.text = "Rp %,d".format(harga)

        // Status Produk
        val statusText = data.statusProduk ?: "Habis / Nonaktif"
        holder.status.text = statusText

        if (statusText.contains("Tersedia") || statusText.equals("Aktif", ignoreCase = true)) {
            holder.status.setTextColor(holder.itemView.context.getColor(R.color.white))
            holder.status.setChipBackgroundColorResource(R.color.primary_teal)
            holder.status.chipStrokeWidth = 0f
        } else {
            holder.status.setTextColor(holder.itemView.context.getColor(R.color.text_dark))
            holder.status.setChipBackgroundColorResource(android.R.color.darker_gray)
            holder.status.chipStrokeWidth = 0f
        }

        holder.img.setImageResource(android.R.drawable.ic_menu_gallery)

        // Klik item
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, TambahProdukActivity::class.java)
            intent.putExtra("produk", data)
            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.findViewById<View>(R.id.ivDelete).setOnClickListener {
            val db = FirebaseDatabase.getInstance().getReference("produk")
            data.idProduk?.let { id ->
                db.child(id).removeValue().addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Produk dihapus", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun updateData(newList: List<modelProduk>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
