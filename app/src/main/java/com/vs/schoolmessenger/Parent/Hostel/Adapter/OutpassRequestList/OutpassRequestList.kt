package com.vs.schoolmessenger.Parent.Hostel.Adapter.OutpassRequestList



import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.OutpassRequestData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class OutpassRequestList(
    private var itemList: List<OutpassRequestData>?,
    private val context: Context,
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
            holder.bind(filteredList[position], context)

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


        @SuppressLint("SetTextI18n")
        fun bind(
            data: OutpassRequestData,
            context: Context,
        ) {

            lblReason.text = data.reason
            lblRequestOn.text = data.reason
            lblLeaveDurationDays.text = data.requestTime

            if (data.status == Constant.rejected) {

                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_red_3
                )

                lblStatus.text=data.status
                lblStatus.setTextColor(context.getColor(R.color.red))

            }
            else if (data.status == Constant.approved) {

                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_green_3
                )
                lblStatus.text=data.status
                lblStatus.setTextColor(context.getColor(R.color.green))



            }
            else if (data.status == Constant.waiting_for_approval) {

                lblStatus.text=context.getString(R.string.pending)
                lblStatus.setTextColor(context.getColor(R.color.dark_brown_3))



                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_orange_3
                )

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