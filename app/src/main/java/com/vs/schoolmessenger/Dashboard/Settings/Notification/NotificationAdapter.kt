package com.vs.schoolmessenger.Dashboard.Settings.Notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(
    private var itemList: List<NotificationDataClass>?,
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
                holder.bind(item, showDivider)
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

        fun bind(data: NotificationDataClass, showDivider: Boolean) {
            lblSendBy.text = data.sendBy
            lblTitle.text = data.title
            lblContent.text = data.content.replace("•", "")
            first_letter.visibility = View.GONE
            first_letter.text = data.sendBy.firstOrNull()?.toString() ?: "?"
            line.visibility = if (showDivider) View.VISIBLE else View.GONE
        }
    }



    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtHeader: TextView = itemView.findViewById(R.id.txtHeader)
        private val imgHeader: View = itemView.findViewById(R.id.imgHeader)

        fun bind(data: NotificationDataClass) {
            txtHeader.text = data.category

            when (data.category) {
                "Homework" -> imgHeader.setBackgroundResource(R.drawable.home_work_icon_school)
                "Assignment" -> imgHeader.setBackgroundResource(R.drawable.assignment_icon_school)
                "Events" -> imgHeader.setBackgroundResource(R.drawable.graduationevent)
                "Communication" -> imgHeader.setBackgroundResource(R.drawable.communication_icon_dashboard)
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