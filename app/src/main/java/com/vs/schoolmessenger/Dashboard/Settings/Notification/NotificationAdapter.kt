package com.vs.schoolmessenger.Dashboard.Settings.Notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class NotificationAdapter(
    private var itemList: List<NotificationDataClass>?,
    private val listener: NotificationClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private val TYPE_HEADER = 2

    override fun getItemViewType(position: Int): Int {
        return when {
            isLoading -> TYPE_SHIMMER
            itemList.isNullOrEmpty() -> TYPE_DATA
            itemList!![position].isHeader == true -> TYPE_HEADER
            else -> TYPE_DATA
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SHIMMER -> {
                val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.resent_notifications)
                ShimmerViewHolder(shimmerView)
            }

            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_header, parent, false)
                HeaderViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.resent_notifications, parent, false)
                DataViewHolder(view, context)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isLoading) return
        val list = itemList ?: return
        val item = list[position]

        when (holder) {
            is DataViewHolder -> {
                val lastIndex = list.lastIndex
                val nextIsHeader = position < lastIndex && list[position + 1].isHeader
                val showDivider = position < lastIndex && !nextIsHeader
                holder.bind(item, showDivider, listener)
            }

            is HeaderViewHolder -> holder.bind(item)
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblSendBy: TextView = itemView.findViewById(R.id.lblSendBy)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblContent: TextView = itemView.findViewById(R.id.lblContent)
        private val first_letter: TextView = itemView.findViewById(R.id.first_letter)
        private val lblNotification: TextView = itemView.findViewById(R.id.lblNotification)
        private val line: View = itemView.findViewById(R.id.line)
        private val notification_date: TextView = itemView.findViewById(R.id.notification_date)

        private val fab: RelativeLayout = itemView.findViewById(R.id.fab)

        fun bind(
            data: NotificationDataClass,
            showDivider: Boolean,
            listener: NotificationClickListener
        ) {
            lblSendBy.text = data.sendBy
            lblTitle.text = data.title
            lblContent.text = data.content.replace("•", "")
            first_letter.visibility = View.GONE
            notification_date.text = Constant.convertDateAndTimeFormat(data.sent_on)
            first_letter.text = data.sendBy.firstOrNull()?.toString() ?: "?"
            line.visibility = if (showDivider) View.VISIBLE else View.GONE

            fab.setOnClickListener {
                listener.onClickListener(data, it, adapterPosition)
            }

        }
    }


    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtHeader: TextView = itemView.findViewById(R.id.txtHeader)
        private val imgHeader: View = itemView.findViewById(R.id.imgHeader)

        fun bind(data: NotificationDataClass) {
            txtHeader.text = data.category

            when (data.menu_id) {
                1 -> imgHeader.setBackgroundResource(R.drawable.attendance_report_icon)
                2 -> imgHeader.setBackgroundResource(R.drawable.assignment_icon_school)
                3 -> imgHeader.setBackgroundResource(R.drawable.attendance_marking)
                7 -> imgHeader.setBackgroundResource(R.drawable.communication_icon_dashboard)
                8 -> imgHeader.setBackgroundResource(R.drawable.collect)
                9 -> imgHeader.setBackgroundResource(R.drawable.graduationevent)
                14 -> imgHeader.setBackgroundResource(R.drawable.fee_pending_reports)
                15 -> imgHeader.setBackgroundResource(R.drawable.home_work_icon_school)
                18 -> imgHeader.setBackgroundResource(R.drawable.leave_request_icon_school)
                19 -> imgHeader.setBackgroundResource(R.drawable.lessonplanimage)
                20 -> imgHeader.setBackgroundResource(R.drawable.lsrw_icon)
                21 -> imgHeader.setBackgroundResource(R.drawable.attendanceimage)
                22 -> imgHeader.setBackgroundResource(R.drawable.message_f_management)
                23 -> imgHeader.setBackgroundResource(R.drawable.noticeboard_icon)
                26 -> imgHeader.setBackgroundResource(R.drawable.ptm_icon)
                27 -> imgHeader.setBackgroundResource(R.drawable.quiz_icon)
                29 -> imgHeader.setBackgroundResource(R.drawable.graduationevent)
                30 -> imgHeader.setBackgroundResource(R.drawable.schedule_exam_icon)
                31 -> imgHeader.setBackgroundResource(R.drawable.school_strength)
                33 -> imgHeader.setBackgroundResource(R.drawable.staff_attendance_report)
                35 -> imgHeader.setBackgroundResource(R.drawable.student_report)
                39 -> imgHeader.setBackgroundResource(R.drawable.attachement_icon)
            }
        }
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            if (itemView is ShimmerFrameLayout) {
                itemView.startShimmer()
            }
        }
    }
}