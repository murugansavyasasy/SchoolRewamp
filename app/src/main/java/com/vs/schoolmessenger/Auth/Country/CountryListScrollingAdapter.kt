package com.vs.schoolmessenger.Auth.Country

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class CountryListScrollingAdapter :
    ListAdapter<CountryItem, CountryListScrollingAdapter.FeatureItemViewHolder>(CountryCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureItemViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.iscountry_item, parent, false)
        return FeatureItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FeatureItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageFeature: ImageView = view.findViewById(R.id.imageFeature)

        fun bind(feature: CountryItem) {
            imageFeature.setImageResource(feature.iconResource)
            imageFeature.contentDescription = feature.contentDescription
        }
    }
}
