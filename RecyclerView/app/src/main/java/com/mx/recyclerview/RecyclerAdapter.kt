package com.mx.recyclerview

import android.content.Context
import com.mx.recyclerview.model.Animal
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mx.recyclerview.base.BaseViewHolder
import kotlinx.android.synthetic.main.details_row.view.*


class RecyclerAdapter(private val context: Context, val listDetail:List<Animal>):RecyclerView.Adapter<BaseViewHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return DetailOpportunitiesViewHolder(LayoutInflater.from(context).inflate(R.layout.details_row,parent,false))
    }

    override fun getItemCount(): Int = listDetail.size

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when(holder){
            is DetailOpportunitiesViewHolder -> holder.bind(listDetail[position],position)
            else -> throw IllegalArgumentException("ViewHolder not found")
        }
    }

    inner class DetailOpportunitiesViewHolder(itemView:  View):BaseViewHolder<Animal> (itemView){
        override fun bind(item: Animal, position: Int) {
            Glide.with(context).load(item.imagen).into(itemView.img)
            itemView.txt_title.text = item.nombre
        }
    }

}