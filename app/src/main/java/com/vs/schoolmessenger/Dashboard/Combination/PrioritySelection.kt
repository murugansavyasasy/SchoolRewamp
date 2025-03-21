package com.vs.schoolmessenger.Dashboard.Combination

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.School.Dashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.RoleSelecionBinding

class PrioritySelection : BaseActivity<RoleSelecionBinding>(), View.OnClickListener {

    private lateinit var isStudentDetailAdapter: StudentDetailAdapter
    private lateinit var isStaffDetailAdapter: StaffDetailAdapter

    override fun getViewBinding(): RoleSelecionBinding {
        return RoleSelecionBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()
        binding.lblParent.setOnClickListener(this)
        binding.lblTeacher.setOnClickListener(this)
        binding.btnGo.setOnClickListener(this)
        isLoadData(true)

        binding.lblTeacher.text = Constant.user_details!!.role_name
        binding.btnGo.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }
    }
    private fun isLoadData(isStaff: Boolean) {
        if (isStaff) {
            isStaffDetailAdapter = StaffDetailAdapter(Constant.isStaffDetails, this)
            binding.recyclerViews.layoutManager = LinearLayoutManager(this)
            binding.recyclerViews.adapter = isStaffDetailAdapter
        } else {
            isStudentDetailAdapter = StudentDetailAdapter(Constant.isChildDetails, this)
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
}