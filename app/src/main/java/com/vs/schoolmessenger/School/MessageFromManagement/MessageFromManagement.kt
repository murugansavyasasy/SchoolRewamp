package com.vs.schoolmessenger.School.MessageFromManagement

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentFilePathAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentDataReport
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.AttachmentMediaAdapter
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.MessageFromStaffAdapter
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.RoundedBackgroundSpan
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.MessageFromManagementBinding
import me.relex.circleindicator.CircleIndicator2


class MessageFromManagement : BaseActivity<MessageFromManagementBinding>(),
    View.OnClickListener,MsgStaffListener {

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
    var isMenuCount=-1
    private var isDialogShowing = false


    var TYPE: String? = ""
    var selectedSchoolId=""
    var isMultipleSchool=false


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
        isStaffDetails = SharedPreference.getStaffDetails(this)

        if (userDetails?.staff_role.equals(Constant.isStaffRole)){
            binding.rytSpinner.visibility=View.GONE
            isAccessToken = isStaffDetails!!.access_token
            isMultipleSchool=false
            isGetMessageFromStaff()
            binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
            binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
        }
        else{
            if (userDetails?.staff_details?.size!! > 1) {
                binding.rytSpinner.visibility = View.VISIBLE
                isMultipleSchool=true
                //Important Note:see actually what ever token we pass,From backend we recieve all the data from all school we are suppose to filter them using the school id this scenrio is for multiple school
                userDetails?.let { setupSchoolSpinner(it.staff_details) }
            }
            else{
                isAccessToken = userDetails!!.staff_details.get(0).access_token
                binding.rytSpinner.visibility=View.GONE
                isMultipleSchool=false
                isGetMessageFromStaff()
                binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
                binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name
            }
        }

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        isMenuCount=Constant.isSchoolMenuCount

        appViewModel?.isGetMessageStaff?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcMessageStaff.visibility = View.VISIBLE
//                    binding.lytList2.visibility = View.GONE
                    binding.lytList.visibility = View.GONE
                    isLoadMsgStaff(response.data)
                    completeAttachmentList=response.data
                }
                else {
                    binding.rytSpinner.visibility=View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                    binding.rytSearch1.visibility = View.GONE
                    binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                    binding.rcMessageStaff.visibility = View.GONE
//                    binding.lytList2.visibility = View.VISIBLE
                    ErrorMessage(response.message)
                }
            } else {
                binding.rytSpinner.visibility=View.GONE
                binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                binding.rytSearch1.visibility = View.GONE
                binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                binding.rcMessageStaff.visibility = View.GONE
//                binding.lytList2.visibility = View.VISIBLE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }

        appViewModel?.isGetMessageStaffArchive?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                   if(response.data.isNotEmpty()){
                       val updatedList = completeAttachmentList.toMutableList()
                       updatedList.addAll(response.data)
                       completeAttachmentList = updatedList
                       isMsgStaff=completeAttachmentList

                       // Reinitialize adapter if shimmer was active
                       if (!::adapter.isInitialized || adapter.getItemViewType(0) == 0) {
                           adapter = MessageFromStaffAdapter(mutableListOf(), this, this, Constant.isShimmerViewDisable)
                           binding.rcMessageStaff.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
                           binding.rcMessageStaff.adapter = adapter
                           binding.rcMessageStaff.isNestedScrollingEnabled = false
                       }

                       if (isMultipleSchool){
                           if (selectedSchoolId==Constant.All_){
                               adapter.AppendData(response.data)
                           }
                           else{
                               val filteredList = completeAttachmentList.filter { it.school_id == selectedSchoolId }
                               Log.d("SpinnerSelection", "Selected school id: ${selectedSchoolId}, " +"Data: $filteredList, Token: $isAccessToken")
                               adapter.AppendData(filteredList)
                           }
                       }
                       else{
                           //if role is staff or only handle one school means we are directly update the response direclty to adapter
                           adapter.AppendData(response.data)
                       }

                       if(adapter!!.getCurrentListSize()>0){
                           binding.rytSearch1.visibility = View.GONE
                           binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                           binding.txtSearch1.text.clear()
                       }
                       else{
                           binding.rytSearch1.visibility = View.GONE
                           binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                           binding.txtSearch1.text.clear()
                       }

                       ShowData()
//                       binding.lytList2.visibility = View.GONE
                       binding.txtSearch1.text.clear()
                       binding.isArchiveErrorMsg.visibility=View.GONE
                   }
                    else{
                       binding.isArchiveErrorMsg.visibility=View.VISIBLE
                       binding.isArchiveErrorMsg.text=response.message
                       if(adapter.getCurrentListSize()==0){
//                           binding.lytList2.visibility = View.VISIBLE
                           binding.lytList.visibility = View.VISIBLE
                           binding.txtNoData.visibility=View.GONE
                           binding.rytSearch1.visibility = View.GONE
                           binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                           binding.txtSearch1.text.clear()

                       }else{
//                           binding.lytList2.visibility = View.VISIBLE
                           binding.lytList.visibility = View.GONE
                           // Set top margin to 15dp dynamically
                           val layoutParams = binding.isArchiveErrorMsg.layoutParams as ViewGroup.MarginLayoutParams
                           val topMarginInDp = TypedValue.applyDimension(
                               TypedValue.COMPLEX_UNIT_DIP,
                               15f,
                               resources.displayMetrics
                           ).toInt()
                           layoutParams.topMargin = topMarginInDp
                           binding.isArchiveErrorMsg.layoutParams = layoutParams
                           binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                       }
                   }
                }
                else {
                    binding.isArchiveErrorMsg.visibility=View.VISIBLE
                    binding.isArchiveErrorMsg.text=response.message
                    if(adapter.getCurrentListSize()==0){
//                        binding.lytList2.visibility = View.VISIBLE
                        binding.lytList.visibility = View.VISIBLE
                        binding.txtNoData.visibility=View.GONE
                        binding.rytSearch1.visibility = View.GONE
                        binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                        binding.txtSearch1.text.clear()

                    }else{
//                        binding.lytList2.visibility = View.VISIBLE
                        binding.lytList.visibility = View.GONE
                        // Set top margin to 15dp dynamically
                        val layoutParams = binding.isArchiveErrorMsg.layoutParams as ViewGroup.MarginLayoutParams
                        val topMarginInDp = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            15f,
                            resources.displayMetrics
                        ).toInt()
                        layoutParams.topMargin = topMarginInDp
                        binding.isArchiveErrorMsg.layoutParams = layoutParams
                        binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                    }
                }
            } else {
                binding.isArchiveErrorMsg.visibility=View.VISIBLE
                binding.isArchiveErrorMsg.text=getString(R.string.something_went_wrong_please_try_again_later)
                if(adapter.getCurrentListSize()==0){
//                    binding.lytList2.visibility = View.VISIBLE
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.visibility=View.GONE
                    binding.rytSearch1.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                    binding.txtSearch1.text.clear()
                }else{
//                    binding.lytList2.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                }
            }
        }


        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearch1.text.clear()
            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtSearch1.text.clear()

            }
        }


        binding.txtSearch1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
                Log.d("Search",s.toString())

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

    private fun setupSchoolSpinner(staffList: List<StaffDetails>) {

        val schoolNames = mutableListOf<String>()
        schoolNames.add("All")
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
                            selectedSchoolId = Constant.All_

                        } else {
                            // Specific school selected
                            val selectedStaff = staffList[position - 1]
                            isAccessToken = selectedStaff.access_token
                            isStaffDetails = selectedStaff
                            selectedSchoolId = selectedStaff.school_id

                            val filteredList = completeAttachmentList.filter { it.school_id == selectedSchoolId }
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
//            binding.lytList2.visibility = View.GONE
            ShowData()
            adapter.updateData(filteredList)
        } else {
            binding.rlaMessageFFromStaff.visibility = View.VISIBLE
            binding.rcMessageStaff.visibility = View.GONE
//            binding.lytList2.visibility = View.VISIBLE
            ErrorMessage(getString(R.string.no_data_found))
        }
    }


    fun ShowData() {
        binding.rlaMessageFFromStaff.visibility = View.VISIBLE
        binding.rcMessageStaff.visibility = View.VISIBLE
        binding.lytList.visibility = View.GONE
    }

