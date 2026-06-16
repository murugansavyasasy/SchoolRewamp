package com.vs.schoolmessenger.Parent.CertificateRequest

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class CertificateRequestAdapter(
    private var itemList: List<CertificateListData>?,
    private var listener: CertificateListener,
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
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.item_certificaterequest)
            DataViewHolder.ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_certificaterequest, parent, false)
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

    fun updateData(newList: List<CertificateListData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblCertificateTitle: TextView = itemView.findViewById(R.id.lblCertificateTitle)
        private val lblCertificateReason: TextView =
            itemView.findViewById(R.id.lblCertificateReason)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val rytCertificate: RelativeLayout = itemView.findViewById(R.id.rytCertificate)

        fun bind(
            data: CertificateListData,
            position: Int,
            listener: CertificateListener,
            adapter: CertificateRequestAdapter
        ) {
            lblCertificateTitle.text = data.type
            lblCertificateReason.text = buildSpannedString {
                bold { color(Color.BLACK) { append(context.getString(R.string.reason_2)) } }

                val reasonText = data.reason ?: ""
                val shortReason = if (reasonText.length > 10) {
                    reasonText.take(10) + "..."
                } else {
                    reasonText
                }
                append(shortReason)
            }



            lblDate.text = Constant.convertDateTimeFormat2(data.requested_on)

            rytCertificate.setOnClickListener {

                val context = it.context
                val myIntent = Intent(context, CertificateViewActivity::class.java)
                val saveCertificateData = CertificateListData(
                    url = data.url,
                    type = data.type,
                    reason = data.reason,
                    urgency_level = data.urgency_level,
                    requested_on = data.requested_on,
                    status = data.status,
                    issued_on = data.issued_on,
                    message = data.message
                )
                Constant.isCertificateData = saveCertificateData
                context.startActivity(myIntent)
            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}
