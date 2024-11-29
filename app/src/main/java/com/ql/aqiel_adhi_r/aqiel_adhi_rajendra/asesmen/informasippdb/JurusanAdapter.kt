package com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb.databinding.ItemJurusanBinding
import com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb.databinding.HeaderJurusanBinding

class JurusanAdapter(private var jurusanList: List<Jurusan>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HEADER_VIEW_TYPE = 0
    private val ITEM_VIEW_TYPE = 1

    inner class HeaderViewHolder(val binding: HeaderJurusanBinding) : RecyclerView.ViewHolder(binding.root)

    inner class JurusanViewHolder(val binding: ItemJurusanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER_VIEW_TYPE -> {
                val binding = HeaderJurusanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            ITEM_VIEW_TYPE -> {
                val binding = ItemJurusanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                JurusanViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                // No action needed, header is static
            }
            is JurusanViewHolder -> {
                val jurusan = jurusanList[position - 1] // Adjust for header
                holder.binding.apply {
                    textNo.text = jurusan.no.toString()
                    textnama.text = jurusan.nama // Updated to display 'nama'
                    textNilaiTotal.text = jurusan.nilaiTotal.toString()
                }
            }
        }
    }

    override fun getItemCount(): Int = jurusanList.size + 1 // Add one for the header

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER_VIEW_TYPE else ITEM_VIEW_TYPE
    }

    fun updateData(newList: List<Jurusan>) {
        jurusanList = newList.sortedByDescending { it.nilaiTotal } // Urutkan berdasarkan nilaiTotal terbesar
        notifyDataSetChanged()
    }
}

