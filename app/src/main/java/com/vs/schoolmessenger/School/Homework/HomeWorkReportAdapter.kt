package com.vs.schoolmessenger.School.Homework

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport
import com.vs.schoolmessenger.Utils.ShimmerUtil
import com.vs.schoolmessenger.databinding.HomeworkParentItemBinding
import java.util.Locale


class HomeWorkReportAdapter(
    private var isHomeWorkData: List<HomeWorkReport>,
    private val listener: HomeWorkReportClickListener,
    private var isLoading: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList: List<HomeWorkReport> = ArrayList(isHomeWorkData)

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
        if (holder is DataViewHolder && !isLoading && position < isHomeWorkData.size) {
            val item = isHomeWorkData[position]
            holder.bind(item, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else isHomeWorkData.size
    }

    fun updateList(newData: List<HomeWorkReport>) {
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
                it.subject_name.lowercase(Locale.getDefault())
                    .contains(lowerCaseQuery) || it.title.lowercase(Locale.getDefault())
                    .contains(lowerCaseQuery)
            }
        }
        notifyDataSetChanged()
    }

    fun removeItemAt(position: Int) {
        if (position in isHomeWorkData.indices) {
            val mutableList = isHomeWorkData.toMutableList()
            val removedItem = mutableList.removeAt(position)
            isHomeWorkData = mutableList
            originalList = originalList.filter { it != removedItem }
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, isHomeWorkData.size)
        }
    }

    class DataViewHolder(private val binding: HomeworkParentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeWorkReport, listener: HomeWorkReportClickListener) {
            binding.lblSubject.text = item.subject_name
            binding.lblTitle.text = item.title
            binding.redDot.visibility = View.GONE
            binding.imgSuccess.visibility = View.GONE
            binding.progressContainer.visibility = View.GONE
            binding.imgEditAndDelete.visibility = View.VISIBLE

            binding.imgEditAndDelete.setOnClickListener {
                listener.onClickListener(item, it, adapterPosition)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}