package com.virtualmapdevs.ar_vr_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

internal class MapAdapter(private var mapsList: List<String>) :
    RecyclerView.Adapter<MapAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mapNameTxt: TextView = view.findViewById(R.id.mapNameTextView)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = mapsList[position]
        holder.mapNameTxt.text = item
    }
    override fun getItemCount(): Int {
        return mapsList.size
    }
}