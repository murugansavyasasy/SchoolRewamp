package com.vs.schoolmessenger.Dashboard.Parent

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Dashboard.Model.AdItem
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.Parent.Video.ParentVideo
import com.vs.schoolmessenger.R


class ChildMenuAdapter(
    private var context: Context,
    private val itemList: List<GridItem>?,
    private val specialImages: List<AdItem>? = null, // New parameter for special images
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private val TYPE_SPECIAL = 2

    override fun getItemViewType(position: Int): Int {
        return when {
            isLoading -> TYPE_SHIMMER
            position == 8 -> TYPE_SPECIAL // Use TYPE_SPECIAL for the 8th position
            else -> TYPE_DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SHIMMER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_dashboard_item, parent, false)
                ShimmerViewHolder(view)
            }
            TYPE_SPECIAL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ad_item, parent, false) // Inflate the special view layout
                SpecialViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.menu_item_card, parent, false)
                DataViewHolder(view, context)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DataViewHolder -> holder.bind(itemList!![position])
            is SpecialViewHolder -> {
                // Pass the specialImages list and the current position
                holder.bind(specialImages ?: emptyList(), position)
            }
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
        private val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)
        private val rlaMenu: RelativeLayout = itemView.findViewById(R.id.rlaMenu)

        fun bind(data: GridItem) {
            imgMenu.setImageResource(data.image)
            lblMenuName.text = data.title

            rlaMenu.setOnClickListener {
                when (data.title) {
                    "Video" -> context.startActivity(Intent(context, ParentVideo::class.java))
                }
            }
        }
    }

    class SpecialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val specialImageView: ImageView = itemView.findViewById(R.id.imgAd)

        fun bind(images: List<AdItem>, position: Int) {
            Log.d("SpecialViewHolder", "bind called with position: $position")
            if (images.isNotEmpty() && position < images.size) {
                Glide.with(itemView)
                    .load(images[position].image) // Load the image using the URL
                    .into(specialImageView)
                Log.d("images", images[position].image)
            } else {
                Log.d("SpecialViewHolder", "No images to load")
            }
        }
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }
}


//class ChildMenuAdapter(
//    private var context: Context, private val itemList: List<GridItem>?,
//    private val isLoading: Boolean
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val view =
//                LayoutInflater.from(parent.context).inflate(R.layout.shimmer_dashboard_item, parent, false)
//            ShimmerViewHolder(view)
//        } else {
//            val view =
//                LayoutInflater.from(parent.context).inflate(R.layout.menu_item_card, parent, false)
//            DataViewHolder(view, context) // Pass context to DataViewHolder
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is DataViewHolder) {
//            // Bind actual data when loading is complete
//            holder.bind(itemList!![position])
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 20 // Show shimmer items while loading
//        else itemList?.size ?: 0
//    }
//
//    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
//        private val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
//        private val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)
//        private val rlaMenu: RelativeLayout = itemView.findViewById(R.id.rlaMenu)
//
//        fun bind(data: GridItem) {
//            imgMenu.setImageResource(data.image)
//            lblMenuName.text = data.title
//
//            rlaMenu.setOnClickListener {
//                when (data.title) {
//                    "Video" -> {
//                        context.startActivity(Intent(context, ParentVideo::class.java))
//                    }
//                }
//            }
//        }
//    }
//
//    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)
//
//        init {
//            shimmerLayout.startShimmer() // Start shimmer effect
//        }
//    }
//}
