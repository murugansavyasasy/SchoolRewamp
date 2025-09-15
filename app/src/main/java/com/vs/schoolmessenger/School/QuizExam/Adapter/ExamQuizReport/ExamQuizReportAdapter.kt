package com.vs.schoolmessenger.School.QuizExam.Adapter.ExamQuizReport
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData
import com.vs.schoolmessenger.School.QuizExam.QuizExamReport.AddQuestion
import com.vs.schoolmessenger.School.QuizExam.QuizExamReport.SubmitReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamQuizReportAdapter(
    private var itemList: List<GetQuizExamReportData>?,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.exam_quiz_report_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.exam_quiz_report_item, parent, false)
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


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblQuizDescription: TextView = itemView.findViewById(R.id.lblQuizDescription)
        private val subjectvalue: TextView = itemView.findViewById(R.id.subjectvalue)
        private val lblPostedBy: TextView = itemView.findViewById(R.id.lblPostedBy)
        private val lblCreatedOn: TextView = itemView.findViewById(R.id.lblCreatedOn)
        private val imgItem: ImageView = itemView.findViewById(R.id.imgItem)
        private val lblLevelStatus: TextView = itemView.findViewById(R.id.lblLevelStatus)
        private val lblAdd: TextView = itemView.findViewById(R.id.lblAdd)
        private val lblSubmitted: TextView = itemView.findViewById(R.id.lblSubmitted)

        fun bind(data: GetQuizExamReportData, position: Int) {
            lblTitle.text = data.title
            lblQuizDescription.text = data.description
            subjectvalue.text = data.subject
            lblLevelStatus.text = context.getString(R.string.level) + data.level.toString()
            lblPostedBy.text = context.getString(R.string.posted_by)+" : " + data.sent_by
            lblCreatedOn.text = context.getString(R.string.sent_at) + Constant.convertDateFormatType(data.sent_time)

            val images = listOf(
                R.drawable.quiz1,
                R.drawable.quiz2,
                R.drawable.quiz3
            )

            // pick drawable based on position
            val imageRes = if (position < 3) {
                images[position]
            } else {
                images[position % 3]   // loop 0,1,2
            }


            Glide.with(itemView.context)
                .load(imageRes)
                .placeholder(R.drawable.image_placeholder)
                .into(imgItem)


            lblAdd.setOnClickListener{
                if (data.submitted_count <= 0) {
                    val intent = Intent(context, AddQuestion::class.java)
                    intent.putExtra("quiz_Id", data.id)
                    intent.putExtra("quiz_Title", data.title)
                    intent.putExtra("limitQuestion", data.no_of_questions)
                    intent.putExtra("submittedCount", data.submitted_count)
                    intent.putExtra("subjectID", data.subject_id)
                    context.startActivity(intent)
                }
                else{
                    val studentText = if (data.submitted_count == 1) {
                        context.getString(R.string.student_)
                    } else {
                        context.getString(R.string.students)
                    }

                    val isMessage = context.getString(R.string.this_question_has_already_been_submitted_by) +
                            " ${data.submitted_count} $studentText " +
                            context.getString(R.string.do_you_want_to_update_it)

                    val activity = context as? Activity
                    activity?.let {
                        Constant.showSendConfirmationDialog(
                            it,
                            context.getString(R.string.confirmation),
                            context.getString(R.string.permission_ok),
                            context.getString(R.string.Cancel),
                            "",
                            isMessage
                        ) { confirmed ->
                            if (confirmed) {
                                val intent = Intent(context, AddQuestion::class.java)
                                intent.putExtra("quiz_Id", data.id)
                                intent.putExtra("quiz_Title", data.title)
                                intent.putExtra("limitQuestion", data.no_of_questions)
                                intent.putExtra("submittedCount", data.submitted_count)
                                intent.putExtra("subjectID", data.subject_id)
                                context.startActivity(intent)
                            }
                        }
                    }
                }

            }
            lblSubmitted.setOnClickListener{
                val intent1 = Intent(context, SubmitReport::class.java)
                intent1.putExtra("quiz_Id", data.id)
                context.startActivity(intent1)
            }

        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}