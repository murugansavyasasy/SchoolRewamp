
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
import com.vs.schoolmessenger.Parent.QuizExam.AttendQuiz
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class QuizUpcomingAdapter(
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

        fun bind(data: GetQuizExamListData, position: Int) {
            lblTitle.text = data.title
            lblQuizDescription.text = data.description
            subjectvalue.text = data.subject
            lblAttendanceStatus.text = "Level " + data.level.toString()
            lblPostedBy.text = "Posted By: " + data.SentBy
            lblCreatedOn.text = "Created On " + Constant.convertDateFormatType(data.created_on)

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
                val intent = Intent(context, AttendQuiz::class.java)
                intent.putExtra("isRSQuizId", data.quiz_id)
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

//package com.vs.schoolmessenger.Parent.QuizExam
//
//import android.content.Context
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.facebook.shimmer.ShimmerFrameLayout
//import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateListener
//import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestAdapter
//import com.vs.schoolmessenger.R
//
//class QuizUpcomingAdapter (
//    private var itemList: List<QuizUpcomingData>?,
//    private var listener: QuizUpcomingListener,
//    private var context: Context,
//    private var isLoading: Boolean
//
//) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//    private var selectedPosition = RecyclerView.NO_POSITION
//
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.shimmer_view_small_list, parent, false)
//            DataViewHolder.ShimmerViewHolder(view)
//        } else {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.quiz_upcominglist, parent, false)
//            DataViewHolder(view, context) // Pass context to DataViewHolder
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is DataViewHolder) {
//            // Bind actual data when loading is complete
//            holder.bind(itemList!![position], position, listener, this) // Pass adapter reference
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 20 // Show shimmer items while loading
//        else itemList?.size ?: 0
//    }
//
//
//
//
//    class DataViewHolder(itemView: View, private val context: Context) :
//        RecyclerView.ViewHolder(itemView) {
//        private val quizname: TextView = itemView.findViewById(R.id.quizname)
//        private val quizdescription: TextView = itemView.findViewById(R.id.quizdescription)
//        private val subjectvalue: TextView = itemView.findViewById(R.id.subjectvalue)
//        private val qustionsvalue: TextView = itemView.findViewById(R.id.qustionsvalue)
//        private val playnow: TextView = itemView.findViewById(R.id.playnow)
//        private val playnow2: ImageView = itemView.findViewById(R.id.playnow2)
//
//
//
//        fun bind(
//            data: QuizUpcomingData,
//            position: Int,
//            listener: QuizUpcomingListener,
//            adapter: QuizUpcomingAdapter
//        ) {
//            quizname.text = data.quizname
//            quizdescription.text = data.quizdescription
//            subjectvalue.text = data.subjectvalue
//            qustionsvalue.text = data.qustionsvalue
//
//
//            playnow.setOnClickListener {
//                val intent = Intent(context, QuizExam::class.java)
//                context.startActivity(intent)
//            }
//            playnow2.setOnClickListener {
//                val intent = Intent(context, QuizExam::class.java)
//                context.startActivity(intent)
//            }
//        }
//
//        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            private val shimmerLayout: ShimmerFrameLayout =
//                itemView.findViewById(R.id.shimmer_view_container)
//            init {
//                shimmerLayout.startShimmer() // Start shimmer effect
//            }
//        }
//    }
//}
