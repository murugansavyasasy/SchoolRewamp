package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventDataClass
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Adapter.HolidayAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.Holiday
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.EventParentBinding

class Event : BaseActivity<EventParentBinding>(), View.OnClickListener, EventClickListener {

    override fun getViewBinding(): EventParentBinding {
        return EventParentBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: EventAdapter
    lateinit var isHolidayAdapter: HolidayAdapter

    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Event)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        binding.toolbarLayout.lnrParent.visibility = View.VISIBLE
        binding.toolbarLayout.lblStudentName.text = ""
        binding.toolbarLayout.lblStudentSection.text = ""

        binding.toolbarLayout.lblLeftSideBar.text = resources.getText(R.string.HoliDay)
        binding.toolbarLayout.lblRightSideBar.text = resources.getText(R.string.Event)

        loadeventdata()

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::isHolidayAdapter.isInitialized) {
                    isHolidayAdapter.filter.filter(s)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        appViewModel?.IsGetEventReport?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyEvent.visibility = View.VISIBLE
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                isloadeventData(response.data)
            } else {
                binding.rcyEvent.visibility = View.GONE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: "No data found"
            }
        }


        appViewModel?.IsGetHolidayReport?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyEvent.visibility = View.VISIBLE
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                isloadholidayData(response.data)
            } else {
                binding.rcyEvent.visibility = View.GONE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: "No data found"
            }
        }


        binding.toolbarLayout.lblRightSideBar.setOnClickListener {
            isBackRoundChange(binding.toolbarLayout.lblRightSideBar)
            loadeventdata()
        }

        binding.toolbarLayout.lblLeftSideBar.setOnClickListener {
            isBackRoundChange(binding.toolbarLayout.lblLeftSideBar)
            loadHolidayData()
        }
    }

    private fun isloadeventData(newData: List<EventDataClass>?) {
        mAdapter = EventAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyEvent.adapter = mAdapter
    }

    private fun isloadholidayData(newData: List<Holiday>?) {
        isHolidayAdapter = HolidayAdapter(newData, this, Constant.isShimmerViewDisable)
        binding.rcyEvent.adapter = isHolidayAdapter
    }

    private fun loadeventdata() {
        mAdapter = EventAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyEvent.layoutManager = LinearLayoutManager(this)
        binding.rcyEvent.isNestedScrollingEnabled = false
        binding.rcyEvent.adapter = mAdapter
        appViewModel!!.IsGetEventReport(isAccessToken!!, this)
    }

    private fun loadHolidayData() {
        isHolidayAdapter = HolidayAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcyEvent.layoutManager = LinearLayoutManager(this)
        binding.rcyEvent.isNestedScrollingEnabled = false
        binding.rcyEvent.adapter = isHolidayAdapter
        appViewModel!!.IsGetHolidayReport(isAccessToken!!, this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    private fun isBackRoundChange(isClickingId: TextView) {
        if (isClickingId == binding.toolbarLayout.lblRightSideBar) {
            binding.toolbarLayout.lblLeftSideBar.background = null
            binding.toolbarLayout.lblLeftSideBar.setTextColor(
                ContextCompat.getColor(this, R.color.dark_blue)
            )
        }

        if (isClickingId == binding.toolbarLayout.lblLeftSideBar) {
            binding.toolbarLayout.lblRightSideBar.background = null
            binding.toolbarLayout.lblRightSideBar.setTextColor(
                ContextCompat.getColor(this, R.color.dark_blue)
            )
        }

        isClickingId.background =
            ContextCompat.getDrawable(this, R.drawable.white_radious)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.black))
    }
}
