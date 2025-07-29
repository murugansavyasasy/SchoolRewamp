package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.EventsHolidays.CalendarFragment
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventDataClass
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.HolidayClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.EventParentBinding
import com.vs.schoolmessenger.databinding.EventRewampBinding

class Event : BaseActivity<EventRewampBinding>(), View.OnClickListener{

    override fun getViewBinding(): EventRewampBinding {
        return EventRewampBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: EventAdapter

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
//        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
//        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
//        binding.toolbarLayout.lnrParent.visibility = View.VISIBLE
//
//        binding.toolbarLayout.lblLeftSideBar.text = resources.getText(R.string.HoliDay)
//        binding.toolbarLayout.lblRightSideBar.text = resources.getText(R.string.Event)
        binding.lblStudentName.text = isChildDetails?.name
        binding.lblStudentSection.text = isChildDetails?.standard_name + " - " + isChildDetails?.section_name
//        loadeventdata()
//
//        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (::mAdapter.isInitialized) {
//                    mAdapter.filter.filter(s)
//                }
//            }
//            override fun afterTextChanged(s: Editable?) {}
//        })


//        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (::isHolidayAdapter.isInitialized) {
//                    isHolidayAdapter.filter.filter(s)
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })


//        appViewModel?.IsGetEventReport?.observe(this) { response ->
//            if (response?.status == true && !response.data.isNullOrEmpty()) {
//                binding.rcyEvent.visibility = View.VISIBLE
//                binding.tool5barLayout.rytSearch.visibility = View.VISIBLE
//                binding.nomessage.visibility = View.GONE
//                binding.txtNoData.visibility = View.GONE
//                isloadeventData(response.data)
//            } else {
//                binding.rcyEvent.visibility = View.GONE
//                binding.toolbarLayout.rytSearch.visibility = View.GONE
//                binding.nomessage.visibility = View.VISIBLE
//                binding.txtNoData.visibility = View.VISIBLE
//                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
//            }
//        }
//

//        appViewModel?.IsGetHolidayReport?.observe(this) { response ->
//            if (response?.status == true && !response.data.isNullOrEmpty()) {
//                binding.rcyEvent.visibility = View.GONE
//                binding.toolbarLayout.rytSearch.visibility = View.GONE
//                binding.nomessage.visibility = View.GONE
//                binding.txtNoData.visibility = View.GONE
//                binding.calendarFragmentContainer.visibility = View.VISIBLE
////                isloadholidayData(response.data)
//                val calendarFragment = CalendarFragment.newInstance(response.data)
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.calendarFragmentContainer, calendarFragment)
//                    .commit()
//            } else {
//                binding.rcyEvent.visibility = View.GONE
//                binding.calendarFragmentContainer.visibility = View.GONE
//                binding.toolbarLayout.rytSearch.visibility = View.GONE
//                binding.nomessage.visibility = View.VISIBLE
//                binding.txtNoData.visibility = View.VISIBLE
//                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
//            }
//        }
//
//
//        binding.toolbarLayout.lblRightSideBar.setOnClickListener {
//            binding.toolbarLayout.lblRightSideBar.isEnabled = false
//            binding.toolbarLayout.lblLeftSideBar.isEnabled = true
//            isBackRoundChange(binding.toolbarLayout.lblRightSideBar)
//            loadeventdata()
//        }
//
//        binding.toolbarLayout.lblLeftSideBar.setOnClickListener {
//            binding.toolbarLayout.lblLeftSideBar.isEnabled = false
//            binding.toolbarLayout.lblRightSideBar.isEnabled = true
//            isBackRoundChange(binding.toolbarLayout.lblLeftSideBar)
//            loadHolidayData()
//        }
//    }
//
//    private fun isloadeventData(newData: List<EventDataClass>?) {
//        mAdapter = EventAdapter(newData, this, this, Constant.isShimmerViewDisable)
//        binding.rcyEvent.adapter = mAdapter
//    }
//
////    private fun isloadholidayData(newData: List<Holiday>?) {
////        isHolidayAdapter = HolidayAdapter(newData, this, Constant.isShimmerViewDisable, this)
////        binding.rcyEvent.adapter = isHolidayAdapter
//////    }
//
//    private fun loadeventdata() {
//        clearSearchText()
//        mAdapter = EventAdapter(null, this, this, Constant.isShimmerViewShow)
//        binding.rcyEvent.layoutManager = LinearLayoutManager(this)
//        binding.rcyEvent.isNestedScrollingEnabled = false
//        binding.rcyEvent.adapter = mAdapter
//        appViewModel!!.IsGetEventReport(isAccessToken!!, this)
//    }
//
//    fun clearSearchText() {
//        binding.toolbarLayout.txtVideoMenu.text.clear()
//        binding.toolbarLayout.txtVideoMenu.clearFocus();
//        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(binding.toolbarLayout.txtVideoMenu.windowToken, 0)
//    }
//
//    private fun loadHolidayData() {
//        clearSearchText()
////        isHolidayAdapter = HolidayAdapter(null, this, Constant.isShimmerViewShow, this)
//        appViewModel!!.IsGetHolidayReport(isAccessToken!!, this)
//    }
//

