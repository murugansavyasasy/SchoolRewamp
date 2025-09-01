package com.vs.schoolmessenger.School.MessageFromManagement.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaff
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData
import com.vs.schoolmessenger.School.QuizExam.QuizExamReport.AddQuestion
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class MessageFromStaffAdapter(
    private var itemList: List<GetMessagesStaffData>?,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.message_from_staff)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_from_staff, parent, false)
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
        private val lblLogo: TextView = itemView.findViewById(R.id.lblLogo)
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblRole: TextView = itemView.findViewById(R.id.lblRole)
        private val lblTimeDate: TextView = itemView.findViewById(R.id.lblTimeDate)
        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
        private val lblView: TextView = itemView.findViewById(R.id.lblView)
        private val lblType: TextView = itemView.findViewById(R.id.lblType)
        private val rlaHeader: RelativeLayout = itemView.findViewById(R.id.rlaHeader)


        fun bind(data: GetMessagesStaffData, position: Int) {
            lblTitle.text = data.title
            var name="Dr M Santhosh Kumar"
            lblLogo.text = Constant.getNameInitials(name)
            lblName.text = name
            lblRole.text = "Teacher"
            lblTimeDate.text = Constant.isFormatDate(data.date.toString())+" "+data.time

            when (data.type) {
                Constant.TEXT -> {
                    lblType.apply {
                        text = context.getString(R.string.text)
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.text_msg_icon, 0, 0, 0)
                        compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.PrimaryColor)
                    }
                }

                Constant.VOICE -> {
                    lblType.apply {
                        text = context.getString(R.string.voice)
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.voice_icon_2, 0, 0, 0)
                        compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.green)
                    }
                }

                Constant.ATTACHMENT_ -> {
                    lblType.apply {
                        text = context.getString(R.string.attachment)
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.attachment_icon_2, 0, 0, 0)
                        compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.red)
                    }
                }
            }



            rlaHeader.setOnClickListener{
                    val intent = Intent(context, AddQuestion::class.java)
                    intent.putExtra("quiz_Id", data.id)
                    intent.putExtra("quiz_Title", data.title)
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