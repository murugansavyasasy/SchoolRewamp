package com.vs.schoolmessenger.Dashboard.Settings.WhatsNew

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.Model.WhatsNewUpdateData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class WhatsNewAdapter(
    private var itemList: List<WhatsNewUpdateData>?,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.whatsnew_recyclerview)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.whatsnew_recyclerview, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            itemList?.get(position)?.let { holder.bind(it) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById<TextView>(R.id.menu_name)
        private val descText = itemView.findViewById<TextView>(R.id.description_value)
        private val bannerImage = itemView.findViewById<ImageView>(R.id.banner_image)
        private val btnLearnMore = itemView.findViewById<Button>(R.id.btnLearnMore)

        fun bind(data: WhatsNewUpdateData) {
            titleText.text = data.name
            descText.text = data.description

            Glide.with(itemView.context)
                .load(data.downloadable_image)
                .placeholder(R.drawable.nodataimage)
                .into(bannerImage)

            btnLearnMore.setOnClickListener {
                val redirectUrl = data.app_redirect_link

                if (!redirectUrl.isNullOrEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
                        itemView.context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(itemView.context, "Unable to open link", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(itemView.context, "No redirect link available", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}



