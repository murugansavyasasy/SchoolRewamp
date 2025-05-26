package com.vs.schoolmessenger.Parent.FeeDetails

interface InvoiceClickListener {
    fun onItemClick(data: InvoiceDetails, holder: FeeReceiptAdapter.DataViewHolder)
}