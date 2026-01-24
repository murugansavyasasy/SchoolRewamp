package com.vs.schoolmessenger.School.Homework

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import com.vs.schoolmessenger.databinding.HomeworkParentItemBinding
import java.util.Locale


class HomeWorkReportAdapter(
    private var context: Context,
    private var isHomeWorkData: List<HomeWorkReportData>,
    private val listener: HomeWorkReportClickListener,
    private var isLoading: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList: List<HomeWorkReportData> = ArrayList(isHomeWorkData)

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
            holder.bind(item, listener, context)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else isHomeWorkData.size
    }

    fun updateList(newData: List<HomeWorkReportData>) {
        isHomeWorkData = newData
        originalList = newData
        isLoading = false
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

        fun bind(
            item: HomeWorkReportData,
            listener: HomeWorkReportClickListener,
            context: Context
        ) {
            binding.lblSubject.text = item.subject_name
            binding.lblTitle.text = item.title
            binding.redDot.visibility = View.GONE
            binding.imgSuccess.visibility = View.GONE
            binding.progressContainer.visibility = View.GONE
            if (item.can_edit && item.can_delete) {
                binding.imgEditAndDelete.visibility = View.VISIBLE
            } else {
                binding.imgEditAndDelete.visibility = View.GONE
            }

            binding.imgEditAndDelete.setOnClickListener {
                listener.onClickListener(item, it, adapterPosition)
            }


            binding.cardRoot.setOnClickListener {
                Constant.isVideoPostedDate = item.created_on
                val convertedList = item.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }


                val isHomeWorkData = FilePreview(
                    id = "",
                    title = item.title,
                    description = item.description,
                    subjectName = item.subject_name,
                    sentBy = item.sent_by,
                    thumbnail = "",
                    created_date = item.created_on,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_HOMEWORK,
                    fileList = convertedList,
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}