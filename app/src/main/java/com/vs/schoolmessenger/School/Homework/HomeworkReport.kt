package com.vs.schoolmessenger.School.Homework

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.AcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.StandardDropDownListAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SectionDropDownListAdapter
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HomeworkReportBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HomeworkReport : BaseActivity<HomeworkReportBinding>(),
    HomeWorkReportClickListener, OnDateSelectedListener, View.OnClickListener {

    override fun getViewBinding(): HomeworkReportBinding {
        return HomeworkReportBinding.inflate(layoutInflater)
    }

    var isFirstLoad = false
    var isAcademicYear: List<AcademicYear>? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    var isValidAcademicYear = false
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    private var isStaffDetails: StaffDetails? = null
    var isSection: List<Section>? = null
    var isGetStandard: List<Standard>? = null
    private var isHomeWorkReportDataData: List<HomeWorkReportData>? = null
    var mHomeWorkReportAdapter: HomeWorkReportAdapter? = null
    private var fullHomeworkList: List<HomeWorkReportData> = listOf()
    var isSectionId = -1
    var isSelectedDate = ""
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isTotalSelectedItem = 0
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    var isHomeWorkId = ""
    var isHomeWorkPosition = 0


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {

            Constant.hideKeyboardIfOpen(this)
            if (binding.search.visibility == View.VISIBLE) {
                binding.search.visibility = View.GONE
                binding.edtSearch.text.clear()

            } else {
                binding.search.visibility = View.VISIBLE
                binding.edtSearch.text.clear()
            }
        }

        binding.rytStartDate.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        binding.toolbarLayout.layoutCreateSlot.visibility = View.GONE
        binding.toolbarLayout.layoutCreateSlot.setOnClickListener {
            val intent = Intent(this, HomeWorkCreate::class.java)
            startActivity(intent)
        }

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        isSelectedDate = Constant.getCurrentDate()
        binding.txtStartDate.text = Constant.convertToReadableDate(isSelectedDate)
        binding.lblDay.text = getDayLabel(isSelectedDate)



        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)
        isValidAcademicYear = isAcademicYear?.any { it.current_academic_year == true } == true
        isAcademicYearId = isAcademicYear!![0].id
        isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
        isAcademicYearId = Constant.isCurrentAcademicYearId
        isGetStandardSection()


        appViewModel!!.isDeleteHomeWork?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@HomeworkReport)
                    mHomeWorkReportAdapter!!.removeItemAt(isHomeWorkPosition)
                    fetchHomeWorkReportData()
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }


        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                isGetStandard = response.data
                isGetStandard?.size?.let {
                    if (it > 0) {
                        binding.rytStandardDropDown.visibility = View.VISIBLE
                        binding.rytSectionDropDown.visibility = View.VISIBLE
                        isSectionId = isGetStandard!![0].sections[0].id
                        if (isGetStandard!!.get(0).sections.isNotEmpty()) {
                            isLoadStandard(isGetStandard)
                            isSection = isGetStandard!!.get(0).sections
                            binding.noDataFound.visibility = View.GONE
                            binding.lytNoDataFound.visibility = View.GONE
                            binding.line1.visibility = View.VISIBLE
                            binding.line2.visibility = View.VISIBLE
                        }
                    } else {
                        binding.rytStandardDropDown.visibility = View.GONE
                        binding.rytSectionDropDown.visibility = View.GONE
                        binding.rcyHomeWorkReport.visibility = View.GONE
                        binding.lytNoDataFound.visibility = View.VISIBLE
                        binding.noDataFound.visibility = View.VISIBLE
                        binding.noDataFound.text = response.message
                        binding.line1.visibility = View.GONE
                        binding.line2.visibility = View.GONE
                    }
                }
            }
        }


        appViewModel!!.isGetHomeWorkReport?.observe(this) { response ->
            if (response != null) {
                isFirstLoad = true
                if (response.status) {
                    binding.rcyHomeWorkReport.visibility = View.VISIBLE
                    binding.lytNoDataFound.visibility = View.GONE
                    binding.line1.visibility = View.VISIBLE
                    binding.line2.visibility = View.VISIBLE
                    val isHomeWorkReport = response.data
                    isHomeWorkReportDataData = isHomeWorkReport
                    fullHomeworkList = isHomeWorkReport
                    loadHomeWorkReportData(isHomeWorkReportDataData!!)
                } else {
                    binding.search.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.line1.visibility = View.GONE
                    binding.line2.visibility = View.GONE
                    binding.noDataFound.visibility = View.VISIBLE
                    binding.rcyHomeWorkReport.visibility = View.GONE
                    binding.lytNoDataFound.visibility = View.VISIBLE
                    binding.noDataFound.text = response.message
                }
            }
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase(Locale.ROOT)
                filterHomeWorkReport(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun isLoadAcademicYear(isAcademicYear: List<AcademicYear>?) {
        val adapter = AcademicYearAdapter(this, isAcademicYear)
        binding.isSpinner.adapter = adapter
        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                if (isFirstLoad) {
                    val selectedOption = isAcademicYear!![position]
                    isAcademicYearId = selectedOption.id
                    isCurrentAcademicYear = selectedOption.current_academic_year
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                    )

                    isGetStandardSection()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isLoadStandard(isStandard: List<Standard>?) {
        val adapter = StandardDropDownListAdapter(this, isStandard)
        binding.isSpinnerStandard.adapter = adapter
        binding.isSpinnerStandard.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    isStandard!![position]
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${isStandard[position].id}, Year = ${isStandard[position].name}"
                    )

                    isSectionId = isStandard[position].id
                    isSection = isStandard[position].sections
                    isLoadSection(isSection)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isLoadSection(isSection: List<Section>?) {
        val adapter = SectionDropDownListAdapter(this, isSection)
        binding.isSpinnerSection.adapter = adapter
        binding.isSpinnerSection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                    val selectedOption = isSection!![position]
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${isSection[position].id}, Year = ${isSection[position].name}"
                    )
                    isSectionId = selectedOption.id
                    fetchHomeWorkReportData()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun filterHomeWorkReport(query: String) {
        val lowerQuery = query.lowercase(Locale.getDefault())

        val filteredList = if (query.isEmpty()) {
            fullHomeworkList
        } else {
            fullHomeworkList.filter {
                it.title.lowercase(Locale.getDefault())
                    .contains(lowerQuery) || it.description.lowercase(Locale.getDefault())
                    .contains(lowerQuery) || it.subject_name.lowercase(Locale.getDefault())
                    .contains(lowerQuery)
            }
        }

        mHomeWorkReportAdapter = HomeWorkReportAdapter(this, filteredList, this, false)
        binding.rcyHomeWorkReport.layoutManager =
            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.rcyHomeWorkReport.setHasFixedSize(true)
        binding.rcyHomeWorkReport.adapter = mHomeWorkReportAdapter

        if (filteredList.isEmpty()) {
            binding.rcyHomeWorkReport.visibility = View.GONE
            binding.lytNoDataFound.visibility = View.VISIBLE
            binding.noDataFound.visibility = View.VISIBLE
            binding.noDataFound.text = getString(R.string.no_matching_homework_found)
        } else {
            binding.rcyHomeWorkReport.visibility = View.VISIBLE
            binding.lytNoDataFound.visibility = View.GONE
        }
    }

    private fun fetchHomeWorkReportData() {
        binding.rcyHomeWorkReport.visibility = View.VISIBLE
        mHomeWorkReportAdapter =
            HomeWorkReportAdapter(this, emptyList(), this, Constant.isShimmerViewShow)
        binding.rcyHomeWorkReport.layoutManager =
            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.rcyHomeWorkReport.setHasFixedSize(true)
        binding.rcyHomeWorkReport.isNestedScrollingEnabled = false
        binding.rcyHomeWorkReport.adapter = mHomeWorkReportAdapter
        appViewModel?.isGetHomeWorkReport(
            isAccessToken!!, isSectionId, isAcademicYearId, isSelectedDate, this
        )
    }

    private fun loadHomeWorkReportData(isHomeWorkReportDataDetails: List<HomeWorkReportData>) {
        if (isHomeWorkReportDataDetails.isNullOrEmpty()) {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.search.visibility = View.GONE

        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            binding.search.visibility = View.GONE


            binding.rcyHomeWorkReport.visibility = View.VISIBLE
            mHomeWorkReportAdapter = HomeWorkReportAdapter(
                this, isHomeWorkReportDataDetails, this, Constant.isShimmerViewDisable
            )
            binding.rcyHomeWorkReport.layoutManager =
                GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
            binding.rcyHomeWorkReport.setHasFixedSize(true)
            binding.rcyHomeWorkReport.isNestedScrollingEnabled = false
            binding.rcyHomeWorkReport.adapter = mHomeWorkReportAdapter
        }
    }

    private fun isGetStandardSection() {
        appViewModel!!.isGetStandardSection(isAccessToken!!.toString(), isAcademicYearId, this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDateSelected(date: String) {
        isSelectedDate = date
        binding.txtStartDate.text = Constant.convertToReadableDate1(date)
        val labelDay = getDayLabel(date)
        binding.lblDay.text = labelDay
        fetchHomeWorkReportData()
    }

//        @RequiresApi(Build.VERSION_CODES.O)
//    override fun onDateSelected(date: String) {
//        isSelectedDate = date
//        binding.txtStartDate.text = Constant.convertToReadableDate(date)
//            val (day, formattedDate) = Constant.getDayAndDateOnly2(binding.txtStartDate.text.toString())// 13 Monday
//            binding.lblDay.text = formattedDate
//        fetchHomeWorkReportData()
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayLabel(dateStr: String): String {
        // Match the input format: "03-11-2025"
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val selectedDate = LocalDate.parse(dateStr, formatter)
        val today = LocalDate.now()

        return when {
            selectedDate.isEqual(today) -> getString(R.string.today)
            selectedDate.isEqual(today.plusDays(1)) -> getString(R.string.tomorrow)
            selectedDate.isEqual(today.minusDays(1)) -> getString(R.string.yesterday)
            else -> selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        }
    }


    override fun onClickListener(data: HomeWorkReportData, anchorView: View, isPosition: Int) {
        isHomeWorkId = data.id
        isHomeWorkPosition = isPosition
        showEditDeletePopup(data, anchorView)
    }

    fun showEditDeletePopup(data: HomeWorkReportData, anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_delete, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        val layoutEdit = popupView.findViewById<LinearLayout>(R.id.layout_edit)
        val layoutDelete = popupView.findViewById<LinearLayout>(R.id.layout_delete)

        layoutEdit.setOnClickListener {
            Constant.isClickEdit = true
            val intent = Intent(this, HomeWorkCreate::class.java)
            intent.putExtra(Constant.homework_data, data)
            startActivity(intent)
            popupWindow.dismiss()
        }

        layoutDelete.setOnClickListener {
            showSendConfirmationDialog(false)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, 0, 10)
    }

    fun showSendConfirmationDialog(isHomeWorkUpdate: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)

        alertMessage.text = getString(R.string.are_you_sure_want_to_delete)


        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            val jsonObject = JsonObject()
            jsonObject.addProperty(APIKeyNames.id, isHomeWorkId)
            appViewModel?.isHomeWorkDelete(isAccessToken!!, jsonObject, this)

        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rytStartDate -> {
                showDatePickerDialogSelectedDate(this, isSelectedDate, this)
            }
        }
    }
}