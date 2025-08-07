package com.vs.schoolmessenger.School.LeaveRequests

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
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
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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




    class DataViewHolder(itemView: View, private val context: Context,    private val listener: SchoolLRClickListener
    ) :
        RecyclerView.ViewHolder(itemView) {

        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textDate: TextView = itemView.findViewById(R.id.textDate)
        private val textReason: TextView = itemView.findViewById(R.id.textReason)
        private val textNoOfDays: TextView = itemView.findViewById(R.id.textNoOfDays)
        private val btnCancel: TextView = itemView.findViewById(R.id.btnCancel)
        private val btnApprove: TextView = itemView.findViewById(R.id.btnApprove)
        private val btnStatus: TextView = itemView.findViewById(R.id.btnStatus)
        private val lnrButtons: LinearLayout = itemView.findViewById(R.id.lnrButtons)
        private val textLeaveType: TextView = itemView.findViewById(R.id.textLeaveType)


        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: LeaveData, position: Int) {
            textName.text = data.student_name
            textDate.text = "${Constant.convertDateTimeFormat(data.leave_from ?: "")} - ${Constant.convertDateTimeFormat(data.leave_to ?: "")}"
            textNoOfDays.text = "${data.no_of_days} ${if (data.no_of_days == "1") "Day" else "Days"} Application"
            textReason.text = data.reason

            if (data.status == Constant.rejected) {
                lnrButtons.visibility=View.GONE
                btnStatus.text ="Rejected"
                btnStatus.visibility=View.VISIBLE
                applyTintedBackground(btnApprove, R.drawable.bg_leave_approved, R.color.light_red_1)
                btnStatus.setTextColor(Color.parseColor("#D32F2F"))


//                btnCancel.visibility = View.VISIBLE
//                btnApprove.visibility = View.GONE

            } else if (data.status == Constant.approved) {
                lnrButtons.visibility=View.GONE
                btnStatus.text ="Approved"
                btnStatus.visibility=View.VISIBLE
                applyTintedBackground(btnApprove, R.drawable.bg_leave_approved, R.color.light_green_1)
               btnStatus.setTextColor(Color.parseColor("#2E7D32"))

//                btnCancel.visibility = View.GONE
//                btnApprove.visibility = View.VISIBLE

            } else if (data.status == Constant.waiting_for_approval) {
                btnStatus.visibility=View.GONE
                lnrButtons.visibility=View.VISIBLE

                btnCancel.text ="Reject"
                btnApprove.text = "Approve"

            }
            if (data.leave_type==""){
                textLeaveType.visibility=View.GONE
            }else{
                textLeaveType.visibility=View.VISIBLE
                textLeaveType.text=data.leave_type
            }

            btnApprove.setOnClickListener {
                if (data.status.equals(Constant.waiting_for_approval)) {
                    listener.onApproveClicked(data, position,true) { isApproved ->
                        if (isApproved) {
                            data.status = Constant.approved
                            listener.onUpdateStatus(data)
                        }
                    }
                }
            }


            btnCancel.setOnClickListener {
                if(data.status.equals(Constant.waiting_for_approval)) {

                    listener.onApproveClicked(data, position,false) { isApproved ->
                        if (isApproved) {
                            data.status = Constant.rejected
                            listener.onUpdateStatus(data)
                        }
                    }
                }
            }

        }

        fun applyTintedBackground(view: View, drawableRes: Int, colorRes: Int) {
            val context = view.context
            val bgDrawable = ContextCompat.getDrawable(context, drawableRes)
            bgDrawable?.setTint(ContextCompat.getColor(context, colorRes))
            view.background = bgDrawable
        }


    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}


