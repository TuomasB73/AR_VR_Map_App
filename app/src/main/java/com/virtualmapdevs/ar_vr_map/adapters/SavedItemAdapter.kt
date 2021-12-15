package com.virtualmapdevs.ar_vr_map.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.model.ARItem
import com.virtualmapdevs.ar_vr_map.utils.Constants

// Adapter for the saved maps recycler view
class SavedItemAdapter(
    private var arItemsList: MutableList<ARItem>?,
    private val clickListener: ClickListener,
    val context: Context
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
        var imageView: ImageView = view.findViewById(R.id.itemLogoImageView)
        var savedArListItemCard: MaterialCardView = view.findViewById(R.id.savedArListItemCard)
    }

    override fun onBindViewHolder(holder: ArItemViewHolder, position: Int) {
        holder.itemNameTextView.text = arItemsList?.get(position)?.name

        // The logo image is loaded with Glide
        Glide.with(context)
            .load("${Constants.AR_ITEM_MODEL_BASE_URL}${arItemsList?.get(position)?.logoImageReference}")
            .error(R.drawable.logo)
            .into(holder.imageView)

        holder.savedArListItemCard.setOnClickListener {
            clickListener.onItemClick(
                arItemsList?.get(position)?._id,
                arItemsList?.get(position)?.description,
                arItemsList?.get(position)?.latitude,
                arItemsList?.get(position)?.longitude
            )
        }
    }

    /* Updates the list with new search result content. As the list will be relatively short, the usage of
    notifyDataSetChanged is not a problem */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(updatedList: List<ARItem>) {
        arItemsList?.clear()
        arItemsList?.addAll(updatedList)
        notifyDataSetChanged()
    }

    // Interface for the item click action
    interface ClickListener {
        fun onItemClick(
            arItemId: String?,
            description: String?,
            latitude: Double?,
            longitude: Double?
        )
    }
}