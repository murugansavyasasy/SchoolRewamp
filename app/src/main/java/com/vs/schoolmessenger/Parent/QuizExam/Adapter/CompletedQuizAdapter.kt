package com.vs.schoolmessenger.Parent.QuizExam.Adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.QuizExam.Model.QuizExamList.GetQuizExamListData
import com.vs.schoolmessenger.Parent.QuizExam.SubmittedQuizPreview
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class CompletedQuizAdapter(
    private var itemList: List<GetQuizExamListData>?,
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
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.reciver_quiz_upcoming_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.reciver_quiz_upcoming_item, parent, false)
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

    fun updateData(newList: List<GetQuizExamListData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblQuizDescription: TextView = itemView.findViewById(R.id.lblQuizDescription)
        private val lblSubject: TextView = itemView.findViewById(R.id.lblSubject)
        private val lblMaxMarks: TextView = itemView.findViewById(R.id.lblMaxMarks)
        private val lblPostedby: TextView = itemView.findViewById(R.id.lblPostedby)
        private val lblCreatedOn: TextView = itemView.findViewById(R.id.lblCreatedOn)
        private val lnrEntireQuiz: LinearLayout = itemView.findViewById(R.id.lnrEntireQuiz)
        private val lblLevel: TextView = itemView.findViewById(R.id.lblLevel)
        private val lblQuestion: TextView = itemView.findViewById(R.id.lblQuestion)
        private val lblPlayNow: TextView = itemView.findViewById(R.id.lblPlayNow)
        private val lblnext: ImageView = itemView.findViewById(R.id.lblnext)

        fun bind(data: GetQuizExamListData, position: Int) {
            lblTitle.text = data.title
            lblQuizDescription.text = data.description
            lblSubject.text = data.subject
            lblMaxMarks.text = data.max_mark.toString()
            lblLevel.text = data.level.toString()
            lblQuestion.text = data.no_of_questions.toString()
            lblPostedby.text = "${context.getString(R.string.posted_by)}: ${data.sent_by}"
            lblCreatedOn.text =
                "${context.getString(R.string.created_on)} ${Constant.convertDateFormatType(data.created_on)}"

            lblPlayNow.visibility = View.GONE
            lblnext.visibility = View.VISIBLE
            (lblPostedby.layoutParams as RelativeLayout.LayoutParams).apply {
                addRule(RelativeLayout.START_OF, R.id.lblnext)
            }

            // Open QuizExam on click
            //Staff/Principal no need to send the access token only student_id is enough
            //Parent no need to send the student_id only  access token is enough
            val openExam = View.OnClickListener {
                val intent = Intent(context, SubmittedQuizPreview::class.java)
                intent.putExtra(Constant.isRSSubmittedQuizId, data.quiz_id)
                intent.putExtra(Constant.isRSSubmittedSubject, data.subject)
                intent.putExtra(Constant.isRSSubmittedSubmittedOn, data.submitted_on)
                intent.putExtra(Constant.isQuizScreenRole, false)

                context.startActivity(intent)
            }
            lnrEntireQuiz.setOnClickListener(openExam)
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}
