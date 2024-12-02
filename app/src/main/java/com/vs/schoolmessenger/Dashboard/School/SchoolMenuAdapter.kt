package com.vs.schoolmessenger.Dashboard.School

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.Parent.Video.ParentVideo
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesStudentMark
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.NoticeBoard.CreateNoticeBoard

class SchoolMenuAdapter(
    private var context: Context, private val itemList: List<GridItem>?,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.shimmer_dashboard_item, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.menu_item_card, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position])
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
        private val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)
        private val rlaMenu: RelativeLayout = itemView.findViewById(R.id.rlaMenu)

        fun bind(data: GridItem) {
            imgMenu.setImageResource(data.image)
            lblMenuName.text = data.title

            rlaMenu.setOnClickListener {
                if (data.title == "Video Upload") {
                    context.startActivity(Intent(context, ParentVideo::class.java))
                } else if (data.title == "") {
                    context.startActivity(Intent(context, ParentVideo::class.java))
                }
                else if (data.title.equals("Attendance Marking")) {
                    context.startActivity(Intent(context, AbsenteesStudentMark::class.java))
                }

                else if (data.title.equals("Event")) {
                    context.startActivity(Intent(context, CreateEvent::class.java))
                }

                else if (data.title.equals("Notice Board")) {
                    context.startActivity(Intent(context, CreateNoticeBoard::class.java))
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
