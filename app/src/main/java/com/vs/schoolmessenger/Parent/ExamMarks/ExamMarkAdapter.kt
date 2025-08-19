package com.vs.schoolmessenger.Dashboard.Parent

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResults
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamMarkAdapter(
    private var itemList: List<com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData>,
    private var listener: ExamMarkListener,
    private var context: Context,
    private var isLoading: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData> =
        itemList ?: listOf()
    private var filteredList: List<com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData> =
        itemList ?: listOf()

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
                filteredList =
                    results?.values as? List<com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData>
                        ?: listOf()
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
        private val rootHeader: LinearLayout = itemView.findViewById(R.id.rootHeader)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            exam: com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData,
            position: Int,
            adapter: ExamMarkAdapter
        ) {
            rootHeader.background.alpha = (0.2f * 255).toInt()
            textExamTitle.text = exam.name
            btnViewMarks.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ExamMarkResults::class.java)
                intent.putExtra("exam_title", exam.name)
                intent.putExtra("exam_id", exam.id)
                context.startActivity(intent)
            }
            btnViewProgress.setOnClickListener {
                listener.onExamSelected(exam.id ?: "", exam.name ?: "")

            }

        }
    }


    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}

