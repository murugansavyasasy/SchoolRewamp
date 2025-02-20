package com.vs.schoolmessenger.Testing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.databinding.CouponMenuBinding

class CouponMenuAdapter(private val categoryList: List<Category>) :
    RecyclerView.Adapter<CouponMenuAdapter.ViewHolder>() {

    class ViewHolder(val binding: CouponMenuBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CouponMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.textView.text = category.category_name
        Glide.with(holder.binding.root.context)
            .load(category.category_image)
            .into(holder.binding.imageView1)
    }

    override fun getItemCount(): Int = categoryList.size
}
