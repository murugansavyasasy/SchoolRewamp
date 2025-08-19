package com.vs.schoolmessenger.Dashboard.School

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ScrollItem

class AutoScrollAdapterWithDots(
    private var items: List<MenuDetail>,
    private var listener: MenuClickListener,

    ) : RecyclerView.Adapter<AutoScrollAdapterWithDots.ViewHolder>() {

    fun updateList(newItems: List<MenuDetail>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.resent_using_app_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item,listener)
    }

    fun getMiddlePosition(): Int {
        return Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % itemCount)
    }


    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        private val itemText: TextView = itemView.findViewById(R.id.itemText)

        fun bind(item: MenuDetail,listener: MenuClickListener) {
            itemText.text = item.name

            when (item.id) {
                Constant.M_COMMUNICATION -> itemImage.setImageResource(R.drawable.communication_icon_dashboard)
                Constant.M_ASSIGNMENT -> itemImage.setImageResource(R.drawable.assignment_icon_school)
                Constant.M_HOMEWORK -> itemImage.setImageResource(R.drawable.home_work_icon_school)
                Constant.M_ATTENDANCE_MARKING -> itemImage.setImageResource(R.drawable.attendance_marking)
                Constant.M_ABSENTEES_REPORT -> itemImage.setImageResource(R.drawable.absentees_report)
                Constant.M_SCHOOL_STRENGTH -> itemImage.setImageResource(R.drawable.school_strength)
                Constant.M_NOTICEBOARD -> itemImage.setImageResource(R.drawable.noticeboard_icon)
                Constant.M_SCHOOL_CLASS_EVENTS -> itemImage.setImageResource(R.drawable.graduationevent)
                Constant.M_SCHEDULE_EXAM_TEST -> itemImage.setImageResource(R.drawable.schedule_exam_icon)
                Constant.M_MESSAGES_FROM_MANAGEMENT -> itemImage.setImageResource(R.drawable.message_f_management)
                Constant.M_FINANCE -> itemImage.setImageResource(R.drawable.finance_icon)
                Constant.M_ATTACHMENTS -> itemImage.setImageResource(R.drawable.attachement_icon)
                Constant.M_ONLINE_MEETING -> itemImage.setImageResource(R.drawable.online_meeting_icon)
                Constant.M_DAILY_COLLECTION -> itemImage.setImageResource(R.drawable.collect)
                Constant.M_STUDENT_REPORT -> itemImage.setImageResource(R.drawable.student_report)
                Constant.M_LESSON_PLAN -> itemImage.setImageResource(R.drawable.lessonplanimage)
                Constant.M_FEEDBACK -> itemImage.setImageResource(R.drawable.fee_pending_reports)
                Constant.M_VERY_IMPORTANT_INFO -> itemImage.setImageResource(R.drawable.very_important_icon)
                Constant.M_MARK_YOUR_ATTENDANCE -> itemImage.setImageResource(R.drawable.attendanceimage)
                Constant.M_STAFF_WISE_ATTENDANCE_REPORT -> itemImage.setImageResource(R.drawable.staff_attendance_report)
                Constant.M_LEAVE_REQUEST -> itemImage.setImageResource(R.drawable.leave_request_icon_school)
                Constant.M_PTM -> itemImage.setImageResource(R.drawable.ptm_school)
                Constant.M_FEE_PENDING_REPORT -> itemImage.setImageResource(R.drawable.fee_pending_reports)
                Constant.M_SCHOOL_NEEDS -> itemImage.setImageResource(R.drawable.school_needs)
            }

            try {
                val color = ContextCompat.getColor(itemView.context, R.color.light_white_3)
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(color)
                    cornerRadius = 24f
                }
                itemImage.background = drawable
            } catch (_: Exception) { }

            // safe click listener
            itemView.setOnClickListener {
                it.bounceAnimation()
                Constant.isSchoolMenuName=item.name
                Constant.SELECTED_SCHOOL_MENU = item.id
                listener.onClick(item)
            }
        }
    }
}

fun View.bounceAnimation() {
    if (animation != null) return

    animate()
        .scaleX(0.95f)
        .scaleY(0.95f)
        .setDuration(100)
        .withEndAction {
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start()
        }
        .start()
}




