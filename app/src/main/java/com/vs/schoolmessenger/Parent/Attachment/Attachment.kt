package com.vs.schoolmessenger.Parent.Attachment

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentDataReport
import com.vs.schoolmessenger.School.Attachment.OnAttachmentReportClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentAttachmentBinding
import androidx.core.view.isVisible
import androidx.core.view.isGone
import com.vs.schoolmessenger.Utils.TourDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Attachment : BaseActivity<ParentAttachmentBinding>(), View.OnClickListener,
    OnAttachmentReportClickListener {

    override fun getViewBinding(): ParentAttachmentBinding {
        return ParentAttachmentBinding.inflate(layoutInflater)
    }

    private var fromDateMillis: Long? = null
    private var toDateMillis: Long? = null
    private var originalAttachmentList = mutableListOf<AttachmentDataReport>()
    private var originalArchiveList = mutableListOf<AttachmentDataReport>()

    private var activeList = mutableListOf<AttachmentDataReport>()
    private var isArchiveMode = false

    private enum class ReadFilter {
        ALL, READ, UNREAD
    }

    private var currentReadFilter = ReadFilter.ALL

    private val apiDateFormat =
        SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())

    var mAttachmentReportAdapter: AttachmentAdapter? = null
    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private var currentSearchQuery: String = ""

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    var userDetails: UserDetails? = null

    private var isTourDialogShown = false

    override fun setupViews() {
        super.setupViews()
        // showTourIfNeeded()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)
        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)

        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)

            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )

            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }


        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.imgFilter.setOnClickListener(this)
        binding.lnrToDate.setOnClickListener(this)
        binding.lnrFromDate.setOnClickListener(this)
        binding.lblArchiveMsg.setOnClickListener(this)
        binding.imgClearFilter.setOnClickListener(this)
        binding.root.post {
            val finalName =
                Constant.isSelectedMenuName?.takeIf { it.isNotEmpty() } ?: menu_name ?: ""
            Log.d("NoticeBoard_HeaderFinal", "Setting headerview text: $finalName")
            binding.lblHeaderTitle.text = finalName
            binding.lblHeaderTitle.visibility = View.VISIBLE
        }

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.isVisible) {

                // 🔹 Hide everything
                binding.rytSearch1.visibility = View.GONE
                binding.imgFilter.visibility = View.GONE
                binding.lnrDatePicking.visibility = View.GONE
                binding.lnrFilterRead.visibility = View.GONE

                // 🔹 RESET FILTERS & SHOW ALL DATA
                resetAttachmentFiltersAndShowAll()

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu1.windowToken, 0)

            } else {
                // 🔹 Show search
                binding.rytSearch1.visibility = View.VISIBLE
                binding.imgFilter.visibility = View.VISIBLE

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.txtSearchMenu1, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblStudentSection.text =
            childDetails?.standard_name + " - " + childDetails?.section_name
        binding.linearlayout1.visibility = View.VISIBLE

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        binding.txtSearchMenu1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mAttachmentReportAdapter?.filter?.filter(s)
                if (s!!.isNotEmpty()) {
                    if (binding.isArchiveErrorMsg.isVisible) {
                        binding.isArchiveErrorMsg.visibility = View.GONE
                    }
                } else {
                    if (binding.isArchiveErrorMsg.isGone) {
                        binding.isArchiveErrorMsg.visibility = View.VISIBLE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.txtSearchMenu1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim().orEmpty()

                applyCombinedFilter()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.lnrFromDate.setOnClickListener { showFromDatePicker() }
        binding.lnrToDate.setOnClickListener {
            showToDatePicker()
        }

        appViewModel?.isAttachmentResponseArchive?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    if (response.data.isNotEmpty()) {
                        originalArchiveList.clear()
                        originalArchiveList.addAll(response.data)

                        isArchiveMode = true

                        originalArchiveList.forEach { it.is_archive = true }

                        activeList.addAll(originalArchiveList)

                        mAttachmentReportAdapter = AttachmentAdapter(
                            activeList,
                            this,
                            this,
                            Constant.isShimmerViewDisable,
                            binding.nomessage,
                            binding.txtNoData
                        )
                        applyCombinedFilter()
                        binding.recycleracademic.adapter = mAttachmentReportAdapter
                        binding.txtSearchMenu1.text.clear()
                        binding.isArchiveErrorMsg.visibility = View.GONE
                        binding.recycleracademic.visibility = View.VISIBLE
                        binding.nomessage.visibility = View.GONE
                        binding.txtNoData.visibility = View.GONE

                        if (mAttachmentReportAdapter!!.getCurrentListSize() > 0) {
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                            binding.txtSearchMenu1.text.clear()
                        } else {
                            binding.rytSearch1.visibility = View.GONE
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                            binding.txtSearchMenu1.text.clear()
                        }

                    } else {
                        binding.isArchiveErrorMsg.visibility = View.VISIBLE
                        binding.isArchiveErrorMsg.text = response.message
                        if (mAttachmentReportAdapter!!.getCurrentListSize() == 0) {
                            binding.txtNoData.visibility = View.GONE
                            binding.rytSearch1.visibility = View.GONE
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                            binding.txtSearchMenu1.text.clear()
                        } else {
                            // Set top margin to 15dp dynamically
                            val layoutParams =
                                binding.isArchiveErrorMsg.layoutParams as ViewGroup.MarginLayoutParams
                            val topMarginInDp = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                15f,
                                resources.displayMetrics
                            ).toInt()
                            layoutParams.topMargin = topMarginInDp
                            binding.isArchiveErrorMsg.layoutParams = layoutParams
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                        }
                    }
                } else {
                    binding.isArchiveErrorMsg.visibility = View.VISIBLE
                    binding.isArchiveErrorMsg.text = response.message

                    if (mAttachmentReportAdapter!!.getCurrentListSize() == 0) {
                        binding.txtNoData.visibility = View.GONE
                        binding.rytSearch1.visibility = View.GONE
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                        binding.txtSearchMenu1.text.clear()
                    } else {
                        // Set top margin to 15dp dynamically
                        val layoutParams =
                            binding.isArchiveErrorMsg.layoutParams as ViewGroup.MarginLayoutParams
                        val topMarginInDp = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            15f,
                            resources.displayMetrics
                        ).toInt()
                        layoutParams.topMargin = topMarginInDp
                        binding.isArchiveErrorMsg.layoutParams = layoutParams
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    }

                }
            } else {
                binding.isArchiveErrorMsg.visibility = View.VISIBLE
                binding.isArchiveErrorMsg.text =
                    getString(R.string.something_went_wrong_please_try_again_later)
                if (mAttachmentReportAdapter!!.getCurrentListSize() == 0) {
                    binding.txtNoData.visibility = View.GONE
                    binding.rytSearch1.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.txtSearchMenu1.text.clear()
                } else {
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                }
            }
        }

        appViewModel?.isAttachmentResponse?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val mobileNumber = SharedPreference.getMobileNumber(this)

                val jsonObject = JsonObject().apply {
                    addProperty(APIKeyNames.mobile_number, mobileNumber)
                    addProperty(APIKeyNames.activity, Constant.add_points_view_attachments)
                    addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                    addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                }
                appViewModel?.isAddRewardPoints(isAccessToken ?: "", jsonObject, this)

                binding.txtNoData.visibility = View.GONE
                binding.nomessage.visibility = View.GONE
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                binding.recycleracademic.visibility = View.VISIBLE
                originalAttachmentList.clear()
                originalAttachmentList.addAll(response.data)

                isArchiveMode = false

                activeList.clear()
                activeList.addAll(originalAttachmentList)

                mAttachmentReportAdapter = AttachmentAdapter(
                    activeList,
                    this,
                    this,
                    Constant.isShimmerViewDisable,
                    binding.nomessage,
                    binding.txtNoData
                )
                applyCombinedFilter()
                binding.recycleracademic.adapter = mAttachmentReportAdapter

                Log.d("Message Id Value Indication", msg_id.toString())
                if (fromNotification) {
                    scrollToMessageId(headerId)
                }
            } else {
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                showEmptyState(response?.message ?: getString(R.string.no_data_found))
            }
        }

        binding.lblAll.setOnClickListener {
            selectReadFilter(ReadFilter.ALL)
        }

        binding.lblUnread.setOnClickListener {
            selectReadFilter(ReadFilter.UNREAD)
        }

        binding.lblRead.setOnClickListener {
            selectReadFilter(ReadFilter.READ)
        }

        isGetAttachment()
    }

    private fun updateClearFilterVisibility() {
        binding.imgClearFilter.visibility =
            if (fromDateMillis != null || toDateMillis != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }


    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showFromDatePicker() {
        val todayCal = Calendar.getInstance()

        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth, 0, 0, 0)

                fromDateMillis = selectedCal.timeInMillis

                binding.txtFromDate.text =
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(selectedCal.time)

                // ❌ If From > To → reset To
                if (toDateMillis != null && fromDateMillis!! > toDateMillis!!) {
                    showToast(getString(R.string.from_date_cannot_be_after_to_date))
                    toDateMillis = null
                    binding.txtToDate.text = getString(R.string.to_date)
                }
                updateClearFilterVisibility()
                // 🔥 Re-apply filter
                applyCombinedFilter()
            },
            todayCal.get(Calendar.YEAR),
            todayCal.get(Calendar.MONTH),
            todayCal.get(Calendar.DAY_OF_MONTH)
        )

        // 🔹 Disable FUTURE dates
        dialog.datePicker.maxDate = todayCal.timeInMillis

        // 🔹 If To Date already selected → From ≤ To
        if (toDateMillis != null) {
            dialog.datePicker.maxDate =
                minOf(todayCal.timeInMillis, toDateMillis!!)
        }

        dialog.show()
    }

    private fun showToDatePicker() {
        val todayCal = Calendar.getInstance()

        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth, 23, 59, 59)

                toDateMillis = selectedCal.timeInMillis

                binding.txtToDate.text =
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(selectedCal.time)
                updateClearFilterVisibility()
                // 🔥 Re-apply filter
                applyCombinedFilter()
            },
            todayCal.get(Calendar.YEAR),
            todayCal.get(Calendar.MONTH),
            todayCal.get(Calendar.DAY_OF_MONTH)
        )

        // 🔹 Disable FUTURE dates
        dialog.datePicker.maxDate = todayCal.timeInMillis

        // 🔹 If From Date selected → To ≥ From
        if (fromDateMillis != null) {
            dialog.datePicker.minDate = fromDateMillis!!
        }

        dialog.show()
    }


    private fun selectReadFilter(filter: ReadFilter) {
        currentReadFilter = filter

        // Reset all backgrounds
        binding.lblAll.setBackgroundResource(R.drawable.gray_bg_radius)
        binding.lblUnread.setBackgroundResource(R.drawable.gray_bg_radius)
        binding.lblRead.setBackgroundResource(R.drawable.gray_bg_radius)

        // Highlight selected
        when (filter) {
            ReadFilter.ALL -> binding.lblAll.setBackgroundResource(R.drawable.bg_light_green_radious)
            ReadFilter.UNREAD -> binding.lblUnread.setBackgroundResource(R.drawable.bg_light_green_radious)
            ReadFilter.READ -> binding.lblRead.setBackgroundResource(R.drawable.bg_light_green_radious)
        }

        applyCombinedFilter()
    }

    private fun applyCombinedFilter() {

        var resultList = activeList.toList()

        // 🔹 DATE FILTER (ALL CASES)
        resultList = resultList.filter { item ->
            try {
                val parsedDate = apiDateFormat.parse(item.date) ?: return@filter false
                val itemMillis = parsedDate.time

                when {
                    fromDateMillis != null && toDateMillis != null ->
                        itemMillis in fromDateMillis!!..toDateMillis!!

                    fromDateMillis != null ->
                        itemMillis >= fromDateMillis!!

                    toDateMillis != null ->
                        itemMillis <= toDateMillis!!

                    else -> true
                }
            } catch (e: Exception) {
                false
            }
        }

        // 🔹 READ / UNREAD
        resultList = when (currentReadFilter) {
            ReadFilter.UNREAD -> resultList.filter { it.is_unread }
            ReadFilter.READ -> resultList.filter { !it.is_unread }
            ReadFilter.ALL -> resultList
        }

        // 🔹 SEARCH
        if (currentSearchQuery.isNotEmpty()) {
            val q = currentSearchQuery.lowercase(Locale.getDefault())
            resultList = resultList.filter {
                it.title?.lowercase()?.contains(q) == true ||
                        it.description?.lowercase()?.contains(q) == true ||
                        it.sent_by?.lowercase()?.contains(q) == true
            }
        }

        mAttachmentReportAdapter?.updateFilteredList(resultList)

        binding.recycleracademic.visibility =
            if (resultList.isEmpty()) View.GONE else View.VISIBLE
        binding.nomessage.visibility =
            if (resultList.isEmpty()) View.VISIBLE else View.GONE
        binding.txtNoData.visibility =
            if (resultList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun isGetAttachment() {
        binding.recycleracademic.visibility = View.VISIBLE
        mAttachmentReportAdapter =
            AttachmentAdapter(
                mutableListOf(),
                this,
                this,
                Constant.isShimmerViewShow
            )
        binding.recycleracademic.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycleracademic.adapter = mAttachmentReportAdapter
        binding.recycleracademic.isNestedScrollingEnabled = false


        appViewModel?.getAttachment(isAccessToken.orEmpty(), this)
    }

    private fun isGetAttachmentArchive() {
        appViewModel?.getAttachmentArchive(isAccessToken.orEmpty(), this)
    }

    private fun showEmptyState(message: String) {
        binding.recycleracademic.visibility = View.GONE
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
    }


    private fun scrollToMessageId(headerId: String?) {
        val dataList = mAttachmentReportAdapter?.getCurrentList()
        if (!dataList.isNullOrEmpty()) {
            val index = dataList.indexOfFirst { it.header_id == headerId }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index")
                binding.recycleracademic.post {
                    binding.recycleracademic.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.recycleracademic, index)
                }
            } else {
                Log.d("ScrollDebug", "No index found for msg_id $msg_id")
            }
        }
    }


    private fun highlightItemTemporarily(
        recyclerView: RecyclerView,
        position: Int
    ) {
        recyclerView.post {
            val viewHolder =
                recyclerView.findViewHolderForAdapterPosition(position) as? AttachmentAdapter.DataViewHolder
                    ?: return@post

            val headerLayout = viewHolder.headerLayout

            headerLayout.setBackgroundResource(R.color.light_yellow_5)

            headerLayout.postDelayed({
                headerLayout.setBackgroundResource(R.color.white)
            }, Constant.TIME_OUT)
        }
    }



    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.lblArchiveMsg -> {
                binding.txtSearchMenu1.text.clear()
                isGetAttachmentArchive()
                binding.lblArchiveMsg.visibility = View.GONE
            }

            R.id.imgFilter -> {
                if (binding.lnrDatePicking.visibility == View.VISIBLE) {

                    // 🔹 Hide filter → RESET
                    binding.lnrDatePicking.visibility = View.GONE
                    binding.lnrFilterRead.visibility = View.GONE

                    resetAttachmentFiltersAndShowAll()

                } else {
                    // 🔹 Show filter
                    binding.lnrDatePicking.visibility = View.VISIBLE
                    binding.lnrFilterRead.visibility = View.VISIBLE
                }
            }

            R.id.imgClearFilter -> {
                clearDateFilter()
            }
        }
    }

    private fun resetAttachmentFiltersAndShowAll() {
        binding.imgClearFilter.visibility = View.GONE
        // 🔹 Reset search
        currentSearchQuery = ""
        binding.txtSearchMenu1.setText("")

        // 🔹 Reset date filter
        fromDateMillis = null
        toDateMillis = null
        binding.txtFromDate.text = getString(R.string.from_date)
        binding.txtToDate.text = getString(R.string.to_date)

        // 🔹 Reset read filter UI
        currentReadFilter = ReadFilter.ALL
        binding.lblAll.setBackgroundResource(R.drawable.bg_light_green_radious)
        binding.lblUnread.setBackgroundResource(R.drawable.gray_bg_radius)
        binding.lblRead.setBackgroundResource(R.drawable.bg_gray_light_radiuos)

        // 🔹 Restore full list
        val fullList = if (isArchiveMode) {
            originalArchiveList
        } else {
            originalAttachmentList
        }

        mAttachmentReportAdapter?.updateFilteredList(fullList)

        binding.recycleracademic.visibility =
            if (fullList.isEmpty()) View.GONE else View.VISIBLE
        binding.nomessage.visibility =
            if (fullList.isEmpty()) View.VISIBLE else View.GONE
        binding.txtNoData.visibility = View.GONE
    }


    private fun clearDateFilter() {
        binding.imgClearFilter.visibility = View.GONE
        fromDateMillis = null
        toDateMillis = null
        binding.txtFromDate.text = getString(R.string.FromDate)
        binding.txtToDate.text = getString(R.string.to_date)
        applyCombinedFilter()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onItemClick(
        isData: List<AttachmentDataReport>,
        view: View,
        isPosition: Int
    ) {
    }

    override fun onReadStatusClick(isData: List<AttachmentDataReport>, isPosition: Int) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, Constant.ATTACHMENT)
            addProperty(APIKeyNames.detail_id, isData[isPosition].id)
        }

        if (isData[isPosition].is_archive) {
            appViewModel?.isUpdateStatusArchive(isAccessToken!!, jsonObject, this)
        } else {
            appViewModel?.isUpdateStatusCommunication(isAccessToken!!, jsonObject, this)

        }
    }

    override fun onFilterEmpty(showNoData: Boolean) {
        if (showNoData) {
            Log.d("NoDta", "No data")
            binding.recycleracademic.visibility = View.GONE
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        } else {
            Log.d("NoDta", "data")
            binding.recycleracademic.visibility = View.VISIBLE
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
        }
    }

    private fun showTourIfNeeded() {

        if (isTourDialogShown) return
        if (!SharedPreference.isTourShown(
                this,
                SharedPreference.KEY_PARENT_ATTACHMENT_TOUR
            )
        ) {

            isTourDialogShown = true
            val tourImages = arrayListOf(
                R.drawable.daily_collection_tour_1,
                R.drawable.daily_collection_tour_2
            )

            TourDialog.newInstance(tourImages) {
                SharedPreference.setTourShown(
                    this,
                    SharedPreference.KEY_PARENT_ATTACHMENT_TOUR
                )
            }.show(supportFragmentManager, "parent_attachment_tour")
        }
    }

}