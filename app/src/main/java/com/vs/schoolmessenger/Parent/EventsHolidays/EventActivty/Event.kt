package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
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


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.imgBack.setOnClickListener(this)
        binding.rytSearch.setOnClickListener(this)
        binding.lblStudentName.text = isChildDetails?.name
        binding.lblStudentSection.text =
            "${isChildDetails?.standard_name} - ${isChildDetails?.section_name}"

        loadeventdata()


        binding.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
                if (::eventcompletedadapter.isInitialized) {
                    eventcompletedadapter.filter.filter(s)
                }
                if (::eventupcomingadapter.isInitialized) {
                    eventupcomingadapter.filter.filter(s)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })





        appViewModel?.IsGetEventReport?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val data = response.data[0]

                allOngoingEvents = data.on_going
                allUpcomingEvents = data.up_coming
                allCompletedEvents = data.completed

                val CategoryList = data.categories

                updateVisibility(
                    allOngoingEvents,
                    binding.rcyongoingevent,
                    binding.headerview,
                    binding.dotindicator
                )
                updateVisibility(CategoryList, binding.rcycategoryEvent, binding.categoryHeaderview)
                updateVisibility(
                    allUpcomingEvents, binding.rcyupcomingevent, binding.upcomingeventHeaderview
                )
                updateVisibility(
                    allCompletedEvents, binding.rcycompletedevent, binding.completedeventHeaderview
                )

                isloadeventData(allOngoingEvents)
                isloadCategoryData(CategoryList)
                isloadUpcomingData(allUpcomingEvents)
                isloadCompletedData(allCompletedEvents)

            } else {
                hideAllSections()
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


    private fun filterAllEventLists() {
        val selectedId = selectedCategory?.name

        if (selectedId.isNullOrEmpty()) {
            mAdapter.updateList(allOngoingEvents)
            eventupcomingadapter.updateList(allUpcomingEvents)
            eventcompletedadapter.updateList(allCompletedEvents)
        } else {
            val ongoingFiltered =
                allOngoingEvents?.filter { eventItem -> eventItem.category == selectedId }
            val upcomingFiltered =
                allUpcomingEvents?.filter { eventItem -> eventItem.category == selectedId }
            val completedFiltered =
                allCompletedEvents?.filter { eventItem -> eventItem.category == selectedId }

            mAdapter.updateList(ongoingFiltered)
            eventupcomingadapter.updateList(upcomingFiltered)
            eventcompletedadapter.updateList(completedFiltered)
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.rytSearch -> if (binding.rytSearch1.isVisible) {
                binding.rytSearch1.visibility = View.GONE
            } else {
                binding.rytSearch1.visibility = View.VISIBLE
            }
        }
    }


    override fun onSearchResultEmpty(adapterTag: String, isEmpty: Boolean) {
        when (adapterTag) {
            "ONGOING" -> binding.rcyongoingevent.visibility =
                if (isEmpty) View.GONE else View.VISIBLE

            "COMPLETED" -> binding.rcycompletedevent.visibility =
                if (isEmpty) View.GONE else View.VISIBLE

            "UPCOMING" -> binding.rcyupcomingevent.visibility =
                if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    override fun onCategoryClicked(data: Category) {
        selectedCategory = data
        filterAllEventLists()
    }

}
