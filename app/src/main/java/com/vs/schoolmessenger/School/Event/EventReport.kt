package com.vs.schoolmessenger.School.Event

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventAdapter
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventCompletedAdapter
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventUpcomingAdapter
import com.vs.schoolmessenger.School.Event.Listener.SchoolEventClickListener
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.EventReportBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class EventReport  : BaseActivity<EventReportBinding>(),
    View.OnClickListener,
    SchoolEventClickListener {

    override fun getViewBinding(): EventReportBinding {
        return EventReportBinding.inflate(layoutInflater)
    }

    var isTotalSelectedItem = 0
    private var cameraPermissionDeniedCount = 0

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val MAX_FILES = 10
        internal const val CAMERA_IMAGE_REQUEST = 1004
    }

    var isEventId = ""
    var isEventPosition = 0
    var isSelectedCategory = ""

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    var isFromTime = true
    private var isStaffDetails: StaffDetails? = null
    private var selectedDateField: Int = 0
    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    lateinit var schooleventAdapter: SchoolEventAdapter
    lateinit var eventupcomingadapter: SchoolEventUpcomingAdapter
    lateinit var eventcompletedadapter: SchoolEventCompletedAdapter
    private var allOngoingEvents: List<SchoolEventItem>? = null
    private var allUpcomingEvents: List<SchoolEventItem>? = null
    private var allCompletedEvents: List<SchoolEventItem>? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.rytSearch323.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isAwsUploadingPreSigned = AwsUploadingPreSigned()

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        binding.toolbarLayout.layoutCreateSlot.visibility = View.VISIBLE
        binding.toolbarLayout.layoutCreateSlot.setOnClickListener {
            val intent = Intent(this, CreateEvent::class.java)
            startActivity(intent)
        }



        appViewModel?.IsGetEventSchoolReport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response?.status == true && !response.data.isNullOrEmpty()) {

                val data = response.data[0]

                allOngoingEvents = data.on_going
                allUpcomingEvents = data.up_coming
                allCompletedEvents = data.completed

                binding.dotindicator.visibility =
                    if (!allOngoingEvents.isNullOrEmpty() && allOngoingEvents!!.size > 1) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                updateVisibility(
                    allOngoingEvents,
                    binding.rcyongoingevent,
                    binding.headerview
                )
                updateVisibility(
                    allUpcomingEvents,
                    binding.rcyupcomingevent,
                    binding.upcomingeventHeaderview
                )
                updateVisibility(
                    allCompletedEvents,
                    binding.rcycompletedevent,
                    binding.completedeventHeaderview
                )

                isloadeventData(allOngoingEvents)
                isloadUpcomingData(allUpcomingEvents)
                isloadCompletedData(allCompletedEvents)

            } else {
                hideAllSections()
            }
        }


        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (::schooleventAdapter.isInitialized) schooleventAdapter.filter.filter(s)
                if (::eventcompletedadapter.isInitialized) eventcompletedadapter.filter.filter(s)
                if (::eventupcomingadapter.isInitialized) eventupcomingadapter.filter.filter(s)
                val isAllEmpty = schooleventAdapter.itemCount == 0 &&
                        eventupcomingadapter.itemCount == 0 &&
                        eventcompletedadapter.itemCount == 0

                binding.noDataImage.visibility = if (isAllEmpty) View.VISIBLE else View.GONE
                binding.noDataText.visibility = if (isAllEmpty) View.VISIBLE else View.GONE

                if (schooleventAdapter.itemCount > 0) {
                    binding.rcyongoingevent.visibility = View.VISIBLE
                    binding.headerview.visibility = View.VISIBLE
                } else {
                    binding.rcyongoingevent.visibility = View.GONE
                    binding.headerview.visibility = View.GONE
                }

                if (eventupcomingadapter.itemCount > 0) {
                    binding.rcyupcomingevent.visibility = View.VISIBLE
                    binding.upcomingeventHeaderview.visibility = View.VISIBLE
                } else {
                    binding.rcyupcomingevent.visibility = View.GONE
                    binding.upcomingeventHeaderview.visibility = View.GONE
                }

                if (eventcompletedadapter.itemCount > 0) {
                    binding.rcycompletedevent.visibility = View.VISIBLE
                    binding.completedeventHeaderview.visibility = View.VISIBLE
                } else {
                    binding.rcycompletedevent.visibility = View.GONE
                    binding.completedeventHeaderview.visibility = View.GONE
                }
            }
        })

        schooleventAdapter = SchoolEventAdapter(
            mutableListOf(),
            this,
            this,
            Constant.isShimmerViewDisable
        )
        binding.recyclerView.adapter = schooleventAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)


        fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            schooleventAdapter.filter.filter(s)

            binding.recyclerView.post {
                if (schooleventAdapter.itemCount == 0) {
                    binding.noDataImage.visibility = View.VISIBLE
                    binding.noDataText.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.noDataImage.visibility = View.GONE
                    binding.noDataText.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        }

        appViewModel!!.isEventDelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@EventReport)
                    eventupcomingadapter.removeItemAt(isEventPosition)
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        loadeventdata()
    }


    private fun <T> updateVisibility(
        dataList: List<T>?, recyclerView: RecyclerView, vararg headers: View,
    ) {
        if (!dataList.isNullOrEmpty()) {
            recyclerView.visibility = View.VISIBLE
            headers.forEach { it.visibility = View.VISIBLE }
        } else {
            recyclerView.visibility = View.GONE
            headers.forEach { it.visibility = View.GONE }
        }
    }


    private fun hideAllSections() {
        updateVisibility(
            emptyList<Any>(), binding.rcyongoingevent, binding.headerview
        )
        updateVisibility(
            emptyList<Any>(), binding.rcyupcomingevent, binding.upcomingeventHeaderview
        )
        updateVisibility(
            emptyList<Any>(), binding.rcycompletedevent, binding.completedeventHeaderview
        )
    }


    private fun loadeventdata() {
        Constant.showLoading(this)

        schooleventAdapter =
            SchoolEventAdapter(mutableListOf(), this, this, Constant.isShimmerViewDisable)
        binding.rcyongoingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyongoingevent.isNestedScrollingEnabled = false
        binding.rcyongoingevent.adapter = schooleventAdapter

        eventupcomingadapter =
            SchoolEventUpcomingAdapter(mutableListOf(), this, this, Constant.isShimmerViewDisable)
        binding.rcyupcomingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcyupcomingevent.isNestedScrollingEnabled = false
        binding.rcyupcomingevent.adapter = eventupcomingadapter

        eventcompletedadapter =
            SchoolEventCompletedAdapter(mutableListOf(), this, this, Constant.isShimmerViewDisable)
        binding.rcycompletedevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcycompletedevent.isNestedScrollingEnabled = false
        binding.rcycompletedevent.adapter = eventcompletedadapter


        appViewModel!!.IsGetEventSchoolReport(isAccessToken!!, this)
    }


    private fun isloadeventData(newData: List<SchoolEventItem>?) {
        val list = newData?.toMutableList() ?: mutableListOf()
        if (::schooleventAdapter.isInitialized) {
            schooleventAdapter.updateList(list)
        } else {
            schooleventAdapter = SchoolEventAdapter(list, this, this, Constant.isShimmerViewDisable)
            binding.rcyongoingevent.adapter = schooleventAdapter
        }
    }


    private fun isloadUpcomingData(newData: List<SchoolEventItem>?) {
        eventupcomingadapter =
            SchoolEventUpcomingAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyupcomingevent.adapter = eventupcomingadapter
    }

    private fun isloadCompletedData(newData: List<SchoolEventItem>?) {
        eventcompletedadapter =
            SchoolEventCompletedAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcycompletedevent.adapter = eventcompletedadapter
    }


    override fun onSearchResultEmpty(type: String, isEmpty: Boolean) {
        when (type) {
            Constant.ONGOING -> {
                binding.rcyongoingevent.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.headerview.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }

            Constant.UPCOMING -> {
                binding.rcyupcomingevent.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.upcomingeventHeaderview.visibility =
                    if (isEmpty) View.GONE else View.VISIBLE
            }

            Constant.COMPLETED -> {
                binding.rcycompletedevent.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.completedeventHeaderview.visibility =
                    if (isEmpty) View.GONE else View.VISIBLE
            }
        }
        val isAllEmpty =
            schooleventAdapter.itemCount == 0 &&
                    eventupcomingadapter.itemCount == 0 &&
                    eventcompletedadapter.itemCount == 0

        binding.noDataImage.visibility = if (isAllEmpty) View.VISIBLE else View.GONE
        binding.noDataText.visibility = if (isAllEmpty) View.VISIBLE else View.GONE
    }

    override fun onDeleteEvent(type: String?, id: String?, position: Int) {
        showConfirmationDialog(
            title = getString(R.string.delete_event),
            message = getString(R.string.are_you_sure_you_want_to_delete_this_event),
            activity = this
        ) {
            val json = JSONObject()
            json.put(APIKeyNames.id, id)
            json.toString().toRequestBody("application/json".toMediaTypeOrNull())

//            appViewModel?.isEventDelete(isAccessToken!!, requestBody, this)

            appViewModel!!.isEventDelete?.observe(this) { response ->
                if (response != null) {
                    if (response.status) {
                        Constant.hideLoading(this@EventReport)
                        eventupcomingadapter.removeItemAt(position)
                    } else {
                        showConfirmationDialog(
                            resources.getString(R.string.fail),
                            response.message,
                            this
                        ) {}
                    }
                }
            }
        }
    }

    fun showEditDeletePopup(data: SchoolEventItem, anchor: View) {
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
            Constant.isClickEdit=true
            val intent = Intent(this, CreateEvent::class.java)
            intent.putExtra(Constant.event_data, data)
            startActivity(intent)
           // isEditProcess(data)
            popupWindow.dismiss()
        }

        layoutDelete.setOnClickListener {
            showSendConfirmationDialog(false)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, 0, 10)
    }

    fun showSendConfirmationDialog(isEventUpdate: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
//        if (isEventUpdate) {
//            alertMessage.text = getString(R.string.are_you_sure_want_to_update_this_event)
//        } else {
        alertMessage.text = getString(R.string.are_you_sure_want_to_delete)
        //    }

        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
//            if (isEventUpdate) {
//                ProgressDialogHelper.show(this)
//                ProgressDialogHelper.updateProgress(10)
//             //   isUploadFilesInServer(Constant.file_)
//            } else {
            val jsonObject = JsonObject()
            jsonObject.addProperty(APIKeyNames.id, isEventId)
            appViewModel?.isEventDelete(isAccessToken!!, jsonObject, this)
            //  }
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    override fun onEditAndDelete(
        data: SchoolEventItem, anchorView: View, adapterPosition: Int
    ) {
        isEventId = data.id
        isEventPosition = adapterPosition
        showEditDeletePopup(data, anchorView)
    }

    override fun onClick(v: View?) {

    }

    fun showConfirmationDialog(
        title: String,
        message: String,
        activity: Activity,
        onConfirm: () -> Unit
    ) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val titleText = view.findViewById<TextView>(R.id.alertTitle)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        val cancelButton = view.findViewById<TextView>(R.id.btnCancel)

        titleText.text = title
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.text = getString(R.string.confirm)
        cancelButton.visibility = View.VISIBLE
        cancelButton.text = getString(R.string.Cancel)

        okButton.setOnClickListener {
            closePopup()
            onConfirm()
        }

        cancelButton.setOnClickListener {
            closePopup()
        }
    }
}