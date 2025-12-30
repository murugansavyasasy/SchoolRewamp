package com.vs.schoolmessenger.Dashboard.Settings.WhatsNew

import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.Model.WhatsNewUpdateData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ActivityWhatsNewBinding

class WhatsNewActivity : BaseActivity<ActivityWhatsNewBinding>(), View.OnClickListener {
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var whatsnewAdapter: WhatsNewAdapter? = null
    private var currentPosition = 0
    private var isChildDetails: ChildDetails? = null
    private var isStaffDetails: StaffDetails? = null
    var userDetails: UserDetails? = null

    override fun getViewBinding(): ActivityWhatsNewBinding {
        return ActivityWhatsNewBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.imgBack.setOnClickListener(this)
        isChildDetails = SharedPreference.getChildDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        userDetails = SharedPreference.getUserDetails(this)

        if (Constant.isParentChoose) {
            isAccessToken = isChildDetails?.access_token
        } else {
            if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                isAccessToken = isStaffDetails!!.access_token
            } else {
                isAccessToken = userDetails!!.staff_details[0].access_token
            }
        }
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        loadwhatsnewdata()
        appViewModel?.getdashboardnewupdates?.observe(this) { response ->
            if (response != null && response.status) {
                binding.rcywhatsnew.visibility = View.VISIBLE
                binding.lytList.visibility = View.GONE
                getWhatsNewData(response.data)

            } else {
                binding.rcywhatsnew.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text =
                    getString(R.string.something_went_wrong_please_try_again_later)
            }
        }
    }

    private fun loadwhatsnewdata() {
        whatsnewAdapter = WhatsNewAdapter(
            null,
            this,
            Constant.isShimmerViewShow,
            binding.rcywhatsnew
        )

        binding.rcywhatsnew.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcywhatsnew.adapter = whatsnewAdapter

        if (Constant.isParentChoose) {
            appViewModel!!.getdashboardnewupdates(isAccessToken!!, Constant.parent,this)
        } else {
            appViewModel!!.getdashboardnewupdates(
                isAccessToken!!,
                Constant.user_details!!.staff_role,this
            )
        }
    }

    private fun getWhatsNewData(data: List<WhatsNewUpdateData>?) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcywhatsnew.layoutManager = layoutManager

        whatsnewAdapter = WhatsNewAdapter(
            data,
            this,
            isLoading = false,
            binding.rcywhatsnew
        )
        binding.rcywhatsnew.adapter = whatsnewAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rcywhatsnew)

        binding.rcywhatsnew.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val view = snapHelper.findSnapView(layoutManager)
                val position =
                    if (view != null) layoutManager.getPosition(view) else RecyclerView.NO_POSITION
                if (position != RecyclerView.NO_POSITION && position != currentPosition) {
                    currentPosition = position
                    updateDotIndicator(position)
                }
            }
        })

        data?.let {
            if (it.size > 1) {
                setupDots(it.size)
                binding.dotIndicatorContainer.visibility = View.VISIBLE
            } else {
                binding.dotIndicatorContainer.visibility = View.GONE
                currentPosition = 0
            }
        }
    }


    private fun setupDots(count: Int) {
        binding.dotIndicatorContainer.removeAllViews()

        for (i in 0 until count) {
            val dot = ImageView(this)
            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            dot.setImageResource(R.drawable.dot_unselected)
            binding.dotIndicatorContainer.addView(dot)
        }
        updateDotIndicator(0)
    }

    private fun updateDotIndicator(position: Int) {
        for (i in 0 until binding.dotIndicatorContainer.childCount) {
            val dot = binding.dotIndicatorContainer.getChildAt(i) as ImageView
            dot.setImageResource(
                if (i == position) R.drawable.dot_selected else R.drawable.dot_unselected
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }
}
