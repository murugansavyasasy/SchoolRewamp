package com.vs.schoolmessenger.Parent.BusTracking

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Parent.BusTracking.Adapter.BusListAdapter
import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.BusListData
import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.getBusList
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.selctedHotelDetails
import com.vs.schoolmessenger.School.Hostel.SchoolHostelDashboard
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.BusListActivityBinding
import kotlin.toString

class BusList : BaseActivity<BusListActivityBinding>(), View.OnClickListener, BusClickListner {

    override fun getViewBinding(): BusListActivityBinding {
        return BusListActivityBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    lateinit var mAdapter: BusListAdapter
    var isToolBarTitle = ""
    var isToolBarDescription = ""

    var isVendor: String? = ""
    var userDetails: UserDetails? = null


    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)
        isVendor  = userDetails?.child_details[0]?.gps_type

        Log.d("isVendorValue", isVendor.toString())

        if (Constant.isParentChoose) {
            val childDetails = SharedPreference.getChildDetails(this)
            isAccessToken = childDetails?.access_token
            isToolBarTitle = childDetails?.name.toString()
            isToolBarDescription = childDetails?.standard_name + " - " + childDetails?.section_name
        } else {
            val isStaffDetails = SharedPreference.getStaffDetails(this)
            isAccessToken = isStaffDetails?.access_token
            isToolBarTitle = Constant.isSelectedMenuName
            isToolBarDescription = isStaffDetails?.school_name.toString()
        }

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblStudentName.text = isToolBarTitle
//        binding.lblHeaderTitle.text= Constant.isSelectedMenuName
//        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblStudentSection.text = isToolBarDescription

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        appViewModel?.isGetBusList?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    if (response.data.isNotEmpty()) {

                        binding.rvBusList.visibility = View.VISIBLE
                        binding.lytList.visibility = View.GONE
                        isLoadBusList(response.data)
                    } else {
                        binding.rvBusList.visibility = View.GONE
                        binding.lytList.visibility = View.VISIBLE
                        binding.txtNoData.text = getString(R.string.no_data_found)
                    }
                } else {

                    binding.rvBusList.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.text = response.message
                }
            } else {
                binding.rvBusList.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text = getString(R.string.Something_went_wrong_Please_try_again)
            }
        }
    }

    private fun isLoadBusList(newData: List<BusListData>?) {
        mAdapter = BusListAdapter(newData, this, this, Constant.isShimmerViewDisable,isVendor)
        binding.rvBusList.adapter = mAdapter
    }

    private fun isGetBusList() {
        mAdapter = BusListAdapter(null, this, this, Constant.isShimmerViewShow,isVendor)

        binding.rvBusList.layoutManager = LinearLayoutManager(this)
        binding.rvBusList.isNestedScrollingEnabled = false
        binding.rvBusList.adapter = mAdapter

        appViewModel!!.isGetBusList(isAccessToken!!, this)

    }

    override fun onClick(p0: View?) {

    }

    override fun onResume() {
        super.onResume()
        isGetBusList()
    }

    override fun OnBusClick(data: BusListData) {
        val intent = Intent(this, LiveBusTracking::class.java)
        intent.putExtra("bus_data", data)
        startActivity(intent)
    }

    override fun onCustomClick(data: BusListData, status: String) {
        val intent = Intent(this, LiveBusTracking::class.java)
        intent.putExtra("bus_data", data)
        intent.putExtra("status", status)
        startActivity(intent)
    }

}
