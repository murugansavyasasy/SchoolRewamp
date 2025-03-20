package com.vs.schoolmessenger.Parent.QuizExam

import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestAdapter
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestData

interface QuizUpcomingListener {
    fun onItemClick(data: QuizUpcomingData, holder: QuizUpcomingAdapter.DataViewHolder)
}