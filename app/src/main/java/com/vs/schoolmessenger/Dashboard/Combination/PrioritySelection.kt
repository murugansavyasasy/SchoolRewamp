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

        val isStaff = userDetails?.is_staff
        val isParent = userDetails?.is_parent
        val staff_role = userDetails?.staff_role
        val role_name = userDetails?.role_name


        if(isStaff == true && isParent == true){
            binding.lblTeacher.visibility = View.VISIBLE
            binding.lblParent.visibility = View.VISIBLE
        }

        else if(isStaff == true){
            binding.lblTeacher.visibility = View.VISIBLE
            binding.lblParent.visibility = View.GONE
        }

        else if(isParent == true){
            binding.lblTeacher.visibility = View.GONE
            binding.lblParent.visibility = View.VISIBLE
        }

        if(staff_role.equals(Constant.isStaffRole)){
            binding.btnGo.visibility = View.GONE;
            binding.recyclerViews.visibility = View.VISIBLE;
        }
        else{
            binding.btnGo.visibility = View.VISIBLE
        }

        if (userDetails!!.is_staff) {
            binding.lblTeacher.text = role_name
        }

        if (userDetails!!.is_parent) {
            binding.lblParent.text = "Student"
        }

        binding.btnGo.setOnClickListener {
            val intent = Intent(this, SchoolDashboard::class.java)
//            SharedPreference.putChildDetails(this, userDetails!!)
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
        SharedPreference.putChildDetails(this, data)
        startActivity(intent)
    }
}