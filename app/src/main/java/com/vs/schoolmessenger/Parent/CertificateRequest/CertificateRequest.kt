package com.vs.schoolmessenger.Parent.CertificateRequest

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CertificateRequestParentBinding


class CertificateRequest : BaseActivity<CertificateRequestParentBinding>(), View.OnClickListener {
    private lateinit var adapter: CertificateRequestAdapter
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private lateinit var certificateRequestList: List<CertificateListData>
    private lateinit var certificateTypes: List<CertificateTypesData>
    private var isSelectedCertificateName: String? = null
    private var urgency_level: String? = "Not Urgent"

    override fun getViewBinding(): CertificateRequestParentBinding {
        return CertificateRequestParentBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        binding.ivradio.setOnClickListener(this)
        binding.ivradio1.setOnClickListener(this)

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblLeftSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblRightSideBar.setOnClickListener(this)
        binding.btnSendCertificateRequest.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        binding.toolbarLayout.lnrParent.visibility = View.VISIBLE
        binding.toolbarLayout.lblLeftSideBar.text = "Certificates"
        binding.toolbarLayout.lblRightSideBar.text = "Request"
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name

        isAccessToken = isChildDetails?.access_token
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        loadCertificateTypes()
        binding.ivradio.setImageResource(R.drawable.selected_radio_button)
        binding.ivradio1.setImageResource(R.drawable.unselected_radio_button)

        appViewModel!!.isCertificateRequestList?.observe(this) { response ->
            if (response != null && response.status) {
                certificateRequestList = response.data
                if (certificateRequestList.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.lnrNoRecords.visibility = View.GONE
                    setupRecyclerView()
                }
            } else {
                binding.recyclerView.visibility = View.GONE
                binding.lnrNoRecords.visibility = View.VISIBLE
                binding.txtNoData.text = "No data found!"
            }
        }

        appViewModel!!.isCertificateType?.observe(this) { response ->
            if (response != null && response.status) {
                val certificateTypeList = response.data
                loadCertificates(certificateTypeList)
            }
        }



        appViewModel!!.isSendCertificateRequest?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                Constant.showTopAlertPopup(response!!.message, this)
            }
        }

    }

    private fun loadCertificates(certificateTypes: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, certificateTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerText.adapter = adapter

        binding.spinnerText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                isSelectedCertificateName = certificateTypes[position]
                Log.d("isSelectedCertificateName", isSelectedCertificateName!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun setupRecyclerView() {
        adapter = CertificateRequestAdapter(certificateRequestList, object : CertificateListener {
            override fun onItemClick(
                data: CertificateListData, holder: CertificateRequestAdapter.DataViewHolder
            ) {

            }
        }, this, Constant.isShimmerViewDisable)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun showShimmer() {
        adapter = CertificateRequestAdapter(null, object : CertificateListener {
            override fun onItemClick(
                data: CertificateListData, holder: CertificateRequestAdapter.DataViewHolder
            ) {

            }
        }, this, Constant.isShimmerViewShow)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadCertificateRequestData() {
        showShimmer()
        appViewModel?.getCertificateRequestList(
            isAccessToken.orEmpty(), activity = this
        )
    }

    private fun loadCertificateTypes() {
        showShimmer()
        appViewModel?.getCertificateTypes(
            isAccessToken.orEmpty(), activity = this
        )
    }

    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {

            R.id.lblLeftSideBar -> {
                binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.bg_light_green)
                binding.toolbarLayout.lblRightSideBar.setTextColor(Color.BLACK)
                binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.white_radious)
                binding.rytRequestTap.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                loadCertificateRequestData()
            }

            R.id.lblRightSideBar -> {
                binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.white_radious)
                binding.toolbarLayout.lblRightSideBar.setTextColor(Color.BLACK)
                binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.bg_light_green)
                binding.rytRequestTap.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.lnrNoRecords.visibility = View.GONE

            }

            R.id.ivradio -> {
                urgency_level = "Not Urgent"
                binding.ivradio.setImageResource(R.drawable.selected_radio_button)
                binding.ivradio1.setImageResource(R.drawable.unselected_radio_button)
            }

            R.id.ivradio1 -> {
                urgency_level = "Urgent"
                binding.ivradio.setImageResource(R.drawable.unselected_radio_button)
                binding.ivradio1.setImageResource(R.drawable.selected_radio_button)
            }

            R.id.imgBack -> onBackPressed()

            R.id.btnSendCertificateRequest -> {
                if (binding.txtReason.text.isNotEmpty()) {
                    Constant.showLoading(this)
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("requested_for", isSelectedCertificateName)
                    jsonObject.addProperty("urgency_level", urgency_level)
                    jsonObject.addProperty("reason", binding.txtReason.text.toString())
                    appViewModel?.sendCertificateRequest(
                        isAccessToken.orEmpty(), jsonObject, activity = this
                    )
                } else {
                    Toast.makeText(
                        this, "Please enter the reason", Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
    }
}
