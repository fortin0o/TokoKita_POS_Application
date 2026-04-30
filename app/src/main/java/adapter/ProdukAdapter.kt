package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import com.google.android.material.chip.Chip
import model.modelProduk

class ProdukAdapter(private val list: ArrayList<modelProduk>) :
    RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img_produk)
        val nama: TextView = itemView.findViewById(R.id.tv_nama_produk)
        val harga: TextView = itemView.findViewById(R.id.tv_harga_produk)
        val status: Chip = itemView.findViewById(R.id.chip_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_produk, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        // ✅ Nama Produk
        holder.nama.text = data.namaProduk ?: "-"

        // ✅ Harga Produk (fix dari hargaJual → hargaProduk)
        val harga = data.hargaJual ?: 0
        holder.harga.text = "Rp %,d".format(harga)

        // ✅ Status Produk
        val statusText = data.statusProduk ?: "Aktif"
        holder.status.text = statusText

        if (statusText.equals("Aktif", ignoreCase = true)) {
            holder.status.setTextColor(holder.itemView.context.getColor(R.color.green))
            holder.status.chipStrokeColor =
                holder.itemView.context.getColorStateList(R.color.green)
        } else {
            holder.status.setTextColor(holder.itemView.context.getColor(R.color.red))
            holder.status.chipStrokeColor =
                holder.itemView.context.getColorStateList(R.color.red)
        }

        // ✅ Placeholder gambar (sementara)
        holder.img.setImageResource(android.R.drawable.ic_menu_gallery)


        // 🔥 Klik item (siap untuk next fitur)
        holder.itemView.setOnClickListener {
            // nanti bisa ke detail produk
        }
    }

    // 🔥 WAJIB untuk search biar smooth
    fun updateData(newList: List<modelProduk>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
