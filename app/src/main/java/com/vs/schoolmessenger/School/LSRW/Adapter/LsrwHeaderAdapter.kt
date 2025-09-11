package com.vs.schoolmessenger.School.LSRW.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.Model.LsrwHeaderItem
import com.vs.schoolmessenger.databinding.ItemLsrwHeaderBinding

class LsrwHeaderAdapter(
    private val items: List<LsrwHeaderItem>,
    private val onItemClick: (LsrwHeaderItem) -> Unit
) : RecyclerView.Adapter<LsrwHeaderAdapter.ViewHolder>() {

    private var selectedPosition = 0 // auto-select first item by default

    inner class ViewHolder(val binding: ItemLsrwHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // refresh only changed items
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                onItemClick(items[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLsrwHeaderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            txtTitle.text = item.title
            txtCount.text = item.percentage
            txtSubTitle.text = item.studentCount


            if (position == selectedPosition) {
                cardview.setBackgroundResource(R.drawable.custom_coupon_rounded_background_click)
            } else {
                cardview.setBackgroundResource(R.drawable.rect_shadow_white)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
