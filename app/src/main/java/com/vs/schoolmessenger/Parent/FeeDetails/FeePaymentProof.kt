package com.vs.schoolmessenger.Parent.FeeDetails

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeePaymentProofBinding

class FeePaymentProof : BaseActivity<FeePaymentProofBinding>(), View.OnClickListener {

    override fun getViewBinding(): FeePaymentProofBinding {
        return FeePaymentProofBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    var isProgressLoading = false

    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.rvPaymentProof.layoutManager = LinearLayoutManager(this)

        loadfeepaymentproof()

        appViewModel?.isPaymentProof?.observe(this) { response ->
            Constant.hideLoading(this)
            Log.d("Data", "isResponseComing")

            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val studentPayment = response.data[0]
                binding.tvStudentName.text =
                    "${studentPayment.student_details?.student_name} - " +
                            "Class ${studentPayment.student_details?.class_name} " +
                            studentPayment.student_details?.section_name

                val paymentList = studentPayment.payment_details
                if (paymentList.isNullOrEmpty()) {
                    binding.rvPaymentProof.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvPaymentProof.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvPaymentProof.adapter = PaymentProofAdapter(
                        paymentList
                    ) { imageUrl ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl)))
                    }
                }
            } else {
                binding.rvPaymentProof.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                Toast.makeText(
                    this,
                    response?.message ?: getString(R.string.unable_to_fetch_invoice),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
    private fun loadfeepaymentproof() {
        Constant.showLoading(this)
        appViewModel?.isPaymentProof(
            isAccessToken!!,"8","1",this
        )
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}