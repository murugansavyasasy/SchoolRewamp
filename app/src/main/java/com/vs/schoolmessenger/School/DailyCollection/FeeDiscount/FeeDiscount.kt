package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.CommonScreens.SchoolList.NewAcademicYearAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList.Standard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.DateFilterHelper
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeeDiscountBinding
import android.widget.Toast
import com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Helper.FilterDropDownAdapter
import com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Helper.FilterOption
import com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Model.DiscountReport.DiscountReportData
import com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.adapter.FeeDiscountCategoryAdapter

class FeeDiscount : BaseActivity<FeeDiscountBinding>(), View.OnClickListener,
    OnDateSelectedListener {

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var countryId: String? = null

    // academic year
    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1
    var isCurrentAcademicYear = true
    var isValidAcademicYear = false
    private var lastLoadedYearId = -1

    // standard / section data from API
    private var standardList: List<Standard> = emptyList()

    // dropdown selections (default = "0" = All) -> used to skip duplicate API calls
    private var selectedTypeId = ALL
    private var selectedClassId = ALL
    private var selectedSectionId = ALL

    // discount type dropdown (loaded from API for the selected Type)
    // "0" = All (default, also used when the API has no data)
    private var selectedDiscountTypeId: String = ALL

    // discount types are fetched once on first load and afterwards ONLY when Type changes
    private var isDiscountTypeRequestedOnce = false

    // true when the discount type API failed: discount type, class, section and the
    // date filter are disabled and the fee discount API is not called
    private var isFiltersLocked = false

    // date filter (only sent when the toggle is ON)
    private var fromDate: String = ""
    private var toDate: String = ""
    private var fromDateMillis: Long = 0L
    private var selectedDateTarget: Int = 0

    private var mAdapter: FeeDiscountCategoryAdapter? = null

    companion object {
        private const val ALL = "0"
        private const val TYPE_DISCOUNT = "1"
        private const val TYPE_SUBSIDY = "2"
    }

    override fun getViewBinding(): FeeDiscountBinding {
        return FeeDiscountBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.tvFromDate.setOnClickListener(this)
        binding.tvToDate.setOnClickListener(this)

        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        countryId = SharedPreference.getCountryId(this)?.toString()

        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        binding.switchDateFilter.apply {
            thumbTintList = null
            trackTintList = null
            setThumbDrawable(
                androidx.appcompat.content.res.AppCompatResources
                    .getDrawable(context, R.drawable.switch_thumb_fee)
            )
            setTrackDrawable(
                androidx.appcompat.content.res.AppCompatResources
                    .getDrawable(context, R.drawable.switch_track_fee)
            )
        }

        val params =
            binding.toolbarLayout.lytTitleAndName.layoutParams as RelativeLayout.LayoutParams
        params.removeRule(RelativeLayout.START_OF)
        params.addRule(RelativeLayout.START_OF, R.id.rlaSpinner)
        binding.toolbarLayout.lytTitleAndName.layoutParams = params

        binding.toolbarLayout.rlaSpinner.visibility = View.VISIBLE
        binding.toolbarLayout.layoutCreateSlot.visibility = View.GONE
        binding.toolbarLayout.imgSearchToolBarforCreate.visibility = View.GONE

        binding.rcyFeeDiscount.layoutManager = LinearLayoutManager(this)
        binding.rcyFeeDiscount.isNestedScrollingEnabled = false
        mAdapter = FeeDiscountCategoryAdapter(emptyList())
        binding.rcyFeeDiscount.adapter = mAdapter

        // Type / Discount type / Class / Section
        // Type, Class, Section start with "All" (0); section disabled until a class is picked;
        // discount type starts with "All" (0) and gets the API values after its call
        loadTypeDropdown()
        loadDiscountTypeDropdown(emptyList())
        loadClassDropdown()
        loadSectionDropdown()

        setupDateToggle()
        observeStandardSection()
        observeDiscountType()
        observeFeeDiscount()

        // 1) academic year -> 2) standard/section -> 3) discount type -> 4) fee discount
        isAcademicYear = Constant.isAcademicYearList
        isLoadAcademicYear(isAcademicYear)
        if (!isAcademicYear.isNullOrEmpty()) {
            isValidAcademicYear = isAcademicYear!!.any { it.current_academic_year }
            isAcademicYearId = isAcademicYear!![0].id
            Constant.isUploadMarksSelectedAcademicID = isAcademicYearId.toString()
            isCurrentAcademicYear = isAcademicYear!![0].current_academic_year
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

                    if (selectedOption.id == lastLoadedYearId) return

                    isAcademicYearId = selectedOption.id
                    isCurrentAcademicYear = selectedOption.current_academic_year
                    Constant.isUploadMarksSelectedAcademicID = isAcademicYearId.toString()
                    lastLoadedYearId = isAcademicYearId
                    Log.d("DropdownMenu", "Academic year: ID = ${selectedOption.id}")

                    isGetStandardSection()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun isGetStandardSection() {
        Constant.showLoading(this)
        appViewModel!!.isGetStandardSection(isAccessToken!!, isAcademicYearId, this)
    }


    private fun observeStandardSection() {
        appViewModel!!.isStandardSectionList?.observe(this) { response ->
            standardList = if (response != null && response.status && !response.data.isNullOrEmpty()) {
                response.data
            } else {
                Toast.makeText(
                    this,
                    response?.message?.takeIf { it.isNotBlank() }
                        ?: getString(R.string.something_went_wrong_please_try_again_later),
                    Toast.LENGTH_SHORT
                ).show()
                emptyList()
            }

            // class & section go back to default (All); the date filter is NOT touched
            selectedClassId = ALL
            selectedSectionId = ALL
            loadClassDropdown()
            loadSectionDropdown()

            // next step: first load -> discount types, then fee discount.
            // Later academic year changes go straight to the report, EXCEPT when the discount
            // type API had failed (filters locked): then it is retried first.
            if (!isDiscountTypeRequestedOnce || isFiltersLocked) {
                isGetDiscountType()
            } else {
                isGetFeeDiscount()
            }
        }
    }

    private fun classOptions(): List<FilterOption> =
        listOf(FilterOption(ALL, getString(R.string.fd_all_classes))) +
                standardList.map { FilterOption(it.id.toString(), it.name ?: "") }

    private fun sectionOptions(): List<FilterOption> {
        val all = FilterOption(ALL, getString(R.string.fd_all_sections))
        if (selectedClassId == ALL) return listOf(all)
        val sections = standardList.firstOrNull { it.id.toString() == selectedClassId }
            ?.sections.orEmpty()
        return listOf(all) + sections.map { FilterOption(it.id.toString(), it.name ?: "") }
    }


    private fun isGetDiscountType() {
        isDiscountTypeRequestedOnce = true
        Constant.showLoading(this)
        appViewModel!!.isDiscountType(isAccessToken ?: "", selectedTypeId, this)
    }

    private fun observeDiscountType() {
        appViewModel!!.isDiscountTypeList?.observe(this) { response ->

            // ---- API failed (no response / status false) ----
            // lock discount type, class, section and the date filter, show the error with the
            // illustration and do NOT call the fee discount API
            if (response == null || !response.status) {
                val message = response?.message?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.something_went_wrong_please_try_again_later)

                isFiltersLocked = true
                loadDiscountTypeDropdown(emptyList())   // only "All" (0)
                applyFilterLock()
                Constant.hideLoading(this)
                ErrorMessage(message)
                return@observe
            }

            // ---- API ok ----
            isFiltersLocked = false

            // status true but empty list -> toast, dropdown only has "All", report still loads
            if (response.data.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.no_data_available), Toast.LENGTH_SHORT).show()
            }

            val options: List<FilterOption> =
                response.data.orEmpty().mapNotNull { t ->
                    t.id?.takeIf { it.isNotBlank() }
                        ?.let { FilterOption(it, t.discountName ?: "", t.type_name ?: "-") }
                }

            // "All" (0) is always the first value and the default selection
            loadDiscountTypeDropdown(options)
            applyFilterLock()

            // last step of the chain: exactly one fee discount call
            isGetFeeDiscount()
        }
    }

    /**
     * List = "All" (id 0, no type) + API values.
     * "All" is selected by default, so discount_type_id = 0 unless the user picks another one.
     */
    private fun loadDiscountTypeDropdown(options: List<FilterOption>) {
        val list = listOf(FilterOption(ALL, getString(R.string.fd_type_all), null)) + options

        // set BEFORE binding so the spinner's automatic first selection is treated as "same value"
        selectedDiscountTypeId = ALL

        bindDropdown(binding.isDiscountType, list, showType = true) { picked ->
            if (picked.id == selectedDiscountTypeId) return@bindDropdown
            selectedDiscountTypeId = picked.id
            isGetFeeDiscount()
        }
    }

    /** Enable / dim a view together. */
    private fun setEnabledDim(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        view.alpha = if (enabled) 1f else 0.5f
    }

    /**
     * isFiltersLocked = true  -> discount type, class, section, "Filter by given date" label,
     *                            its toggle and the From/To row are disabled (dimmed)
     * isFiltersLocked = false -> back to normal (section still needs a class to be enabled)
     * The Type dropdown stays enabled so the user can retry by changing Type.
     */
    private fun applyFilterLock() {
        val enabled = !isFiltersLocked
        setEnabledDim(binding.isDiscountType, enabled)
        setEnabledDim(binding.isClassSpinner, enabled)
        setEnabledDim(binding.isSectionSpinner, enabled && selectedClassId != ALL)
        setEnabledDim(binding.switchDateFilter, enabled)
        setEnabledDim(binding.tvFromDate, enabled)
        setEnabledDim(binding.tvToDate, enabled)
        binding.lblGivenDate.alpha = if (enabled) 1f else 0.5f
    }


    /** One generic setup for Type / Discount type / Class / Section. */
    private fun bindDropdown(
        spinner: Spinner,
        options: List<FilterOption>,
        showType: Boolean = false,
        onPicked: (FilterOption) -> Unit
    ) {
        val adapter = FilterDropDownAdapter(this, options, showType)
        adapter.selectedPosition = 0
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()
                onPicked(options[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadTypeDropdown() {
        val options = listOf(
            FilterOption(ALL, getString(R.string.fd_type_all)),
            FilterOption(TYPE_DISCOUNT, getString(R.string.discount)),
            FilterOption(TYPE_SUBSIDY, getString(R.string.subsidy))
        )
        bindDropdown(binding.isTypeSpinner, options) { picked ->
            if (picked.id == selectedTypeId) return@bindDropdown
            selectedTypeId = picked.id
            // Type changed -> reload discount types; the fee discount call follows from its observer
            isGetDiscountType()
        }
    }

    private fun loadClassDropdown() {
        setEnabledDim(binding.isClassSpinner, !isFiltersLocked)

        bindDropdown(binding.isClassSpinner, classOptions()) { picked ->
            if (picked.id == selectedClassId) return@bindDropdown
            selectedClassId = picked.id
            selectedSectionId = ALL                                // section resets to All
            loadSectionDropdown()
            isGetFeeDiscount()
        }
    }

    private fun loadSectionDropdown() {
        // section is enabled only when a specific class is selected (and filters are not locked)
        setEnabledDim(binding.isSectionSpinner, !isFiltersLocked && selectedClassId != ALL)

        bindDropdown(binding.isSectionSpinner, sectionOptions()) { picked ->
            if (picked.id == selectedSectionId) return@bindDropdown
            selectedSectionId = picked.id
            isGetFeeDiscount()
        }
    }


    private fun setupDateToggle() {
        // initially OFF -> date row hidden, no dates sent
        binding.switchDateFilter.isChecked = false
        binding.lytDateRange.visibility = View.GONE
        binding.viewDivider.visibility = View.GONE

        binding.switchDateFilter.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // default range: from = today - 1 year, to = today
                val (from, to) = DateFilterHelper.defaultRange()
                fromDate = from
                toDate = to
                fromDateMillis = DateFilterHelper.toMillis(from)
                binding.tvFromDate.text = Constant.convertToReadableDate1(from)
                binding.tvToDate.text = Constant.convertToReadableDate1(to)
                binding.lytDateRange.visibility = View.VISIBLE
                binding.viewDivider.visibility = View.VISIBLE
            } else {
                binding.lytDateRange.visibility = View.GONE
                binding.viewDivider.visibility = View.GONE
            }
            isGetFeeDiscount()
        }
    }

    override fun onDateSelected(date: String) {
        when (selectedDateTarget) {
            R.id.tvFromDate -> {
                if (date == fromDate) return
                fromDate = date
                fromDateMillis = DateFilterHelper.toMillis(date)
                binding.tvFromDate.text = Constant.convertToReadableDate1(date)
                // keep To >= From
                if (DateFilterHelper.toMillis(toDate) < fromDateMillis) {
                    toDate = date
                    binding.tvToDate.text = Constant.convertToReadableDate1(date)
                }
            }

            R.id.tvToDate -> {
                if (date == toDate) return
                toDate = date
                binding.tvToDate.text = Constant.convertToReadableDate1(date)
            }
        }
        isGetFeeDiscount()
    }


    private fun isGetFeeDiscount() {
        // never load the report while the discount type API is in a failed state
        if (isFiltersLocked) return

        val dateOn = binding.switchDateFilter.isChecked
        Constant.showLoading(this)

        appViewModel!!.isFeeDiscount(
            isAccessToken ?: "",
            isAcademicYearId.toString(),
            countryId,
            selectedClassId,          // 0 = all classes
            selectedTypeId,           // 0 = all, 1 = discount, 2 = subsidy
            selectedSectionId,        // 0 = all sections
            selectedDiscountTypeId,   // 0 = all discount types, otherwise the selected id
            if (dateOn) fromDate else null,
            if (dateOn) toDate else null,
            this
        )
    }

    private fun observeFeeDiscount() {
        appViewModel!!.isFeeDiscountDetails?.observe(this) { response ->
            Constant.hideLoading(this)

            if (response == null) {
                ErrorMessage(getString(R.string.something_went_wrong_please_try_again_later))
                return@observe
            }
            val data = response.data.firstOrNull()
            if (!response.status || data == null) {
                ErrorMessage(response.message ?: getString(R.string.no_data_available))
                return@observe
            }
            showReport(data)
        }
    }

    private fun showReport(data: DiscountReportData) {
        val categories = data.discountDetails.filter { it.discountData.isNotEmpty() }
        if (categories.isEmpty()) {
            ErrorMessage(getString(R.string.no_data_available))
            return
        }
        binding.tvTotalDiscount.text = data.total_discount_and_subsidy ?: ""
        binding.tvRecordsAndCategoriesCount.text = getString(
            R.string.fd_records_categories,
            data.student_data_count ?: "0",
            categories.size
        )
        binding.lblPaidFee.text = data.totalDiscount ?: ""
        binding.lblDiscountFee.text = data.total_subsidy ?: ""

        mAdapter?.submit(categories)
        ShowData()
    }

    fun ShowData() {
        binding.scrollFeeDiscount.visibility = View.VISIBLE
        binding.lytNoDataFound.visibility = View.GONE
    }

    fun ErrorMessage(errorMessage: String) {
        binding.scrollFeeDiscount.visibility = View.GONE
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = errorMessage
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.tvFromDate -> {
                selectedDateTarget = R.id.tvFromDate
                dailycollectionshowDatePickerDialog(
                    this, this,
                    isFromDate = true,
                    fromDateMillis = fromDateMillis,
                    preSelectedDate = fromDate
                )
            }

            R.id.tvToDate -> {
                selectedDateTarget = R.id.tvToDate
                dailycollectionshowDatePickerDialog(
                    this, this,
                    isFromDate = false,
                    fromDateMillis = fromDateMillis,
                    preSelectedDate = toDate
                )
            }
        }
    }
}