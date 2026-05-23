package kategori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.donald.aplikasikedua.R
import com.google.android.material.chip.Chip
import model.modelKategori

class KategoriAdapter(private val list: ArrayList<modelKategori>) :
    RecyclerView.Adapter<KategoriAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val chipStatus: Chip = itemView.findViewById(R.id.chipAdd)
        val ivEdit: ImageView = itemView.findViewById(R.id.ivEdit)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.tvNama.text = data.namaKategori

        // 🔥 LOGIC ICON + TEXT
        if (data.statusKategori.equals("aktif", true)) {
            holder.chipStatus.text = holder.itemView.context.getString(R.string.status_aktif)
            holder.chipStatus.setChipBackgroundColorResource(R.color.green)
        } else {
            holder.chipStatus.text = holder.itemView.context.getString(R.string.status_tidak_aktif)
            holder.chipStatus.setChipBackgroundColorResource(R.color.red)
        }
        
        holder.ivEdit.setOnClickListener {
            // Handle edit
        }
        
        holder.ivDelete.setOnClickListener {
            // Handle delete
        }
    }
}