//
//
//    override fun onSearchResultEmpty(isEmpty: Boolean) {
//        if (isEmpty) {
//            binding.nomessage.visibility = View.VISIBLE
//            binding.txtNoData.visibility = View.VISIBLE
//            binding.txtNoData.text = "No matching Event found"
//            binding.rcyEvent.visibility = View.GONE
//        } else {
//            binding.nomessage.visibility = View.GONE
//            binding.txtNoData.visibility = View.GONE
//            binding.rcyEvent.visibility = View.VISIBLE
//        }
//    }
//
//    override fun onSearchHolidayResultEmpty(isEmpty: Boolean) {
//        if (isEmpty) {
//            binding.nomessage.visibility = View.VISIBLE
//            binding.txtNoData.visibility = View.VISIBLE
//            binding.txtNoData.text = "No matching Holiday found"
//            binding.rcyEvent.visibility = View.GONE
//        } else {
//            binding.nomessage.visibility = View.GONE
//            binding.txtNoData.visibility = View.GONE
//            binding.rcyEvent.visibility = View.VISIBLE
//        }
//    }
//
//    private fun loadCalendarFragment() {
//        val fragment = CalendarFragment()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.calendarFragmentContainer, fragment)
//            .commit()
//    }
//
//
//    private fun isBackRoundChange(isClickingId: TextView) {
//        if (isClickingId == binding.toolbarLayout.lblRightSideBar) {
//            binding.toolbarLayout.lblLeftSideBar.background = null
//            binding.relativeLayout.setBackgroundResource(R.drawable.bg_parent_backround)
//            binding.toolbarLayout.lblLeftSideBar.setTextColor(
//                ContextCompat.getColor(this, R.color.dark_blue)
//            )
//
//            binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
//            binding.rcyEvent.visibility = View.VISIBLE
//            binding.calendarFragmentContainer.visibility = View.GONE
//        }
//
//        if (isClickingId == binding.toolbarLayout.lblLeftSideBar) {
//            binding.toolbarLayout.lblRightSideBar.background = null
//            binding.relativeLayout.setBackgroundResource(R.color.white)
//            binding.toolbarLayout.lblRightSideBar.setTextColor(
//                ContextCompat.getColor(this, R.color.dark_blue)
//            )
//
//            binding.toolbarLayout.rytSearch.visibility = View.GONE
//            binding.rcyEvent.visibility = View.GONE
//            binding.calendarFragmentContainer.visibility = View.VISIBLE
//            loadCalendarFragment()
//        }
//
//        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.white_radious)
//        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.black))
//    }

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
}
