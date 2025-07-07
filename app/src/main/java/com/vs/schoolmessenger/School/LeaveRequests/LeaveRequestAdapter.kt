package com.vs.schoolmessenger.School.LeaveRequests

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filter.FilterResults
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.NoticeBoardAdapter
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.NoticeBoardAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.Parent.Noticeboard.Notice
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoardClickListener
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LeaveRequests.Listener.SchoolLRClickListener
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class LeaveRequestAdapter(
    private var itemList: List<LeaveData>?,
    private var listener: SchoolLRClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<LeaveData> = itemList ?: listOf()
    private var filteredList: List<LeaveData> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.leave_request_list_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.leave_request_list_item, parent, false)
            DataViewHolder(view, context,listener)
        }


    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position)

        }  else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else filteredList?.size ?: 0
    }

    fun updateData(newList: List<LeaveData>) {
        this.fullList = newList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.student_name.lowercase().contains(query) ||
                                it.section_name.lowercase().contains(query) ||
                                it.reason.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<LeaveData> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }

        }
    }


    class DataViewHolder(itemView: View, private val context: Context,    private val listener: SchoolLRClickListener
    ) :
        RecyclerView.ViewHolder(itemView) {

        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textDate: TextView = itemView.findViewById(R.id.textDate)
        private val textReason: TextView = itemView.findViewById(R.id.textReason)
        private val textNoOfDays: TextView = itemView.findViewById(R.id.textNoOfDays)
        private val textFirstLetter: TextView = itemView.findViewById(R.id.textFirstLetter)
        private val btnCancel: TextView = itemView.findViewById(R.id.btnCancel)
        private val btnApprove: TextView = itemView.findViewById(R.id.btnApprove)



        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: LeaveData, position: Int) {
            textName.text = data.student_name
            textFirstLetter.text = data.student_name.first().toString()
//            lblSection.text = data.class_name
            textDate.text =  Constant.convertDateTimeFormat(data.leave_from.toString()) + " - "+Constant.convertDateTimeFormat(data.leave_to.toString())
            if(data.no_of_days.equals("1")){
                textNoOfDays.text = "( "+data.no_of_days+" Day )"
            }
            else{
                textNoOfDays.text = "( "+data.no_of_days+" Days )"
            }
           // lbldate.text =data.applied_on
            textReason.text = data.reason

            if (data.status == "Rejected") {
                btnCancel.text ="Cancelled"
                btnCancel.visibility = View.VISIBLE
                btnApprove.visibility = View.GONE

            } else if (data.status == "Approved") {
                btnApprove.text = "Approved"
                btnCancel.visibility = View.GONE
                btnApprove.visibility = View.VISIBLE

            } else if (data.status == "Waiting for approval") {
                btnCancel.visibility = View.VISIBLE
                btnApprove.visibility = View.VISIBLE
                btnCancel.text ="Cancel"
                btnApprove.text = "Approve"

            }
            btnApprove.setOnClickListener {
                if(data.status.equals("Waiting for approval")) {
                    listener.onApproveClicked(data, position)
                }
            }
            btnCancel.setOnClickListener {
                if(data.status.equals("Waiting for approval")) {
                    listener.onRejectClicked(data, position)
                }
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}