package com.virtualmapdevs.ar_vr_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.virtualmapdevs.ar_vr_map.model.ARItem

class SavedItemAdapter(
    private val arItemsList: List<ARItem>?,
    private val clickListener: ClickListener
) :
    RecyclerView.Adapter<SavedItemAdapter.ArItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return ArItemViewHolder(itemView)
    }

    override fun getItemCount() = arItemsList?.size ?: 0

    inner class ArItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemNameTextView = view.findViewById<TextView>(R.id.itemNameTextView)
    }

    override fun onBindViewHolder(holder: ArItemViewHolder, position: Int) {
        holder.itemNameTextView.text = arItemsList?.get(position)?.name

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(arItemsList?.get(position)?._id)
        }
    }

    interface ClickListener {
        fun onItemClick(arItemId: String?)
    }
}