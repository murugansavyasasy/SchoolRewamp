package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter

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
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.Category
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class EventCategoryAdapter(
    private var itemList: List<Category>?,
    val listener: EventClickListener,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private val TYPE_STATIC = 2

    private var selectedPosition = 0

    override fun getItemViewType(position: Int): Int {
        return when {
            isLoading -> TYPE_SHIMMER
            position == 0 -> TYPE_STATIC
            else -> TYPE_DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SHIMMER -> {
                val shimmerView =
                    ShimmerUtil.wrapWithShimmer(parent, R.layout.event_category_rewamp)
                ShimmerViewHolder(shimmerView)
            }

            TYPE_STATIC -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.event_category_rewamp, parent, false)
                StaticViewHolder(view, context)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.event_category_rewamp, parent, false)
                DataViewHolder(view, context)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ShimmerViewHolder -> holder.startShimmer()

            is StaticViewHolder -> {
                val isSelected = position == selectedPosition
                holder.bind(isSelected, this)
            }

            is DataViewHolder -> {
                val actualPosition = position - 1
                val isSelected = position == selectedPosition
                itemList?.getOrNull(actualPosition)?.let {
                    holder.bind(it, actualPosition, isSelected, this)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else (itemList?.size ?: 0) + 1
    }

    fun onCategorySelected(newPosition: Int) {
        val oldPosition = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(oldPosition)
        notifyItemChanged(newPosition)
    }

    fun updateList(newList: List<Category>?) {
        itemList = newList
        isLoading = false
        notifyDataSetChanged()
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val categoryImage: ImageView = itemView.findViewById(R.id.category_image)
        private val layout: RelativeLayout = itemView.findViewById(R.id.relative_layout)


        fun bind(
            data: Category, position: Int, isSelected: Boolean, adapter: EventCategoryAdapter
        ) {
            categoryName.text = data.name
            Glide.with(context).load(data.url).placeholder(R.drawable.allimage).into(categoryImage)

            categoryImage.setBackgroundResource(
                if (isSelected) R.drawable.custom_coupon_rounded_background_click
                else R.drawable.custom_coupon_rounded_background1
            )

            categoryName.setTextColor(
                if (isSelected) ContextCompat.getColor(context, R.color.gnt_blue)
                else ContextCompat.getColor(context, R.color.black)
            )

            layout.setOnClickListener {
                adapter.onCategorySelected(adapterPosition)
                adapter.listener.onCategoryClicked(data)
            }
        }
    }

    class StaticViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val categoryImage: ImageView = itemView.findViewById(R.id.category_image)
        private val layout: RelativeLayout = itemView.findViewById(R.id.relative_layout)


        fun bind(
            isSelected: Boolean,
            adapter: EventCategoryAdapter
        ) {
            categoryName.text = context.getString(R.string.all)
            categoryImage.setImageResource(R.drawable.allimage)

            categoryImage.setBackgroundResource(
                if (isSelected) R.drawable.custom_coupon_rounded_background_click
                else R.drawable.custom_coupon_rounded_background1
            )

            categoryName.setTextColor(
                if (isSelected) ContextCompat.getColor(context, R.color.gnt_blue)
                else ContextCompat.getColor(context, R.color.black)
            )

            itemView.setOnClickListener {
                adapter.onCategorySelected(adapterPosition)
                adapter.listener.onCategoryClicked(Category(0, Constant.All_, ""))
            }
        }
    }
}
