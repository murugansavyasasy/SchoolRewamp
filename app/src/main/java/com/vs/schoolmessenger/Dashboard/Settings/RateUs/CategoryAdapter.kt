package com.vs.schoolmessenger.Dashboard.Settings.RateUs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.CategoryItem
import com.vs.schoolmessenger.R

class CategoryAdapter(
    private val items: MutableList<CategoryItem>,
    private val onSelect: () -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {

    inner class CategoryHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val txt = view.findViewById<TextView>(R.id.txtCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false)
        return CategoryHolder(v)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        val item = items[position]

        holder.txt.text = item.name
        holder.txt.isSelected = item.selected == true

        holder.txt.setOnClickListener {
            item.selected = !(item.selected ?: false)
            notifyItemChanged(position)
            onSelect()
        }
    }

    override fun getItemCount() = items.size
}
