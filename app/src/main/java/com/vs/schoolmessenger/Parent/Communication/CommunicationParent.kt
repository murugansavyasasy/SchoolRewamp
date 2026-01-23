package com.vs.schoolmessenger.Parent.Communication

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.TourDialog
import com.vs.schoolmessenger.databinding.CommunicationBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CommunicationParent : BaseActivity<CommunicationBinding>(), View.OnClickListener,
    VoiceClickListener {

    override fun getViewBinding() = CommunicationBinding.inflate(layoutInflater)
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var allVoiceData = mutableListOf<VoiceData>()
    private var adapter: UnifiedVoiceAdapter? = null
    private var isInitialLoad = true
    private var isFromArchive = false
    private var hasFetchedMore = false
    private var isFilterType: String = Constant.ALL
    private var isCommunicationType = 1
    var isSeeMoreClick = true

    // DATE FILTER
    private var fromDateMillis: Long? = null
    private var toDateMillis: Long? = null

    // DATE FORMAT (MATCH API DATE)
    private val apiDateTimeFormat =
        java.text.SimpleDateFormat("dd-MM-yyyy hh:mm a", java.util.Locale.getDefault())


    var isFilterClick = false
    private var currentSearchQuery: String = ""

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    var userDetails: UserDetails? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaVoiceMessage.setOnClickListener(this)
        binding.seeMoreLabel.setOnClickListener(this)
        binding.imgFilter.setOnClickListener(this)
        binding.imgClearFilter.setOnClickListener(this)

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



        isFromArchive = intent.getBooleanExtra(Constant.fromArchive, false)
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        showShimmer()

        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails.standard_name + " - " + isChildDetails.section_name

        binding.root.post {
            val finalName =
                Constant.isSelectedMenuName?.takeIf { it.isNotEmpty() } ?: menu_name ?: ""
            Log.d("lblHeaderTitle", "Setting headerview text: $finalName")
            binding.lblHeaderTitle.text = finalName
            binding.lblHeaderTitle.visibility = View.VISIBLE
        }

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.linearlayout1.visibility == View.VISIBLE) {

                // 🔹 Hide search & filter
                binding.linearlayout1.visibility = View.GONE
                binding.rytFilter.visibility = View.GONE
                binding.lnrDatePicking.visibility = View.GONE

                resetAllFiltersAndShowFullList()

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)

            } else {
                // 🔹 Show search
                binding.linearlayout1.visibility = View.VISIBLE
                binding.rytFilter.visibility = View.GONE
                binding.txtSearchMenu.setText("")
            }
        }

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter!!.pauseMediaPlayer()
                currentSearchQuery = s.toString()
                applyCombinedFilter()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        appViewModel?.isGetCommmunicationlist?.observe(this) { response ->
            if (response != null) {
                if (response?.status == true) {
                    if (response.data.isNotEmpty()) {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                        appendData(response.data, archiveFlag = true)
                        if (fromNotification) {
                            scrollToMessageId(headerId)
                        }

                    } else {
                        hasFetchedMore = true
                        if (allVoiceData.isNotEmpty()) {
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                        } else {
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                        }
                        checkAndShowNoData(
                            filteredList = allVoiceData,
                            message = response.message
                        )
                    }
                } else {
                    if (allVoiceData.isNotEmpty()) {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    } else {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    }
                    checkAndShowNoData(
                        message = response?.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
            }
        }


        appViewModel?.isGetCommmunicationlistload?.observe(this) { response ->
            if (response != null) {
                if (response?.status == true) {
                    appendData(response.data, archiveFlag = false)
                    scrollToMessageId(headerId)

                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_messages)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                        addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)

                } else {
                    if (allVoiceData.isNotEmpty()) {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    } else {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    }
                    checkAndShowNoData(message = response?.message)
                }
            }
        }

        binding.rdgCommunication.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.RdbAll -> {

                    if (isCommunicationType == 1) {
                        isFilterType = Constant.ALL
                    } else if (isCommunicationType == 2) {
                        isFilterType = Constant.UNREAD
                    } else if (isCommunicationType == 3) {
                        isFilterType = Constant.READ
                    }
                    applyCombinedFilter()
                }

                R.id.RdbVoice -> {
                    if (isCommunicationType == 1) {
                        isFilterType = Constant.VOICE
                    } else if (isCommunicationType == 2) {
                        isFilterType = Constant.VOICE_UNREAD
                    } else if (isCommunicationType == 3) {
                        isFilterType = Constant.VOICE_READ
                    }
                    applyCombinedFilter()
                }

                R.id.RdbText -> {
                    if (isCommunicationType == 1) {
                        isFilterType = Constant.TEXT
                    } else if (isCommunicationType == 2) {
                        isFilterType = Constant.TEXT_UNREAD
                    } else if (isCommunicationType == 3) {
                        isFilterType = Constant.TEXT_READ
                    }
                    applyCombinedFilter()
                }
            }
        }

        binding.lblAll.setOnClickListener {
            isChangeBackgroundFilter(binding.lblAll)
        }

        binding.lblUnread.setOnClickListener {
            isChangeBackgroundFilter(binding.lblUnread)
        }

        binding.lblRead.setOnClickListener {
            isChangeBackgroundFilter(binding.lblRead)
        }

        binding.lnrFromDate.setOnClickListener {
            showFromDatePicker()
        }

        binding.lnrToDate.setOnClickListener {
            showToDatePicker()
        }


        fetchInitialData()
    }

    private fun showToast(msg: String) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
    }
    private fun showFromDatePicker() {
        val todayCal = Calendar.getInstance()

        val dialog = DatePickerDialog(
            this,
            { _, y, m, d ->

                val selectedCal = Calendar.getInstance()
                selectedCal.set(y, m, d, 0, 0, 0)

                fromDateMillis = selectedCal.timeInMillis

                binding.txtFromDate.text =
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(selectedCal.time)

                // 🔴 If invalid range
                if (toDateMillis != null && fromDateMillis!! > toDateMillis!!) {
                    showToast(getString(R.string.from_date_cannot_be_after_to_date))
                    toDateMillis = null
                    binding.txtToDate.text = getString(R.string.to_date)
                }

                applyCombinedFilter()
            },
            todayCal.get(Calendar.YEAR),
            todayCal.get(Calendar.MONTH),
            todayCal.get(Calendar.DAY_OF_MONTH)
        )

        // 🔹 Disable future dates
        dialog.datePicker.maxDate = todayCal.timeInMillis

        // 🔹 From Date ≤ To Date (if selected)
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
            { _, y, m, d ->

                val selectedCal = Calendar.getInstance()
                selectedCal.set(y, m, d, 23, 59, 59)

                toDateMillis = selectedCal.timeInMillis

                binding.txtToDate.text =
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(selectedCal.time)

                applyCombinedFilter()
            },
            todayCal.get(Calendar.YEAR),
            todayCal.get(Calendar.MONTH),
            todayCal.get(Calendar.DAY_OF_MONTH)
        )

        // 🔹 Disable future dates
        dialog.datePicker.maxDate = todayCal.timeInMillis

        // 🔹 To Date ≥ From Date
        if (fromDateMillis != null) {
            dialog.datePicker.minDate = fromDateMillis!!
        }

        dialog.show()
    }


    private fun scrollToMessageId(headerId: String?) {
        if (msg_id == -1) return

        allVoiceData?.let { list ->
            val index = list.indexOfFirst { it.header_id == headerId }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in ongoing")
                binding.recyclerInitial.post {
                    binding.recyclerInitial.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.recyclerInitial, index)
                }
            } else {
                Log.d("ScrollDebug", "No item found with headerId: $headerId")
            }
        }
        Log.d("ScrollDebug", "No index found for headerId $headerId")
    }

    private fun highlightItemTemporarily(recyclerView: RecyclerView, position: Int) {
        recyclerView.post {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.itemView?.let { itemView ->
                val originalBackground = itemView.background

                itemView.setBackgroundColor(
                    resources.getColor(R.color.light_yellow_5, null)
                )

                Handler(Looper.getMainLooper()).postDelayed({
                    itemView.background = originalBackground
                }, Constant.TIME_OUT)
            }
        }
    }

    private fun isChangeBackgroundFilter(isSelectedFilter: TextView) {
        binding.lblAll.setBackgroundResource(R.drawable.bg_gray_light_radiuos)
        binding.lblUnread.setBackgroundResource(R.drawable.bg_gray_light_radiuos)
        binding.lblRead.setBackgroundResource(R.drawable.bg_gray_light_radiuos)

        isSelectedFilter.setBackgroundResource(R.drawable.bg_light_green_radious)

        if (isSelectedFilter == binding.lblAll) {
            isCommunicationType = 1
            if (binding.RdbAll.isChecked) {
                isFilterType = Constant.ALL
            } else if (binding.RdbText.isChecked) {
                isFilterType = Constant.TEXT_ALL
            } else if (binding.RdbVoice.isChecked) {
                isFilterType = Constant.VOICE_ALL
            }
        } else if (isSelectedFilter == binding.lblUnread) {
            isCommunicationType = 2
            if (binding.RdbAll.isChecked) {
                isFilterType = Constant.UNREAD
            } else if (binding.RdbText.isChecked) {
                isFilterType = Constant.TEXT_UNREAD
            } else if (binding.RdbVoice.isChecked) {
                isFilterType = Constant.VOICE_UNREAD
            }
        } else if (isSelectedFilter == binding.lblRead) {
            isCommunicationType = 3
            if (binding.RdbAll.isChecked) {
                isFilterType = Constant.READ
            } else if (binding.RdbText.isChecked) {
                isFilterType = Constant.TEXT_READ
            } else if (binding.RdbVoice.isChecked) {
                isFilterType = Constant.VOICE_READ
            }
        }

        applyCombinedFilter()
    }

    private fun applyCombinedFilter() {

        var filteredList = allVoiceData.toList()

        // 🔹 DATE FILTER (ALL CASES HANDLED)
        filteredList = filteredList.filter { item ->
            try {
                val dateTime = "${item.date} ${item.time}"
                val itemMillis =
                    apiDateTimeFormat.parse(dateTime)?.time ?: return@filter false

                when {
                    // ✅ From + To
                    fromDateMillis != null && toDateMillis != null ->
                        itemMillis in fromDateMillis!!..toDateMillis!!

                    // ✅ Only From → future data
                    fromDateMillis != null ->
                        itemMillis >= fromDateMillis!!

                    // ✅ Only To → past data
                    toDateMillis != null ->
                        itemMillis <= toDateMillis!!

                    // ✅ No date filter
                    else -> true
                }
            } catch (e: Exception) {
                false
            }
        }

        // 🔹 TYPE + READ FILTER (UNCHANGED)
        filteredList = when (isFilterType) {

            Constant.TEXT, Constant.TEXT_ALL ->
                filteredList.filter { it.type == Constant.TEXT }

            Constant.VOICE, Constant.VOICE_ALL ->
                filteredList.filter { it.type == Constant.VOICE }

            Constant.READ ->
                filteredList.filter { !it.is_unread!! }

            Constant.UNREAD ->
                filteredList.filter { it.is_unread!! }

            Constant.TEXT_READ ->
                filteredList.filter { it.type == Constant.TEXT && !it.is_unread!! }

            Constant.VOICE_READ ->
                filteredList.filter { it.type == Constant.VOICE && !it.is_unread!! }

            Constant.TEXT_UNREAD ->
                filteredList.filter { it.type == Constant.TEXT && it.is_unread!! }

            Constant.VOICE_UNREAD ->
                filteredList.filter { it.type == Constant.VOICE && it.is_unread!! }

            else -> filteredList
        }

        // 🔹 SEARCH FILTER (UNCHANGED)
        if (currentSearchQuery.isNotEmpty()) {
            val q = currentSearchQuery
            filteredList = filteredList.filter {
                it.title.orEmpty().contains(q, true) ||
                        it.content.orEmpty().contains(q, true) ||
                        it.type.orEmpty().contains(q, true) ||
                        it.time.orEmpty().contains(q, true)
            }
        }

        adapter?.updateList(filteredList, isSeeMoreClick)
        checkAndShowNoData(filteredList)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.imgFilter -> {
                if (binding.rytFilter.isVisible) {
                    // 🔹 HIDE FILTER → RESET EVERYTHING
                    binding.rytFilter.visibility = View.GONE
                    binding.lnrDatePicking.visibility = View.GONE

                    resetAllFiltersAndShowFullList()

                } else {
                    // 🔹 SHOW FILTER
                    isFilterClick = true
                    binding.rytFilter.visibility = View.VISIBLE
                    binding.lnrDatePicking.visibility = View.VISIBLE
                }
            }

            R.id.rlaTextMessage -> {
                adapter?.updateData()
                adapter?.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage,
                    binding.imgTextMessage,
                    binding.lblTextMessage
                )
            }

            R.id.rlaVoiceMessage -> {
                adapter?.updateData()
                adapter?.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(
                    binding.rlaVoiceMessage,
                    binding.imgVoiceMessage,
                    binding.lblVoiceMessage
                )
            }

            R.id.seeMoreLabel -> {
                if (!hasFetchedMore) {
                    hasFetchedMore = true
                    isSeeMoreClick = false
                    binding.seeMoreLabel.visibility = View.GONE
                    binding.txtSearchMenu.text.clear()
                    fetchMoreData()
                }
            }

            R.id.imgClearFilter -> {
                clearDateFilter()
            }
        }
    }

    private fun resetAllFiltersAndShowFullList() {

        // 🔹 Reset search
        currentSearchQuery = ""
        binding.txtSearchMenu.setText("")

        // 🔹 Reset read/unread selection (UI)
        isCommunicationType = 1
        isFilterType = Constant.ALL

        fromDateMillis = null
        toDateMillis = null
        binding.txtFromDate.text = getString(R.string.from_date)
        binding.txtToDate.text = getString(R.string.to_date)

        binding.lblAll.setBackgroundResource(R.drawable.bg_light_green_radious)
        binding.lblUnread.setBackgroundResource(R.drawable.bg_gray_light_radiuos)
        binding.lblRead.setBackgroundResource(R.drawable.bg_gray_light_radiuos)

        // 🔹 Reset radio buttons (voice/text/all)
        binding.RdbAll.isChecked = true

        // 🔹 Show full API data
        adapter?.updateList(allVoiceData, isSeeMoreClick)
        checkAndShowNoData(allVoiceData)

        isFilterClick = false
    }


    private fun fetchInitialData() {
        isInitialLoad = true
        appViewModel?.isGetCommmunicationlistload(isAccessToken.orEmpty(), this)
    }

    private fun fetchMoreData() {
        isInitialLoad = false
        appViewModel?.isGetCommmunicationlist(isAccessToken.orEmpty(), this)
    }

    private fun appendData(newData: List<VoiceData>?, archiveFlag: Boolean) {
        if (isInitialLoad) allVoiceData.clear()
        Log.d("isSeeMoreClick", isSeeMoreClick.toString())
        newData.let {
            val processedData = it!!.map { item -> item.copy(is_archive = archiveFlag) }
            allVoiceData.addAll(processedData)
            binding.recyclerInitial.visibility = View.VISIBLE
            if (adapter == null) {
                adapter = UnifiedVoiceAdapter(
                    allVoiceData as ArrayList<VoiceData>,
                    this,
                    this,
                    Constant.isShimmerViewDisable,
                    this,
                    isAccessToken.orEmpty(),
                    archiveFlag,
                    isSeeMoreClick
                )
                binding.recyclerInitial.layoutManager = LinearLayoutManager(this)
                binding.recyclerInitial.isNestedScrollingEnabled = false
                binding.recyclerInitial.adapter = adapter
            } else {
                adapter?.setIsFromArchive(archiveFlag)
                adapter?.updateList(allVoiceData, isSeeMoreClick)
            }

            if (allVoiceData.isNotEmpty()) {
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            } else {
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            }
            applyCombinedFilter()
        }
        checkAndShowNoData()
    }

    private fun checkAndShowNoData(filteredList: List<VoiceData>? = null, message: String? = null) {
        val listToCheck = filteredList ?: allVoiceData
        val isEmpty = listToCheck.isEmpty()
        binding.txtNoData.visibility = if (isEmpty) View.VISIBLE else View.GONE

        if (!isFilterClick) {
            binding.seeMoreLabel.visibility =
                if (isEmpty && isSeeMoreClick) View.VISIBLE else View.GONE
        }
        binding.nomessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerInitial.visibility = if (isEmpty) View.GONE else View.VISIBLE

        if (isEmpty) {
            binding.txtNoData.text = message ?: getString(R.string.no_list_found)
        }
    }


    private fun showShimmer() {
        val shimmerAdapter = UnifiedVoiceAdapter(
            null,
            this,
            this,
            Constant.isShimmerViewShow,
            this,
            isAccessToken.orEmpty(),
            isFromArchive,
            isSeeMoreClick
        )
        binding.recyclerInitial.layoutManager = LinearLayoutManager(this)
        binding.recyclerInitial.isNestedScrollingEnabled = false
        binding.recyclerInitial.adapter = shimmerAdapter
    }

    override fun onUpdateArchiveStatus(type: String?, id: String?) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, type)
            addProperty(APIKeyNames.detail_id, id)
        }
        isAccessToken?.let {
            appViewModel?.isUpdateStatusArchive(it, jsonObject, this)
        }
    }

    override fun onUpdateCommunicationStatus(type: String?, id: String?) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, type)
            addProperty(APIKeyNames.detail_id, id)
        }

        isAccessToken?.let {
            appViewModel?.isUpdateStatusCommunication(it, jsonObject, this)
        }
    }

    override fun onItemClick(data: VoiceData, holder: UnifiedVoiceAdapter.DataViewHolder) {

    }

    override fun onSeeMoreClick(
        data: VoiceData,
        holder: UnifiedVoiceAdapter.DataViewHolder
    ) {
//        isSeeMoreClick = false
//        if (!hasFetchedMore) {
//            hasFetchedMore = true
//            fetchMoreData()
//        }
    }

    private fun isChangeBackRoundCommunicationType(
        isTypeCommunication: RelativeLayout,
        imgTypeCommunication: ImageView,
        lblTypeCommunication: TextView
    ) {
        binding.rlaVoiceMessage.background = null
        binding.rlaTextMessage.background = null
        isTypeCommunication.background =
            ContextCompat.getDrawable(this, R.drawable.bg_gradient_redious_parent)

        binding.lblVoiceMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.lblTextMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        lblTypeCommunication.setTextColor(ContextCompat.getColor(this, R.color.white))

        binding.imgVoiceMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.mic_icon_black
            )
        )
        binding.imgTextMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.text_icon_black
            )
        )

        when (imgTypeCommunication) {
            binding.imgVoiceMessage -> binding.imgVoiceMessage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.mic_icon
                )
            )

            binding.imgTextMessage -> binding.imgTextMessage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.text_icon
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
        adapter?.pauseMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.onDestroyMediaPlayer()
    }

    override fun onBackPressed() {
        if (adapter != null) {
            adapter!!.releaseMediaPlayer()
        }
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


    private fun clearDateFilter() {
        fromDateMillis = null
        toDateMillis = null
        binding.txtFromDate.text = getString(R.string.FromDate)
        binding.txtToDate.text = getString(R.string.to_date)
        applyCombinedFilter()
    }

}