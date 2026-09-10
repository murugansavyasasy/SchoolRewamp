package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof


import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.Adapter.FeeAdapter
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.Adapter.PaymentProofFileAdapter
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.Adapter.TransactionReceiptsAdapter
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.FeeBreakDownCustom.FeeItemMapper
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofDataModel.PaymentDetails
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofDataModel.PaymentProofFeeDetails
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.ProofDetails
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.ProofUploaded
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ViewIndividualFeePaymentProofDetailsBinding

class ViewIndividualFeePaymentProofDetails :
    BaseActivity<ViewIndividualFeePaymentProofDetailsBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): ViewIndividualFeePaymentProofDetailsBinding {
        return ViewIndividualFeePaymentProofDetailsBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private var paymentDetails: PaymentDetails? = null


    private var appViewModel: App? = null

    private var feeDetails: PaymentProofFeeDetails? = null

    private var hasBeenInitialized = false
    private var expandedSectionId: String? = null   // the ONLY expand/collapse state for the whole list

    private val feeAdapter: FeeAdapter by lazy {
        FeeAdapter { clickedSectionId ->
            expandedSectionId = if (expandedSectionId == clickedSectionId) null else clickedSectionId
            refreshFeeBreakdownList()
        }
    }


    override fun setupViews() {

        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel =
            ViewModelProvider(this)[App::class.java]

        appViewModel?.init()

        isChildDetails =
            SharedPreference.getChildDetails(this)


        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails!!.standard_name + " - " + isChildDetails!!.section_name

        isAccessToken =
            isChildDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener(this)

        paymentDetails =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    "individual_fee_payment_proof_details",
                    PaymentDetails::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(
                    "individual_fee_payment_proof_details"
                )
            }


        if (paymentDetails == null) {

            binding.lytList.visibility = View.VISIBLE
            binding.lnrMainFeeProof.visibility = View.GONE
            binding.txtNoData.text = getString(R.string.no_data_found)

        } else {

            binding.lytList.visibility = View.GONE
            binding.lnrMainFeeProof.visibility = View.VISIBLE

            val data = paymentDetails!!

            binding.lblUserEntered.text=data.user_enter_amount?:"-"
            binding.lblTotalPayment.text=displayValue(data.total_amount?:"")
            binding.lblAIDeteched.text=displayValue(data.ai_detected_amount?:"")
            binding.lblValidatedBy.text=displayValue(data.validated_by?:"")
            binding.lblValidatedOn.text=displayValue(data.validated_on?:"")
            binding.lblPaymentID.text=displayValue(data.payment_id?:"")
            binding.lblCreatedOn.text=displayValue(data.created_on?:"")
            binding.lblRemarks.text=displayValue(data.remarks?:"")

            setupPaymentProofList(data.proof_uploaded)
            setupTransactionReceiptList(data.proof_details)
            setupFeeBreakdownList(data.fee_details)
            val transactionDetailsCount=data.proof_details?.size?:0
            val transactionWord=if(transactionDetailsCount>1) getString(R.string.details) else getString(R.string.detail)
            val reciptWord=if(transactionDetailsCount>1)  getString(R.string.receipts) else getString(R.string.receipt)
            binding.lblTransactionDetailsAndCount.text="${getString(R.string.transaction)} ${transactionWord} (${transactionDetailsCount} ${reciptWord} ${getString(R.string.detected)}) "

            val status = data.is_payment_validated
                ?.trim()
                ?.lowercase()
                ?: "pending"

            binding.lblStatus.text = data.is_payment_validated

            when (status) {
                "approved" -> {
                    binding.lblStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.green
                        )
                    )
                }

                "rejected" -> {
                    binding.lblStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.red
                        )
                    )
                }
                "pending" -> {
                    binding.lblStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.dark_brown_3
                        )
                    )
                }

                else -> {
                    binding.lblStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black
                        )
                    )
                }
            }

        }

    }

    private fun setupFeeBreakdownList(details: PaymentProofFeeDetails?) {

        feeDetails = details

        if (details == null) {
            binding.cardLnrFeeBreakDownDetails.visibility = View.GONE
            binding.lblFeeBreakDown.visibility = View.GONE
            return
        }

        val hasNoFeeDetails =
            details.term.isEmpty() &&
                    details.others.isEmpty() &&
                    details.carryover.isEmpty() &&
                    details.transport.isEmpty() &&
                    details.hostel.isEmpty() &&
                    details.quantity.isEmpty()

        if (hasNoFeeDetails) {
            binding.cardLnrFeeBreakDownDetails.visibility = View.GONE
            binding.lblFeeBreakDown.visibility = View.GONE
            return
        }

        binding.cardLnrFeeBreakDownDetails.visibility = View.VISIBLE
        binding.lblFeeBreakDown.visibility = View.VISIBLE

        binding.rcFeesBreakDown.layoutManager = LinearLayoutManager(this)
        binding.rcFeesBreakDown.setHasFixedSize(false)
        binding.rcFeesBreakDown.itemAnimator = null

        binding.rcFeesBreakDown.adapter = feeAdapter

        // Default-expand the first card, but only on initial load — if the user
        // has already toggled something (expandedSectionId no longer null, or
        // was explicitly collapsed to null by tapping the open section again),
        // don't override their choice on a later call to this function.
        if (expandedSectionId == null && !hasBeenInitialized) {
            expandedSectionId = FeeItemMapper.firstSectionId(this, details)
        }
        hasBeenInitialized = true

        refreshFeeBreakdownList()
    }

