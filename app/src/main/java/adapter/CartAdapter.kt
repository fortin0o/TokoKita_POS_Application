package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import model.modelCartItem

class CartAdapter(
    private var list: MutableList<modelCartItem>,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvHarga: TextView = view.findViewById(R.id.tvHarga)
        val tvJumlah: TextView = view.findViewById(R.id.tvJumlah)
        val btnMin: ImageView = view.findViewById(R.id.btnMin)
        val btnPlus: ImageView = view.findViewById(R.id.btnPlus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.produk?.namaProduk
        val hargaTotal = (item.produk?.hargaJual ?: 0) * item.jumlah
        holder.tvHarga.text = "Rp %,d".format(hargaTotal)
        holder.tvJumlah.text = item.jumlah.toString()

        holder.btnPlus.setOnClickListener {
            item.jumlah++
            notifyItemChanged(position)
            onUpdate()
        }

        holder.btnMin.setOnClickListener {
            if (item.jumlah > 1) {
                item.jumlah--
                notifyItemChanged(position)
            } else {
                list.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, list.size)
            }
            onUpdate()
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: MutableList<modelCartItem>) {
        list = newList
        notifyDataSetChanged()
    }
}
