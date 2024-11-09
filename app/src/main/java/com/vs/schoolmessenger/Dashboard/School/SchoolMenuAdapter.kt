package com.vs.schoolmessenger.Dashboard.School

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.R

class SchoolMenuAdapter(private val itemList: List<GridItem>?, private val isLoading: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var isPosition = 0


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_SHIMMER) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.shimmer_item, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.menu_item_card, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position])

//            when (isPosition) {
//                0 -> {
//                    holder.itemView.background = ContextCompat.getDrawable(
//                        holder.itemView.context,
//                        R.drawable.rect_shadow_violet
//                    )
//                    isPosition = 1
//                }
//
//                1 -> {
//                    holder.itemView.background = ContextCompat.getDrawable(
//                        holder.itemView.context,
//                        R.drawable.rect_shadow_blue
//                    )
//                    isPosition = 2
//                }
//
//                2 -> {
//                    holder.itemView.background = ContextCompat.getDrawable(
//                        holder.itemView.context,
//                        R.drawable.rect_shadow_green
//                    )
//                    isPosition = 3
//                }
//
//                3 -> {
//                    holder.itemView.background =
//                        ContextCompat.getDrawable(
//                            holder.itemView.context,
//                            R.drawable.rect_shadow_light_sky_blue
//                        )
//                    isPosition = 0
//                }
//            }
        }
    }


    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: GridItem) {
            val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
            val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)

            imgMenu.setImageResource(data.image)
            lblMenuName.text = data.title
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show 5 shimmer items while loading
        else itemList?.size ?: 0
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}