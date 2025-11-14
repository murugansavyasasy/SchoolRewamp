package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty

import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventCategoryAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventCompletedAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventUpcomingAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.Category
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.EventItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.EventRewampBinding

class Event : BaseActivity<EventRewampBinding>(), View.OnClickListener, EventClickListener {

    override fun getViewBinding(): EventRewampBinding {
        return EventRewampBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: EventAdapter
    lateinit var categoryadapter: EventCategoryAdapter
    lateinit var eventupcomingadapter: EventUpcomingAdapter
    lateinit var eventcompletedadapter: EventCompletedAdapter

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private var selectedCategory: Category? = null

    private var allOngoingEvents: List<EventItem>? = null
    private var allUpcomingEvents: List<EventItem>? = null
    private var allCompletedEvents: List<EventItem>? = null


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
            SharedPreference.putChildDetails(this,matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.toolbarLayout.rytSearch.isVisible) {
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.toolbarLayout.txtVideoMenu.setText("")
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.toolbarLayout.txtVideoMenu.windowToken, 0)
            } else {
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.toolbarLayout.txtVideoMenu.setText("")
                binding.toolbarLayout.txtVideoMenu.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(
                    binding.toolbarLayout.txtVideoMenu,
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
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name
        binding.toolbarLayout.lblStudentSection.text =
            "${isChildDetails?.standard_name} - ${isChildDetails?.section_name}"

        loadeventdata()

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""

                // Always filter from full lists (global search, ignores category)
                val ongoingFiltered = if (query.isEmpty()) {
                    allOngoingEvents
                } else {
                    allOngoingEvents?.filter { it.title.contains(query, true) }
                }
                mAdapter.updateList(ongoingFiltered)

                val upcomingFiltered = if (query.isEmpty()) {
                    allUpcomingEvents
                } else {
                    allUpcomingEvents?.filter { it.title.contains(query, true) }
                }
                eventupcomingadapter.updateList(upcomingFiltered)

                val completedFiltered = if (query.isEmpty()) {
                    allCompletedEvents
                } else {
                    allCompletedEvents?.filter { it.title.contains(query, true) }
                }
                eventcompletedadapter.updateList(completedFiltered)

                binding.root.postDelayed({
                    // Same visibility updates as before...
                    val isAllEmpty = mAdapter.itemCount == 0 &&
                            eventupcomingadapter.itemCount == 0 &&
                            eventcompletedadapter.itemCount == 0

                    binding.lytNoDataFound.visibility = if (isAllEmpty) View.VISIBLE else View.GONE

                    binding.rcyongoingevent.visibility = if (mAdapter.itemCount > 0) View.VISIBLE else View.GONE
                    binding.headerview.visibility = if (mAdapter.itemCount > 0) View.VISIBLE else View.GONE
                    binding.dotindicator.visibility = if (mAdapter.itemCount > 1) View.VISIBLE else View.GONE

                    binding.rcyupcomingevent.visibility = if (eventupcomingadapter.itemCount > 0) View.VISIBLE else View.GONE
                    binding.upcomingeventHeaderview.visibility = if (eventupcomingadapter.itemCount > 0) View.VISIBLE else View.GONE

                    binding.rcycompletedevent.visibility = if (eventcompletedadapter.itemCount > 0) View.VISIBLE else View.GONE
                    binding.completedeventHeaderview.visibility = if (eventcompletedadapter.itemCount > 0) View.VISIBLE else View.GONE

                    updateDotIndicator()
                }, 100)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })




