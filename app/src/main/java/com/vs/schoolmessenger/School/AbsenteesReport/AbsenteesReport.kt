package com.vs.schoolmessenger.School.AbsenteesReport

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesReportAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesReportDetailAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesDetailClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeData
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteesDetailData
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise
import com.vs.schoolmessenger.Utils.Constant

import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AbsenteesReportBinding


class AbsenteesReport : BaseActivity<AbsenteesReportBinding>(), View.OnClickListener,
    AbsenteesClickListener,
    AbsenteesDetailClickListener {

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var dateadapter: AbsenteesReportAdapter
    private lateinit var classadapter: AbsenteesReportDetailAdapter

    override fun getViewBinding(): AbsenteesReportBinding {
        return AbsenteesReportBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        isStaffDetails = SharedPreference.getStaffDetails(this)
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails?.school_name ?: ""

        isAccessToken = isStaffDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        binding.rlaabsenteesreport.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchAbsenteeData()


        appViewModel?.getabsenteescountbydate?.observe(this) { response ->

            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                return@observe
            }
            if (response.status) {
                isLoadDailyCollectionData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }
    }

    private fun fetchAbsenteeData() {
        appViewModel?.getabsenteescountbydate(
            isAccessToken ?: "",
            this
        )
    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rlaabsenteesreport.visibility = View.GONE
        binding.rlaabsenteesreport2.visibility = View.GONE
    }

    private fun isLoadDailyCollectionData(data: List<AbsenteeData>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI(getString(R.string.no_absentee_data_available))
            return
        }

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rlaabsenteesreport.visibility = View.VISIBLE
        binding.rlaabsenteesreport2.visibility = View.VISIBLE

        dateadapter = AbsenteesReportAdapter(data, this, this, false)
        binding.rlaabsenteesreport.adapter = dateadapter


        dateadapter.setSelectedPosition(0)


        onDateSelected(data[0])

        Log.d("AbsenteesReport", "Class-wise size: ${data[0].class_wise.size}")
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }


    override fun onDateSelected(data: AbsenteeData) {
        classadapter = AbsenteesReportDetailAdapter(data.class_wise, this, this, false, data.date)
        binding.rlaabsenteesreport2.layoutManager = LinearLayoutManager(this)
        binding.rlaabsenteesreport2.adapter = classadapter
    }

    override fun onItemClick(
        data: AbsenteesDetailData,
        holder: AbsenteesReportDetailAdapter.DataViewHolder
    ) {

    }

    override fun onClassSelected(data: ClassWise) {
        Toast.makeText(this, "${getString(R.string.class_clicked)} ${data.class_name}", Toast.LENGTH_SHORT).show()
    }
}
