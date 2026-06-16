package com.vs.schoolmessenger.Parent.Hostel.Adapter.OutpassRequestList



import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Hostel.Listner.gatePassClickListner
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.OutpassRequestData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Listner.OutpassRequestClickListner
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class OutpassRequestList(
    private var itemList: List<OutpassRequestData>?,
    private val context: Context,
    private val listner: gatePassClickListner,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<OutpassRequestData> = itemList ?: listOf()
    private var filteredList: List<OutpassRequestData> = fullList


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.parent_outpass_request_list)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.parent_outpass_request_list, parent, false)
            DataViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(filteredList[position], context,listner)

        }
    }

    fun updateData(newList: List<OutpassRequestData>) {
        fullList = newList
        filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }


    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblRequestOn: TextView = itemView.findViewById(R.id.lblRequestOn)
        private val lblReason: TextView = itemView.findViewById(R.id.lblReason)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val lblLeaveDurationDays: TextView = itemView.findViewById(R.id.lblLeaveDurationDays)
        private val lblLeaveDurationToDays: TextView = itemView.findViewById(R.id.lblLeaveDurationToDays)
        private val lblSeeGatepass: TextView = itemView.findViewById(R.id.lblSeeGatepass)


        @SuppressLint("SetTextI18n")
        fun bind(
            data: OutpassRequestData,
            context: Context,
            listner: gatePassClickListner
        ) {

            val input = data?.fromdate_todate?:" - "
            val parts = input.split(" - ")
            val from = parts.getOrNull(0)?.trim() ?: ""
            val to = parts.getOrNull(1)?.trim() ?: ""


            lblReason.text = data.reason
            lblRequestOn.text ="${context.getString(R.string.requested_on)} : ${Constant.convertDateFormatType2(data.request_time)}"
            lblLeaveDurationDays.text = "Out : ${Constant.convertDateFormatType2(from)}"
            lblLeaveDurationToDays.text = "Return : ${Constant.convertDateFormatType2(to)}"

            if (data.status == Constant.rejected.uppercase()) {
                lblSeeGatepass.visibility= View.GONE
                lblStatus.background = ContextCompat.getDrawable(context, R.drawable.rect_bg_light_red_rejected)
                lblStatus.text =  data.status.lowercase().replaceFirstChar { it.uppercase() }
                lblStatus.setTextColor(ContextCompat.getColor(context, R.color.red))

            }
            else if (data.status == Constant.approved.uppercase()) {
                lblSeeGatepass.visibility= View.VISIBLE
                lblSeeGatepass.setOnClickListener {
                    listner.onGatePassClick(data)
                }
                lblStatus.background = ContextCompat.getDrawable(context, R.drawable.rect_bg_light_green_approved)
                lblStatus.text =  data.status.lowercase().replaceFirstChar { it.uppercase() }
                lblStatus.setTextColor(ContextCompat.getColor(context, R.color.green))

            }
            else if (data.status == Constant.pending.uppercase()) {
                lblSeeGatepass.visibility= View.GONE
                lblStatus.background = ContextCompat.getDrawable(context, R.drawable.rect_bg_light_orange_pending)
                lblStatus.text =  data.status.lowercase().replaceFirstChar { it.uppercase() }
                lblStatus.setTextColor(ContextCompat.getColor(context, R.color.dark_brown_3))
            }
            else{
                lblSeeGatepass.visibility= View.GONE
                lblStatus.background = ContextCompat.getDrawable(context, R.drawable.rect_bg_light_orange_pending)
                lblStatus.text =  data.status.lowercase().replaceFirstChar { it.uppercase() }
                lblStatus.setTextColor(ContextCompat.getColor(context, R.color.dark_brown_3))
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
        init {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}