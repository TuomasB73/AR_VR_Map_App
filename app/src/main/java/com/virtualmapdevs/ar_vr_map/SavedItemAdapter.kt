package com.virtualmapdevs.ar_vr_map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.virtualmapdevs.ar_vr_map.model.ARItem
import com.virtualmapdevs.ar_vr_map.utils.Constants

class SavedItemAdapter(
    private var arItemsList: MutableList<ARItem>?,
    private val clickListener: ClickListener,
    val context : Context
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


        Glide.with(context).load("${Constants.AR_ITEM_MODEL_BASE_URL}${arItemsList?.get(position)?.logoImageReference}")
            .error(R.drawable.testlogo4)
            .into(holder.imageView)

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

    fun updateData(updatedList: List<ARItem>) {
        arItemsList?.clear()
        arItemsList?.addAll(updatedList)
        arItemsList = updatedList.toMutableList()
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onItemClick(arItemId: String?)
        fun onDeleteButtonPressed(arItemId: String?)
    }
}