package com.vs.schoolmessenger.Parent.Coupon.CouponAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.CouponMenuClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.Category
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class CouponMenuAdapter(
    private val itemList: List<Category>,
    private val listener: CouponMenuClickListener,
    private val context: Context,
    private val isLoading: Boolean,
    private var selectedPosition: Int = RecyclerView.NO_POSITION
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.coupon_menu)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.coupon_menu, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }


    }

    fun selectPosition(position: Int) {
        val previousSelected = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousSelected)
        notifyItemChanged(selectedPosition)
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else itemList.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageview: ImageView = itemView.findViewById(R.id.imageView1)
        private val textView: TextView = itemView.findViewById(R.id.textView)

        fun bind(data: Category, position: Int) {
            textView.text = data.categoryName
            Glide.with(context)
                .load(data.categoryImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()  // Skip fade-in for snappier lists
                .priority(Priority.HIGH)  // Prioritize over other loads
                .placeholder(R.drawable.allimage)
                .into(imageview)

            // Set selected/unselected styles
            if (position == selectedPosition) {
                imageview.setBackgroundResource(R.drawable.custom_coupon_rounded_background_click)
                textView.setTextColor(ContextCompat.getColor(context, R.color.gnt_blue))
            } else {
                imageview.setBackgroundResource(R.drawable.custom_coupon_rounded_background)
                textView.setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            itemView.setOnClickListener {
                val clickedPosition = adapterPosition
                listener.onCategoryClick(data)
                if (clickedPosition != selectedPosition) {
                    val previousSelected = selectedPosition
                    selectedPosition = clickedPosition
                    notifyItemChanged(previousSelected)
                    notifyItemChanged(selectedPosition)
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
