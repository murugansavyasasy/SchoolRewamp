package com.vs.schoolmessenger.School.MessageFromManagement.Adapter

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.MessageFromManagement.MessageFromManagement
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
import com.vs.schoolmessenger.School.MessageFromManagement.MsgStaffListener
import com.vs.schoolmessenger.School.QuizExam.QuizExamReport.AddQuestion
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class MessageFromStaffAdapter(
    private var itemList: List<GetMessagesStaffData>?,
    private val listener: MsgStaffListener,
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
            var name=data.sent_by!!
            lblLogo.text = Constant.getNameInitials(name)
            lblName.text = name
            lblRole.text = "Teacher"
            lblTimeDate.text = Constant.isFormatDate(data.date.toString())+" "+data.time

            lblDescription.apply {
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 1
            }

            if (data.is_unread){
                lblView.apply {
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.eye_closed, 0, 0, 0)
                    compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.PrimaryColor)
                }
            }
            else{
                lblView.apply {
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.eye_icon, 0, 0, 0)
                    compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.PrimaryColor)
                }
            }

            when (data.type) {
                Constant.TEXT -> {
                    lblDescription.visibility=View.VISIBLE
                    lblType.apply {
                        text = context.getString(R.string.text)
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.text_msg_icon, 0, 0, 0)
                        compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.PrimaryColor)
                        lblType.backgroundTintList = ContextCompat.getColorStateList(context, R.color.light_bg_blue)
                        setTextColor(ContextCompat.getColor(context, R.color.PrimaryColor))
                    }
                    lblDescription.text=data.content
                }

                Constant.VOICE -> {

                    if (data.is_emergency){
                        lblType.apply {
                            text = context.getString(R.string.voice)
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.voice_icon_2, 0, 0, 0)
                            compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.white)
                            lblType.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red)
                            setTextColor(ContextCompat.getColor(context, R.color.white))

                        }
                    }
                    else{
                        lblType.apply {
                            text = context.getString(R.string.voice)
                            setCompoundDrawablesWithIntrinsicBounds(R.drawable.voice_icon_2, 0, 0, 0)
                            compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.dark_bg_orange_2)
                            lblType.backgroundTintList = ContextCompat.getColorStateList(context, R.color.pale_light_yellow)
                            setTextColor(ContextCompat.getColor(context, R.color.dark_bg_voilet))
                        }
                    }

                    lblDescription.visibility=View.GONE
                }

                Constant.ATTACHMENT_ -> {
                    lblDescription.visibility=View.VISIBLE

                    lblType.apply {
                        text = context.getString(R.string.attachment)
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.attachment_icon_2, 0, 0, 0)
                        compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.dark_bg_orange_4)
                        lblType.backgroundTintList = ContextCompat.getColorStateList(context, R.color.pale_light_orange)
                        setTextColor(ContextCompat.getColor(context, R.color.pale_light_brown))
                    }
                    lblDescription.text=data.description
                }
            }



            rlaHeader.setOnClickListener{
                listener.onStaffClick(data)
            }
            lblView.setOnClickListener{
                listener.onStaffClick(data)
            }
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}