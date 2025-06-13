package com.vs.schoolmessenger.School.LeaveRequests

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.leave_request_list)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.leave_request_list, parent, false)
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
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblSection: TextView = itemView.findViewById(R.id.lblSection)
        private val lblFromData: TextView = itemView.findViewById(R.id.lblFromData)
        private val lblToDate: TextView = itemView.findViewById(R.id.lblToDate)
        private val lbldays: TextView = itemView.findViewById(R.id.lbldays)
        private val lbldate: TextView = itemView.findViewById(R.id.lbldate)
        private val leaverequestdesc: TextView = itemView.findViewById(R.id.leaverequestdesc)
        private  val bottomlinear_layout: LinearLayout = itemView.findViewById(R.id.bottomlinear_layout)
        private val bottomstatuslinear_layout: LinearLayout = itemView.findViewById(R.id.bottomstatuslinear_layout)
        private val bottomstatusrelative_layout: RelativeLayout = itemView.findViewById(R.id.bottomstatusrelative_layout)
        private val status_textlabel: TextView = itemView.findViewById(R.id.status_textlabel)
        private val relative_layout: RelativeLayout = itemView.findViewById(R.id.relative_layout)
        private val statustext_top: TextView = itemView.findViewById(R.id.statustext_top)
        private val btnrejected: RelativeLayout = itemView.findViewById(R.id.btnrejected)
        private val btnapprove: RelativeLayout = itemView.findViewById(R.id.btnapprove)
        private val imagearrow_view: ImageView = itemView.findViewById(R.id.imagearrow_view)
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: LeaveData, position: Int) {
            lblName.text = data.student_name
            lblSection.text = data.section_name
            lblFromData.text = Constant.convertDateTimeFormat(data.leave_from.toString())
            lblToDate.text = Constant.convertDateTimeFormat(data.leave_to.toString())
            lbldays.text = data.no_of_days
            lbldate.text =data.applied_on
            leaverequestdesc.text = data.reason
            imagearrow_view.setColorFilter(ContextCompat.getColor(context, R.color.navi_blue2), PorterDuff.Mode.SRC_IN)


            if (data.status == "Rejected") {
                bottomlinear_layout.visibility = View.GONE
                bottomstatuslinear_layout.visibility = View.VISIBLE
                bottomstatusrelative_layout.setBackgroundResource(R.drawable.bg_red_radoius_10dp)
                status_textlabel.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
                relative_layout.setBackgroundResource(R.drawable.bg_red_radoius_15dp)
                statustext_top.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                statustext_top.text = data.status
                status_textlabel.text = "Rejected On"+ data.updated_on

            } else if (data.status == "Approved") {
                bottomlinear_layout.visibility = View.GONE
                bottomstatuslinear_layout.visibility = View.VISIBLE
                bottomstatusrelative_layout.setBackgroundResource(R.drawable.bg_green_radoius_10dp)
                status_textlabel.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                relative_layout.setBackgroundResource(R.drawable.bg_green_radoius_10dp)
                statustext_top.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                status_textlabel.text = "Approved On"+ data.updated_on
                statustext_top.text = data.status

            } else if (data.status == "Waiting for approval") {
                bottomstatuslinear_layout.visibility = View.GONE
                bottomlinear_layout.visibility = View.VISIBLE
                relative_layout.setBackgroundResource(R.drawable.bg_orange_radoius_10dp)
                statustext_top.text = "Pending"
                statustext_top.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            }
            btnapprove.setOnClickListener {
                listener.onApproveClicked(data, position)
            }
            btnrejected.setOnClickListener {
                listener.onRejectClicked(data, position)
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}