package com.vs.schoolmessenger.Dashboard.Parent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.R

class ChildMenuAdapter(private val itemList: List<GridItem>) : RecyclerView.Adapter<ChildMenuAdapter.GridViewHolder>() {

    inner class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgMenu)
        val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item_card, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = itemList[position]
        holder.imageView.setImageResource(item.image)
        holder.lblMenuName.text = item.title
    }

    override fun getItemCount(): Int = itemList.size
}