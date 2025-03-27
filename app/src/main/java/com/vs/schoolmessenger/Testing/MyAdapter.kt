package com.vs.schoolmessenger.Testing


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Utils.ShimmerLoadingItem
import com.vs.schoolmessenger.databinding.ItemLayoutBinding

class MyAdapter(
    private var dataList: List<String>,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SHIMMER = 0
    private val VIEW_TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SHIMMER) {
            ShimmerViewHolder(
                ComposeView(parent.context).apply {
                    setContent { ShimmerLoadingItem() }
                }
            )
        } else {
            val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DataViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(dataList[position])
        }
    }

    override fun getItemCount(): Int = if (isLoading) 5 else dataList.size // Show 5 shimmer items while loading

    class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DataViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.textView.text = item
        }
    }
}


