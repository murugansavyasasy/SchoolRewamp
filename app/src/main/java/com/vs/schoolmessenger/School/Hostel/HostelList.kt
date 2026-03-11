package com.vs.schoolmessenger.School.Hostel



import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Adapter.HotelList.HostelListAdapter
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.HostelListData
import com.vs.schoolmessenger.Utils.Constant

import com.vs.schoolmessenger.Utils.SharedPreference

import com.vs.schoolmessenger.databinding.HostelListBinding

class HostelList : BaseActivity<HostelListBinding>(),
    View.OnClickListener {

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
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        
        appViewModel?.getleaverequest?.observe(this) { response ->

            if (response != null) {

                if (response.status) {

                    val dummyData = getDummyHostelListData()

                    if (dummyData.isNotEmpty()) {

                        binding.rcRoomAvailability.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE

                        isLoadHostelList(dummyData)

                    } else {

                        binding.rcRoomAvailability.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.VISIBLE
                        binding.lblErrorMessage.text = getString(R.string.no_data_found)
                    }

                } else {

                    binding.rcRoomAvailability.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.VISIBLE
                    binding.imgNoDataFound.visibility = View.VISIBLE
                    binding.lblErrorMessage.text = response.message
                }

            } else {

                binding.rcRoomAvailability.visibility = View.GONE
                binding.lblErrorMessage.visibility = View.VISIBLE
                binding.imgNoDataFound.visibility = View.VISIBLE
                binding.lblErrorMessage.text =
                    getString(R.string.Something_went_wrong_Please_try_again)
            }
        }

        isGetAttendanceHistory()


    }


    private fun isLoadHostelList(newData: List<HostelListData>?) {
        mAdapter =
            HostelListAdapter(newData,this, Constant.isShimmerViewDisable)
        binding.rcRoomAvailability.adapter = mAdapter
    }

    private fun isGetAttendanceHistory() {

        mAdapter = HostelListAdapter(null, this, Constant.isShimmerViewDisable)

        binding.rcRoomAvailability.layoutManager = LinearLayoutManager(this)
        binding.rcRoomAvailability.isNestedScrollingEnabled = false
        binding.rcRoomAvailability.adapter = mAdapter

        val dummyData = getDummyHostelListData()
        isLoadHostelList(dummyData)
    }

    private fun getDummyHostelListData(): List<HostelListData> {

        val list = ArrayList<HostelListData>()

        list.add(
            HostelListData(
                HostelName = "Boys Hostel Block A",
                SchoolName = "St. Mary's Higher Secondary School",
                Place = "Chennai"
            )
        )

        list.add(
            HostelListData(
                HostelName = "Girls Hostel Block B",
                SchoolName = "St. Mary's Higher Secondary School",
                Place = "Chennai"
            )
        )

        list.add(
            HostelListData(
                HostelName = "Junior Boys Hostel",
                SchoolName = "St. Joseph Matriculation School",
                Place = "Coimbatore"
            )
        )

        list.add(
            HostelListData(
                HostelName = "Senior Girls Hostel",
                SchoolName = "St. Joseph Matriculation School",
                Place = "Madurai"
            )
        )

        list.add(
            HostelListData(
                HostelName = "Engineering Students Hostel",
                SchoolName = "ABC Engineering College",
                Place = "Salem"
            )
        )

        return list
    }


    override fun onClick(p0: View?) {
    }


}