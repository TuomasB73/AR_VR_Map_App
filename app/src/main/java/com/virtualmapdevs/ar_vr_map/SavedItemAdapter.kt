package com.virtualmapdevs.ar_vr_map

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.virtualmapdevs.ar_vr_map.model.ARItem

class SavedItemAdapter(
    var arItemsList: List<ARItem>?,
    private val clickListener: ClickListener
) :
    RecyclerView.Adapter<SavedItemAdapter.ArItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.saved_ar_list_item, parent, false)
        return ArItemViewHolder(itemView)
    }

    override fun getItemCount() = arItemsList?.size ?: 0

    inner class ArItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemNameTextView: TextView = view.findViewById(R.id.itemNameTextView)
        var imageView: ImageView = view.findViewById(R.id.mapImageView)
        var deleteSavedItemButton: Button = view.findViewById(R.id.deleteSavedItemButton)

    }

    override fun onBindViewHolder(holder: ArItemViewHolder, position: Int) {
        holder.itemNameTextView.text = arItemsList?.get(position)?.name

        // just a placeholder image
        holder.imageView.setImageResource(R.drawable.testlogo3)

        holder.imageView.setOnClickListener {
            clickListener.onItemClick(arItemsList?.get(position)?._id)
        }

        holder.itemNameTextView.setOnClickListener {
            clickListener.onItemClick(arItemsList?.get(position)?._id)
        }

        holder.deleteSavedItemButton.setOnClickListener {
            clickListener.onDeleteButtonPressed(arItemsList?.get(position)?._id)
        }
    }

    interface ClickListener {
        fun onItemClick(arItemId: String?)
        fun onDeleteButtonPressed(arItemId: String?)
    }

    fun updateData(updatedList: List<ARItem>){
        arItemsList = updatedList
        Log.d("test", updatedList.toString())
        notifyDataSetChanged()
    }
}