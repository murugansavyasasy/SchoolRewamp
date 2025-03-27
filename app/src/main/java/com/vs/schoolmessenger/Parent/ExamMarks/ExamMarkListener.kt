package com.vs.schoolmessenger.Parent.ExamMarks

import com.vs.schoolmessenger.Dashboard.Parent.ExamMarkAdapter
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestAdapter
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestData

interface ExamMarkListener  {
    fun onItemClick(data: ExamMarkDataModel, holder: ExamMarkResultsAdapter.DataViewHolder)
}
