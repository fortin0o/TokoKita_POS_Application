package kategori

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import com.google.android.material.card.MaterialCardView
import model.modelPegawai

class AdapterPegawai(
    private var list: List<modelPegawai>,
    private val onEdit: (modelPegawai) -> Unit,
    private val onDelete: (modelPegawai) -> Unit
) : RecyclerView.Adapter<AdapterPegawai.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaPegawai)
        val tvRole: TextView = view.findViewById(R.id.tvRole)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val btnEdit: MaterialCardView = view.findViewById(R.id.btnEdit)
        val btnDelete: MaterialCardView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.namaPegawai
        holder.tvRole.text = item.rolePegawai
        holder.tvPhone.text = item.phonePegawai

        // Dynamic Role Color
        when (item.rolePegawai) {
            "Manager" -> holder.tvRole.setTextColor(Color.parseColor("#E91E63")) // Pink
            "Admin" -> holder.tvRole.setTextColor(Color.parseColor("#2196F3")) // Blue
            "Kasir" -> holder.tvRole.setTextColor(Color.parseColor("#4CAF50")) // Green
            else -> holder.tvRole.setTextColor(Color.parseColor("#9C27B0")) // Purple
        }

        // Handle Status Color (Implicitly by the status field in model, can add a status view if needed)
        // If status was "Tidak Aktif", we could gray out the card or change name color
        if (item.statusPegawai == "Tidak Aktif") {
            holder.tvNama.setTextColor(Color.GRAY)
        } else {
            holder.tvNama.setTextColor(Color.parseColor("#004D40")) // Primary Brand color
        }

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<modelPegawai>) {
        list = newList
        notifyDataSetChanged()
    }
}
