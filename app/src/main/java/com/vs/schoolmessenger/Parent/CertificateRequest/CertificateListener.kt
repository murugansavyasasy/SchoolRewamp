package com.vs.schoolmessenger.Parent.CertificateRequest

import com.vs.schoolmessenger.Parent.LSRW.LSRWAdapter
import com.vs.schoolmessenger.Parent.LSRW.LSRWData

interface CertificateListener {

    fun onItemClick(data: CertificateRequestData, holder: CertificateRequestAdapter.DataViewHolder)
}
