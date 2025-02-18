package com.vs.schoolmessenger.Parent.Assignment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Dashboard.School.Dashboard
import com.vs.schoolmessenger.R

class AssignmentAdapter(
    private var itemList: List<AssignmentData>?,
    private var listener: AssignmentClickListener,
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
                    .inflate(R.layout.assignment_item_, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblSubject: TextView = itemView.findViewById(R.id.lblSubject)
        private val lblCategotry: TextView = itemView.findViewById(R.id.lblCategotry)
        private val lblSubmissionDue: TextView = itemView.findViewById(R.id.lblSubmissionDue)
        private val lblSubmissionCount: TextView = itemView.findViewById(R.id.lblSubmissionCount)
        private val lblSendby: TextView = itemView.findViewById(R.id.lblSendby)
        private val lblDateAndTime: TextView = itemView.findViewById(R.id.lblDateAndTime)
        private val btnView: TextView = itemView.findViewById(R.id.btnView)
        private val btnSubmit: TextView = itemView.findViewById(R.id.btnSubmit)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: AssignmentData,
            position: Int,
            listener: AssignmentClickListener,
            adapter: AssignmentAdapter
        ) {
            lblTitle.text = data.isTitle
            lblSubject.text = data.isSubject
            lblCategotry.text = data.isCategory
            lblSubmissionDue.text = data.isSubmissionDue
            lblSubmissionCount.text = data.isSubmissionCount
            lblSendby.text = data.isSendBy
            lblDateAndTime.text = data.isDateAndTime

            btnView.setOnClickListener {

            }

            btnSubmit.setOnClickListener {
                val intent = Intent(context, AssignmentFilePicking::class.java)
                context.startActivity(intent)
            }
        }
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }
}