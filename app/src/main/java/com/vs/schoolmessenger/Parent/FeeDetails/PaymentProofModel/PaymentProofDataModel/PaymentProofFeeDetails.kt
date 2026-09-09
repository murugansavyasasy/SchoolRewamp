package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofDataModel

import android.os.Parcelable
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel.CarryOverFee
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.HostelModel.HostelFee
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.OthersModel.GenericFee
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.Quantity
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TermModel.Term
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TransportModel.TransportFee
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentProofFeeDetails(
    val term: List<Term>,
    val others: List<GenericFee>,
    val carryover: List<CarryOverFee>,
    val transport: List<TransportFee>,
    val hostel: List<HostelFee>,
    val quantity: List<Quantity>
): Parcelable