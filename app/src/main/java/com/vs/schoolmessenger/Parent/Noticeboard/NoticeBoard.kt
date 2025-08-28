package com.vs.schoolmessenger.Parent.Noticeboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.NoticeBoardAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.NoticeRevampBinding

class NoticeBoard : BaseActivity<NoticeRevampBinding>(), View.OnClickListener,
    NoticeBoardClickListener {

    override fun getViewBinding(): NoticeRevampBinding {
        return NoticeRevampBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: NoticeBoardAdapter
    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        isGetNoticeBoardList()
        binding.lblStudentName.text = isChildDetails?.name
        binding.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name
        binding.imgBack.setOnClickListener(this)

        binding.rytSearch.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)

        binding.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        appViewModel?.isNoticeBoardReport?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyNoticeBoard.visibility = View.VISIBLE
                binding.rytSearch.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                isloadhomeworkData(response.data)
            } else {
                binding.rcyNoticeBoard.visibility = View.GONE
                binding.rytSearch.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: "No data found"
            }
        }

        val channel = NotificationChannel(
            "reminder_channel",
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

    }


    private fun isloadhomeworkData(newData: List<Notice>?) {
        mAdapter =
            NoticeBoardAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyNoticeBoard.adapter = mAdapter
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.rytSearch -> {
                if (binding.rytsearch.visibility == View.VISIBLE) {
                    binding.rytsearch.visibility = View.GONE
                    binding.txtVideoMenu.setText("")
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.txtVideoMenu.windowToken, 0)
                } else {
                    binding.rytsearch.visibility = View.VISIBLE
                    binding.txtVideoMenu.setText("")
                    binding.txtVideoMenu.requestFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.txtVideoMenu, InputMethodManager.SHOW_IMPLICIT)
                }
            }
            R.id.imgSearch -> {
                if (binding.rytsearch.isVisible) {
                    binding.rytsearch.visibility = View.GONE
                } else {
                    binding.rytsearch.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_notices_found)
            binding.rcyNoticeBoard.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcyNoticeBoard.visibility = View.VISIBLE
        }
    }


    private fun isGetNoticeBoardList() {
        mAdapter = NoticeBoardAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyNoticeBoard.layoutManager = GridLayoutManager(this, 2)
        binding.rcyNoticeBoard.isNestedScrollingEnabled = false
        binding.rcyNoticeBoard.adapter = mAdapter
        appViewModel!!.isNoticeBoardReport(
            isAccessToken!!, this
        )
    }
}