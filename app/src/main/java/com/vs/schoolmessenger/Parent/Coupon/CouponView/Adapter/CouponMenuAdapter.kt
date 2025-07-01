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
    private val listener: OnCategoryClickListener?
) : RecyclerView.Adapter<CouponMenuAdapter.ViewHolder>() {

    interface OnCategoryClickListener {
        fun onCategoryClick(category: Category?)
    }

    private val categoryList: MutableList<Category> = mutableListOf()
    private var selectedPosition = RecyclerView.NO_POSITION

    fun setData(newList: List<Category>) {
        categoryList.clear()
        categoryList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.coupon_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        holder.textView.text = category.categoryName

        if (category.drawableResId != -1) {
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

        holder.relativeLayout.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            listener?.onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = categoryList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView1)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val relativeLayout: RelativeLayout = itemView.findViewById(R.id.relative_layout)
    }
}
