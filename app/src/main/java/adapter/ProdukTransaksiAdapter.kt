package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import model.modelProduk

class ProdukTransaksiAdapter(
    private var list: List<modelProduk>,
    private val onAdd: (modelProduk) -> Unit
) : RecyclerView.Adapter<ProdukTransaksiAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvHarga: TextView = view.findViewById(R.id.tvHarga)
        val tvStok: TextView = view.findViewById(R.id.tvStok)
        val btnTambah: Button = view.findViewById(R.id.btnTambah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaksi_produk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.namaProduk
        holder.tvHarga.text = "Rp %,d".format(item.hargaJual)
        holder.tvStok.text = if (item.tanpaBatas == true) "Stok: ∞" else "Stok: ${item.stokProduk}"

        holder.btnTambah.setOnClickListener { onAdd(item) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<modelProduk>) {
        list = newList
        notifyDataSetChanged()
    }
}