        appViewModel?.IsGetEventReport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response?.status == true && !response.data.isNullOrEmpty()) {
                val data = response.data[0]

                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_view_events)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                        addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints("" ?: "", jsonObject)
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE

                allOngoingEvents = data.on_going
                allUpcomingEvents = data.up_coming
                allCompletedEvents = data.completed

                val isAllEmpty = allOngoingEvents.isNullOrEmpty() &&
                        allUpcomingEvents.isNullOrEmpty() &&
                        allCompletedEvents.isNullOrEmpty()

                binding.lytNoDataFound.visibility = if (isAllEmpty) View.VISIBLE else View.GONE

                val CategoryList = data.categories

                updateVisibility(allOngoingEvents, binding.rcyongoingevent, binding.headerview)
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

                updateDotIndicator()

                Log.d("Message Id Value Indication", msg_id.toString())
                scrollToMessageId(headerId)
            } else {
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                hideAllSections()
                binding.lytNoDataFound.visibility = View.VISIBLE
                binding.txtNoDataFound.text=response.message?:getString(R.string.no_data_found)
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
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
        // Add: Ensures dot is GONE
        binding.dotindicator.visibility = View.GONE
    }


    private fun isloadeventData(newData: List<EventItem>?) {
        if (::mAdapter.isInitialized) {
            mAdapter.updateList(newData)
        } else {
            mAdapter = EventAdapter(newData, this, this, Constant.isShimmerViewDisable)
            binding.rcyongoingevent.adapter = mAdapter
        }
    }
    private fun isloadCategoryData(newData: List<Category>?) {
        categoryadapter = EventCategoryAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcycategoryEvent.adapter = categoryadapter
    }

    private fun isloadUpcomingData(newData: List<EventItem>?) {
        if (::eventupcomingadapter.isInitialized) {
            eventupcomingadapter.updateList(newData)
        } else {
            eventupcomingadapter = EventUpcomingAdapter(newData, this, this, Constant.isShimmerViewDisable)
            binding.rcyupcomingevent.adapter = eventupcomingadapter
        }
    }
    private fun isloadCompletedData(newData: List<EventItem>?) {
        if (::eventcompletedadapter.isInitialized) {
            eventcompletedadapter.updateList(newData)
        } else {
            eventcompletedadapter = EventCompletedAdapter(newData, this, this, Constant.isShimmerViewDisable)
            binding.rcycompletedevent.adapter = eventcompletedadapter
        }
    }


    private fun loadeventdata() {
        Constant.showLoading(this)
        mAdapter = EventAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcyongoingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyongoingevent.isNestedScrollingEnabled = false
        binding.rcyongoingevent.adapter = mAdapter


        categoryadapter = EventCategoryAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcycategoryEvent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcycategoryEvent.isNestedScrollingEnabled = false
        binding.rcycategoryEvent.adapter = categoryadapter


        eventupcomingadapter = EventUpcomingAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcyupcomingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcyupcomingevent.isNestedScrollingEnabled = false
        binding.rcyupcomingevent.adapter = eventupcomingadapter


        eventcompletedadapter =
            EventCompletedAdapter(null, this, this, Constant.isShimmerViewDisable)
        binding.rcycompletedevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcycompletedevent.isNestedScrollingEnabled = false
        binding.rcycompletedevent.adapter = eventcompletedadapter


        appViewModel!!.IsGetEventReport(isAccessToken!!, this)
    }


    private fun scrollToMessageId(headerId: String?) {

        allOngoingEvents?.let { list ->
            val index = list.indexOfFirst { it.id== headerId }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in ongoing")
                binding.rcyongoingevent.post {
                    binding.rcyongoingevent.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rcyongoingevent, index)
                }
            } else {
                Log.d("ScrollDebug", "No item found with headerId: $headerId")
            }
        }

        allUpcomingEvents?.let { list ->
            val index = list.indexOfFirst { it.id== headerId }

            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in ongoing")
                binding.rcyupcomingevent.post {
                    binding.rcyupcomingevent.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rcyupcomingevent, index)
                }
            } else {
                Log.d("ScrollDebug", "No item found with headerId: $headerId")
            }
        }

        allCompletedEvents?.let { list ->
            val index = list.indexOfFirst { it.id== headerId }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in ongoing")
                binding.rcycompletedevent.post {
                    binding.rcycompletedevent.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rcycompletedevent, index)
                }
            } else {
                Log.d("ScrollDebug", "No item found with headerId: $headerId")
            }
        }
        Log.d("ScrollDebug", "No index found for msg_id $msg_id")
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

    private fun updateDotIndicator() {
        val ongoingCount = mAdapter.itemCount
        binding.dotindicator.visibility = if (ongoingCount > 1) View.VISIBLE else View.GONE
        Log.d("OngoingCOunt", ongoingCount.toString())
    }


    private fun filterAllEventLists() {
        val selectedId = selectedCategory?.name
        Log.d("selectedId", selectedId.toString())


        val ongoingFiltered: List<EventItem>? = when {
            selectedId.isNullOrEmpty() -> allOngoingEvents
            selectedId.equals("All", ignoreCase = true) -> allOngoingEvents
            else -> allOngoingEvents?.filter { it.category.equals(selectedId, ignoreCase = true) }
        }

        val upcomingFiltered: List<EventItem>? = when {
            selectedId.isNullOrEmpty() -> allUpcomingEvents
            selectedId.equals("All", ignoreCase = true) -> allUpcomingEvents
            else -> allUpcomingEvents?.filter { it.category.equals(selectedId, ignoreCase = true) }
        }

        val completedFiltered: List<EventItem>? = when {
            selectedId.isNullOrEmpty() -> allCompletedEvents
            selectedId.equals("All", ignoreCase = true) -> allCompletedEvents
            else -> allCompletedEvents?.filter { it.category.equals(selectedId, ignoreCase = true) }
        }


        mAdapter.updateList(ongoingFiltered)
        eventupcomingadapter.updateList(upcomingFiltered)
        eventcompletedadapter.updateList(completedFiltered)


        val hasOngoing = !ongoingFiltered.isNullOrEmpty()
        binding.rcyongoingevent.visibility   = if (hasOngoing) View.VISIBLE else View.GONE
        binding.headerview.visibility        = if (hasOngoing) View.VISIBLE else View.GONE
        binding.dotindicator.visibility      = if (hasOngoing && ongoingFiltered!!.size > 1) View.VISIBLE else View.GONE


        val hasUpcoming = !upcomingFiltered.isNullOrEmpty()
        binding.rcyupcomingevent.visibility        = if (hasUpcoming) View.VISIBLE else View.GONE
        binding.upcomingeventHeaderview.visibility = if (hasUpcoming) View.VISIBLE else View.GONE


        val hasCompleted = !completedFiltered.isNullOrEmpty()
        binding.rcycompletedevent.visibility        = if (hasCompleted) View.VISIBLE else View.GONE
        binding.completedeventHeaderview.visibility = if (hasCompleted) View.VISIBLE else View.GONE


        val allEmpty = !hasOngoing && !hasUpcoming && !hasCompleted
        binding.lytNoDataFound.visibility = if (allEmpty) View.VISIBLE else View.GONE
        binding.toolbarLayout.imgSearchToolBar.visibility = if (allEmpty) View.GONE else View.VISIBLE
        binding.toolbarLayout.rytSearch.visibility = if (allEmpty) View.GONE else View.VISIBLE
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }


    override fun onSearchResultEmpty(adapterTag: String, isEmpty: Boolean) {
        when (adapterTag) {
            Constant.ONGOING -> binding.rcyongoingevent.visibility =
                if (isEmpty) View.GONE else View.VISIBLE

            Constant.COMPLETED -> binding.rcycompletedevent.visibility =
                if (isEmpty) View.GONE else View.VISIBLE

            Constant.UPCOMING -> binding.rcyupcomingevent.visibility =
                if (isEmpty) View.GONE else View.VISIBLE
        }

        val isAllEmpty = mAdapter.itemCount == 0 &&
                eventupcomingadapter.itemCount == 0 &&
                eventcompletedadapter.itemCount == 0

        binding.lytNoDataFound.visibility = if (isAllEmpty) View.VISIBLE else View.GONE
        binding.txtNoDataFound.text=getString(R.string.no_data_found)

        updateDotIndicator()
    }


    override fun onCategoryClicked(data: Category) {
        selectedCategory = data
        Log.d("selectedCategory", selectedCategory.toString())
        binding.toolbarLayout.txtVideoMenu.setText("")
        filterAllEventLists()
    }

}
