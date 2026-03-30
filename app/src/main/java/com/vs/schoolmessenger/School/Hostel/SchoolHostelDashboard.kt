package com.vs.schoolmessenger.School.Hostel

import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Hostel.Adapter.RoomAvailability.FloorWiseRoomAvailability
import com.vs.schoolmessenger.School.Hostel.Listner.HostelClickListner
import com.vs.schoolmessenger.School.Hostel.Model.FragmentType
import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility.getFloorwiseAvailability
import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility.getRoomAvailability
import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility.isSelectedRoomData
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.getHostelListData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference

import com.vs.schoolmessenger.databinding.HostelDashboardBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class SchoolHostelDashboard : BaseActivity<HostelDashboardBinding>(),
    View.OnClickListener, HostelClickListner {

    override fun getViewBinding(): HostelDashboardBinding {
        return HostelDashboardBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    lateinit var mAdapter: FloorWiseRoomAvailability

    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    var isValidAcademicYear = false



    private var appViewModel: App? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )


        binding.toolbarLayout.lblInitialName.visibility= View.GONE
        binding.toolbarLayout.imgCall.visibility= View.GONE
        binding.toolbarLayout.lblClassAndRoomDetails.visibility= View.GONE
        binding.toolbarLayout.lblHostelName.visibility= View.GONE
        binding.toolbarLayout.lblName.visibility= View.GONE
        binding.toolbarLayout.imgSearchIcon.visibility= View.GONE
        binding.toolbarLayout.lblSchoolNameHostelName.visibility= View.VISIBLE


        binding.toolbarLayout.lblToday.visibility= View.VISIBLE
        binding.toolbarLayout.lblDate.visibility= View.VISIBLE
        binding.toolbarLayout.rlaSpinner.visibility= View.VISIBLE

        val currentDate = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE,MMMM dd"))

        binding.toolbarLayout.lblDate.text=currentDate
        binding.toolbarLayout.imgCall.visibility= View.GONE

        binding.toolbarLayout.lblSchoolNameHostelName.text=Constant.isSelectedHostelFromHostelListData?.name?:""
        binding.toolbarLayout.lblMenuName.text=Constant.isSelectedMenuName


        binding.consOutpassRequests.setOnClickListener(this)
        binding.consTotalStudent.setOnClickListener(this)
        binding.cardAttendanceRequired.setOnClickListener(this)

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)

        if (!isAcademicYear.isNullOrEmpty()) {
            isValidAcademicYear =
                isAcademicYear!!.any { it.current_academic_year }
            isAcademicYearId = isAcademicYear!![0].id
            isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
        }

        appViewModel?.getHostelDashboardDetails?.observe(this) { response ->
            if (response != null) {
                if (response.status) {

                    val data=response.data.getOrNull(0)?.floors?:emptyList()
                    val stats=response.data.getOrNull(0)?.stats

                    binding.lblTotalStudentCount.text=stats?.total_students?:""
                    binding.lblOutpassRequestCount.text=stats?.outpass_requests?:""

                    if (data.isNotEmpty()) {
                        binding.rcRoomAvailability.visibility = View.VISIBLE
                        binding.imgNoDataFound.visibility = View.GONE
                        binding.lblErrorMessage.visibility = View.GONE
                        isLoadRoomAvailability(data)

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

        setDrawableBackgroundColor(binding.consTotalStudent,R.color.green_3)
        setDrawableBackgroundColor(binding.consOutpassRequests,R.color.dark_brown_3)

    }
    private fun openBottomSheet(type: String) {
        val bottomSheet = BottomSheet.newInstance(type)
        bottomSheet.show(supportFragmentManager, "BottomSheet")
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
        mAdapter = FloorWiseRoomAvailability(null, this,this, Constant.isShimmerViewShow)
        binding.rcRoomAvailability.layoutManager = LinearLayoutManager(this)
        binding.rcRoomAvailability.isNestedScrollingEnabled = false
        binding.rcRoomAvailability.adapter = mAdapter

        appViewModel!!.isGetHostelDashboardDetails(
            isAccessToken!!, Constant.isSelectedHostelFromHostelListData?.id.toString(),isAcademicYearId.toString(), this
        )

    }

    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = NewAcademicYearAdapter(this, isAcademicYear)
        binding.toolbarLayout.isAcademicSpinner.adapter = adapter
        binding.toolbarLayout.isAcademicSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    val selectedOption = isAcademicYear!![position]
                    isAcademicYearId = selectedOption.id
                    isCurrentAcademicYear = selectedOption.current_academic_year
                    Constant.isUploadMarksSelectedAcademicID=isAcademicYearId.toString()
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                    )
                    isGetRoomAvailability()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }



    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.consOutpassRequests -> {
                openBottomSheet(FragmentType.OUTPASSREQUESTS.toString())
                Constant.isSelectedAcademicYear=isAcademicYearId.toString()
                Constant.isHostelName=Constant.isSelectedHostelFromHostelListData?.name?:""

            }
            R.id.cardAttendanceRequired -> {
                openBottomSheet(FragmentType.ATTENDANCEHISTORYHOSTEL.toString())
                Constant.isSelectedAcademicYear=isAcademicYearId.toString()
                Constant.isHostelName=Constant.isSelectedHostelFromHostelListData?.name?:""
            }


            //            R.id.consStudent -> {
//                openBottomSheet(FragmentType.FEEMANAGEMENT.toString())
//            }

//            R.id.consBedOccupied -> {
//                openBottomSheet(FragmentType.ROOMATTENDANCE.toString())
//            }

//            R.id.consPending -> {
//                openBottomSheet(FragmentType.PENDINGISSUES.toString())
//            }

//            R.id.consMessTimeTable -> {
//                openBottomSheet(FragmentType.MESSTIMETABLE.toString())
//            }


        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
    }

    override fun onHostelClick(data: getHostelListData) {
    }

    override fun onRoomClick(data: getRoomAvailability) {
        val saveSelectedHostelRoomData = isSelectedRoomData(
            id=data.id,
            number=data.number,
            current_occupancy=data.current_occupancy,
            max_occupancy=data.max_occupancy,
            total_beds=data.total_beds,
            students=data.students,
            isSelectedAcademicYear=isAcademicYearId,
        )
        //We are Saving all the data in Constant as List Here
        Constant.isSelectedHostelRoomData = saveSelectedHostelRoomData
        Constant.isHostelName=Constant.isSelectedHostelFromHostelListData?.name?:""


        openBottomSheet(FragmentType.ROOMATTENDANCE.toString())

    }
}