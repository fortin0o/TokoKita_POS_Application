package adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import com.donald.aplikasikedua.R
import android.content.Context
import android.view.View
import android.widget.TextView
import model.modelKategori

class DetailKategoriAdapter (private val kategorilist: List<modelKategori>) :
    RecyclerView.Adapter<DetailKategoriAdapter.KategoriViewHolder>(){
    lateinit var appContext: Context
    interface OnItemClickListener {
        fun onItemClicked(kategori: modelKategori)
    }
    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_data, parent, false)
        appContext = parent.context
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: KategoriViewHolder,
        position: Int
    ) {
        val kategori = kategorilist[position]
        holder.bind(kategori)
    }

    override fun getItemCount(): Int {
        return kategorilist.size
    }
    inner class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvNamaKategori: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val chipStatus: TextView = itemView.findViewById(R.id.chipAdd)

        fun bind(kategori: modelKategori) {
            tvNamaKategori.text = kategori.namaKategori
            val status = kategori.statusKategori

            itemView.setOnClickListener {
                listener?.onItemClicked(kategori)
            }
        }
    }
}