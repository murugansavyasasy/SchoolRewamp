package com.vs.schoolmessenger.Dashboard.Combination

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.RoleSelecionBinding

class PrioritySelection : BaseActivity<RoleSelecionBinding>(), View.OnClickListener,
    PriorityClickListener {

    private lateinit var isStudentDetailAdapter: StudentDetailAdapter
    private lateinit var isStaffDetailAdapter: StaffDetailAdapter

    override fun getViewBinding(): RoleSelecionBinding {
        return RoleSelecionBinding.inflate(layoutInflater)
    }

    var userDetails: UserDetails? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()
        binding.lblParent.setOnClickListener(this)
        binding.lblTeacher.setOnClickListener(this)
        binding.btnGo.setOnClickListener(this)
        isLoadData(true)

        userDetails = SharedPreference.getUserDetails(this@PrioritySelection)
        if (userDetails!!.is_staff) {
            binding.lblTeacher.text = userDetails!!.role_name
        }
        if (userDetails!!.is_parent) {
            binding.lblParent.text = "Student"
        }

        binding.btnGo.setOnClickListener {
            val intent = Intent(this, SchoolDashboard::class.java)
            SharedPreference.putToken(this, userDetails!!.staff_details[0].access_token)
            startActivity(intent)
        }
    }

    private fun isLoadData(isStaff: Boolean) {
        if (isStaff) {
            isStaffDetailAdapter = StaffDetailAdapter(Constant.isStaffDetails, this)
            binding.recyclerViews.layoutManager = LinearLayoutManager(this)
            binding.recyclerViews.adapter = isStaffDetailAdapter
        } else {
            isStudentDetailAdapter = StudentDetailAdapter(Constant.isChildDetails, this, this)
            binding.recyclerViews.layoutManager = LinearLayoutManager(this)
            binding.recyclerViews.adapter = isStudentDetailAdapter
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lblTeacher -> {
                isBackRoundChange(binding.lblTeacher)
            }

            R.id.lblParent -> {
                isBackRoundChange(binding.lblParent)
            }
        }
    }

    private fun isBackRoundChange(isClickingId: TextView) {
        if (isClickingId == binding.lblParent) {
            binding.lblTeacher.background = null
            binding.lblTeacher.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
            binding.btnGo.visibility = View.GONE
            isLoadData(false)
            Constant.isParentChoose = true
        }
        if (isClickingId == binding.lblTeacher) {
            binding.lblParent.background = null
            binding.lblParent.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
            binding.btnGo.visibility = View.VISIBLE
            isLoadData(true)
            Constant.isParentChoose = false

        }

        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.bg_blue)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    override fun onItemClick(data: ChildDetails) {
        val intent = Intent(this, ParentDashboard::class.java)
        SharedPreference.putToken(this, data.access_token)
        startActivity(intent)
    }
}