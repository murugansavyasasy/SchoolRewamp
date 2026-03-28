package com.vs.schoolmessenger.School.Hostel.Adapter.OutpassRequest


import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Listner.OutpassRequestClickListner
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.StatusWiseOutpassRequestData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StatusWiseOutpassRequest(
    private var itemList: List<StatusWiseOutpassRequestData>?,
    private val context: Context,
    private val listner: OutpassRequestClickListner,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    var fullList: List<StatusWiseOutpassRequestData> = itemList ?: emptyList()
    private var filteredList: List<StatusWiseOutpassRequestData> = fullList

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.status_wise_admin_request)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.status_wise_admin_request, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position],listner)
        }
    }

    fun updateData(newList: List<StatusWiseOutpassRequestData>) {
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<StatusWiseOutpassRequestData> {
        return itemList!!
    }


    fun removeItemById(id: String) {
        val updatedList = fullList.mapNotNull { monthData ->
            val updatedDetails = monthData.attd_details.filterNot { it.room_no == id }
            if (updatedDetails.isNotEmpty()) {
                StatusWiseOutpassRequestData(status = monthData.status, attd_details = updatedDetails)
            } else null
        }
        updateData(updatedList)
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
        val rcAdminRequestWise: RecyclerView =
            itemView.findViewById(R.id.rcAdminRequestWise)

        fun bind(
            data: StatusWiseOutpassRequestData,
            listner: OutpassRequestClickListner,
        ) {
            when(data.status){

                Constant.rejected_ ->{
                    lblStatus.setTextColor(Color.parseColor("#D32F2F"))
                    imgStatus.setImageResource(R.drawable.close_red_color)
                    lblStatus.text = "${context.getString(R.string.rejected)} (${data.attd_details.size})"
                }

                Constant.approved, Constant.approval ->{
                    lblStatus.text = "${context.getString(R.string.approved)} (${data.attd_details.size})"
                    lblStatus.setTextColor(Color.parseColor("#2E7D32"))
                    imgStatus.setImageResource(R.drawable.tick_icon_2)
                    imgStatus.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.green),
                        PorterDuff.Mode.SRC_IN
                    )
                }

                else->{
                    lblStatus.text = "${context.getString(R.string.pending_approval)} (${data.attd_details.size})"
                    lblStatus.setTextColor(context.getColor(R.color.dark_orange_3))
                    imgStatus.setImageResource(R.drawable.waiting_for_approval)
                    imgStatus.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.dark_orange_3),
                        PorterDuff.Mode.SRC_IN
                    )
                }
            }

            if (data.attd_details.isEmpty()) {
                rcAdminRequestWise.visibility = View.GONE
            } else {
                rcAdminRequestWise.visibility = View.VISIBLE
                rcAdminRequestWise.layoutManager = LinearLayoutManager(context)
                rcAdminRequestWise.isNestedScrollingEnabled = false
                rcAdminRequestWise.adapter = OutpassRequestWise(
                    data.attd_details,
                    context,
                    listner,
                    false
                )
            }

        }


    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}