package com.vs.schoolmessenger.School.Hostel.Adapter.AttendanceHistory



import android.annotation.SuppressLint
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getHAStudentData

import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttendanceStudentHistoryWise(
    private var itemList: List<getHAStudentData>,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<getHAStudentData> = itemList ?: listOf()
    private var filteredList: List<getHAStudentData> = fullList


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.hostel_student_attendance_history_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.hostel_student_attendance_history_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(filteredList[position], context)

        }
    }

    fun updateData(newList: List<getHAStudentData>) {
        fullList = newList
        filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }


    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblInitialName: TextView = itemView.findViewById(R.id.lblInitialName)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val lblFullName: TextView = itemView.findViewById(R.id.lblFullName)
        private val lblPrimarymobile: TextView = itemView.findViewById(R.id.lblPrimarymobile)
        private val lblRoomAndClassDetails: TextView = itemView.findViewById(R.id.lblRoomAndClassDetails)


        private val cardHeader: ConstraintLayout = itemView.findViewById(R.id.cardHeader)


        @SuppressLint("SetTextI18n")
        fun bind(
            data: getHAStudentData,
            context: Context,
        ) {

            lblRoomAndClassDetails.text = "${data.admission_no} • ${context.getString(R.string.class_)} ${data.class_name} - ${data.section_name}"
            lblFullName.text = data.student_name
            lblInitialName.text= Constant.getInitials(data.student_name?:"")

            lblPrimarymobile.text = data.primary_mobile

            if (data.status == Constant.ABSENT) {

                applyTintedBackground(
                    lblInitialName,
                    R.drawable.circle_bg_orange,
                    R.color.red
                )


                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_red_3
                )

                lblStatus.text=data.status
                lblStatus.setTextColor(context.getColor(R.color.red))

                setDrawableColor(cardHeader, R.color.light_red)



            }
            else if (data.status == Constant.PRESENT) {

                applyTintedBackground(
                    lblInitialName,
                    R.drawable.circle_bg_orange,
                    R.color.green
                )
                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_green_3
                )
                lblStatus.text=data.status
                lblStatus.setTextColor(context.getColor(R.color.green))
                setDrawableColor(cardHeader, R.color.white)



            }
            else {
                applyTintedBackground(
                    lblInitialName,
                    R.drawable.circle_bg_orange,
                    R.color.dark_orange_3
                )
                lblStatus.text=context.getString(R.string.not_taken)
                lblStatus.setTextColor(context.getColor(R.color.dark_brown_3))

                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_orange_3
                )
                setDrawableColor(cardHeader, R.color.white)

            }
        }

        fun applyTintedBackground(view: View, drawableRes: Int, colorRes: Int) {
            val context = view.context
            val bgDrawable = ContextCompat.getDrawable(context, drawableRes)
            bgDrawable?.setTint(ContextCompat.getColor(context, colorRes))
            view.background = bgDrawable
        }

        fun setDrawableColor(view: View, colorRes: Int) {
            val drawable = view.background as? android.graphics.drawable.GradientDrawable
            drawable?.setColor(ContextCompat.getColor(view.context, colorRes))
        }

    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}