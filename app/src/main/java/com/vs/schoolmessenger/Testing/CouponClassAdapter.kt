package com.vs.schoolmessenger.Testing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ItemCouponBinding

class CouponClassAdapter(private val itemList: List<Campaign>) :
    RecyclerView.Adapter<CouponClassAdapter.CouponViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val binding = ItemCouponBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CouponViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    class CouponViewHolder(private val binding: ItemCouponBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Campaign) {
            // Load thumbnail using Glide
            Glide.with(binding.root.context)
                .load(item.thumbnail)
                .into(binding.thumbnail) // Set image to ImageView

            // Set text values
            binding.categoryname.text = item.category_name
            binding.campaignname.text = item.campaign_name
            binding.merchantname.text = item.merchant_name
        }
    }

}
