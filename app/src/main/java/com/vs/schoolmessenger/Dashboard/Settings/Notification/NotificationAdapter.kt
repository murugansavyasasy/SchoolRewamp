package com.vs.schoolmessenger.Dashboard.Settings.Notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.NoticeBoard.NoticeClickListener
import com.vs.schoolmessenger.School.NoticeBoard.NoticeData
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter.DataViewHolder
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.Utils.ImageSliderAdapter
import de.hdodenhof.circleimageview.CircleImageView
import me.relex.circleindicator.CircleIndicator

class NotificationAdapter(
    private var itemList: List<NotificationDataClass>?,
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
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.resent_notifications, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        val imgRoundCard: CircleImageView = itemView.findViewById(R.id.imgRoundCard)
        val lblSendBy: TextView = itemView.findViewById(R.id.lblSendBy)
        val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        val lblContent: TextView = itemView.findViewById(R.id.lblContent)
        val lblView: TextView = itemView.findViewById(R.id.lblView)
        val lblNotification: TextView = itemView.findViewById(R.id.lblNotification)

        fun bind(data: NotificationDataClass, position: Int) {

            lblSendBy.text = data.sendBy
            lblTitle.text = data.title
            lblContent.text = data.content

            when (position) {
                1 -> {
                    lblNotification.visibility = View.VISIBLE
                    imgRoundCard.setImageResource(R.drawable.voice)
                }

                2 -> {
                    lblNotification.visibility = View.VISIBLE
                    imgRoundCard.setImageResource(R.drawable.phone_icon)
                }
                3 -> {
                    lblNotification.visibility = View.GONE
                    imgRoundCard.setImageResource(R.drawable.mail_icon)
                }

                4 -> {
                    lblNotification.visibility = View.VISIBLE
                    imgRoundCard.setImageResource(R.drawable.text_notification)

                }

                5 -> {
                    lblNotification.visibility = View.GONE
                    imgRoundCard.setImageResource(R.drawable.voice)
                }

                6 -> {
                    lblNotification.visibility = View.GONE
                    imgRoundCard.setImageResource(R.drawable.phone_icon)
                }

                7 -> {
                    lblNotification.visibility = View.VISIBLE
                    imgRoundCard.setImageResource(R.drawable.mail_icon)
                }

                8 -> {
                    lblNotification.visibility = View.GONE
                    imgRoundCard.setImageResource(R.drawable.text_notification)
                }
                9 -> {
                    lblNotification.visibility = View.VISIBLE
                    imgRoundCard.setImageResource(R.drawable.voice)
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)
        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}