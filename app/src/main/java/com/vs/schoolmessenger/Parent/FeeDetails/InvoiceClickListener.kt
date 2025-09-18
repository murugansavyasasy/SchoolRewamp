package com.vs.schoolmessenger.Parent.FeeDetails

import com.vs.schoolmessenger.Parent.FeeDetails.Model.FeeInvoiceResponse

interface InvoiceClickListener {
    fun onItemClick(data: FeeInvoiceResponse.InvoiceData, holder: FeeReceiptAdapter.DataViewHolder)
}
