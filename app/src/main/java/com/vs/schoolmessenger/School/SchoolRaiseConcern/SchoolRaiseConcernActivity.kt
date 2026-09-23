package com.vs.schoolmessenger.School.SchoolRaiseConcern

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.Parent.RaiseConcern.ParentConcernlistModel.ParentConcern
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.isCommunicationType
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolRaiseConcernBinding


class SchoolRaiseConcernActivity : BaseActivity<SchoolRaiseConcernBinding>(), View.OnClickListener {

    override fun getViewBinding(): SchoolRaiseConcernBinding {
        return SchoolRaiseConcernBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var concernAdapter: SchoolRaiseConcernAdapter
    private var concernList: MutableList<ParentConcern> = mutableListOf()

    private lateinit var actionTakenLauncher: ActivityResultLauncher<Intent>


    private var userDetails: UserDetails? = null
    private var msg_id: Int = -1
    private var headerId: String? = null
    private var instituteId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false



    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

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
        isAccessToken = isStaffDetails!!.access_token

        actionTakenLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                callParentConcernListApi()
            }
        }

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.toolbarLayout.rytSearch.isVisible) {
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.toolbarLayout.txtSearch.setText("")
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.toolbarLayout.txtSearch.windowToken, 0)
            } else {
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.toolbarLayout.txtSearch.setText("")
                binding.toolbarLayout.txtSearch.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(
                    binding.toolbarLayout.txtSearch,
                    InputMethodManager.SHOW_IMPLICIT
                )
            }
        }

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        setupRecyclerView()
        callParentConcernListApi()


        appViewModel?.isParentConcernlist?.observe(this) { response ->
            if (response != null) {
                Constant.hideLoading(this)
                if (response.status && !response.data.isNullOrEmpty()) {
                    val data = response.data ?: emptyList()
                    concernList.clear()
                    concernList.addAll(data)
                    concernAdapter.updateList(concernList)
                    toggleEmptyState(concernList.isEmpty())
                    if (fromNotification) {
                        scrollToMessageId(headerId)
                    }
                } else {
                    toggleEmptyState(true)
                }
            }
        }

        appViewModel?.isActionTakenConcern?.observe(this) { response ->
            if (response != null) {
                Constant.hideLoading(this)
                if (response.status) {
                    callParentConcernListApi()
                     showTopAlertPopup(response.message, this)
                } else {
                    showTopAlertPopup(response.message, this)
                }
            }
        }
    }


    private fun scrollToMessageId(headerId: String?) {
        if (msg_id == -1 || headerId.isNullOrEmpty()) return

        val pos = concernAdapter?.getPositionById(headerId)
            ?: concernList?.indexOfFirst { it.id == headerId } ?: -1

        if (pos == -1) {
            Log.d("ScrollDebug", "No item found with headerId: $headerId")
            return
        }

        Log.d("ScrollDebug", "Scrolling to index $pos")

        binding.rvConcernList.post {
            (binding.rvConcernList.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(pos, 0)

            val listener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        rv.removeOnScrollListener(this)
                        rv.post { highlightItemTemporarily(rv, pos) }
                    }
                }
            }

            binding.rvConcernList.addOnScrollListener(listener)
            binding.rvConcernList.postDelayed({
                highlightItemTemporarily(binding.rvConcernList, pos)
            }, 60)
        }
    }



    private fun highlightItemTemporarily(recyclerView: RecyclerView, position: Int) {
        // Try a few times if not yet bound.
        val maxRetries = 6
        val retryDelay = 80L

        fun tryHighlight(attempt: Int) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            if (viewHolder?.itemView != null) {
                val itemView = viewHolder.itemView
                val originalBackground = itemView.background

                itemView.setBackgroundColor(resources.getColor(R.color.light_yellow_5, null))


                Handler(Looper.getMainLooper()).postDelayed({
                    itemView.background = originalBackground
                }, Constant.TIME_OUT)

            } else if (attempt < maxRetries) {
                recyclerView.postDelayed({ tryHighlight(attempt + 1) }, retryDelay)
            } else {
                Log.d(
                    "ScrollDebug",
                    "Failed to highlight position $position after $maxRetries attempts"
                )
            }
        }

        tryHighlight(0)
    }

    fun showTopAlertPopup(message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
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

        okButton.setOnClickListener {
            isAwsUploadedFiles.clear()
            selectedFiles.clear()
            isCommunicationType = 1
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        binding.rvConcernList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.nomessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.txtNoData.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        concernAdapter = SchoolRaiseConcernAdapter(
            list = concernList,
            context = this,
            onAcknowledgeClick = { concern, description -> onAcknowledgeClicked(concern, description) },
             onActionTakenClick = { concern -> onActionTakenClicked(concern) }
        )
        binding.rvConcernList.layoutManager = LinearLayoutManager(this)
        binding.rvConcernList.adapter = concernAdapter
    }

    private fun callParentConcernListApi() {
        appViewModel!!.isParentConcernlist(isAccessToken!!, this)
    }

    private fun onAcknowledgeClicked(concern: ParentConcern, description: String) {
        val requestBody = JsonObject().apply {
            addProperty("concern_id", concern.id)
            addProperty("action", "acknowledge")
            addProperty("description", description)
            addProperty("student_id", concern.student_id)
        }
        appViewModel!!.isActionTakenConcern(isAccessToken!!, requestBody, this)
    }

    private fun onActionTakenClicked(concern: ParentConcern) {
        val intent = Intent(this, ActionTakenActivity::class.java)
        intent.putExtra("concern_id", concern.id)
        intent.putExtra("student_id", concern.student_id)
        actionTakenLauncher.launch(intent)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}