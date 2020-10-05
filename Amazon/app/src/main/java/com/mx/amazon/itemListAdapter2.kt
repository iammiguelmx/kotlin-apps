package com.mx.amazon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ItemListAdapter2: RecyclerView.Adapter<ItemListAdapter2.ItemViewHolder>(){

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder (
        LayoutInflater.from(parent.context).
        inflate(R.layout.item_layout2,
            parent,false)
    )

    override fun getItemCount(): Int = 10

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    }

}