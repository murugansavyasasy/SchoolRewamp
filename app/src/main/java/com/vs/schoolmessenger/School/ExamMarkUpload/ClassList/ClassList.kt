package com.vs.schoolmessenger.School.ExamMarkUpload.ClassList

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.RelativeLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ClassListBinding

class ClassList : BaseActivity<ClassListBinding>(), View.OnClickListener {

    override fun getViewBinding(): ClassListBinding {
        return ClassListBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    var isValidAcademicYear = false

    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter: ClassListAdapter
    private var isClassList: List<StandardSection>? = emptyList()

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        val params =
            binding.toolbarLayout.lytTitleAndName.layoutParams as RelativeLayout.LayoutParams// Get current layout params (RelativeLayout.LayoutParams)
        params.removeRule(RelativeLayout.START_OF)
        params.addRule(RelativeLayout.START_OF, R.id.rlaSpinner)
        binding.toolbarLayout.lytTitleAndName.layoutParams = params


        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)
        if (!isAcademicYear.isNullOrEmpty()) {
            isValidAcademicYear =
                isAcademicYear!!.any { it.current_academic_year == true }
            isAcademicYearId = isAcademicYear!![0].id
            isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
        }

        binding.toolbarLayout.rlaSpinner.visibility = View.VISIBLE
        binding.toolbarLayout.layoutCreateSlot.visibility = View.GONE
        binding.toolbarLayout.imgBack.setOnClickListener(this)


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName


        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name


        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            if (response != null) {
                if (response.status && response.data.isNotEmpty()) {
                    binding.toolbarLayout.imgSearchToolBarforCreate.visibility = View.VISIBLE
                    loadSectionStandard(response.data)
                    ShowData()
                } else {
                    binding.rcClassList.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBarforCreate.visibility = View.GONE
                    ErrorMessage(
                        response.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }

                binding.rytSearch1.visibility = View.GONE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)
            }
        }

        binding.toolbarLayout.imgSearchToolBarforCreate.setOnClickListener {
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)

            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtSearch1.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)

            }
        }

        binding.txtSearch1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
                Log.d("Search", s.toString())


            }
        })
    }

    private fun loadSectionStandard(data: List<Standard>) {

        val standardSectionList = mutableListOf<StandardSection>()

        for (standard in data) {
            for (section in standard.sections) {
                standardSectionList.add(
                    StandardSection(
                        standardId = standard.id.toString(),
                        standardName = standard.name,
                        sectionId = section.id.toString(),
                        sectionName = section.name
                    )
                )
            }
        }

        isClassList = standardSectionList //for search

        adapter = ClassListAdapter(standardSectionList, this, false)
        binding.rcClassList.apply {
            layoutManager = object : LinearLayoutManager(context) {
                override fun canScrollVertically() = false
            }
            adapter = this@ClassList.adapter
        }

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
                    Log.d(
                        "DropdownMenu",
                        "Clicked Standard Year: ID = ${selectedOption.id}, Year = ${selectedOption.year}, Current = ${selectedOption.current_academic_year}"
                    )

                    isGetStandardSection()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isGetStandardSection() {

        adapter = ClassListAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcClassList.apply {
            layoutManager = object : LinearLayoutManager(context) {
                override fun canScrollVertically() = false
            }
            adapter = this@ClassList.adapter
        }

        appViewModel!!.isGetStandardSection(isAccessToken!!, isAcademicYearId, this)
    }


    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            isClassList.orEmpty()
        } else {
            isClassList.orEmpty().filter { isSubList ->
                val fieldsToSearch = mutableListOf(
                    isSubList.standardName?.lowercase().orEmpty(),
                    isSubList.sectionName?.lowercase().orEmpty(),
                )

                searchWords.all { word ->
                    fieldsToSearch.any { field -> field.contains(word) }
                }
            }
        }

        // Update UI
        if (filteredList.isNotEmpty()) {
            ShowData()
            adapter.updateData(filteredList)
        } else {
            binding.rcClassList.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }

    fun ShowData() {
        binding.rcClassList.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
        binding.lblSelectClass.visibility = View.VISIBLE
        binding.lblClassDetail.visibility = View.VISIBLE
    }

    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
        binding.lblSelectClass.visibility = View.GONE
        binding.lblClassDetail.visibility = View.GONE
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}