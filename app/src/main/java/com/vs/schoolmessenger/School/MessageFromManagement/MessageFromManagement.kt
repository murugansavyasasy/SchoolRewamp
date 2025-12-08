package com.vs.schoolmessenger.School.MessageFromManagement

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentFilePathAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.AttachmentMediaAdapter
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.MessageFromStaffAdapter
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.MessageFromManagementBinding
import me.relex.circleindicator.CircleIndicator2


class MessageFromManagement : BaseActivity<MessageFromManagementBinding>(),
    View.OnClickListener, MsgStaffListener {

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var appViewModel: App? = null
    private lateinit var adapter2: AttachmentFilePathAdapter
    private lateinit var adapter: MessageFromStaffAdapter
    private var handler: Handler? = null
    private var mediaPlayer: MediaPlayer? = null
    private var completeAttachmentList: List<GetMessagesStaffData> = emptyList()
    private var isMsgStaff: List<GetMessagesStaffData>? = emptyList()
    private var userDetails: UserDetails? = null
    private var updateRunnable: Runnable? = null
    var isMenuCount = -1
    private var isDialogShowing = false


    var TYPE: String? = ""
    var selectedSchoolId = ""
    var isMultipleSchool = false

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var instituteId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false


    override fun getViewBinding(): MessageFromManagementBinding {
        return MessageFromManagementBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblArchiveMsg.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()


        userDetails = SharedPreference.getUserDetails(this)

        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)

        if (fromNotification) {
            Constant.isParentChoose = false
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            instituteId = intent.getStringExtra(Constant.institute_id)
            menu_name = intent.getStringExtra(Constant.menu_name)

            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )

            val matchedChild = userDetails?.staff_details?.find { it.school_id == instituteId }
            SharedPreference.putStaffDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }


        isStaffDetails = SharedPreference.getStaffDetails(this)

        if (userDetails?.staff_role.equals(Constant.isStaffRole)) {
            binding.rytSpinner.visibility = View.GONE
            isAccessToken = isStaffDetails!!.access_token
            isMultipleSchool = false
            isGetMessageFromStaff()
            binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        } else {
            if (userDetails?.staff_details?.size!! > 1) {
                binding.rytSpinner.visibility = View.VISIBLE
                isMultipleSchool = true
                //Important Note:see actually what ever token we pass,From backend we recieve all the data from all school we are suppose to filter them using the school id this scenrio is for multiple school
                userDetails?.let { setupSchoolSpinner(it.staff_details) }
            } else {
                isAccessToken = userDetails!!.staff_details.get(0).access_token
                binding.rytSpinner.visibility = View.GONE
                isMultipleSchool = false
                isGetMessageFromStaff()
                binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
                binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
            }
        }

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        isMenuCount = Constant.isSchoolMenuCount

        appViewModel?.isGetMessageStaff?.observe(this) { response ->
            Constant.hideLoading(this)
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.txtSearch1.windowToken, 0)
            if (response != null) {
                if (response.status) {
                    binding.rcMessageStaff.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                    isLoadMsgStaff(response.data)
                    completeAttachmentList = response.data
                    if (fromNotification) {
                        scrollToMessageId(headerId)
                    }
                } else {
                    binding.rytSpinner.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.rytSearch1.visibility = View.GONE
                    binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                    binding.rcMessageStaff.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.rytSpinner.visibility = View.GONE
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                binding.rytSearch1.visibility = View.GONE
                binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                binding.rcMessageStaff.visibility = View.GONE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }

        appViewModel?.isGetMessageStaffArchive?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response.status) {
                    if (response.data.isNotEmpty()) {
                        val updatedList = completeAttachmentList.toMutableList()
                        updatedList.addAll(response.data)
                        completeAttachmentList = updatedList
                        isMsgStaff = completeAttachmentList

                        if (!::adapter.isInitialized || adapter.getItemViewType(0) == 0) {
                            adapter = MessageFromStaffAdapter(
                                mutableListOf(),
                                this,
                                this,
                                Constant.isShimmerViewDisable
                            )
                            binding.rcMessageStaff.layoutManager =
                                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                            binding.rcMessageStaff.adapter = adapter
                            binding.rcMessageStaff.isNestedScrollingEnabled = false
                        }

                        if (isMultipleSchool) {
                            binding.rytSpinner.visibility = View.VISIBLE//last fix

                            if (selectedSchoolId == Constant.All_Schools) {
                                adapter.AppendData(response.data)
                            } else {
                                val filteredList =
                                    completeAttachmentList.filter { it.school_id == selectedSchoolId }
                                Log.d(
                                    "SpinnerSelection",
                                    "Selected school id: ${selectedSchoolId}, " + "Data: $filteredList, Token: $isAccessToken"
                                )
                                adapter.updateData(filteredList)
                            }
                        } else {
                            binding.rytSpinner.visibility = View.GONE//last fix
                            //if role is staff or only handle one school means we are directly update the response direclty to adapter
                            adapter.AppendData(response.data)
                        }

                        if (adapter!!.getCurrentListSize() > 0) {
                            Log.d("Item", "Item There")
                            binding.rytSearch1.visibility = View.GONE
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                            binding.txtSearch1.text.clear()


                            binding.rcMessageStaff.visibility = View.VISIBLE//last fix
                            binding.lytList.visibility = View.GONE//last fix
                            //last fix
                            if (isMultipleSchool) {
                                binding.rytSpinner.visibility = View.VISIBLE
                            } else {
                                binding.rytSpinner.visibility = View.GONE
                            }


                        } else {
                            Log.d("Item", " No Item")

                            binding.rytSearch1.visibility = View.GONE
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                            binding.txtSearch1.text.clear()

                            binding.rcMessageStaff.visibility = View.GONE//last fix
                            binding.lytList.visibility = View.VISIBLE//last fix
                            binding.rytSpinner.visibility = View.GONE //last fix


                        }

//                        ShowData() //last fix
                        binding.txtSearch1.text.clear()
                        binding.isArchiveErrorMsg.visibility = View.GONE
                    } else {
                        binding.isArchiveErrorMsg.visibility = View.VISIBLE
                        binding.isArchiveErrorMsg.text = response.message
                        if (adapter.getCurrentListSize() == 0) {
                            binding.lytList.visibility = View.VISIBLE
                            binding.txtNoData.visibility = View.GONE
                            binding.rytSearch1.visibility = View.GONE
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                            binding.txtSearch1.text.clear()

                        } else {
                            binding.lytList.visibility = View.GONE
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
                    if (adapter.getCurrentListSize() == 0) {
                        binding.lytList.visibility = View.VISIBLE
                        binding.txtNoData.visibility = View.GONE
                        binding.rytSearch1.visibility = View.GONE
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                        binding.txtSearch1.text.clear()

                    } else {
                        binding.lytList.visibility = View.GONE
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
                if (adapter.getCurrentListSize() == 0) {
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.visibility = View.GONE
                    binding.rytSearch1.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.txtSearch1.text.clear()
                } else {
                    binding.lytList.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                }
            }

        }


        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
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

                if (s!!.isNotEmpty()) {
                    if (binding.isArchiveErrorMsg.visibility == View.VISIBLE) {
                        binding.isArchiveErrorMsg.visibility = View.GONE
                    }
                } else {
                    if (binding.isArchiveErrorMsg.visibility == View.GONE) {
                        binding.isArchiveErrorMsg.visibility = View.VISIBLE
                    }
                }

            }
        })

    }


    private fun scrollToMessageId(headerId: String?) {
        if (msg_id == -1) return

        completeAttachmentList?.let { list ->
            val index = list.indexOfFirst { it.header_id == headerId }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in ongoing")
                binding.rcMessageStaff.post {
                    binding.rcMessageStaff.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rcMessageStaff, index)
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

                itemView.setBackgroundColor(Color.parseColor("#FFE082"))

                Handler(Looper.getMainLooper()).postDelayed({
                    itemView.background = originalBackground
                }, 3000)
            }
        }
    }

    private fun setupSchoolSpinner(staffList: List<StaffDetails>) {

        val schoolNames = mutableListOf<String>()
        schoolNames.add("All Schools")
        schoolNames.addAll(staffList.map { it.school_name })

        val adapter = SpinnerLoadingAdapter(this, schoolNames)
        binding.schoollistfilter.adapter = adapter
        binding.schoollistfilter.setSelection(0)

        binding.schoollistfilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                private var lastSelectedPosition: Int = -1

                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    // update adapter UI selection
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    if (position != lastSelectedPosition) {
                        lastSelectedPosition = position

                        if (position == 0) {
                            // “All” selected
                            isLoadMsgStaff(completeAttachmentList)
                            binding.toolbarLayout.lblSchoolName.visibility = View.GONE
                            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
                            selectedSchoolId = Constant.All_Schools

                        } else {
                            // Specific school selected
                            val selectedStaff = staffList[position - 1]
                            isAccessToken = selectedStaff.access_token
                            isStaffDetails = selectedStaff
                            selectedSchoolId = selectedStaff.school_id

                            val filteredList =
                                completeAttachmentList.filter { it.school_id == selectedSchoolId }
                            Log.d(
                                "SpinnerSelection",
                                "Selected school: ${selectedStaff.school_name}, " +
                                        "Selected school id: ${selectedStaff.school_id}, " +
                                        "Data: $filteredList, Token: $isAccessToken"
                            )

                            isLoadMsgStaff(filteredList)
                            binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
                            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        //Important Note:see actually what ever token we pass,From backend we recieve all the data from all school we are suppose to filter them using the school id this scenrio is for multiple school
        // Initial fetch for all schools
        if (staffList.isNotEmpty()) {
            isAccessToken = staffList[0].access_token
            isStaffDetails = staffList[0]
            Log.d("DefaultSelection", "Default token: $isAccessToken")
            isGetMessageFromStaff()
        }
    }


    private fun filter(text: String) {
        val searchWords = text.trim().lowercase().split("\\s+".toRegex())

        val filteredList = if (searchWords.isEmpty() || searchWords.first().isBlank()) {
            isMsgStaff.orEmpty()
        } else {
            isMsgStaff.orEmpty().filter { msgStaff ->
                val fieldsToSearch = mutableListOf(
                    msgStaff.title?.lowercase().orEmpty(),
                    msgStaff.description?.lowercase().orEmpty(),
                    msgStaff.time?.lowercase().orEmpty(),
                    msgStaff.role?.lowercase().orEmpty(),
                    msgStaff.sent_by?.lowercase().orEmpty()
                )

                // 🔹 Skip content if VOICE
                if (msgStaff.type?.uppercase() != Constant.VOICE) {
                    fieldsToSearch.add(msgStaff.content?.lowercase().orEmpty())
                }

                searchWords.all { word ->
                    fieldsToSearch.any { field -> field.contains(word) }
                }
            }
        }

        // 🔹 Update UI
        if (filteredList.isNotEmpty()) {
            ShowData()
            adapter.updateData(filteredList)
        } else {
            binding.rlaMessageFFromStaff.visibility = View.VISIBLE
            binding.rcMessageStaff.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }


    fun ShowData() {
        binding.rlaMessageFFromStaff.visibility = View.VISIBLE
        binding.rcMessageStaff.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }


    private fun isLoadMsgStaff(data: List<GetMessagesStaffData>) {
        if (data.isNotEmpty()) {
            isMsgStaff = data
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            binding.rytSearch1.visibility = View.GONE
            adapter = MessageFromStaffAdapter(
                data.toMutableList(),
                this,
                this,
                Constant.isShimmerViewDisable
            )
            binding.rcMessageStaff.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.rcMessageStaff.adapter = adapter
            binding.rcMessageStaff.isNestedScrollingEnabled = false
            binding.rcMessageStaff.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.rytSearch1.visibility = View.GONE
            binding.rcMessageStaff.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        }
    }


    fun isGetMessageFromStaff() {
        Constant.showLoading(this)
        adapter = MessageFromStaffAdapter(mutableListOf(), this, this, Constant.isShimmerViewShow)
        binding.rcMessageStaff.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcMessageStaff.adapter = adapter
        binding.rcMessageStaff.isNestedScrollingEnabled = false
        appViewModel?.isGetMessageStaff(isAccessToken ?: "")
    }

    fun isGetMessageFromStaffArchive() {
        Constant.showLoading(this)
        appViewModel?.isGetMessageStaffArchive(isAccessToken ?: "")
    }

    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }

    fun showResumeListDialog(
        activity: Activity,
        data: GetMessagesStaffData
    ) {
        if (isDialogShowing || activity.isFinishing || activity.isDestroyed) return
        isDialogShowing = true


        val dialogView =
            LayoutInflater.from(activity).inflate(R.layout.msg_from_staff_preview, null)
        val builder = AlertDialog.Builder(activity).setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!activity.isFinishing && !activity.isDestroyed) {
            alertDialog.show()
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcAttachement)
        val indicator = dialogView.findViewById<CircleIndicator2>(R.id.indicator)
        dialogView.findViewById<TextView>(R.id.lblSentTime)
        val lblSendBy = dialogView.findViewById<TextView>(R.id.lblSendBy)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)
        val imgBack = dialogView.findViewById<ImageView>(R.id.imgBack)
        val rlaAudioDetails = dialogView.findViewById<RelativeLayout>(R.id.rlaAudioDetails)
        val rytDescription = dialogView.findViewById<RelativeLayout>(R.id.rytDescription)
        val lblEmergency = dialogView.findViewById<TextView>(R.id.lblEmergency)
        val imgEmergency = dialogView.findViewById<ImageView>(R.id.imgEmergency)
        val tvPostOn = dialogView.findViewById<TextView>(R.id.tvPostOn)

        // FIX: use dialogView.findViewById instead of findViewById
        val lblRecentTotalDuration: TextView = dialogView.findViewById(R.id.lblRecentTotalDuration)
        val tvAudioTittle: TextView = dialogView.findViewById(R.id.tvAudioTittle)
        val lblEmgRecentduration: TextView = dialogView.findViewById(R.id.lblEmgRecentduration)
        val imgRecentEmgplaypause: ImageView = dialogView.findViewById(R.id.imgRecentEmgplaypause)
        val recentseekbar: SeekBar = dialogView.findViewById(R.id.recentseekbar)
        val recentSeekbarlayout: LinearLayout = dialogView.findViewById(R.id.recentSeekbarlayout)

        tvTitle.text = data.title
        lblSendBy.text = "${getString(R.string.posted_by)} ${data.sent_by}"
        tvPostOn.text = "${Constant.isFormatDate(data.date.toString())} ${data.time}"


        when (data.type) {
            Constant.TEXT -> {
                rytDescription.visibility = View.VISIBLE
                tvDescription.visibility = View.VISIBLE
                rlaAudioDetails.visibility = View.GONE
                recyclerView.visibility = View.GONE
                indicator.visibility = View.GONE
                tvDescription.text = data.description
            }

            Constant.VOICE -> {
                rytDescription.visibility = View.GONE
                tvDescription.visibility = View.GONE
                tvAudioTittle.apply {
                    text = data.title
                    isSingleLine = true
                    ellipsize = TextUtils.TruncateAt.END
                    maxLines = 1
                }
                if (data.is_emergency) {
                    lblEmergency.text = getString(R.string.emergency_voice)
                    imgEmergency.visibility = View.VISIBLE
                } else {
                    lblEmergency.text = getString(R.string.voice)
                    imgEmergency.visibility = View.GONE
                }
                tvDescription.text = data.description
                rlaAudioDetails.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                indicator.visibility = View.GONE

                setupAudioPlayer(
                    data,
                    imgRecentEmgplaypause,
                    recentseekbar,
                    lblEmgRecentduration,
                    lblRecentTotalDuration,
                    recentSeekbarlayout
                )

            }

            Constant.ATTACHMENT_ -> {
                rytDescription.visibility = View.VISIBLE
                tvDescription.visibility = View.VISIBLE
                rlaAudioDetails.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                indicator.visibility = View.VISIBLE
                tvDescription.text = data.description

                if (data.file_path.isNullOrEmpty()) {
                    indicator.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                } else {
                    indicator.visibility = View.VISIBLE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.layoutManager =
                        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.adapter = AttachmentMediaAdapter(
                        data.file_path,
                        activity,
                        Constant.isShimmerViewDisable
                    )
                    indicator.attachToRecyclerView(recyclerView)
                }
            }
        }

        imgBack.setOnClickListener {
            releaseMediaPlayer()
            isDialogShowing = false
            alertDialog.dismiss()
        }
    }


    private fun CircleIndicator2.attachToRecyclerView(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return
        this.createIndicators(adapter.itemCount, 0)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                this@attachToRecyclerView.animatePageSelected(firstVisible)
            }
        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                this@attachToRecyclerView.createIndicators(adapter.itemCount, 0)
            }
        })
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblArchiveMsg -> {
                binding.txtSearch1.text.clear()
                isGetMessageFromStaffArchive()
                binding.lblArchiveMsg.visibility = View.GONE
            }
        }
    }

    override fun onStaffClick(data: GetMessagesStaffData) {
        Log.d("SelectedData", data.toString())
        showResumeListDialog(this, data)
        if (data.is_unread) {
            if (data.type.equals(Constant.TET2)) {
                TYPE = Constant.MGMT_MSG_TEXT
            } else if (data.type.equals(Constant.VOICE)) {
                TYPE = Constant.MGMT_MSG_VOICE
            } else {
                TYPE = Constant.MGMT_MSG_ATTACHMENT
            }

            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.type, TYPE)
                addProperty(APIKeyNames.detail_id, data.id)
            }

            if (data.is_archive) {
                appViewModel?.isUpdateStatusArchive(isAccessToken!!, jsonObject, this)
            } else {
                appViewModel?.isUpdateStatusCommunication(isAccessToken!!, jsonObject, this)
            }
        }
    }


    private fun setupAudioPlayer(
        data: GetMessagesStaffData,
        imgPlayPause: ImageView,
        seekBar: SeekBar,
        lblCurrent: TextView,
        lblTotal: TextView,
        seekBarLayout: LinearLayout
    ) {
        if (data.content.isNullOrEmpty()) {
            seekBarLayout.visibility = View.GONE
            return
        }

        seekBarLayout.visibility = View.VISIBLE
        lblCurrent.text = "00:00"
        lblTotal.text = milliSecondsToTimer(data.duration!! * 1000L)

        imgPlayPause.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                // 🔹 Pause
                mediaPlayer?.pause()
                imgPlayPause.setImageResource(R.drawable.play_icon_2)
            } else {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioStreamType(AudioManager.STREAM_MUSIC)

                        setOnPreparedListener { mp ->
                            seekBar.max = mp.duration
                            lblTotal.text = milliSecondsToTimer(mp.duration.toLong())

                            mp.start()
                            imgPlayPause.setImageResource(R.drawable.pause_icon_2)

                            seekBar.progress = 0
                            lblCurrent.text = "00:00"

                            updateSeekBar(mp, seekBar, lblCurrent, lblTotal)
                        }

                        setOnCompletionListener {
                            imgPlayPause.setImageResource(R.drawable.play_icon_2)
                            seekBar.progress = 0
                            lblCurrent.text = "00:00"
                            handler?.removeCallbacks(updateRunnable!!)
                        }

                        setOnErrorListener { _, what, extra ->
                            Log.e("MediaPlayer", "Error what=$what extra=$extra")
                            releaseMediaPlayer()
                            true
                        }
                    }

                    try {
                        mediaPlayer?.reset()
                        mediaPlayer?.setDataSource(data.content)
                        mediaPlayer?.prepareAsync() // 🔹 async, safe for all versions
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    // Already prepared → resume
                    mediaPlayer?.start()
                    imgPlayPause.setImageResource(R.drawable.pause_icon_2)
                    updateSeekBar(mediaPlayer!!, seekBar, lblCurrent, lblTotal)
                }
            }
        }

        // 🔹 Manual seek
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    lblCurrent.text = milliSecondsToTimer(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    private fun updateSeekBar(
        mediaPlayer: MediaPlayer,
        seekBar: SeekBar,
        lblCurrent: TextView,
        lblTotal: TextView
    ) {
        handler?.removeCallbacks(updateRunnable ?: return)

        handler = Handler(Looper.getMainLooper())
        updateRunnable = object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                    lblCurrent.text = milliSecondsToTimer(mediaPlayer.currentPosition.toLong())
                    lblTotal.text = milliSecondsToTimer(mediaPlayer.duration.toLong())
                    handler?.postDelayed(this, 500)
                }
            }
        }
        handler?.post(updateRunnable!!)
    }

    fun milliSecondsToTimer(milliseconds: Long): String {
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = ((milliseconds % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val seconds = ((milliseconds % (1000 * 60)) / 1000).toInt()

        val minutesString = if (minutes < 10) "0$minutes" else "$minutes"
        val secondsString = if (seconds < 10) "0$seconds" else "$seconds"

        return if (hours > 0) "$hours:$minutesString:$secondsString"
        else "$minutesString:$secondsString"
    }

    private fun releaseMediaPlayer() {
        handler?.removeCallbacks(updateRunnable ?: return)
        updateRunnable = null
        handler = null

        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, SchoolDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
