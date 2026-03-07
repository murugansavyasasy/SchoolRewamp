package com.vs.schoolmessenger.School.Hostel

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Adapter.PreviewStaffLeaveRequest.StaffLeaveHistory
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveHistory.GetStaffLeaveHistory
import com.vs.schoolmessenger.School.Hostel.Adapter.RoomAvailability.FloorWiseRoomAvailability
import com.vs.schoolmessenger.School.Hostel.Listner.HostelClickListner
import com.vs.schoolmessenger.School.Hostel.Model.RoomAvailabaility.getFloorwiseAvailability
import com.vs.schoolmessenger.School.Hostel.Model.RoomAvailabaility.getHostellarDetails
import com.vs.schoolmessenger.School.Hostel.Model.RoomAvailabaility.getRoomAvailability
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference

import com.vs.schoolmessenger.databinding.HostelDashboardBinding


class HostelDashboard : BaseActivity<HostelDashboardBinding>(),
    View.OnClickListener, HostelClickListner {

    override fun getViewBinding(): HostelDashboardBinding {
        return HostelDashboardBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: FloorWiseRoomAvailability



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

        appViewModel?.getleaverequest?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    // Always load dummy data (Ignore API response completely)
                    val dummyData = getDummyFloorWiseRoomAvailabilityData()
                    if (dummyData.isNotEmpty()) {
                        binding.rcRoomAvailability.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE

                        isLoadRoomAvailability(dummyData)

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
                binding.lblErrorMessage.text = getString(R.string.Something_went_wrong_Please_try_again)
            }
        }
        isGetRoomAvailability()


        setDrawableBackgroundColor(binding.consHome,R.color.PrimaryColor)
        setDrawableBackgroundColor(binding.consStudent,R.color.green_3)
        setDrawableBackgroundColor(binding.consBedOccupied,R.color.purple_200)
        setDrawableBackgroundColor(binding.consPending,R.color.light_red_2)
        setDrawableBackgroundColor(binding.consOutPass,R.color.dark_blue_color)


    }

    private fun isLoadRoomAvailability(newData: List<getFloorwiseAvailability>?) {
        mAdapter = FloorWiseRoomAvailability(newData, this, this,Constant.isShimmerViewDisable)
        binding.rcRoomAvailability.adapter = mAdapter
    }

    fun setDrawableBackgroundColor(view: View, colorRes: Int) {
        val bgDrawable = view.background as? GradientDrawable
        bgDrawable?.setColor(ContextCompat.getColor(view.context, colorRes))
    }

    private fun isGetRoomAvailability() {
//        Constant.showLoading(this)
        mAdapter = FloorWiseRoomAvailability(null, this,this, Constant.isShimmerViewDisable)
        binding.rcRoomAvailability.layoutManager = LinearLayoutManager(this)
        binding.rcRoomAvailability.isNestedScrollingEnabled = false
        binding.rcRoomAvailability.adapter = mAdapter
//        appViewModel!!.getleaverequest(
//            isAccessToken!!, Constant.STAFF__, this
//        )

        val dummyData = getDummyFloorWiseRoomAvailabilityData()
        isLoadRoomAvailability(dummyData)
    }

    private fun getDummyFloorWiseRoomAvailabilityData(): List<getFloorwiseAvailability> {

        val groundFloorRooms = listOf(
            getRoomAvailability(
                id = "1",
                room_no = "G101",
                total_occupancy = "4",
                current_occupancy = "2",
                hostellarDetails = listOf(
                    getHostellarDetails(
                        id = "101",
                        hostellar_name = "Arun Kumar"
                    ),
                    getHostellarDetails(
                        id = "102",
                        hostellar_name = "Rahul Das"
                    )
                )
            ),
            getRoomAvailability(
                id = "2",
                room_no = "G102",
                total_occupancy = "4",
                current_occupancy = "3",
                hostellarDetails = listOf(
                    getHostellarDetails(
                        id = "103",
                        hostellar_name = "Priya Sharma"
                    ),
                    getHostellarDetails(
                        id = "104",
                        hostellar_name = "Sneha Reddy"
                    ),
                    getHostellarDetails(
                        id = "105",
                        hostellar_name = "Vikram Singh"
                    )
                )
            )
        )

        val firstFloorRooms = listOf(
            getRoomAvailability(
                id = "3",
                room_no = "F201",
                total_occupancy = "4",
                current_occupancy = "1",
                hostellarDetails = listOf(
                    getHostellarDetails(
                        id = "106",
                        hostellar_name = "Karthik Raj"
                    )
                )
            ),
            getRoomAvailability(
                id = "4",
                room_no = "F202",
                total_occupancy = "4",
                current_occupancy = "4",
                hostellarDetails = listOf(
                    getHostellarDetails(
                        id = "107",
                        hostellar_name = "Deepak Kumar"
                    ),
                    getHostellarDetails(
                        id = "108",
                        hostellar_name = "Manoj Patel"
                    ),
                    getHostellarDetails(
                        id = "109",
                        hostellar_name = "Ramesh Gupta"
                    ),
                    getHostellarDetails(
                        id = "110",
                        hostellar_name = "Sanjay Verma"
                    )
                )
            )
        )

        val secondFloorRooms = listOf(
            getRoomAvailability(
                id = "5",
                room_no = "S301",
                total_occupancy = "3",
                current_occupancy = "2",
                hostellarDetails = listOf(
                    getHostellarDetails(
                        id = "111",
                        hostellar_name = "Ajay Kumar"
                    ),
                    getHostellarDetails(
                        id = "112",
                        hostellar_name = "Naveen Raj"
                    )
                )
            )
        )

        return listOf(
            getFloorwiseAvailability(
                floor = "Ground Floor",
                details = groundFloorRooms
            ),
            getFloorwiseAvailability(
                floor = "First Floor",
                details = firstFloorRooms
            ),
            getFloorwiseAvailability(
                floor = "Second Floor",
                details = secondFloorRooms
            )
        )
    }



    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.lblErrorMessage.visibility = View.VISIBLE
            binding.imgNoDataFound.visibility = View.VISIBLE
            binding.rcRoomAvailability.visibility = View.GONE
        } else {
            binding.lblErrorMessage.visibility = View.GONE
            binding.imgNoDataFound.visibility = View.GONE
            binding.rcRoomAvailability.visibility = View.VISIBLE
        }
    }
}