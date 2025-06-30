package com.vs.schoolmessenger.Parent.Coupon.CouponView.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu.Category
import com.vs.schoolmessenger.R

class CouponMenuAdapter(
    private val context: Context,
    categoryList: MutableList<Category>,
    initiallySelectedPosition: Int,
    listener: OnCategoryClickListener?
) : RecyclerView.Adapter<CouponMenuAdapter.ViewHolder?>() {
    interface OnCategoryClickListener {
        fun onCategoryClick(category: Category?)
    }

    private val categoryList: MutableList<Category>
    private val listener: OnCategoryClickListener?
    private var selectedPosition = RecyclerView.NO_POSITION

    init {
        this.categoryList = categoryList
        this.listener = listener
        this.selectedPosition = initiallySelectedPosition
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.coupon_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category: Category = categoryList.get(position)
        holder.textView.setText(category.categoryName)

        if (category.drawableResId !== -1) {
            holder.imageView.setImageResource(category.drawableResId)
        } else {
            Glide.with(context)
                .load(category.categoryImage)
                .placeholder(R.drawable.allimage)
                .into(holder.imageView)
        }

        if (position == selectedPosition) {
            holder.imageView.setBackgroundResource(R.drawable.custom_coupon_rounded_background_click)
            holder.textView.setTextColor(ContextCompat.getColor(context, R.color.gnt_blue))
        } else {
            holder.imageView.setBackgroundResource(R.drawable.custom_coupon_rounded_background)
            holder.textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        holder.relative_layout.setOnClickListener(View.OnClickListener { v: View? ->
            val previousSelected = selectedPosition
            selectedPosition = holder.getAdapterPosition()
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            if (listener != null) {
                listener.onCategoryClick(category)
            }
        })
    }


    override fun getItemCount(): Int {
        return categoryList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var textView: TextView
        var relative_layout: RelativeLayout

        init {
            imageView = itemView.findViewById<ImageView?>(R.id.imageView1)
            textView = itemView.findViewById<TextView?>(R.id.textView)
            relative_layout = itemView.findViewById<RelativeLayout?>(R.id.relative_layout)
        }
    }
}