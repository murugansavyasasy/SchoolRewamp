package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.Category
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.EventItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class EventCategoryAdapter(
    private var itemList: List<Category>?,
    private var listener: EventClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private val TYPE_STATIC = 2

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
                val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.event_category_rewamp)
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
            is DataViewHolder -> {
                val actualPosition = position - 1
                itemList?.getOrNull(actualPosition)?.let {
                    holder.bind(it, actualPosition, listener, this)
                }
            }

            is ShimmerViewHolder -> {
                holder.startShimmer()
            }

            is StaticViewHolder -> {
                holder.bind(listener)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else (itemList?.size ?: 0) + 1
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val category_name: TextView = itemView.findViewById(R.id.category_name)
        private val category_image: ImageView = itemView.findViewById(R.id.category_image)

        fun bind(
            data: Category,
            position: Int,
            listener: EventClickListener,
            adapter: EventCategoryAdapter
        ) {
            category_name.text = data.name
            Glide.with(context)
                .load(data.url)
                .placeholder(R.drawable.allimage)
                .into(category_image)

//            itemView.setOnClickListener {
//                listener.onCategoryClicked(data)
//            }
        }
    }

    class StaticViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val category_name: TextView = itemView.findViewById(R.id.category_name)
        private val category_image: ImageView = itemView.findViewById(R.id.category_image)

        fun bind(listener: EventClickListener) {
            category_name.text = "All"
            category_image.setImageResource(R.drawable.allimage)

//            itemView.setOnClickListener {
//                val staticCategory = Category(1, "All","")
//            }
        }
    }

}
