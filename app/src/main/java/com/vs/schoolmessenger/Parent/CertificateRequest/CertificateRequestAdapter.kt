package com.vs.schoolmessenger.Parent.CertificateRequest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil


class CertificateRequestAdapter (
    private var itemList: List<CertificateListData>?,
    private var listener: CertificateListener,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectedPosition = RecyclerView.NO_POSITION


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.item_certificaterequest)
            DataViewHolder.ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_certificaterequest, parent, false)
               DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this) // Pass adapter reference
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val certificate_value: TextView = itemView.findViewById(R.id.certificate_value)
        private val reason_value: TextView = itemView.findViewById(R.id.reason_value)
        private val created_value: TextView = itemView.findViewById(R.id.created_value)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val rytStatus: RelativeLayout = itemView.findViewById(R.id.rytStatus)
        private val rytDownload: RelativeLayout = itemView.findViewById(R.id.rytDownload)

        fun bind(
            data: CertificateListData,
            position: Int,
            listener: CertificateListener,
            adapter: CertificateRequestAdapter
        ) {
            certificate_value.text = data.type
            reason_value.text = data.reason
            created_value.text =  Constant.convertDateTimeFormat(data.requested_on)
            lblStatus.text = data.status
            lblStatus.text = data.status

            if(data.url.isNotEmpty()){
                rytDownload.visibility = View.VISIBLE
            }
            else{
                rytDownload.visibility = View.GONE
            }

            if(data.status.equals("Approved")) {
                rytStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_leave_approved))
            }
            else {
                rytStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_leave_waiting))
            }

        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}
