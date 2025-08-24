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
        return if (isLoading) {
            TYPE_SHIMMER
        } else if (itemList!![position].isHeader) {
            TYPE_HEADER
        } else {
            TYPE_DATA
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
        if (itemList.isNullOrEmpty() || position >= itemList!!.size) return

        val item = itemList!![position]

        when (holder) {
            is DataViewHolder -> holder.bind(item, position)
            is HeaderViewHolder -> holder.bind(item)
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        val lblSendBy: TextView = itemView.findViewById(R.id.lblSendBy)
        val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        val lblContent: TextView = itemView.findViewById(R.id.lblContent)
        val first_letter: TextView = itemView.findViewById(R.id.first_letter)
        val lblNotification: TextView = itemView.findViewById(R.id.lblNotification)

        fun bind(data: NotificationDataClass, position: Int) {
            lblSendBy.text = "Posted by : ${data.sendBy}"
            lblTitle.text = data.title
            lblContent.text = data.content.replace("•", "")
            first_letter.visibility = View.GONE
            first_letter.text = data.sendBy.firstOrNull()?.toString() ?: "?"
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