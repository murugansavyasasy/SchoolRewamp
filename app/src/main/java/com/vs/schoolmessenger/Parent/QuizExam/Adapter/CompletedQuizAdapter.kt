package com.vs.schoolmessenger.Parent.QuizExam.Adapter
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.quiz_upcominglist)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.quiz_upcominglist, parent, false)
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
        private val rlaAttendance: RelativeLayout = itemView.findViewById(R.id.rlaAttendance)
        private val lblAttendanceStatus: TextView = itemView.findViewById(R.id.lblAttendanceStatus)
        private val playnow2: ImageView = itemView.findViewById(R.id.playnow2)
        private val next: ImageView = itemView.findViewById(R.id.next)
        private val lblSubmitttedOn: TextView = itemView.findViewById(R.id.lblSubmitttedOn)

        fun bind(data: GetQuizExamListData, position: Int) {
            playnow2.visibility=View.GONE
            next.visibility=View.VISIBLE
            lblSubmitttedOn.visibility=View.VISIBLE
            lblTitle.text = data.title
            lblQuizDescription.text = data.description
            subjectvalue.text = data.subject
            lblAttendanceStatus.text = "Level " + data.level.toString()
            lblPostedBy.text = "Posted By: " + data.SentBy
            lblCreatedOn.text = "Created On " + Constant.convertDateFormatType(data.created_on)
            lblSubmitttedOn.text = "Submitted On " + Constant.convertDateFormatType(data.submitted_on)

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

            // Open QuizExam on click
            val openExam = View.OnClickListener {
                val intent = Intent(context, SubmittedQuizPreview::class.java)
                intent.putExtra("isRSSubmittedQuizId", data.quiz_id)
                context.startActivity(intent)
            }
            rlaAttendance.setOnClickListener(openExam)
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}
