package com.vs.schoolmessenger.School.QuizExam.Adapter.SubmitReport


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        private val lblName: TextView = itemView.findViewById(R.id.tvName)
        private val tvStatus1: TextView = itemView.findViewById(R.id.tvStatus1)
        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        private val tvStatus: LinearLayout = itemView.findViewById(R.id.tvStatus)
        private val lblStandardSection: TextView = itemView.findViewById(R.id.tvClass)
        private val tvSubmittedOn: TextView = itemView.findViewById(R.id.tvSubmittedOn)



        fun bind(data: GetQuizSubmissionListData, position: Int) {
            lblName.text = data.student_name
            lblStandardSection.text = "${context.getString(R.string.Class_)}: ${data.standard}-${data.section}"
            if (data.is_submit){
//                tvSubmittedOn.visibility=View.VISIBLE
                tvSubmittedOn.text =Constant.convertDateFormatType2(data.submitted_on)
                tvStatus1.text =context.getString(R.string.submitted)
                tvStatus.background.setTint(ContextCompat.getColor(context, R.color.green))
                tvStatus1.setTextColor(ContextCompat.getColor(context,R.color.white))

            }
            else{
//                tvSubmittedOn.visibility=View.GONE
                tvStatus1.text =context.getString(R.string.pending)
                tvStatus.background.setTint(ContextCompat.getColor(context,R.color.orange))
                tvStatus1.setTextColor(ContextCompat.getColor(context,R.color.white))
            }

            if (data.gender.equals("male", ignoreCase = true)) {
                Glide.with(imgAvatar.context)
                    .load(R.drawable.avatar)   // your male drawable
                    .placeholder(R.drawable.person_circle)
                    .into(imgAvatar)
            } else if (data.gender.equals("female", ignoreCase = true)) {
                Glide.with(imgAvatar.context)
                    .load(R.drawable.girl_avatar) // your female drawable
                    .placeholder(R.drawable.person_circle)
                    .into(imgAvatar)
            } else {
                Glide.with(imgAvatar.context)
                    .load(R.drawable.girl_avatar) // default drawable
                    .placeholder(R.drawable.person_circle)
                    .into(imgAvatar)
            }





        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}