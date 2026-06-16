package com.vs.schoolmessenger.Parent.Coupon.CouponListener

import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummary

interface TicketCouponClickListener {
    fun onticketCouponSummaryClick(ticketSummary: TicketSummary?)
    fun onSearchResultEmpty(isEmpty: Boolean)
}