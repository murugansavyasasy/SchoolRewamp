package com.vs.schoolmessenger.Parent.Hostel

import android.app.Dialog
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelDetailedAttendance.HostelDetailedAttendanceAdpater
import com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelFeeDetails.HostelFeeDetail
import com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelInformation.HostelInfoAdapter
import com.vs.schoolmessenger.Parent.Hostel.Adapter.OutpassRequestList.OutpassRequestList
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.DetailedAttendanceRecords.DayAttendance
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.DetailedAttendanceRecords.getHostelDetailedAttendance
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.FeeDetails
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.GatePass
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.HostelInfo
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.OutpassRequestData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.School.Hostel.Adapter.HotelList.HostelListAdapter
import com.vs.schoolmessenger.Utils.Constant

import com.vs.schoolmessenger.Utils.SharedPreference

import com.vs.schoolmessenger.databinding.ParentHostelDashboardBinding
import java.util.Calendar


class ParentHostelDashboard : BaseActivity<ParentHostelDashboardBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): ParentHostelDashboardBinding {
        return ParentHostelDashboardBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private lateinit var mAdapter: HostelDetailedAttendanceAdpater
    lateinit var nAdapter: OutpassRequestList
    var outpassRequestList: List<OutpassRequestData> = emptyList()
    lateinit var oAdapter: HostelInfoAdapter
    lateinit var sAdapter: HostelFeeDetail

    private var currentYear: Int = 0
    private var currentMonth: Int = 0



    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )


        binding.toolbarLayout.lblInitialName.visibility = View.VISIBLE
        binding.toolbarLayout.imgCall.visibility = View.VISIBLE
        binding.toolbarLayout.lblClassAndRoomDetails.visibility = View.VISIBLE
        binding.toolbarLayout.lblHostelName.visibility = View.VISIBLE
        binding.toolbarLayout.lblName.visibility = View.VISIBLE

        binding.toolbarLayout.lblToday.visibility = View.GONE
        binding.toolbarLayout.lblDate.visibility = View.GONE
        binding.toolbarLayout.rlaSpinner.visibility = View.GONE

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblSeeMore.setOnClickListener(this)
        binding.lblCalendar.setOnClickListener(this)
        binding.lblApplyNewOutpassRequest.setOnClickListener(this)

        isGetParentHostelDetails()



        appViewModel?.parentHotelDetails?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    if (response.data.isNotEmpty()) {
                        isGetHostelDashBoardDetails(response.data.firstOrNull()?.hostel_id?:"")
                    }
                    else {
                        showEntireParentHostelDashBoardNoData(getString(R.string.no_data_found))
                    }
                } else {
                    showEntireParentHostelDashBoardNoData(response.message)
                }
            }
            else {
                showEntireParentHostelDashBoardNoData(getString(R.string.something_went_wrong_please_try_again_later))
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


                        //Hostel Fee Pending check
                        if (fee_details.isNotEmpty()){
                            isLoadPendingFeeCollection(fee_details)
                        }
                        else{
                            showPendingFeeCollectionsNoData(getString(R.string.no_data_found))
                        }

                        //Gate Pass Check
                        if (gate_pass.isNotEmpty()){
                            setSingleLineDashes(binding.txtDashLine)
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
        binding.cardMonthlyStats.visibility = View.GONE
        binding.cardOutpassDetails.visibility = View.GONE
        binding.cardGatePass.visibility = View.GONE
        binding.imgEntireParenthHostelDashboardNoDataFound.visibility = View.VISIBLE
        binding.lblEntireParenthHostelDashboardErrorMessage.visibility = View.VISIBLE
        binding.lblEntireParenthHostelDashboardErrorMessage.text = message
    }

    private fun showEntireParentHostelDashBoardInfo(){
        binding.cardHostelDetails.visibility = View.VISIBLE
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
        nAdapter = OutpassRequestList(null, this, Constant.isShimmerViewShow)
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
            binding.lblReason.text=data?.reason?:""
            binding.tvRoomId.text=data?.room_no?:""
            binding.lblPersonName.text=data?.profile?:""
            binding.lblSessionNo.text= Constant.getInitials(data?.profile?:"")
            binding.lblStudentRollNumber.text= data?.admission_no?:""

            val input = data?.fromdate_todate?:" - "
            val parts = input.split(" - ")
            val from = parts.getOrNull(0)?.trim() ?: ""
            val to = parts.getOrNull(1)?.trim() ?: ""

            binding.tvExitTime.text= Constant.getOnlyTime(data?.request_time?:"")
            binding.tvValidFrom.text= Constant.convertDateFormatType2(from)
            binding.tvValidTo.text= Constant.convertDateFormatType2(to)
            binding.tvBlockName.text=data?.floor_no?:""

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

    private fun isGetPendingFeeCollection()
    {
        sAdapter = HostelFeeDetail(this, Constant.isShimmerViewShow)
        binding.rcPendingFeeCollections.layoutManager = LinearLayoutManager(this)
        binding.rcPendingFeeCollections.adapter = sAdapter
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

//    private fun isLoadPendingFeeCollection(Data: List<FeeDetails>)
//    {
//        if (Data.size>0){
//            showPendingFeeCollections()
//            sAdapter = HostelFeeDetail(this, Constant.isShimmerViewDisable)
//            binding.rcPendingFeeCollections.adapter = sAdapter
//        }
//        else{
//            showPendingFeeCollectionsNoData(getString(R.string.no_data_found))
//        }
//
//    }


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
                startActivity(intent)
            }
            R.id.lblApplyNewOutpassRequest->{
                val intent = Intent(this, ParentHostelOutpassApply::class.java)
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

        val btnClose = dialog.findViewById<Button>(R.id.btnClose)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}