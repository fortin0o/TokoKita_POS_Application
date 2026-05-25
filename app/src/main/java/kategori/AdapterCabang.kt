package kategori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import com.google.android.material.chip.Chip
import model.modelCabang

class AdapterCabang(
    private var list: List<modelCabang>,
    private val onEdit: (modelCabang) -> Unit,
    private val onDelete: (modelCabang) -> Unit
) : RecyclerView.Adapter<AdapterCabang.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaCabang)
        val tvAlamat: TextView = view.findViewById(R.id.tvAlamatCabang)
        val chipStatus: Chip = view.findViewById(R.id.chipStatus)
        val ivEdit: View = view.findViewById(R.id.ivEdit)
        val ivDelete: View = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.namaCabang
        holder.tvAlamat.text = item.alamatCabang
        holder.chipStatus.text = item.statusCabang

        if (item.statusCabang == "Pusat") {
            holder.chipStatus.setChipBackgroundColorResource(R.color.primary_teal)
            holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.white))
        } else {
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
            holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.text_dark))
        }

        holder.ivEdit.setOnClickListener { onEdit(item) }
        holder.ivDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<modelCabang>) {
        list = newList
        notifyDataSetChanged()
    }
}
