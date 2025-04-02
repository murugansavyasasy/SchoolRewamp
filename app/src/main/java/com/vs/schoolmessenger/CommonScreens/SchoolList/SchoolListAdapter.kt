package com.vs.schoolmessenger.CommonScreens.SchoolList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R

class SchoolListAdapter(
    private var isMultipleSchool: Boolean,
    private val selectedIds: MutableList<Int>,
    private var itemList: List<StaffDetails>?,
    private var listener: SchoolListClickListener,
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
            holder.bind(itemList!![position], position, listener, isMultipleSchool, selectedIds)
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
        val rlaHeader: RelativeLayout = itemView.findViewById(R.id.rlaHeader)
        val chMultipleSchool: CheckBox = itemView.findViewById(R.id.chMultipleSchool)
        val imgSingleSchoolArrow: ImageView = itemView.findViewById(R.id.imgSingleSchoolArrow)

        fun bind(
            data: StaffDetails,
            position: Int,
            listener: SchoolListClickListener,
            isMultipleSchool: Boolean,
            selectedIds: MutableList<Int>
        ) {

            if (isMultipleSchool) {
                chMultipleSchool.visibility = View.VISIBLE
                imgSingleSchoolArrow.visibility = View.GONE
            } else {
                chMultipleSchool.visibility = View.GONE
                imgSingleSchoolArrow.visibility = View.VISIBLE
            }

            lblSchoolName.text = data.school_name
            lblSchoolAddress.text = data.school_address

            Glide.with(context)
                .load(data.school_logo)
                .into(imgSchoolLogo)

            rlaHeader.setOnClickListener {
                if (!isMultipleSchool) {
                    listener.onItemClick(data)
                }
            }

            chMultipleSchool.setOnClickListener {
                if (chMultipleSchool.isChecked) {
                    selectedIds.add(data.schedule_call_type)
                } else {
                    selectedIds.remove(position)
                }
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