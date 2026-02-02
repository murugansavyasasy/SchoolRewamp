package com.vs.schoolmessenger.Auth.Introduction

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.vs.schoolmessenger.Auth.Introduction.Model.GetFeatureData
import com.vs.schoolmessenger.R

class OnboardingAdapter(
    private val items: List<GetFeatureData>,
    private var context: Context
) : RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgOnboard)
        val title: TextView = itemView.findViewById(R.id.txtTitle)
        val desc: TextView = itemView.findViewById(R.id.txtDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Load image from URL using Glide
        Glide.with(context)
            .load(item.file_path.get(0).url)
            .thumbnail(0.1f)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(600, 600)
            .dontAnimate()  // Skip fade-in for snappier lists
            .priority(Priority.HIGH)  // Prioritize over other loads
            .placeholder(R.drawable.no_search_message)
            .error(R.drawable.no_search_message)
            .into(holder.img)

//        Glide.with(context)
//            .load(item.file_path.get(0).url)
//            .placeholder(R.drawable.no_search_message)
//            .thumbnail(0.25f)  // Slightly higher for better preview quality without much cost
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .override(120, 120)  // Much smaller: matches ~30dp display size (adjust if needed)
//            .centerCrop()  // Crop to fit circle efficiently
//            .dontAnimate()  // Skip fade-in for snappier lists
//            .priority(Priority.HIGH)  // Prioritize over other loads
//            .error(R.drawable.no_search_message)
//            .listener(object : RequestListener<Drawable> {
//                override fun onLoadFailed(
//                    e: GlideException?,
//                    model: Any?,
//                    target: com.bumptech.glide.request.target.Target<Drawable>,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    Log.e("GlideError", "Image load failed", e)
//                    return false
//                }
//                override fun onResourceReady(
//                    resource: Drawable,
//                    model: Any,
//                    target: Target<Drawable>,
//                    dataSource: DataSource,
//                    isFirstResource: Boolean
//                ): Boolean = false
//            })
//            .into(holder.img)

        holder.title.text = item.title
        holder.desc.text = item.description
    }
}