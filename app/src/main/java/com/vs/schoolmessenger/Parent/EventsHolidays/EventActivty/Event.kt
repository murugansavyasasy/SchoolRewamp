package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.imgBack.setOnClickListener(this)
        binding.rytSearch.setOnClickListener(this)
        binding.lblStudentName.text = isChildDetails?.name
        binding.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        binding.rcyongoingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcycategoryEvent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyupcomingevent.layoutManager = LinearLayoutManager(this)
        binding.rcycompletedevent.layoutManager = LinearLayoutManager(this)

        loadeventdata()


        appViewModel?.IsGetEventReport?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val onGoingList = response.data[0].on_going
                val CategoryList = response.data[0].categories
                val UpcomingList = response.data[0].up_coming
                val CompletedList = response.data[0].completed
                binding.rcyongoingevent.visibility = View.VISIBLE
                binding.rcycategoryEvent.visibility = View.VISIBLE
                binding.rcyupcomingevent.visibility = View.VISIBLE
                isloadeventData(onGoingList)
                isloadCategoryData(CategoryList)
                isloadUpcomingData(UpcomingList)
                isloadCompletedData(CompletedList)

            } else {
                binding.rcyongoingevent.visibility = View.GONE
                binding.rcycategoryEvent.visibility = View.GONE
                binding.rcyupcomingevent.visibility = View.GONE
                binding.rcycompletedevent.visibility = View.GONE
            }
        }

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
        mAdapter = EventAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyongoingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyongoingevent.isNestedScrollingEnabled = false
        binding.rcyongoingevent.adapter = mAdapter


        categoryadapter = EventCategoryAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcycategoryEvent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcycategoryEvent.isNestedScrollingEnabled = false
        binding.rcycategoryEvent.adapter = categoryadapter


        eventupcomingadapter = EventUpcomingAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyupcomingevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcyupcomingevent.isNestedScrollingEnabled = false
        binding.rcyupcomingevent.adapter = eventupcomingadapter


        eventcompletedadapter = EventCompletedAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcycompletedevent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcycompletedevent.isNestedScrollingEnabled = false
        binding.rcycompletedevent.adapter = eventupcomingadapter



        appViewModel!!.IsGetEventReport(isAccessToken!!, this)
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

    override fun onSearchResultEmpty(isEmpty: Boolean) {

    }

    override fun onCategoryClicked(data: Category) {
        TODO("Not yet implemented")
    }
}
