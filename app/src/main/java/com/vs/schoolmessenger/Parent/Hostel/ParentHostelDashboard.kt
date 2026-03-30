package com.vs.schoolmessenger.Parent.Hostel

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelDetailedAttendance.HostelDetailedAttendanceAdpater
import com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelFeeDetails.HostelFeeDetail
import com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelInformation.HostelInfoAdapter
import com.vs.schoolmessenger.Parent.Hostel.Adapter.OutpassRequestList.OutpassRequestList
import com.vs.schoolmessenger.Parent.Hostel.Adapter.TodayAttendance.TodayAttendanceAdapter
import com.vs.schoolmessenger.Parent.Hostel.Listner.gatePassClickListner
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.DetailedAttendanceRecords.DayAttendance
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.DetailedAttendanceRecords.getHostelDetailedAttendance
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.FeeDetails
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.GatePass
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.HostelInfo
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.OutpassRequestData
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDetails.getParentHostelDetailsData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory.getStaffLeaveRequestHistory
import com.vs.schoolmessenger.School.Hostel.Adapter.HotelList.HostelListAdapter
import com.vs.schoolmessenger.Utils.Constant

import com.vs.schoolmessenger.Utils.SharedPreference

import com.vs.schoolmessenger.databinding.ParentHostelDashboardBinding
import java.util.Calendar


