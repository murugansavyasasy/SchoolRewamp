package com.vs.schoolmessenger.School.Event


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventAdapter
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventCategoryAdapter
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventCompletedAdapter
import com.vs.schoolmessenger.School.Event.Adapter.SchoolEventUpcomingAdapter
import com.vs.schoolmessenger.School.Event.Listener.SchoolEventClickListener
import com.vs.schoolmessenger.School.Event.Model.EventCategory
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem

import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.EventReportBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

import kotlin.collections.isNullOrEmpty

class EventReport : BaseActivity<EventReportBinding>(), View.OnClickListener,
    SchoolEventClickListener {

    override fun getViewBinding(): EventReportBinding {
        return EventReportBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: SchoolEventAdapter
    lateinit var categoryadapter: SchoolEventCategoryAdapter
    lateinit var eventupcomingadapter: SchoolEventUpcomingAdapter
    lateinit var eventcompletedadapter: SchoolEventCompletedAdapter

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private var selectedCategory: EventCategory? = null

    private var allOngoingEvents: List<SchoolEventItem>? = null
    private var allUpcomingEvents: List<SchoolEventItem>? = null
    private var allCompletedEvents: List<SchoolEventItem>? = null


    var isEventId = ""
    var isEventPosition = 0

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

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
        binding.headerview.visibility = View.GONE
        binding.categoryHeaderview.visibility = View.GONE
        binding.upcomingeventHeaderview.visibility = View.GONE
        binding.completedeventHeaderview.visibility = View.GONE
        binding.dotindicator.visibility = View.GONE
        binding.rcycategoryEvent.visibility = View.GONE
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        loadeventdata()

        binding.toolbarLayout.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""

                if (selectedCategory == null || selectedCategory?.name.equals("All", true)) {
                    if (::mAdapter.isInitialized) mAdapter.filter.filter(query)
                    if (::eventcompletedadapter.isInitialized) eventcompletedadapter.filter.filter(
                        query
                    )
                    if (::eventupcomingadapter.isInitialized) eventupcomingadapter.filter.filter(
                        query
                    )
                } else {
                    val categoryName = selectedCategory?.name ?: ""

                    val ongoingFiltered = allOngoingEvents?.filter { it.category == categoryName }
                    mAdapter.updateList(
                        if (query.isEmpty()) ongoingFiltered else ongoingFiltered?.filter {
                            it.title.contains(
                                query,
                                true
                            )
                        }
                    )

                    val upcomingFiltered = allUpcomingEvents?.filter { it.category == categoryName }
                    eventupcomingadapter.updateList(
                        if (query.isEmpty()) upcomingFiltered else upcomingFiltered?.filter {
                            it.title.contains(
                                query,
                                true
                            )
                        }
                    )

                    val completedFiltered =
                        allCompletedEvents?.filter { it.category == categoryName }
                    eventcompletedadapter.updateList(
                        if (query.isEmpty()) completedFiltered else completedFiltered?.filter {
                            it.title.contains(
                                query,
                                true
                            )
                        }
                    )
                }

                binding.root.postDelayed({
                    val isAllEmpty = mAdapter.itemCount == 0 &&
                            eventupcomingadapter.itemCount == 0 &&
                            eventcompletedadapter.itemCount == 0

                    binding.lytNoDataFound.visibility = if (isAllEmpty) View.VISIBLE else View.GONE


                    binding.rcyongoingevent.visibility =
                        if (mAdapter.itemCount > 0) View.VISIBLE else View.GONE
                    binding.headerview.visibility =
                        if (mAdapter.itemCount > 0) View.VISIBLE else View.GONE

                    binding.rcyupcomingevent.visibility =
                        if (eventupcomingadapter.itemCount > 0) View.VISIBLE else View.GONE
                    binding.upcomingeventHeaderview.visibility =
                        if (eventupcomingadapter.itemCount > 0) View.VISIBLE else View.GONE

                    binding.rcycompletedevent.visibility =
                        if (eventcompletedadapter.itemCount > 0) View.VISIBLE else View.GONE
                    binding.completedeventHeaderview.visibility =
                        if (eventcompletedadapter.itemCount > 0) View.VISIBLE else View.GONE
                    updateDotIndicator()
                }, 100)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })




        appViewModel?.IsGetEventSchoolReport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val data = response.data[0]
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE

                allOngoingEvents = data.on_going
                allUpcomingEvents = data.up_coming
                allCompletedEvents = data.completed

                mAdapter.updateList(allOngoingEvents)
                updateDotIndicator()


                val isAllEmpty = allOngoingEvents.isNullOrEmpty() &&
                        allUpcomingEvents.isNullOrEmpty() &&
                        allCompletedEvents.isNullOrEmpty()

                binding.lytNoDataFound.visibility = if (isAllEmpty) View.VISIBLE else View.GONE

                val CategoryList = data.categories

                updateVisibility(
                    allOngoingEvents,
                    binding.rcyongoingevent,
                    binding.headerview
                )
                updateVisibility(CategoryList, binding.rcycategoryEvent, binding.categoryHeaderview)
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
                isloadCategoryData(CategoryList)
                isloadUpcomingData(allUpcomingEvents)
                isloadCompletedData(allCompletedEvents)


            } else {
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                hideAllSections()
                binding.lytNoDataFound.visibility = View.VISIBLE
            }
        }

        appViewModel!!.isEventDelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@EventReport)
                    eventupcomingadapter.removeItemAt(isEventPosition)
                    loadeventdata()
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

    }

    private fun <T> updateVisibility(
        dataList: List<T>?, recyclerView: RecyclerView, vararg headers: View
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
            emptyList<Any>(), binding.rcyongoingevent, binding.headerview, binding.dotindicator
        )
        updateVisibility(emptyList<Any>(), binding.rcycategoryEvent, binding.categoryHeaderview)
        updateVisibility(
            emptyList<Any>(), binding.rcyupcomingevent, binding.upcomingeventHeaderview
        )
        updateVisibility(
            emptyList<Any>(), binding.rcycompletedevent, binding.completedeventHeaderview
        )
    }

    private fun isloadeventData(newData: List<SchoolEventItem>?) {
        if (::mAdapter.isInitialized) {
            mAdapter.updateList(newData)
        } else {
            mAdapter = SchoolEventAdapter(newData, this, this, Constant.isShimmerViewDisable)
            binding.rcyongoingevent.adapter = mAdapter
        }
    }

    private fun isloadCategoryData(newData: List<EventCategory>?) {
        categoryadapter =
            SchoolEventCategoryAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcycategoryEvent.adapter = categoryadapter
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


    private fun loadeventdata() {
        Constant.showLoading(this)
        mAdapter = SchoolEventAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcyongoingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyongoingevent.isNestedScrollingEnabled = false
        binding.rcyongoingevent.adapter = mAdapter


        categoryadapter =
            SchoolEventCategoryAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcycategoryEvent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcycategoryEvent.isNestedScrollingEnabled = false
        binding.rcycategoryEvent.adapter = categoryadapter


        eventupcomingadapter =
            SchoolEventUpcomingAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcyupcomingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcyupcomingevent.isNestedScrollingEnabled = false
        binding.rcyupcomingevent.adapter = eventupcomingadapter


        eventcompletedadapter =
            SchoolEventCompletedAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcycompletedevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcycompletedevent.isNestedScrollingEnabled = false
        binding.rcycompletedevent.adapter = eventcompletedadapter


        appViewModel!!.IsGetEventSchoolReport(isAccessToken!!, this)
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

        if(data.can_edit) {
            layoutEdit.visibility = View.VISIBLE
        } else {
            layoutEdit.visibility = View.GONE
        }

        if(data.can_delete) {
            layoutDelete.visibility = View.VISIBLE
        } else {
            layoutDelete.visibility = View.GONE
        }

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


    fun showEditDeletePopupCompleted(data: SchoolEventItem, anchor: View) {
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


        if(data.can_edit) {
            layoutEdit.visibility = View.VISIBLE
        } else {
            layoutEdit.visibility = View.GONE
        }

        if(data.can_delete) {
            layoutDelete.visibility = View.VISIBLE
        } else {
            layoutDelete.visibility = View.GONE
        }
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

    private fun updateDotIndicator() {
        val ongoingCount = mAdapter.itemCount
        binding.dotindicator.visibility = if (ongoingCount > 1) View.VISIBLE else View.GONE
        Log.d("OngoingCOunt", ongoingCount.toString())
    }


    private fun filterAllEventLists() {
        val selectedId = selectedCategory?.name
        Log.d("selectedId", selectedId.toString())

        if (selectedId.isNullOrEmpty()) {
            mAdapter.updateList(allOngoingEvents)
            eventupcomingadapter.updateList(allUpcomingEvents)
            eventcompletedadapter.updateList(allCompletedEvents)
        } else {
            Log.d("isComing", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            val ongoingFiltered = if (selectedId == "All") {
                allOngoingEvents
            } else {
                allOngoingEvents?.filter { it.category == selectedId }
            }

            val upcomingFiltered = if (selectedId == "All") {
                allUpcomingEvents
            } else {
                allUpcomingEvents?.filter { it.category == selectedId }
            }

            val completedFiltered = if (selectedId == "All") {
                allCompletedEvents
            } else {
                allCompletedEvents?.filter { it.category == selectedId }
            }

            if (!ongoingFiltered.isNullOrEmpty()) {
                mAdapter.updateList(ongoingFiltered)
                binding.rcyongoingevent.visibility = View.VISIBLE
                binding.headerview.visibility = View.VISIBLE
                updateDotIndicator()
            } else {
                binding.rcyongoingevent.visibility = View.GONE
                binding.headerview.visibility = View.GONE
                binding.dotindicator.visibility = View.GONE
            }

            updateDotIndicator()


            if (upcomingFiltered!!.size > 0) {
                eventupcomingadapter.updateList(upcomingFiltered)
                binding.rcyupcomingevent.visibility = View.VISIBLE
                binding.upcomingeventHeaderview.visibility = View.VISIBLE

            } else {
                binding.rcyupcomingevent.visibility = View.GONE
                binding.upcomingeventHeaderview.visibility = View.GONE

            }

            if (completedFiltered!!.size > 0) {
                eventcompletedadapter.updateList(completedFiltered)
                binding.rcycompletedevent.visibility = View.VISIBLE
                binding.completedeventHeaderview.visibility = View.VISIBLE
            } else {
                binding.rcycompletedevent.visibility = View.GONE
                binding.completedeventHeaderview.visibility = View.GONE
            }

        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }


    override fun onSearchResultEmpty(adapterTag: String, isEmpty: Boolean) {
        when (adapterTag) {
            Constant.ONGOING -> {
                binding.rcyongoingevent.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.headerview.visibility = if (isEmpty) View.GONE else View.VISIBLE  // Also hide header if empty
            }
            Constant.COMPLETED -> {
                binding.rcycompletedevent.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.completedeventHeaderview.visibility = if (isEmpty) View.GONE else View.VISIBLE  // Add header
            }
            Constant.UPCOMING -> {
                binding.rcyupcomingevent.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.upcomingeventHeaderview.visibility = if (isEmpty) View.GONE else View.VISIBLE  // Add header
            }
        }

        val isAllEmpty = mAdapter.itemCount == 0 &&
                eventupcomingadapter.itemCount == 0 &&
                eventcompletedadapter.itemCount == 0

        binding.lytNoDataFound.visibility = if (isAllEmpty) View.VISIBLE else View.GONE

        updateDotIndicator()
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

            appViewModel!!.isEventDelete?.observe(this) { response ->
                if (response != null) {
                    if (response.status) {
                        Constant.hideLoading(this@EventReport)
                        loadeventdata()
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

    fun showSendConfirmationDialog(isEventUpdate: Boolean) {
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
            jsonObject.addProperty(APIKeyNames.id, isEventId)
            appViewModel?.isEventDelete(isAccessToken!!, jsonObject, this)

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

    override fun onEditAndDeleteCompleted(
        data: SchoolEventItem,
        anchorView: View,
        adapterPosition: Int
    ) {
        isEventId = data.id
        isEventPosition = adapterPosition
        showEditDeletePopupCompleted(data, anchorView)
    }


    override fun onCategoryClicked(data: EventCategory) {
        selectedCategory = data
        Log.d("selectedCategory", selectedCategory.toString())
        binding.toolbarLayout.txtSearch.setText("")
        filterAllEventLists()
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
