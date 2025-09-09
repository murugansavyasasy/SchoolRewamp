package com.vs.schoolmessenger.School.QuizExam.Adapter.SubmitReport


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.QuizExam.Model.QuizSubmissionList.GetQuizSubmissionListData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class QuizSubmitReportAdapter(
    private var itemList: List<GetQuizSubmissionListData>?,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.quiz_submit_report_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.quiz_submit_report_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }


    fun updateData(newList: List<GetQuizSubmissionListData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblSubmitStatus: TextView = itemView.findViewById(R.id.lblSubmitStatus)
        private val lblStandardSection: TextView = itemView.findViewById(R.id.lblStandardSection)
        private val tvSubmittedOn: TextView = itemView.findViewById(R.id.tvSubmittedOn)
        private val rlaSubmittedOn: RelativeLayout = itemView.findViewById(R.id.rlaSubmittedOn)


        fun bind(data: GetQuizSubmissionListData, position: Int) {
            lblName.text = data.student_name
            lblStandardSection.text = "${data.standard} - ${data.section}"
            if (data.is_submit){
                rlaSubmittedOn.visibility=View.VISIBLE
                tvSubmittedOn.text =Constant.convertDateFormatType2(data.submitted_on)
                lblSubmitStatus.text =context.getString(R.string.submitted)
                lblSubmitStatus.background.setTint(ContextCompat.getColor(context, R.color.green))
                lblSubmitStatus.setTextColor(ContextCompat.getColor(context,R.color.white))

            }
            else{
                rlaSubmittedOn.visibility=View.GONE
                tvSubmittedOn.text =context.getString(R.string.not_submitted)
                lblSubmitStatus.background.setTint(ContextCompat.getColor(context,R.color.red))
                lblSubmitStatus.setTextColor(ContextCompat.getColor(context,R.color.white))
            }


        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}