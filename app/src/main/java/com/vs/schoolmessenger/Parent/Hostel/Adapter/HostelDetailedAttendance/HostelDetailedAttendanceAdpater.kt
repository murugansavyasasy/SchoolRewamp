package com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelDetailedAttendance

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Hostel.AttendanceUIConfig
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.DetailedAttendanceRecords.DayAttendance
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class HostelDetailedAttendanceAdpater(
    private var itemList: List<DayAttendance>?,
    private var context: Context,
    private var isLoading: Boolean,
    private var headerScroll: HorizontalScrollView
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private val scrollViews = ArrayList<HorizontalScrollView>()
    private var isSyncing = false

    private val columnWidth = AttendanceUIConfig.columnWidth(context)
    private val imageSize = AttendanceUIConfig.imageSize(context)
    private val paddingH = AttendanceUIConfig.paddingH(context)
    private val paddingV = AttendanceUIConfig.paddingV(context)

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.detailed_attendance_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.detailed_attendance_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    fun syncScroll(scrollX: Int) {
        if (isSyncing) return
        isSyncing = true

        headerScroll.scrollTo(scrollX, 0)
        scrollViews.forEach { it.scrollTo(scrollX, 0) }

        isSyncing = false
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txtDay: TextView = itemView.findViewById(R.id.txtDay)
        private val container: LinearLayout = itemView.findViewById(R.id.containerSessions)
        private val rowScroll: HorizontalScrollView = itemView.findViewById(R.id.rowScroll)

        fun bind(data: DayAttendance) {

            txtDay.text = data.date_label
            container.removeAllViews()

            data.status.forEach { status ->

                val parent = LinearLayout(context)
                parent.layoutParams = LinearLayout.LayoutParams(
                    columnWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                parent.gravity = Gravity.CENTER
                parent.setPadding(paddingH, paddingV, paddingH, paddingV)

                val img = ImageView(context)
                img.layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)

                when (status) {
                    "Present" -> img.setImageResource(R.drawable.present_icon_3)
                    "Absent" -> img.setImageResource(R.drawable.absent_icon_3)
                    "Not Taken" -> img.setImageResource(R.drawable.not_taken_icon_2)
                }

                parent.addView(img)
                container.addView(parent)
            }

            if (!scrollViews.contains(rowScroll)) {
                scrollViews.add(rowScroll)
            }

            rowScroll.setOnScrollChangeListener { _, scrollX, _, _, _ ->

                if (isSyncing) return@setOnScrollChangeListener
                isSyncing = true

                headerScroll.scrollTo(scrollX, 0)

                scrollViews.forEach {
                    if (it != rowScroll) it.scrollTo(scrollX, 0)
                }

                isSyncing = false
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}