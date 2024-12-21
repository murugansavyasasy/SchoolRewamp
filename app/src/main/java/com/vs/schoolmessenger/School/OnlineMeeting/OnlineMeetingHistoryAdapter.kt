package com.vs.schoolmessenger.School.OnlineMeeting

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class OnlineMeetingHistoryAdapter(
    private var itemList: List<OnlineMeetingHistoryData>? = emptyList(),
    private var listener: OnlineMeetingClickListener,
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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.onlinemeeting_history, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            itemList?.get(position)?.let { data ->
                holder.bind(data, listener, position) // Pass position to the bind method
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val lblMeetingType: TextView = itemView.findViewById(R.id.lblMeetingType)
        private val lblLink: TextView = itemView.findViewById(R.id.lblLink)
        private val imgReminder: ImageView = itemView.findViewById(R.id.imgReminder)
        private val lblView: TextView = itemView.findViewById(R.id.lblView)
        private val lnrView: LinearLayout = itemView.findViewById(R.id.lnrView)


        fun bind(
            data: OnlineMeetingHistoryData,
            listener: OnlineMeetingClickListener,
            position: Int
        ) {
            lblTitle.text = data.title
            lblDate.text = data.date
            lblTime.text = data.time
            lblMeetingType.text = data.meetingType
            lblLink.text = data.link

            if (position % 2 == 0) {
                lblView.setBackgroundColor(context.resources.getColor(R.color.light_dark_blue))
                lnrView.setBackgroundColor(context.resources.getColor(R.color.light_sky_blue1))
            } else {
                lblView.setBackgroundColor(context.resources.getColor(R.color.orange))
                lnrView.setBackgroundColor(context.resources.getColor(R.color.light_orang1))
            }

            lblLink.setOnClickListener {
                listener.onItemLinkClick(data)
            }
            imgReminder.setOnClickListener {
                listener.onItemReminderClick(data)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}
