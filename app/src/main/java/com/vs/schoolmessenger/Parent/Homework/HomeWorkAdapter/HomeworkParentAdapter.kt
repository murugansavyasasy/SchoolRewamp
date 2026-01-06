package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.content.Context
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.animation.content.Content
import com.vs.schoolmessenger.Parent.Homework.HomeWorkDateClickListener
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil
import com.vs.schoolmessenger.databinding.HomeworkParentItemBinding
import java.util.Locale

class HomeworkParentAdapter(
    var isHomeWorkData: List<GetHomeworkDetails>,
    private val listener: HomeWorkDateClickListener,
    private var isLoading: Boolean,
    isHomeWorkDate: String,
    private var context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    var isDate = isHomeWorkDate

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
            holder.bind(item, listener, isDate)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else isHomeWorkData.size
    }


    fun updateItem(updatedItem: GetHomeworkDetails) {
        val index = isHomeWorkData.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            val mutableList = isHomeWorkData.toMutableList()
            mutableList[index] = updatedItem
            isHomeWorkData = mutableList
            originalList = mutableList
            notifyItemChanged(index)
        }
    }


    fun updateList(newData: List<GetHomeworkDetails>, date: String) {
        isHomeWorkData = newData
        originalList = newData
        isLoading = false
        isDate = date
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

        fun bind(item: GetHomeworkDetails, listener: HomeWorkDateClickListener, isDate: String) {
            binding.lblSubject.text = item.subject_name
            binding.lblTitle.text = item.title

            binding.imgTimimg.setColorFilter(
                ContextCompat.getColor(binding.root.context, R.color.dark_orange),
                PorterDuff.Mode.SRC_IN
            )
            if (item.is_unread) {
                binding.redDot.visibility = View.VISIBLE
                binding.imgTimimg.visibility = View.GONE
            } else {
                binding.redDot.visibility = View.INVISIBLE
                if (item.is_completed) {
                    binding.imgSuccess.visibility = View.VISIBLE
                    binding.imgTimimg.visibility = View.GONE
                }else {
                    binding.imgTimimg.visibility = View.VISIBLE
                    binding.imgSuccess.visibility = View.GONE
                }
            }
            if (item.is_completed) {
                binding.imgTimimg.visibility = View.GONE
                binding.imgSuccess.visibility = View.VISIBLE
            } else {
                if (item.is_unread) {
                    binding.imgTimimg.visibility = View.GONE
                    binding.imgSuccess.visibility = View.GONE
                }else{
                    binding.imgTimimg.visibility = View.VISIBLE
                    binding.imgSuccess.visibility = View.GONE
                }
            }

            binding.cardRoot.setOnClickListener {
                Log.d("data", item.id)

//                item.is_unread = false
                binding.redDot.visibility = View.INVISIBLE
                listener.onItemClick(item, isDate)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