//package com.vs.schoolmessenger.School.LeaveRequests
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.LeaveRequests.Listener.SchoolLRClickListener
//import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.ShimmerUtil
//
//class LeaveRequestAdapter(
//    private var itemList: List<LeaveData>?,
//    private var listener: SchoolLRClickListener,
//    private var context: Context,
//    private var isLoading: Boolean
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//
//    private var fullList: List<LeaveData> = itemList ?: listOf()
//    private var filteredList: List<LeaveData> = itemList ?: listOf()
//
//    init {
//        fullList = itemList ?: listOf()
//        filteredList = fullList
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.leave_request_list_item)
//            ShimmerViewHolder(shimmerView)
//        } else {
//            val view =
//                LayoutInflater.from(parent.context)
//                    .inflate(R.layout.leave_request_list_item, parent, false)
//            DataViewHolder(view, context,listener)
//        }
//
//
//    }
//
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is DataViewHolder) {
//            holder.bind(filteredList[position], position)
//
//        }  else if (holder is ShimmerViewHolder) {
//            holder.startShimmer()
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 20
//        else filteredList?.size ?: 0
//    }
//
//
//
//    fun updateData(newList: List<LeaveData>) {
//        this.fullList = newList
//        notifyDataSetChanged()
//    }
//
//
//    class DataViewHolder(itemView: View, private val context: Context,    private val listener: SchoolLRClickListener
//    ) :
//        RecyclerView.ViewHolder(itemView) {
//
//        private val textName: TextView = itemView.findViewById(R.id.textName)
//        private val textDate: TextView = itemView.findViewById(R.id.textDate)
//        private val textReason: TextView = itemView.findViewById(R.id.textReason)
//        private val textNoOfDays: TextView = itemView.findViewById(R.id.textNoOfDays)
//        private val btnCancel: TextView = itemView.findViewById(R.id.btnCancel)
//        private val btnApprove: TextView = itemView.findViewById(R.id.btnApprove)
//        private val btnStatus: TextView = itemView.findViewById(R.id.btnStatus)
//        private val textLeaveType: TextView = itemView.findViewById(R.id.textLeaveType)
//
//
//
//        @SuppressLint("UseCompatLoadingForDrawables")
//        fun bind(data: LeaveData, position: Int) {
//            textName.text = data.student_name
//            textDate.text = "${Constant.convertDateTimeFormat(data.leave_from ?: "")} - ${Constant.convertDateTimeFormat(data.leave_to ?: "")}"
//            textNoOfDays.text = "${data.no_of_days} ${if (data.no_of_days == "1") "Day" else "Days"} Application"
//            textReason.text = data.reason
//
//            if (data.status == Constant.rejected) {
//                btnCancel.text ="Rejected"
//                btnCancel.visibility = View.VISIBLE
//                btnApprove.visibility = View.GONE
//
//            } else if (data.status == Constant.approved) {
//                btnApprove.text = "Approved"
//                btnCancel.visibility = View.GONE
//                btnApprove.visibility = View.VISIBLE
//
//            } else if (data.status == Constant.waiting_for_approval) {
//                btnCancel.visibility = View.VISIBLE
//                btnApprove.visibility = View.VISIBLE
//                btnCancel.text ="Reject"
//                btnApprove.text = "Approve"
//
//            }
//            if (data.leave_type==""){
//                textLeaveType.visibility=View.GONE
//            }else{
//                textLeaveType.visibility=View.VISIBLE
//                textLeaveType.text=data.leave_type
//            }
//
//            btnApprove.setOnClickListener {
//                if (data.status.equals(Constant.waiting_for_approval)) {
//                    listener.onApproveClicked(data, position,true) { isApproved ->
//                        if (isApproved) {
//                            data.status = Constant.approved
//                            listener.onUpdateStatus(data)
//                        }
//                    }
//                }
//            }
//
//            btnCancel.setOnClickListener {
//                if(data.status.equals(Constant.waiting_for_approval)) {
//
//                    listener.onApproveClicked(data, position,false) { isApproved ->
//                        if (isApproved) {
//                            data.status = Constant.rejected
//                            listener.onUpdateStatus(data)
//                        }
//                    }
//
//
//                }
//            }
//        }
//
//
//
//
//    }
//
//    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun startShimmer() {
//            ShimmerUtil.startShimmer(itemView)
//        }
//    }
//}
