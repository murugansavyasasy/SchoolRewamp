package com.vs.schoolmessenger.CommonScreens.SchoolList

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolListActivityBinding

class SchoolList : BaseActivity<SchoolListActivityBinding>(), SchoolListClickListener,
    View.OnClickListener {

    override fun getViewBinding(): SchoolListActivityBinding {
        return SchoolListActivityBinding.inflate(layoutInflater)
    }

    private val selectedSchoolIds = mutableListOf<Int>()
    var isMultipleSchool = true
    private lateinit var mAdapter: SchoolListAdapter

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.lblMultipleSchool.setOnClickListener(this)
        binding.lblSingleSchool.setOnClickListener(this)
        binding.lblSend.setOnClickListener(this)

        if (Constant.isEmergencyVoiceNoticeBoard!!) {
            binding.lnrTab.visibility = View.GONE
            binding.lblSend.visibility= View.GONE
        } else {
            binding.lnrTab.visibility = View.VISIBLE
            binding.lblSend.visibility= View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        isLoadData()
    }

    private fun isLoadData() {

        mAdapter = SchoolListAdapter(
            isMultipleSchool, selectedSchoolIds, null, this, this, Constant.isShimmerViewShow
        )
        binding.recycleSchools.layoutManager = LinearLayoutManager(this)
        binding.recycleSchools.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter = SchoolListAdapter(
                isMultipleSchool,
                selectedSchoolIds,
                Constant.isStaffDetails,
                this,
                this,
                Constant.isShimmerViewDisable
            )
            binding.recycleSchools.adapter = mAdapter
        }
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblSingleSchool -> {
                binding.lblSend.visibility= View.GONE
                isMultipleSchool = false
                isChangeBackRound(binding.lblSingleSchool)
            }

            R.id.lblMultipleSchool -> {
                binding.lblSend.visibility= View.VISIBLE
                isMultipleSchool = true
                isChangeBackRound(binding.lblMultipleSchool)
            }

            R.id.lblSend -> {
                for (i in selectedSchoolIds.indices) {
                    Log.d("SelectedSchoolId", selectedSchoolIds[i].toString())
                }
            }
        }
    }

    private fun isChangeBackRound(
        lblSelectedTab: TextView
    ) {
        isLoadData()
        // Reset backgrounds and colors
        binding.lblMultipleSchool.background = null
        binding.lblSingleSchool.background = null

        lblSelectedTab.background = ContextCompat.getDrawable(this, R.drawable.white_radious)
        lblSelectedTab.setTextColor(
            ContextCompat.getColor(
                application, R.color.black
            )
        )
    }

    override fun onItemClick(data: StaffDetails) {
        val intent = Intent(this, RecipientActivity::class.java)
        SharedPreference.putStaffDetails(this, data)
        startActivity(intent)
    }
}