package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import model.modelTransaksi

class AdapterTransaksi(private var list: List<modelTransaksi>) :
    RecyclerView.Adapter<AdapterTransaksi.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvIdTransaksi)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val tvItems: TextView = view.findViewById(R.id.tvItems)
        val tvKasir: TextView = view.findViewById(R.id.tvKasir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaksi_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvId.text = item.idTransaksi
        holder.tvTanggal.text = item.tanggal
        holder.tvTotal.text = "Rp %,d".format(item.totalHarga)
        holder.tvKasir.text = "Kasir: ${item.namaPegawai}"

        val itemsText = item.listProduk?.joinToString(", ") { it.produk?.namaProduk ?: "" }
        holder.tvItems.text = "${item.listProduk?.sumOf { it.jumlah }} Items ($itemsText)"
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<modelTransaksi>) {
        list = newList
        notifyDataSetChanged()
    }
}