//    fun setMessageWithCount(textView: TextView, message: String, count: Int) {
//        val fullText = "$message $count"
//        val spannable = SpannableString(fullText)
//
//        val start = fullText.indexOf(count.toString())
//        val end = start + count.toString().length
//
//        spannable.setSpan(
//            RoundedBackgroundSpan(
//                backgroundColor = ContextCompat.getColor(textView.context, R.color.red),
//                textColor = ContextCompat.getColor(textView.context, R.color.white),
//                cornerRadius = 20f,
//                padding = 15f
//            ),
//            start,
//            end,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//        textView.text = spannable
//    }




    private fun isLoadMsgStaff(data: List<GetMessagesStaffData>) {
        if (data.isNotEmpty()) {
            isMsgStaff=data
            binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
            binding.rytSearch1.visibility = View.GONE
            adapter = MessageFromStaffAdapter(data.toMutableList(),this, this, Constant.isShimmerViewDisable)
            binding.rcMessageStaff.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            binding.rcMessageStaff.adapter = adapter
            binding.rcMessageStaff.isNestedScrollingEnabled = false
            binding.rcMessageStaff.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
            binding.rytSearch1.visibility = View.GONE
            binding.rcMessageStaff.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        }
    }



    fun isGetMessageFromStaff(){
        adapter = MessageFromStaffAdapter(mutableListOf(),this, this, Constant.isShimmerViewShow)
        binding.rcMessageStaff.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.rcMessageStaff.adapter = adapter
        binding.rcMessageStaff.isNestedScrollingEnabled = false
        appViewModel?.isGetMessageStaff(isAccessToken ?: "")
    }

    fun isGetMessageFromStaffArchive(){
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
        if (isDialogShowing ||activity.isFinishing || activity.isDestroyed) return
        isDialogShowing = true


        val dialogView = LayoutInflater.from(activity).inflate(R.layout.msg_from_staff_preview, null)
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
        val lblSentTime = dialogView.findViewById<TextView>(R.id.lblSentTime)
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
                rytDescription.visibility=View.VISIBLE
                tvDescription.visibility=View.VISIBLE
                rlaAudioDetails.visibility=View.GONE
                recyclerView.visibility = View.GONE
                indicator.visibility = View.GONE
                tvDescription.text = data.content
            }

            Constant.VOICE ->{
                rytDescription.visibility=View.GONE
                tvDescription.visibility=View.GONE
                tvAudioTittle.apply {
                    text=data.title
                    isSingleLine = true
                    ellipsize = TextUtils.TruncateAt.END
                    maxLines = 1
                }
                if (data.is_emergency){
                    lblEmergency.text=getString(R.string.emergency_voice)
                    imgEmergency.visibility=View.VISIBLE
                }
                else{
                    lblEmergency.text=getString(R.string.voice)
                    imgEmergency.visibility=View.GONE
                }
                tvDescription.text = data.description
                rlaAudioDetails.visibility=View.VISIBLE
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
                rytDescription.visibility=View.VISIBLE
                tvDescription.visibility=View.VISIBLE
                rlaAudioDetails.visibility=View.GONE
                recyclerView.visibility = View.VISIBLE
                indicator.visibility = View.VISIBLE
                tvDescription.text = data.description

                if (data.file_path.isNullOrEmpty()) {
                    indicator.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                } else {
                    indicator.visibility = View.VISIBLE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
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
            R.id.lblArchiveMsg->{
                binding.txtSearch1.text.clear()
                isGetMessageFromStaffArchive()
                binding.lblArchiveMsg.visibility=View.GONE
            }
        }
    }

    override fun onStaffClick(data: GetMessagesStaffData) {
        Log.d("SelectedData",data.toString())
        showResumeListDialog(this,data)
        if (data.is_unread){
            //            isMenuCount-=1
            //            setMessageWithCount(binding.toolbarLayout.lblParentToolBar, Constant.isSchoolMenuName,isMenuCount )
            if(data.type.equals(Constant.TET2)){
                TYPE = Constant.MGMT_MSG_TEXT
            }
            else if(data.type.equals(Constant.VOICE)){
                TYPE = Constant.MGMT_MSG_VOICE
            }
            else{
                TYPE = Constant.MGMT_MSG_ATTACHMENT
            }

            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.type, TYPE)
                addProperty(APIKeyNames.detail_id, data.id)
            }

            if (data.is_archive){
                appViewModel?.isUpdateStatusArchive(isAccessToken!!, jsonObject, this)
            }
            else{
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


}