package com.vs.schoolmessenger.School.Event

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.R
import me.relex.circleindicator.CircleIndicator

class EventHistoryAdapter(
    private var itemList: List<EventHistoryData>?,
    private var listener: EventClickListener,
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
                LayoutInflater.from(parent.context).inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        }
        else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.noticeboard_list_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position],position,listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblContent: TextView = itemView.findViewById(R.id.lblContent)
        private val rytHeader: RelativeLayout = itemView.findViewById(R.id.rytHeader)
        private val viewpager: ViewPager = itemView.findViewById(R.id.viewpager)
        private val indicator: CircleIndicator = itemView.findViewById(R.id.indicator)
        private val rytPin: FrameLayout = itemView.findViewById(R.id.rytPin)

        fun bind(data: EventHistoryData, position: Int, listener: EventClickListener) {

            lblTitle.text = data.title
            lblContent.text = data.content
            lblDate.text = data.date
            rytPin.visibility=View.GONE

            val imageUrls = listOf(
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391795845263.png",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391795387749.png",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391797604035.png",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391799793266.png",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391801142838.png",
            )

            // Set up the adapter
            val viewPagerAdapter   = ImageSliderAdapter(context,imageUrls)
            viewpager.adapter = viewPagerAdapter
            indicator.setViewPager(viewpager)

            rytHeader.setOnClickListener {
                listener.onItemClick(data)
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