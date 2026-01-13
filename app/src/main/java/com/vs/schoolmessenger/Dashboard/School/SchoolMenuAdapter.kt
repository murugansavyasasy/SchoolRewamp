package com.vs.schoolmessenger.Dashboard.School

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuCountDetail
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SchoolMenuAdapter(
    private var context: Context,
    private var listener: MenuClickListener,
    private var itemList: List<MenuDetail>?,
    private var itemCountList: ArrayList<MenuCountDetail>?,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SHIMMER -> {
                val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.dashboard_app_item)
                ShimmerViewHolder(shimmerView)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.dashboard_app_item, parent, false)
                DataViewHolder(view, context)
            }
        }
    }

    fun updateList(newList: List<MenuDetail>) {
        itemList = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DataViewHolder -> {
                itemList?.getOrNull(position)?.let { menuDetail ->
                    holder.bind(menuDetail, position, listener, itemCountList)
                }
            }

            is ShimmerViewHolder -> holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val imgMenu: ImageView = itemView.findViewById(R.id.itemIcon)
        private val itemDescription: TextView = itemView.findViewById(R.id.itemDescription)
        private val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
        private val rlaMenu: CardView = itemView.findViewById(R.id.header)
        private val imgReadCount: View = itemView.findViewById(R.id.imgReadCount)

        fun bind(
            data: MenuDetail,
            position: Int,
            listener: MenuClickListener,
            itemCountList: ArrayList<MenuCountDetail>?
        ) {
            itemTitle.text = data.name
            itemDescription.text = data.description

            if (itemCountList != null && itemCountList.size > position && itemCountList[position].unread_count != 0) {
                imgReadCount.visibility = View.VISIBLE
            } else {
                imgReadCount.visibility = View.GONE
            }

            when (data.id) {
                Constant.M_COMMUNICATION -> imgMenu.setImageResource(R.drawable.communication_icon_dashboard)
                Constant.M_ASSIGNMENT -> imgMenu.setImageResource(R.drawable.assignment_icon_school)
                Constant.M_HOMEWORK -> imgMenu.setImageResource(R.drawable.home_work_icon_school)
                Constant.M_ATTENDANCE_MARKING -> imgMenu.setImageResource(R.drawable.attendance_marking)
                Constant.M_INTERACTION_WITH_STUDENT -> imgMenu.setImageResource(R.drawable.interaction_with_student_dashboard_icon)
                Constant.M_ABSENTEES_REPORT -> imgMenu.setImageResource(R.drawable.absentees_report)
                Constant.M_SCHOOL_STRENGTH -> imgMenu.setImageResource(R.drawable.school_strength)
                Constant.M_QUIZ_EXAM -> imgMenu.setImageResource(R.drawable.quiz_icon)
                Constant.M_NOTICEBOARD -> imgMenu.setImageResource(R.drawable.noticeboard_icon)
                Constant.M_SCHOOL_CLASS_EVENTS -> imgMenu.setImageResource(R.drawable.graduationevent)
                Constant.M_SCHEDULE_EXAM_TEST -> imgMenu.setImageResource(R.drawable.schedule_exam_icon)
                Constant.M_MESSAGES_FROM_MANAGEMENT -> imgMenu.setImageResource(R.drawable.message_f_management)
                Constant.M_FINANCE -> imgMenu.setImageResource(R.drawable.finance_icon)
                Constant.M_ATTACHMENTS -> imgMenu.setImageResource(R.drawable.attachement_icon)
                Constant.M_ONLINE_MEETING -> imgMenu.setImageResource(R.drawable.online_meeting_icon)
                Constant.M_DAILY_COLLECTION -> imgMenu.setImageResource(R.drawable.collect)
                Constant.M_LSRW -> imgMenu.setImageResource(R.drawable.lsrw_icon)
                Constant.M_STUDENT_REPORT -> imgMenu.setImageResource(R.drawable.student_report)
                Constant.M_LESSON_PLAN -> imgMenu.setImageResource(R.drawable.lessonplanimage)
                Constant.M_FEEDBACK -> imgMenu.setImageResource(R.drawable.fee_pending_reports)
                Constant.M_VERY_IMPORTANT_INFO -> imgMenu.setImageResource(R.drawable.very_important_icon)
                Constant.M_MARK_YOUR_ATTENDANCE -> imgMenu.setImageResource(R.drawable.attendanceimage)
                Constant.M_STAFF_WISE_ATTENDANCE_REPORT -> imgMenu.setImageResource(R.drawable.staff_attendance_report)
                Constant.M_LEAVE_REQUEST -> imgMenu.setImageResource(R.drawable.leave_request_icon_school)
                Constant.M_PTM -> imgMenu.setImageResource(R.drawable.ptm_school)
                Constant.M_FEE_PENDING_REPORT -> imgMenu.setImageResource(R.drawable.fee_pending_reports)
                Constant.M_SCHOOL_NEEDS -> imgMenu.setImageResource(R.drawable.school_needs)
                Constant.M_ONLINE_TEXT_BOOK -> imgMenu.setImageResource(R.drawable.book)
                Constant.M_UPLOAD_MARKS -> imgMenu.setImageResource(R.drawable.exam_mark_icon)

            }

            rlaMenu.setOnClickListener {
                Constant.selectedFiles.clear()
                Constant.isSelectedMenuName = data.name
                Constant.SELECTED_MENU_ID = data.id
                listener.onClick(data)
                Constant.isSchoolMenuCount = itemCountList?.getOrNull(position)?.unread_count ?: 0
                val CountMenuname = itemCountList?.getOrNull(position)?.name ?: 0
                Log.d(
                    "Menu Count",
                    "${Constant.isSchoolMenuCount},Selected Menu Dashboard: ${data.name},CountMenuName: ${CountMenuname}",
                )
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