class ParentHostelDashboard : BaseActivity<ParentHostelDashboardBinding>(),
    View.OnClickListener, gatePassClickListner{

    override fun getViewBinding(): ParentHostelDashboardBinding {
        return ParentHostelDashboardBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private lateinit var mAdapter: HostelDetailedAttendanceAdpater
    lateinit var nAdapter: OutpassRequestList
    var outpassRequestList: List<OutpassRequestData> = emptyList()
    lateinit var oAdapter: HostelInfoAdapter
    lateinit var pAdapter: TodayAttendanceAdapter
    lateinit var sAdapter: HostelFeeDetail
    var parentHostelDetails :List<getParentHostelDetailsData>?= emptyList()
    private var isDialogShowing = false



    private var currentYear: Int = 0
    private var hostel_id: String?=null
    private var currentMonth: Int = 0
    private var isChildDetails: ChildDetails? = null



    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.lblMenuName.text=Constant.isSelectedMenuName

        binding.toolbarLayout.lblInitialName.visibility = View.VISIBLE
        binding.toolbarLayout.imgCall.visibility = View.GONE
        binding.toolbarLayout.lblClassAndRoomDetails.visibility = View.VISIBLE
        binding.toolbarLayout.lblHostelName.visibility = View.VISIBLE
        binding.toolbarLayout.lblName.visibility = View.VISIBLE
        binding.toolbarLayout.imgSearchIcon.visibility= View.GONE


        binding.toolbarLayout.lblToday.visibility = View.GONE
        binding.toolbarLayout.lblDate.visibility = View.GONE
        binding.toolbarLayout.rlaSpinner.visibility = View.GONE

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1

        val monthNames = listOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )
        val monthName = monthNames[currentMonth - 1]

        binding.lblCalendar.text = "$monthName $currentYear"

        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblSeeMore.setOnClickListener(this)
        binding.lblCalendar.setOnClickListener(this)
        binding.lblApplyNewOutpassRequest.setOnClickListener(this)

        binding.toolbarLayout.lblName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblInitialName.text = Constant.getInitials(isChildDetails?.name ?: "")
        binding.toolbarLayout.lblClassAndRoomDetails.text =
            "${ isChildDetails?.standard_name} - ${isChildDetails?.section_name} "

        binding.toolbarLayout.lblHostelName.text = parentHostelDetails?.firstOrNull()?.hostel_name

        isGetParentHostelDetails()

        appViewModel?.parentHotelDetails?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    if (response.data.isNotEmpty()) {
                        parentHostelDetails=response.data
                        hostel_id=response.data.firstOrNull()?.hostel_id?:""

                        isGetHostelDashBoardDetails(hostel_id.toString())


                        binding.toolbarLayout.lblClassAndRoomDetails.text =
                            "${ isChildDetails?.standard_name} - ${isChildDetails?.section_name} , ${parentHostelDetails?.firstOrNull()?.floor_name} - ${getString(R.string.room_no)} : ${parentHostelDetails?.firstOrNull()?.room_no}"

                        binding.toolbarLayout.lblHostelName.text = parentHostelDetails?.firstOrNull()?.hostel_name

                    }
                    else {
                        showEntireParentHostelDashBoardNoData(getString(R.string.no_data_found))
                        Constant.showParentDataValidation(getString(R.string.Oops),getString(R.string.no_data_found),this)

                    }
                } else {
                    showEntireParentHostelDashBoardNoData(response.message)
                    Constant.showParentDataValidation(getString(R.string.Oops),response.message,this)

                }
            }
            else {
                showEntireParentHostelDashBoardNoData(getString(R.string.something_went_wrong_please_try_again_later))
                Constant.showParentDataValidation(getString(R.string.Oops),getString(R.string.Something_went_wrong_Please_try_again),this)

            }
        }

        appViewModel?.parentHotelDashBoard?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {

                if (response.status) {
                    val data=response.data.getOrNull(0)
                    val attendance_details_data=data?.attendance_details?:emptyList()
                    val out_pass_requests=data?.out_pass_requests?:emptyList()
                    val hostel_info=data?.hostel_info?:emptyList()
                    val gate_pass=data?.gate_pass?:emptyList()
                    val fee_details=data?.fee_details?:emptyList()
                    val today_attendance=data?.today_attendance?:emptyList()

                    if (data != null) {
                        showEntireParentHostelDashBoardInfo()// here initially  i made all visible check and gone below

                        //Entire Attendance
                        if (attendance_details_data.isNotEmpty()){
                            //  Setup Header
                            setupHeader(attendance_details_data.firstOrNull()?.sessions?:emptyList())
                            //  Load Data
                            isLoadAttendance(attendance_details_data.firstOrNull()?.days?:emptyList())
                        }

                        else{
                            showEntireAttendanceNoData(getString(R.string.no_data_found))
                        }

                            //Outpass Check
                        if (out_pass_requests.isNotEmpty()){
                            isLoadOutpassRequest(out_pass_requests)
                        }
                        else{
                            binding.lblSeeMore.visibility=View.GONE
                            binding.view0.visibility=View.GONE
                            binding.frmOutpassDetails.visibility= View.GONE
                        }

                        //Hostel check
                        if (hostel_info.isNotEmpty()){
                            isLoadHotelInformation(hostel_info)
                        }
                        else{
                            showHostelInfoNoData(getString(R.string.no_data_found))
                        }


                        //Today Attendance Check
                        if (today_attendance.isNotEmpty()){
                            isLoadTodayAttendance(today_attendance)
                        }
                        else{
                            binding.cardTodayAttendance.visibility=View.GONE
                        }

                        //Hostel Fee Pending check
                        if (fee_details.isNotEmpty()){
                            isLoadPendingFeeCollection(fee_details)
                        }
                        else{
                            showPendingFeeCollectionsNoData(getString(R.string.no_data_found))
                        }

                        //Gate Pass Check
                        if (gate_pass.isNotEmpty()){
                            setSingleLineDashes(binding.hostelGatePass.txtDashLine)
                            isLoadGatePass(gate_pass)
                        }
                        else{
                            binding.cardGatePass.visibility=View.GONE
                        }

                    }
                    else {
                        showEntireParentHostelDashBoardNoData(getString(R.string.no_data_found))
                    }

                }
                else {
                    showEntireParentHostelDashBoardNoData(response.message)

                }

            } else {
                showEntireParentHostelDashBoardNoData(getString(R.string.Something_went_wrong_Please_try_again))

            }
        }

    }

    private fun showEntireAttendanceNoData(message: String) {
        binding.lnrEntireAcademicDetails.visibility = View.GONE
        binding.lblErrorMessage.visibility = View.VISIBLE
        binding.imgNoDataFound.visibility = View.VISIBLE
        binding.lblErrorMessage.text = message
    }
    private fun showEntireAttendanceDetails() {
        binding.lnrEntireAcademicDetails.visibility = View.VISIBLE
        binding.lblErrorMessage.visibility = View.GONE
        binding.imgNoDataFound.visibility = View.GONE
    }

    private fun showPendingFeeCollectionsNoData(message: String) {
        binding.rcPendingFeeCollections.visibility = View.GONE
        binding.imgPendingFeeCollectionsNoDataFound.visibility = View.VISIBLE
        binding.lblPendingFeeCollectionsErrorMessage.visibility = View.VISIBLE
        binding.lblPendingFeeCollectionsErrorMessage.text = message
    }

    private fun showPendingFeeCollections(){
        binding.rcPendingFeeCollections.visibility = View.VISIBLE
        binding.imgPendingFeeCollectionsNoDataFound.visibility = View.GONE
        binding.lblPendingFeeCollectionsErrorMessage.visibility = View.GONE
    }

    private fun showHostelInfoNoData(message: String) {
        binding.rcHostelInformation.visibility = View.GONE
        binding.imgHostelInfoNoDataFound.visibility = View.VISIBLE
        binding.lblHostelInfoErrorMessage.visibility = View.VISIBLE
        binding.lblHostelInfoErrorMessage.text = message
    }

    private fun showHostelInfo(){
        binding.rcHostelInformation.visibility = View.VISIBLE
        binding.imgHostelInfoNoDataFound.visibility = View.GONE
        binding.lblHostelInfoErrorMessage.visibility = View.GONE
    }

    private fun showEntireParentHostelDashBoardNoData(message: String) {
        binding.cardHostelDetails.visibility = View.GONE
        binding.cardFeeDetails.visibility = View.GONE
        binding.cardMonthlyStats.visibility = View.GONE
        binding.cardOutpassDetails.visibility = View.GONE
        binding.cardGatePass.visibility = View.GONE
        binding.cardTodayAttendance.visibility=View.GONE
        binding.imgEntireParenthHostelDashboardNoDataFound.visibility = View.VISIBLE
        binding.lblEntireParenthHostelDashboardErrorMessage.visibility = View.VISIBLE
        binding.lblEntireParenthHostelDashboardErrorMessage.text = message
    }

    private fun showEntireParentHostelDashBoardInfo(){
        binding.cardTodayAttendance.visibility=View.VISIBLE
        binding.cardHostelDetails.visibility = View.VISIBLE
        binding.cardFeeDetails.visibility = View.VISIBLE
        binding.cardMonthlyStats.visibility = View.VISIBLE
        binding.cardOutpassDetails.visibility = View.VISIBLE
        binding.cardGatePass.visibility = View.VISIBLE
        binding.imgEntireParenthHostelDashboardNoDataFound.visibility = View.GONE
        binding.lblEntireParenthHostelDashboardErrorMessage.visibility = View.GONE
    }

    private fun isLoadOutpassRequest(newData: List<OutpassRequestData>) {
        if (newData.size>0){

            binding.rcOutpassRequest.visibility = View.VISIBLE
            binding.view0.visibility = View.VISIBLE
            binding.frmOutpassDetails.visibility= View.VISIBLE

            if (newData.size > 3) {
                outpassRequestList=newData
                binding.lblSeeMore.visibility = View.VISIBLE
            } else {
                binding.lblSeeMore.visibility = View.GONE
            }

            // Show only first 3 items if size > 3
            val displayList = if (newData.size > 3) { newData.take(3) }
            else { newData }


            nAdapter = OutpassRequestList(
                displayList,
                this,
                this,
                Constant.isShimmerViewDisable
            )

            binding.rcOutpassRequest.adapter = nAdapter
        }
        else{
            binding.lblSeeMore.visibility=View.GONE
            binding.view0.visibility=View.GONE
            binding.frmOutpassDetails.visibility= View.GONE
        }
    }

    private fun isGetOutpassRequest() {
        nAdapter = OutpassRequestList(null, this,this,Constant.isShimmerViewShow)
        binding.rcOutpassRequest.layoutManager = LinearLayoutManager(this)
        binding.rcOutpassRequest.isNestedScrollingEnabled = true
        binding.rcOutpassRequest.adapter = nAdapter
    }


    private fun isGetParentHostelDetails() {
        Constant.showLoading(this)
        appViewModel!!.isGetParentHostelDetails(isAccessToken!!, this)

        isGetHotelInformation()
        isGetAttendance()
        isGetOutpassRequest()
        isGetTodayAttendance()
        isGetPendingFeeCollection()
    }


    private fun isGetHostelDashBoardDetails(hostel_id : String,) {
        Constant.showLoading(this)
        appViewModel!!.isGetParentHostelDashboard(isAccessToken!!,hostel_id.toIntOrNull()?:0,currentYear,currentMonth,this)
    }

    private fun isLoadHotelInformation(Data: List<HostelInfo>)
    {
        if (Data.size>0){
            showHostelInfo()
            oAdapter = HostelInfoAdapter(Data, this, Constant.isShimmerViewDisable)
            binding.rcHostelInformation.adapter = oAdapter
        }
        else{
            showHostelInfoNoData(getString(R.string.no_data_found))
        }
    }


    private fun isLoadGatePass(Data: List<GatePass>)
    {
        if (Data.size>0){

            val data=Data.firstOrNull()
            binding.cardGatePass.visibility=View.VISIBLE
            binding.hostelGatePass.lblReason.text=data?.reason?:""
            binding.hostelGatePass.tvRoomId.text=data?.room_no?:""
            binding.hostelGatePass.lblPersonName.text=isChildDetails?.name ?: ""
            binding.hostelGatePass.lblSessionNo.text= Constant.getInitials(isChildDetails?.name ?: "")
            binding.hostelGatePass.lblStudentRollNumber.text= data?.admission_no?:""

            val input = data?.fromdate_todate?:" - "
            val parts = input.split(" - ")
            val from = parts.getOrNull(0)?.trim() ?: ""
            val to = parts.getOrNull(1)?.trim() ?: ""

            binding.hostelGatePass.tvExitTime.text= Constant.getOnlyTime(data?.request_time?:"")
            binding.hostelGatePass.tvValidFrom.text= Constant.convertDateFormatType2(from)
            binding.hostelGatePass.tvValidTo.text= Constant.convertDateFormatType2(to)
            binding.hostelGatePass.tvBlockName.text=data?.floor_no?:""
            binding.hostelGatePass.tvlblAuthorizedBy.text=data?.action_by?:""

        }
        else{
            binding.cardGatePass.visibility=View.GONE
        }
    }

    fun setSingleLineDashes(textView: TextView) {

        textView.post {

            val totalWidth = textView.width
            val dash = "— "
            val dashWidth = textView.paint.measureText(dash)
            val count = (totalWidth / dashWidth).toInt()
            val builder = StringBuilder()
            for (i in 0 until count) {
                builder.append(dash)
            }
            textView.text = builder.toString()
            textView.setTextColor(
                ContextCompat.getColor(textView.context, R.color.light_gray_15)
            )
        }
    }


    private fun isGetHotelInformation()
    {
        oAdapter = HostelInfoAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcHostelInformation.layoutManager = LinearLayoutManager(this)
        binding.rcHostelInformation.adapter = oAdapter
    }

    private fun isGetTodayAttendance()
    {
        pAdapter = TodayAttendanceAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcTodayAttendance.layoutManager = LinearLayoutManager(this)
        binding.rcTodayAttendance.adapter = pAdapter
    }

    private fun isGetPendingFeeCollection()
    {
        sAdapter = HostelFeeDetail(this, Constant.isShimmerViewShow)
        binding.rcPendingFeeCollections.layoutManager = LinearLayoutManager(this)
        binding.rcPendingFeeCollections.adapter = sAdapter
    }


    private fun isLoadTodayAttendance(data: List<String>) {

        if (data.size>0){

            binding.cardTodayAttendance.visibility=View.VISIBLE
            pAdapter = TodayAttendanceAdapter(
                data,
                this,
                false,
            )
            binding.rcTodayAttendance.layoutManager = LinearLayoutManager(this)
            binding.rcTodayAttendance.adapter = pAdapter

        }
        else{
            binding.cardTodayAttendance.visibility=View.GONE
        }

    }

    private fun isLoadPendingFeeCollection(data: List<FeeDetails>) {

        if (data.isNotEmpty()) {

            showPendingFeeCollections()

            sAdapter = HostelFeeDetail(
                this,
                true
            )

            binding.rcPendingFeeCollections.layoutManager = LinearLayoutManager(this)
            binding.rcPendingFeeCollections.adapter = sAdapter

            if (::sAdapter.isInitialized) {
                sAdapter.updateData(data)
            }

        } else {

            showPendingFeeCollectionsNoData(getString(R.string.no_data_found))
        }
    }

    private fun isGetAttendance() {

        mAdapter = HostelDetailedAttendanceAdpater(
            null,
            this,
            true,
            binding.headerScroll
        )

        binding.rcDetailedAttendanceRecords.layoutManager = LinearLayoutManager(this)
        binding.rcDetailedAttendanceRecords.adapter = mAdapter

        //Header scroll sync
        binding.headerScroll.viewTreeObserver.addOnScrollChangedListener {
            mAdapter.syncScroll(binding.headerScroll.scrollX)
        }

    }

    private fun isLoadAttendance(list: List<DayAttendance>) {
        if (list.size>0){
            showEntireAttendanceDetails()
            mAdapter = HostelDetailedAttendanceAdpater(
                list,
                this,
                false,
                binding.headerScroll
            )

            binding.rcDetailedAttendanceRecords.layoutManager = LinearLayoutManager(this)
            binding.rcDetailedAttendanceRecords.adapter = mAdapter
        }
        else{
            showEntireAttendanceNoData(getString(R.string.no_data_found))
        }
    }

    private fun setupHeader(sessions: List<String>) {
        if (sessions.size>0){
            showEntireAttendanceDetails()

            val columnWidth = AttendanceUIConfig.columnWidth(this)
            val paddingH = AttendanceUIConfig.paddingH(this)
            val paddingV = AttendanceUIConfig.paddingV(this)

            binding.headerContainer.removeAllViews()

            sessions.forEach {

                val tv = TextView(this)
                tv.layoutParams = LinearLayout.LayoutParams(
                    columnWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                tv.setPadding(paddingH, paddingV, paddingH, paddingV)
                tv.text = it
                tv.gravity = Gravity.CENTER
                tv.setTextAppearance(this, R.style.CustomTextStylePoppinsBold)

                binding.headerContainer.addView(tv)
            }
        }
        else{
            showEntireAttendanceNoData(getString(R.string.no_data_found))
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.lblSeeMore->{
                val intent = Intent(this, ParentOutpassrequestList::class.java)
                intent.putExtra("OUTPASS_LIST", ArrayList(outpassRequestList))
                intent.putExtra("PARENT_HOSTEL_LIST", ArrayList(parentHostelDetails))

                startActivity(intent)
            }
            R.id.lblApplyNewOutpassRequest->{
                val intent = Intent(this, ParentHostelOutpassApply::class.java)
                intent.putExtra("PARENT_HOSTEL_LIST", ArrayList(parentHostelDetails))
                startActivity(intent)
            }

            R.id.lblCalendar->{
               showCalendarPopup()
            }
        }
    }

    private fun showCalendarPopup() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.calender_popup)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val margin = resources.getDimensionPixelSize(R.dimen.twenty)
        val params = dialog.window?.attributes
        params?.width = resources.displayMetrics.widthPixels - (margin * 2)
        params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params
        dialog.window?.setGravity(Gravity.CENTER)

        val year2 = dialog.findViewById<TextView>(R.id.year2)
        val year3 = dialog.findViewById<TextView>(R.id.year3)

        year2.text = (currentYear - 1).toString()
        year3.text = currentYear.toString()

        var selectedYear = currentYear

        val yearViews = listOf(year2, year3)

        val months = mapOf(
            R.id.jan to 1, R.id.feb to 2, R.id.mar to 3,
            R.id.apr to 4, R.id.may to 5, R.id.jun to 6,
            R.id.jul to 7, R.id.aug to 8, R.id.sep to 9,
            R.id.oct to 10, R.id.nov to 11, R.id.dec to 12
        )

        val monthNames = listOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )

        var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

        val monthViews = months.keys.map { dialog.findViewById<TextView>(it) }

        fun selectItem(selectedView: TextView, allViews: List<TextView>) {
            allViews.forEach {
                it.background = ContextCompat.getDrawable(this, R.drawable.bg_box)
                it.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            }

            selectedView.background =
                ContextCompat.getDrawable(this, R.drawable.primary_colour_bg_radius_bg)
            selectedView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        }

        fun updateMonthVisibility(selectedYear: Int) {

            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

            monthViews.forEachIndexed { index, textView ->
                val monthNumber = index + 1

                if (selectedYear == currentYear) {
                    textView.visibility =
                        if (monthNumber <= currentMonth) View.VISIBLE else View.GONE
                } else {
                    textView.visibility = View.VISIBLE
                }
            }
        }

        yearViews.forEach { view ->
            view.setOnClickListener {
                selectItem(view, yearViews)
                selectedYear = view.text.toString().toInt()

                updateMonthVisibility(selectedYear)
            }
        }

        monthViews.forEach { view ->
            view.setOnClickListener {
                selectItem(view, monthViews)
                selectedMonth = months[view.id] ?: 1
            }
        }

        year3.post {
            selectItem(year3, yearViews)
            updateMonthVisibility(currentYear)
        }

        monthViews[selectedMonth - 1].post {
            selectItem(monthViews[selectedMonth - 1], monthViews)
        }

        dialog.findViewById<Button>(R.id.btnClose).setOnClickListener {

            val monthName = monthNames[selectedMonth - 1]
            Log.d("CALENDAR", "Year: $selectedYear Month: $selectedMonth ($monthName)")
            currentMonth = selectedMonth
            currentYear = selectedYear
            binding.lblCalendar.text = "$monthName $selectedYear"
            isGetHostelDashBoardDetails(hostel_id.toString())
            dialog.dismiss()
        }
        dialog.show()
    }


    fun showGatepassDialog(
        activity: Activity,
        gatePass: OutpassRequestData
    ) {

        if (isDialogShowing || activity.isFinishing || activity.isDestroyed) return
        isDialogShowing = true

        val dialogView =
            LayoutInflater.from(activity).inflate(R.layout.parent_hostel_gate_pass, null)

        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()

        val gatePassLayout = dialogView.findViewById<View>(R.id.hostel_gate_pass)

        val lblSessionNo = gatePassLayout.findViewById<TextView>(R.id.lblSessionNo)
        val lblPersonName = gatePassLayout.findViewById<TextView>(R.id.lblPersonName)
        val lblStudentRollNumber = gatePassLayout.findViewById<TextView>(R.id.lblStudentRollNumber)
        val tvExitTime = gatePassLayout.findViewById<TextView>(R.id.tvExitTime)
        val lblReason = gatePassLayout.findViewById<TextView>(R.id.lblReason)
        val tvRoomId = gatePassLayout.findViewById<TextView>(R.id.tvRoomId)
        val tvBlockName = gatePassLayout.findViewById<TextView>(R.id.tvBlockName)
        val tvValidFrom = gatePassLayout.findViewById<TextView>(R.id.tvValidFrom)
        val tvValidTo = gatePassLayout.findViewById<TextView>(R.id.tvValidTo)
        val tvAuthorizedBy = gatePassLayout.findViewById<TextView>(R.id.tvlblAuthorizedBy)

        val input = gatePass.fromdate_todate ?: " - "
        val parts = input.split(" - ")
        val from = parts.getOrNull(0)?.trim() ?: ""
        val to = parts.getOrNull(1)?.trim() ?: ""

        lblSessionNo.text = Constant.getInitials(parentHostelDetails?.firstOrNull()?.student_name ?: "")
        lblPersonName.text = parentHostelDetails?.firstOrNull()?.student_name ?: ""
        lblStudentRollNumber.text = parentHostelDetails?.firstOrNull()?.admission_no ?: ""
        tvExitTime.text = Constant.getOnlyTime(gatePass?.request_time?:"")
        lblReason.text = gatePass.reason ?: ""
        tvRoomId.text = parentHostelDetails?.firstOrNull()?.room_id ?: ""
        tvValidFrom.text = Constant.convertDateFormatType2(from)
        tvValidTo.text = Constant.convertDateFormatType2(to)
        tvAuthorizedBy.text = gatePass.action_by ?: ""
        tvBlockName.text = parentHostelDetails?.firstOrNull()?.floor_name ?: ""

        alertDialog.setOnDismissListener {
            isDialogShowing = false
        }

        gatePassLayout.setOnClickListener {
            alertDialog.dismiss()
            isDialogShowing = false

        }

    }


    override fun onGatePassClick(data: OutpassRequestData) {
        showGatepassDialog(this,data)
    }

}