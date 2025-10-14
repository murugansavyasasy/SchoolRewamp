package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty

import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventCategoryAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventCompletedAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventUpcomingAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.Category
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.EventItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
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

    private var fromNotification: Boolean = false

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        msg_id = intent.getIntExtra(Constant.msg_id, -1)

        fromNotification = intent.getBooleanExtra("fromNotification", false)

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

                if (selectedCategory == null || selectedCategory?.name.equals("All", true)) {
                    if (::mAdapter.isInitialized) mAdapter.filter.filter(query)
                    if (::eventcompletedadapter.isInitialized) eventcompletedadapter.filter.filter(query)
                    if (::eventupcomingadapter.isInitialized) eventupcomingadapter.filter.filter(query)
                } else {
                    val categoryName = selectedCategory?.name ?: ""

                    val ongoingFiltered = allOngoingEvents?.filter { it.category == categoryName }
                    mAdapter.updateList(
                        if (query.isEmpty()) ongoingFiltered else ongoingFiltered?.filter { it.title.contains(query, true) }
                    )

                    val upcomingFiltered = allUpcomingEvents?.filter { it.category == categoryName }
                    eventupcomingadapter.updateList(
                        if (query.isEmpty()) upcomingFiltered else upcomingFiltered?.filter { it.title.contains(query, true) }
                    )

                    val completedFiltered = allCompletedEvents?.filter { it.category == categoryName }
                    eventcompletedadapter.updateList(
                        if (query.isEmpty()) completedFiltered else completedFiltered?.filter { it.title.contains(query, true) }
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
                    binding.dotindicator.visibility =
                        if (mAdapter.itemCount > 0) View.VISIBLE else View.GONE

                    binding.rcyupcomingevent.visibility =
                        if (eventupcomingadapter.itemCount > 0) View.VISIBLE else View.GONE
                    binding.upcomingeventHeaderview.visibility =
                        if (eventupcomingadapter.itemCount > 0) View.VISIBLE else View.GONE

                    binding.rcycompletedevent.visibility =
                        if (eventcompletedadapter.itemCount > 0) View.VISIBLE else View.GONE
                    binding.completedeventHeaderview.visibility =
                        if (eventcompletedadapter.itemCount > 0) View.VISIBLE else View.GONE
                }, 100)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })




        appViewModel?.IsGetEventReport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val data = response.data[0]
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE

                allOngoingEvents = data.on_going
                allUpcomingEvents = data.up_coming
                allCompletedEvents = data.completed

                binding.dotindicator.visibility =
                    if (!allOngoingEvents.isNullOrEmpty() && allOngoingEvents!!.size > 1) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                val isAllEmpty = allOngoingEvents.isNullOrEmpty() &&
                        allUpcomingEvents.isNullOrEmpty() &&
                        allCompletedEvents.isNullOrEmpty()

                binding.lytNoDataFound.visibility = if (isAllEmpty) View.VISIBLE else View.GONE

                binding.dotindicator.visibility =
                    if (!allOngoingEvents.isNullOrEmpty() && allOngoingEvents!!.size > 1) View.VISIBLE else View.GONE


                val CategoryList = data.categories

                updateVisibility(allOngoingEvents, binding.rcyongoingevent, binding.headerview, binding.dotindicator)
                updateVisibility(CategoryList, binding.rcycategoryEvent, binding.categoryHeaderview)
                updateVisibility(allUpcomingEvents, binding.rcyupcomingevent, binding.upcomingeventHeaderview)
                updateVisibility(allCompletedEvents, binding.rcycompletedevent, binding.completedeventHeaderview)

                isloadeventData(allOngoingEvents)
                isloadCategoryData(CategoryList)
                isloadUpcomingData(allUpcomingEvents)
                isloadCompletedData(allCompletedEvents)
                Log.d("Message Id Value Indication",msg_id.toString())
                scrollToMessageId(msg_id)

            } else {
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                hideAllSections()
                binding.lytNoDataFound.visibility = View.VISIBLE
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
    }


    private fun isloadeventData(newData: List<EventItem>?) {
        mAdapter = EventAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyongoingevent.adapter = mAdapter
    }

    private fun isloadCategoryData(newData: List<Category>?) {
        categoryadapter = EventCategoryAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcycategoryEvent.adapter = categoryadapter
    }

    private fun isloadUpcomingData(newData: List<EventItem>?) {
        eventupcomingadapter =
            EventUpcomingAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyupcomingevent.adapter = eventupcomingadapter
    }

    private fun isloadCompletedData(newData: List<EventItem>?) {
        eventcompletedadapter =
            EventCompletedAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcycompletedevent.adapter = eventcompletedadapter
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


    private fun scrollToMessageId(msg_id: Int) {
        if (msg_id == -1) return


        allOngoingEvents?.let { list ->
            val index = list.indexOfFirst { it.id.toIntOrNull() == msg_id }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in ongoing")
                binding.rcyongoingevent.post {
                    binding.rcyongoingevent.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rcyongoingevent, index)
                }
                return
            }
        }

        allUpcomingEvents?.let { list ->
            val index = list.indexOfFirst { it.id.toIntOrNull() == msg_id }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in upcoming")
                binding.rcyupcomingevent.post {
                    binding.rcyupcomingevent.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rcyupcomingevent, index)
                }

                return
            }
        }

        allCompletedEvents?.let { list ->
            val index = list.indexOfFirst { it.id.toIntOrNull() == msg_id }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in completed")
                binding.rcycompletedevent.post {
                    binding.rcycompletedevent.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rcycompletedevent, index)
                }

                return
            }
        }
        Log.d("ScrollDebug", "No index found for msg_id $msg_id")
    }


    private fun highlightItemTemporarily(recyclerView: RecyclerView, position: Int) {
        recyclerView.post {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.itemView?.setBackgroundColor(Color.parseColor("#FFE082"))
            recyclerView.postDelayed({
                viewHolder?.itemView?.setBackgroundColor(Color.TRANSPARENT)
            }, 2000)
        }
    }

    private fun updateDotIndicator() {
        binding.dotindicator.visibility =
            if (mAdapter.itemCount > 1) View.VISIBLE else View.GONE
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

            Log.d("ongoingFiltered", ongoingFiltered!!.size.toString())
            Log.d("ongoingFiltered", ongoingFiltered!!.toString())
            Log.d("upcomingFiltered", upcomingFiltered!!.size.toString())
            Log.d("upcomingFiltered", upcomingFiltered!!.toString())
            Log.d("completedFiltered", completedFiltered!!.size.toString())
            Log.d("completedFiltered", completedFiltered!!.toString())


            if (!ongoingFiltered.isNullOrEmpty()) {
                mAdapter.updateList(ongoingFiltered)
                binding.rcyongoingevent.visibility = View.VISIBLE
                binding.headerview.visibility = View.VISIBLE
                binding.dotindicator.visibility =
                    if (ongoingFiltered.size > 1) View.VISIBLE else View.GONE
            } else {
                binding.rcyongoingevent.visibility = View.GONE
                binding.headerview.visibility = View.GONE
                binding.dotindicator.visibility = View.GONE
            }

            updateDotIndicator()


            if (upcomingFiltered.size > 0) {
                eventupcomingadapter.updateList(upcomingFiltered)
                binding.rcyupcomingevent.visibility = View.VISIBLE
                binding.upcomingeventHeaderview.visibility = View.VISIBLE

            } else {
                binding.rcyupcomingevent.visibility = View.GONE
                binding.upcomingeventHeaderview.visibility = View.GONE

            }

            if (completedFiltered.size > 0) {
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
    }


    override fun onCategoryClicked(data: Category) {
        selectedCategory = data
        Log.d("selectedCategory", selectedCategory.toString())
        binding.toolbarLayout.txtVideoMenu.setText("")
        filterAllEventLists()
    }

}
