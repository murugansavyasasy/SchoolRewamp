package com.vs.schoolmessenger.Parent.OnlineMeeting

import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestAdapter
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestData

interface OnlineMeetingListener {

    fun onItemClick(data: OnlineMeetingData, holder: OnlineMeetingAdapter.DataViewHolder)
}
