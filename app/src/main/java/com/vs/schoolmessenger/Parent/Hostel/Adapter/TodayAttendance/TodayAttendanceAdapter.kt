package com.vs.schoolmessenger.Parent.Hostel.Adapter.TodayAttendance

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class TodayAttendanceAdapter(
    private var itemList: List<String>?,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<String> = itemList ?: listOf()

    override fun getItemViewType(position: Int) =
        if (isLoading) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.item_attendance)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_attendance, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount() = if (isLoading) 3 else fullList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(fullList[position])
        }
    }

    fun updateData(newList: List<String>) {
        fullList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val txtSession = itemView.findViewById<TextView>(R.id.txtSession)
        private val txtStatus = itemView.findViewById<TextView>(R.id.txtStatus)
        private val imgStatus = itemView.findViewById<ImageView>(R.id.imgStatus)

        fun bind(data: String) {

            val parts = data.split(":")
            val session = parts.getOrNull(0)?.trim() ?: ""
            val status = parts.getOrNull(1)?.trim() ?: ""

            txtSession.text = session

            when {
                status.equals("Present", true) -> {
                    txtStatus.text = "Present"
                    txtStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
                    txtStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_status_present)

                    imgStatus.setImageResource(R.drawable.present_icon_3)
                }

                status.equals("Absent", true) -> {
                    txtStatus.text = "Absent"
                    txtStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                    txtStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_status_absent)

                    imgStatus.setImageResource(R.drawable.absent_icon_3)
                }

                status.equals("Not taken", true) || status.equals("Not Marked", true) -> {
                    txtStatus.text = "Not Marked"
                    txtStatus.setTextColor(ContextCompat.getColor(context, R.color.light_gray))
                    txtStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_status_not_marked)

                    imgStatus.setImageResource(R.drawable.not_taken_icon_2)
                }

                else -> {
                    txtStatus.text = status
                    imgStatus.setImageResource(R.drawable.questionmark_circle)
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}