//    private fun setupFeeBreakdownList(details: PaymentProofFeeDetails?) {
//
//        feeDetails = details
//
//        if (details == null) {
//            binding.cardLnrFeeBreakDownDetails.visibility = View.GONE
//            binding.lblFeeBreakDown.visibility = View.GONE
//            return
//        }
//
//        val hasNoFeeDetails =
//            details.term.isEmpty() &&
//                    details.others.isEmpty() &&
//                    details.carryover.isEmpty() &&
//                    details.transport.isEmpty() &&
//                    details.hostel.isEmpty() &&
//                    details.quantity.isEmpty()
//
//        if (hasNoFeeDetails) {
//            binding.cardLnrFeeBreakDownDetails.visibility = View.GONE
//            binding.lblFeeBreakDown.visibility = View.GONE
//            return
//        }
//
//        binding.cardLnrFeeBreakDownDetails.visibility = View.VISIBLE
//        binding.lblFeeBreakDown.visibility = View.VISIBLE
//
//        binding.rcFeesBreakDown.layoutManager = LinearLayoutManager(this)
//        binding.rcFeesBreakDown.setHasFixedSize(false)
//        binding.rcFeesBreakDown.itemAnimator = null
//
//        binding.rcFeesBreakDown.adapter = feeAdapter
//
//        refreshFeeBreakdownList()
//    }

    private fun refreshFeeBreakdownList() {
        val details = feeDetails ?: return
        val newList = FeeItemMapper.buildFlatList(this,details, expandedSectionId)

        feeAdapter.submitList(newList) {
            binding.rcFeesBreakDown.requestLayout()
        }
    }

    private fun setupPaymentProofList(proofUploaded: List<ProofUploaded>?) {
        if (proofUploaded.isNullOrEmpty()){
            binding.rcPaymentProofAttachments.visibility= View.GONE
            binding.lblNoDataFoundForFeeUploadAttachment.visibility= View.VISIBLE
        }else{

            binding.rcPaymentProofAttachments.visibility= View.VISIBLE
            binding.lblNoDataFoundForFeeUploadAttachment.visibility= View.GONE
            binding.rcPaymentProofAttachments.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rcPaymentProofAttachments.adapter =
                PaymentProofFileAdapter(this, proofUploaded)
        }
    }
    private fun setupTransactionReceiptList(tranaction: List<ProofDetails>?) {
        if (tranaction.isNullOrEmpty()){
            binding.rcTransactionReceipts.visibility= View.GONE
            binding.lblNoDataFoundForTransactionReceipts.visibility= View.VISIBLE
        }else{

            binding.rcTransactionReceipts.visibility= View.VISIBLE
            binding.lblNoDataFoundForTransactionReceipts.visibility= View.GONE
            binding.rcTransactionReceipts.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.rcTransactionReceipts.adapter =
                TransactionReceiptsAdapter(this, tranaction)
        }
    }




    private fun displayValue(value: String?): String {
        return value?.takeIf { it.isNotBlank() } ?: "-"
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}