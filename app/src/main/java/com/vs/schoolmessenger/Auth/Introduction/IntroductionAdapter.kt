package com.vs.schoolmessenger.Auth.Introduction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
            .placeholder(R.drawable.no_search_message)
            .error(R.drawable.no_search_message)
            .into(holder.img)

        holder.title.text = item.title
        holder.desc.text = item.description
    }
}