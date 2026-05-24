package kategori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import com.google.android.material.chip.Chip
import model.modelPelanggan

class AdapterPelanggan(
    private var list: List<modelPelanggan>,
    private val onEdit: (modelPelanggan) -> Unit,
    private val onDelete: (modelPelanggan) -> Unit
) : RecyclerView.Adapter<AdapterPelanggan.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaPelanggan)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val chipStatus: Chip = view.findViewById(R.id.chipStatus)
        val btnEdit: View = view.findViewById(R.id.ivEdit)
        val btnDelete: View = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.namaPelanggan
        holder.tvPhone.text = item.phonePelanggan
        holder.chipStatus.text = item.statusPelanggan

        if (item.statusPelanggan == "Aktif") {
            holder.chipStatus.setChipBackgroundColorResource(R.color.primary_teal)
            holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.white))
        } else {
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
            holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.text_dark))
        }

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<modelPelanggan>) {
        list = newList
        notifyDataSetChanged()
    }
}
