package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Homework.HomeWorkDateClickListener
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil
import com.vs.schoolmessenger.databinding.HomeworkParentItemBinding
import java.util.Locale

class HomeworkParentAdapter(
    private var isHomeWorkData: List<GetHomeworkDetails>,
    private val listener: HomeWorkDateClickListener,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList: List<GetHomeworkDetails> = isHomeWorkData

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.homework_parent_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val binding = HomeworkParentItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            DataViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            val item = isHomeWorkData[position]
            holder.bind(item, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else isHomeWorkData.size
    }

    fun updateList(newData: List<GetHomeworkDetails>) {
        isHomeWorkData = newData
        originalList = newData
        isLoading = false
        notifyDataSetChanged()
    }

    fun updateLoading(isLoadingNow: Boolean) {
        isLoading = isLoadingNow
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        isHomeWorkData = if (lowerCaseQuery.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.subject_name.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.title.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            }
        }
        notifyDataSetChanged()
    }

    class DataViewHolder(private val binding: HomeworkParentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GetHomeworkDetails, listener: HomeWorkDateClickListener) {
            binding.lblSubject.text = item.subject_name
            binding.lblTitle.text = item.title

            binding.cardRoot.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
