package kategori

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

        // Status indicator
        val statusText = data.statusKategori ?: "Tidak Aktif"
        holder.chipStatus.text = statusText
        
        if (statusText.equals("aktif", true)) {
            holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.white))
            holder.chipStatus.setChipBackgroundColorResource(R.color.primary_teal)
        } else {
            holder.chipStatus.setTextColor(holder.itemView.context.getColor(R.color.text_dark))
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
        }
        
        holder.ivEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, ModKategoriActivity::class.java)
            intent.putExtra("kategori", data)
            holder.itemView.context.startActivity(intent)
        }
        
        holder.ivDelete.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("kategori")
            data.idKategori?.let { id ->
                myRef.child(id).removeValue().addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Kategori dihapus", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
