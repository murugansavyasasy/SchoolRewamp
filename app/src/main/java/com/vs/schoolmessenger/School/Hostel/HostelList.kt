package com.vs.schoolmessenger.School.Hostel



import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Adapter.HotelList.HostelListAdapter
import com.vs.schoolmessenger.School.Hostel.Listner.HostelClickListner
import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility.getRoomAvailability
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.getHostelListData
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.selctedHotelDetails
import com.vs.schoolmessenger.Utils.Constant

import com.vs.schoolmessenger.Utils.SharedPreference

import com.vs.schoolmessenger.databinding.HostelListBinding

class HostelList : BaseActivity<HostelListBinding>(),
    View.OnClickListener, HostelClickListner {

    override fun getViewBinding(): HostelListBinding {
        return HostelListBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: HostelListAdapter
    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.rlaSpinner.visibility = View.GONE
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.hostel_selection)
        binding.toolbarLayout.lblSchoolName.text = getString(R.string.choose_your_hostel)
        
        appViewModel?.getHostelList?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    if (response.data.isNotEmpty()) {

                        binding.rcHostelAvailablility.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE
                        isLoadHostelList(response.data)
                        val count=response.data.size
                        val hostel=if(count==1){getString(R.string.hostel_available)} else{getString(R.string.hostels_available)}
                        binding.lblAvailableHostel.text="${count} ${hostel}"


                    } else {
                        binding.rcHostelAvailablility.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text = getString(R.string.no_data_found)
                        binding.lblAvailableHostel.text=""
                    }

                } else {

                    binding.rcHostelAvailablility.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message
                    binding.lblAvailableHostel.text=""
                }

            } else {

                binding.rcHostelAvailablility.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
                binding.lblAvailableHostel.text=""
            }
        }

        isGetHostelList()

    }


    private fun isLoadHostelList(newData: List<getHostelListData>?) {
        mAdapter =
            HostelListAdapter(newData,this,this, Constant.isShimmerViewDisable)
        binding.rcHostelAvailablility.adapter = mAdapter
    }

    private fun isGetHostelList() {
        mAdapter = HostelListAdapter(null,this, this, Constant.isShimmerViewShow)

        binding.rcHostelAvailablility.layoutManager = LinearLayoutManager(this)
        binding.rcHostelAvailablility.isNestedScrollingEnabled = false
        binding.rcHostelAvailablility.adapter = mAdapter

        appViewModel!!.isGetHostelList(isAccessToken!!, this)

    }


    override fun onClick(p0: View?) {
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
    }

    override fun onHostelClick(data: getHostelListData) {
        val intent = Intent(this, SchoolHostelDashboard::class.java)
        val saveSelectedHostelData = selctedHotelDetails(
            id=data.id,
            name=data.name,
            institute_id=data.institute_id,
            institute_name=data.institute_name,
            type=data.type,
            max_capacity=data.max_capacity,
            address=data.address,
        )
        //We are Saving all the data in Constant as List Here
        Constant.isSelectedHostelFromHostelListData = saveSelectedHostelData
        startActivity(intent)
    }

    override fun onRoomClick(data: getRoomAvailability) {
    }


}