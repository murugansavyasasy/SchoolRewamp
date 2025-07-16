package com.vs.schoolmessenger.Dashboard.Parent

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import android.view.View
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel.ExamData
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResults
import com.vs.schoolmessenger.Parent.ExamMarks.ExamProgressActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil
class ExamMarkAdapter(
    private var itemList: List<ExamData>?,
    private var listener: ExamMarkListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<ExamData> = itemList ?: listOf()
    private var filteredList: List<ExamData> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.item_exam_card)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exam_card, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position, this)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.name.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<ExamData> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }

    inner class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val textExamTitle: TextView = itemView.findViewById(R.id.textExamTitle)
        private val btnViewMarks: Button = itemView.findViewById(R.id.btnViewMarks)
        private val btnViewProgress: Button = itemView.findViewById(R.id.btnViewProgress)
        @SuppressLint("ClickableViewAccessibility")
        fun bind(exam: ExamData, position: Int, adapter: ExamMarkAdapter) {
            textExamTitle.text = exam.name
            btnViewMarks.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ExamMarkResults::class.java)
                intent.putExtra("exam_id", exam.id)
                context.startActivity(intent)
            }
            btnViewProgress.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ExamProgressActivity::class.java)
                intent.putExtra("exam_id", exam.id)
                context.startActivity(intent)
            }





        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}

