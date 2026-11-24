package com.vs.schoolmessenger.Dashboard.Parent

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel.ExamDataRewamp
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResults
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamMarkAdapter(
    private var itemList: List<ExamDataRewamp>,
    private var listener: ExamMarkListener,
    private var context: Context,
    private var isLoading: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<ExamDataRewamp> =
        itemList ?: listOf()
    private var filteredList: List<ExamDataRewamp> =
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


    // Called after the view-status API succeeds, so the API is not called again for this item
    fun markViewed(reportId: Int, viewType: String) {
        val exam = fullList.find { it.report_id == reportId } ?: return
        when (viewType) {
            Constant.view_type_mark -> exam.mark_viewed = true
            Constant.view_type_report -> exam.report_viewed = true
        }
        // Refresh the card so the NEW tag disappears
        val position = filteredList.indexOf(exam)
        if (position != -1) notifyItemChanged(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.reportName.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList =
                    results?.values as? List<ExamDataRewamp>
                        ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }


    inner class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val textExamTitle: TextView = itemView.findViewById(R.id.textExamTitle)
        private val btnViewMarks: TextView = itemView.findViewById(R.id.btnViewMarks)
        private val btnViewProgress: TextView = itemView.findViewById(R.id.btnViewProgress)
        private val rootHeader: LinearLayout = itemView.findViewById(R.id.rootHeader)
        private val frameViewMarks: View = itemView.findViewById(R.id.frameViewMarks)
        private val frameViewProgress: View = itemView.findViewById(R.id.frameViewProgress)
        private val badgeMarksNew: TextView = itemView.findViewById(R.id.badgeMarksNew)
        private val badgeProgressNew: TextView = itemView.findViewById(R.id.badgeProgressNew)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            exam: ExamDataRewamp,
            position: Int,
            adapter: ExamMarkAdapter
        ) {
            rootHeader.background.alpha = (0.1f * 255).toInt()
            textExamTitle.text = exam.reportName

            // Toggle the whole frame (button + NEW tag) so no empty space is left when hidden
            frameViewMarks.visibility = if (exam.mark_sent) View.VISIBLE else View.GONE
            frameViewProgress.visibility = if (exam.report_sent) View.VISIBLE else View.GONE

            // NEW tag stays until the view-status API confirms it was viewed
            badgeMarksNew.visibility = if (exam.mark_viewed) View.GONE else View.VISIBLE
            badgeProgressNew.visibility = if (exam.report_viewed) View.GONE else View.VISIBLE

            btnViewMarks.setOnClickListener {
                if (!exam.mark_viewed) {
                    listener.onExamViewStatus(exam.report_id, Constant.view_type_mark)
                }
                val context = itemView.context
                val intent = Intent(context, ExamMarkResults::class.java)
                intent.putExtra(Constant.exam_title, exam.reportName)
                intent.putExtra(Constant.exam_id, exam.report_id.toString())
                context.startActivity(intent)
            }
            btnViewProgress.setOnClickListener {
                if (!exam.report_viewed) {
                    listener.onExamViewStatus(exam.report_id, Constant.view_type_report)
                }
                listener.onExamSelected(exam.report_id.toString() ?: "", exam.reportName ?: "",exam.type?:"")

            }

        }
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}

