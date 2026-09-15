package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.Adapter.PaymentProofAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeePaymentProofBinding

class FeePaymentProof :
    BaseActivity<FeePaymentProofBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): FeePaymentProofBinding {
        return FeePaymentProofBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private var appViewModel: App? = null
    private lateinit var paymentAdapter: PaymentProofAdapter
    private var selectedStatus = "all"
    private var country_id = "0"

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

        binding.rvPaymentProof.layoutManager =
            LinearLayoutManager(this)

        binding.btnAll.setOnClickListener(this)
        binding.btnPending.setOnClickListener(this)
        binding.btnApproved.setOnClickListener(this)
        binding.btnRejected.setOnClickListener(this)

        setupSearch()

        loadfeepaymentproof()

        country_id = SharedPreference.getCountryId(this).toString()

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)
            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)

            }
        }

        binding.txtSearch1.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    if (!::paymentAdapter.isInitialized) {
                        return
                    }

                    val searchText =
                        s?.toString()?.trim() ?: ""

                    paymentAdapter.filterByStatusAndSearch(
                        selectedStatus,
                        searchText
                    )

                    updateListVisibility()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        appViewModel?.isPaymentProof?.observe(this) { response ->

            Constant.hideLoading(this)

            if (
                response?.status == true &&
                !response.data.isNullOrEmpty()
            ) {

                val studentPayment =
                    response.data[0]


                val paymentList =
                    studentPayment.payment_details ?: emptyList()

                if (paymentList.isEmpty()) {
                    binding.toolbarLayout.imgSearchToolBar.visibility= View.GONE

                    binding.rvPaymentProof.visibility =
                        View.GONE

                    binding.hsvProofStatusTabs.visibility =
                        View.GONE

                    binding.lytList.visibility =
                        View.VISIBLE

                    binding.txtNoData.text =
                        getString(R.string.no_data_found)

                } else {
                    binding.toolbarLayout.imgSearchToolBar.visibility= View.VISIBLE


                    binding.rvPaymentProof.visibility =
                        View.VISIBLE

                    binding.hsvProofStatusTabs.visibility =
                        View.VISIBLE

                    binding.lytList.visibility =
                        View.GONE

                    paymentAdapter =
                        PaymentProofAdapter(
                            paymentList,
                            this
                        ) { paymentDetails ->

                            val intent = Intent(
                                this,
                                ViewIndividualFeePaymentProofDetails::class.java
                            )

                            intent.putExtra(
                                "individual_fee_payment_proof_details",
                                paymentDetails
                            )

                            startActivity(intent)
                        }

                    binding.rvPaymentProof.adapter =
                        paymentAdapter

                    updateStatusCounts()

                    selectStatus("all")
                }

            } else {

                binding.rvPaymentProof.visibility =
                    View.GONE

                binding.lytList.visibility =
                    View.VISIBLE

                binding.hsvProofStatusTabs.visibility =
                    View.GONE

                binding.toolbarLayout.imgSearchToolBar.visibility= View.GONE

                binding.txtNoData.text =
                    response?.message
                        ?: getString(R.string.no_data_found)
            }
        }
    }

    private fun setupSearch() {

        binding.txtSearch1.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    if (!::paymentAdapter.isInitialized) {
                        return
                    }

                    val searchText =
                        s?.toString()?.trim() ?: ""

                    paymentAdapter.filterByStatusAndSearch(
                        selectedStatus,
                        searchText
                    )

                    updateListVisibility()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )
    }


    private fun loadfeepaymentproof() {

        Constant.showLoading(this)

        appViewModel?.isPaymentProof(
            isAccessToken!!, country_id,
            this
        )
    }

    private fun updateStatusCounts() {

        val allCount =
            paymentAdapter.getStatusCount("all")

        val pendingCount =
            paymentAdapter.getStatusCount("pending")

        val approvedCount =
            paymentAdapter.getStatusCount("approved")

        val rejectedCount =
            paymentAdapter.getStatusCount("rejected")

        binding.btnAll.text =
            "${getString(R.string.all)} $allCount"

        binding.btnPending.text =
            "${getString(R.string.pending)} $pendingCount"

        binding.btnApproved.text =
            "${getString(R.string.approved)} $approvedCount"

        binding.btnRejected.text =
            "${getString(R.string.rejected)} $rejectedCount"
    }


    private fun selectStatus(status: String) {

        if (!::paymentAdapter.isInitialized) {
            return
        }

        selectedStatus =
            status.lowercase()

        val searchText =
            binding.txtSearch1.text
                ?.toString()
                ?.trim()
                ?: ""

        paymentAdapter.filterByStatusAndSearch(
            selectedStatus,
            searchText
        )

        resetStatusButtons()

        when (selectedStatus) {

            "all" -> {
                setSelectedButton(binding.btnAll)
            }

            "pending" -> {
                setSelectedButton(binding.btnPending)
            }

            "approved" -> {
                setSelectedButton(binding.btnApproved)
            }

            "rejected" -> {
                setSelectedButton(binding.btnRejected)
            }
        }

        updateListVisibility()
    }

    private fun updateListVisibility() {

        if (!::paymentAdapter.isInitialized) {
            return
        }

        if (paymentAdapter.itemCount == 0) {

            binding.rvPaymentProof.visibility =
                View.GONE

            binding.lytList.visibility =
                View.VISIBLE

            binding.txtNoData.text =
                if (
                    binding.txtSearch1.text
                        ?.toString()
                        ?.trim()
                        ?.isNotEmpty() == true
                ) {
                    getString(R.string.no_data_found)
                } else {
                    getString(R.string.no_data_found)
                }

        } else {

            binding.rvPaymentProof.visibility =
                View.VISIBLE

            binding.lytList.visibility =
                View.GONE
        }
    }


    private fun resetStatusButtons() {

        val unselectedBackground =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_payment_filter_unselected
            )

        binding.btnAll.background =
            unselectedBackground

        binding.btnPending.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_payment_filter_unselected
            )

        binding.btnApproved.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_payment_filter_unselected
            )

        binding.btnRejected.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_payment_filter_unselected
            )

        val unselectedColor =
            ContextCompat.getColor(
                this,
                R.color.light_gray_44
            )

        binding.btnAll.setTextColor(
            unselectedColor
        )

        binding.btnPending.setTextColor(
            unselectedColor
        )

        binding.btnApproved.setTextColor(
            unselectedColor
        )

        binding.btnRejected.setTextColor(
            unselectedColor
        )
    }


    private fun setSelectedButton(
        textView: TextView
    ) {

        textView.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_payment_filter_selected
            )

        textView.setTextColor(
            ContextCompat.getColor(
                this,
                android.R.color.white
            )
        )
    }


    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.btnAll -> {
                selectStatus("all")
            }

            R.id.btnPending -> {
                selectStatus("pending")
            }

            R.id.btnApproved -> {
                selectStatus("approved")
            }

            R.id.btnRejected -> {
                selectStatus("rejected")
            }
        }
    }
}