//package com.vs.schoolmessenger.Dashboard.School
//
//import android.graphics.Color
//import android.graphics.drawable.GradientDrawable
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.CommonScreens.MenuDetails.FrequentlyUsedMenu
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.ScrollItem
//
//class AutoScrollAdapterWithDots(
//    private var items: List<FrequentlyUsedMenu>,
//    private val onItemClick: (Int) -> Unit   // callback gives only ID
//) : RecyclerView.Adapter<AutoScrollAdapterWithDots.ViewHolder>() {
//
//    fun updateList(newItems: List<FrequentlyUsedMenu>) {
//        items = newItems
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.resent_using_app_item, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = items[position]
//        holder.bind(item)
//    }
//
//    fun getMiddlePosition(): Int {
//        return Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % itemCount)
//    }
//
//
//    override fun getItemCount(): Int = items.size
//
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
//        private val itemText: TextView = itemView.findViewById(R.id.itemText)
//
//        fun bind(item: FrequentlyUsedMenu) {
//            itemText.text = item.name
//
//            when (item.id) {
//                Constant.M_COMMUNICATION -> itemImage.setImageResource(R.drawable.communication_icon_dashboard)
//                Constant.M_ASSIGNMENT -> itemImage.setImageResource(R.drawable.assignment_icon_school)
//                Constant.M_HOMEWORK -> itemImage.setImageResource(R.drawable.home_work_icon_school)
//                Constant.M_ATTENDANCE_MARKING -> itemImage.setImageResource(R.drawable.attendance_marking)
//                Constant.M_ABSENTEES_REPORT -> itemImage.setImageResource(R.drawable.absentees_report)
//                Constant.M_SCHOOL_STRENGTH -> itemImage.setImageResource(R.drawable.school_strength)
//                Constant.M_NOTICEBOARD -> itemImage.setImageResource(R.drawable.noticeboard_icon)
//                Constant.M_SCHOOL_CLASS_EVENTS -> itemImage.setImageResource(R.drawable.graduationevent)
//                Constant.M_SCHEDULE_EXAM_TEST -> itemImage.setImageResource(R.drawable.schedule_exam_icon)
//                Constant.M_MESSAGES_FROM_MANAGEMENT -> itemImage.setImageResource(R.drawable.message_f_management)
//                Constant.M_FINANCE -> itemImage.setImageResource(R.drawable.finance_icon)
//                Constant.M_ATTACHMENTS -> itemImage.setImageResource(R.drawable.attachement_icon)
//                Constant.M_ONLINE_MEETING -> itemImage.setImageResource(R.drawable.online_meeting_icon)
//                Constant.M_DAILY_COLLECTION -> itemImage.setImageResource(R.drawable.collect)
//                Constant.M_STUDENT_REPORT -> itemImage.setImageResource(R.drawable.student_report)
//                Constant.M_LESSON_PLAN -> itemImage.setImageResource(R.drawable.lessonplanimage)
//                Constant.M_FEEDBACK -> itemImage.setImageResource(R.drawable.fee_pending_reports)
//                Constant.M_VERY_IMPORTANT_INFO -> itemImage.setImageResource(R.drawable.very_important_icon)
//                Constant.M_MARK_YOUR_ATTENDANCE -> itemImage.setImageResource(R.drawable.attendanceimage)
//                Constant.M_STAFF_WISE_ATTENDANCE_REPORT -> itemImage.setImageResource(R.drawable.staff_attendance_report)
//                Constant.M_LEAVE_REQUEST -> itemImage.setImageResource(R.drawable.leave_request_icon_school)
//                Constant.M_PTM -> itemImage.setImageResource(R.drawable.ptm_school)
//                Constant.M_FEE_PENDING_REPORT -> itemImage.setImageResource(R.drawable.fee_pending_reports)
//                Constant.M_SCHOOL_NEEDS -> itemImage.setImageResource(R.drawable.school_needs)
//            }
//
//            try {
//                val color = ContextCompat.getColor(itemView.context, R.color.light_white_3)
//                val drawable = GradientDrawable().apply {
//                    shape = GradientDrawable.RECTANGLE
//                    setColor(color)
//                    cornerRadius = 24f
//                }
//                itemImage.background = drawable
//            } catch (_: Exception) { }
//
//            // safe click listener
//            itemView.setOnClickListener {
//                it.bounceAnimation()
//                val pos = bindingAdapterPosition
//                if (pos != RecyclerView.NO_POSITION) {
//                    onItemClick(items[pos].id) // send only ID
//                }
//            }
//        }
//    }
//}
//
//fun View.bounceAnimation() {
//    if (animation != null) return
//
//    animate()
//        .scaleX(0.95f)
//        .scaleY(0.95f)
//        .setDuration(100)
//        .withEndAction {
//            animate()
//                .scaleX(1f)
//                .scaleY(1f)
//                .setDuration(100)
//                .start()
//        }
//        .start()
//}
