package com.vs.schoolmessenger.CommonScreens

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Dashboard.Settings.Notification.NotificationDataClass
import com.vs.schoolmessenger.R
import de.hdodenhof.circleimageview.CircleImageView

class SchoolListAdapter(
    private var itemList: List<SchoolsData>?,
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
                    .inflate(R.layout.schools_list_item, parent, false)
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
        val imgSchoolLogo: ImageView = itemView.findViewById(R.id.imgSchoolLogo)
        val lblSchoolName: TextView = itemView.findViewById(R.id.lblSchoolName)
        val lblSchoolAddress: TextView = itemView.findViewById(R.id.lblSchoolAddress)

        fun bind(data: SchoolsData, position: Int) {

            lblSchoolName.text = data.schoolName
            lblSchoolAddress.text = data.schoolAddress

            Glide.with(context)
                .load(data.schoolLogo)
                .into(imgSchoolLogo)

        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)
        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}