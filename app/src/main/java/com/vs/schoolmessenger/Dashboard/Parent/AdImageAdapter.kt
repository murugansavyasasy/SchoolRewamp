package com.vs.schoolmessenger.Dashboard.Parent

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.R

class AdImageAdapter(val images: List<AdItem>) :
    RecyclerView.Adapter<AdImageAdapter.AdImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.ad_image_item, parent, false)
        return AdImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class AdImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imgAd)
        fun bind(adItem: AdItem) {
            Glide.with(itemView.context)
                .load("https://vs5.voicesnapforschools.com/nodejs/promotions/eviska_app.jpg")
                .into(imageView)
        }
    }
}
