package com.vs.schoolmessenger.Dashboard.Combination

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SchoolClickListener
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.RoleSelecionBinding

class PrioritySelection : BaseActivity<RoleSelecionBinding>(), View.OnClickListener,
    PriorityClickListener, SchoolClickListener {

    private lateinit var isStudentDetailAdapter: StudentDetailAdapter
    private lateinit var isStaffDetailAdapter: StaffDetailAdapter

    override fun getViewBinding(): RoleSelecionBinding {
        return RoleSelecionBinding.inflate(layoutInflater)
    }

    private var userDetails: UserDetails? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()
        binding.lblParent.setOnClickListener(this)
        binding.lblTeacher.setOnClickListener(this)
        binding.btnGo.setOnClickListener(this)

        userDetails = SharedPreference.getUserDetails(this@PrioritySelection)

        val isStaff = userDetails?.is_staff == true
        val isParent = userDetails?.is_parent == true
        val staffRole = userDetails?.staff_role.orEmpty()
        val roleName = userDetails?.role_name.orEmpty()

        when {
            isStaff && isParent -> {
                binding.lblTeacher.visibility = View.VISIBLE
                binding.lblParent.visibility = View.VISIBLE
                binding.lblLoginTeacherOrParent.visibility = View.VISIBLE
                binding.lblLoginTeacherOrParent.text = "Login As $roleName or Student"
                isLoadData(true)
            }

            isStaff -> {
                binding.lblTeacher.visibility = View.VISIBLE
                binding.lblParent.visibility = View.GONE
                binding.lblLoginTeacherOrParent.visibility = View.GONE
                isLoadData(true)
            }

            isParent -> {
                binding.lblTeacher.visibility = View.GONE
                binding.lblParent.visibility = View.VISIBLE
                binding.lblLoginTeacherOrParent.visibility = View.GONE
                isLoadData(false)
            }

            else -> {
                Toast.makeText(this, "Invalid user role", Toast.LENGTH_SHORT).show()
            }
        }

         if (staffRole == Constant.isStaffRole || staffRole.isEmpty()) {
             binding.btnGo.visibility =View.GONE
             binding.proceedlabel.visibility = View.GONE
         } else {
             binding.btnGo.visibility = View.VISIBLE
             binding.proceedlabel.visibility = View.VISIBLE
         }

        if (isStaff) {
            binding.lblTeacher.text = roleName
        }

        if (isParent) {
            binding.lblParent.text = "Student"
        }

        binding.btnGo.setOnClickListener {
            val staffDetails =
                Constant.user_data?.getOrNull(0)?.user_details?.staff_details?.getOrNull(0)
            if (staffDetails != null) {
                SharedPreference.putStaffDetails(this, staffDetails)
                val intent = Intent(this, SchoolDashboard::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Staff details not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLoadData(isStaff: Boolean) {
        if (isStaff) {
            val staffDetails = Constant.isStaffDetails
            if (!staffDetails.isNullOrEmpty()) {
                val staffRole = Constant.user_details?.staff_role.orEmpty()
                isStaffDetailAdapter = StaffDetailAdapter(
                    staffDetails,
                    this,
                    this,
                    staffRole,
                    staffDetails.size
                )
                binding.recyclerViews.layoutManager = LinearLayoutManager(this)
                binding.recyclerViews.adapter = isStaffDetailAdapter
            } else {
                Toast.makeText(this, "No staff data found", Toast.LENGTH_SHORT).show()
            }
        } else {
            val childDetails = Constant.isChildDetails
            if (!childDetails.isNullOrEmpty()) {
                isStudentDetailAdapter = StudentDetailAdapter(childDetails, this, this)
                binding.recyclerViews.layoutManager = LinearLayoutManager(this)
                binding.recyclerViews.adapter = isStudentDetailAdapter
            } else {
                Toast.makeText(this, "No student data found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lblTeacher -> isBackRoundChange(binding.lblTeacher)
            R.id.lblParent -> isBackRoundChange(binding.lblParent)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun isBackRoundChange(isClickingId: TextView) {
        when (isClickingId) {
            binding.lblParent -> {
                binding.lblTeacher.background = null
                binding.lblTeacher.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
                binding.btnGo.visibility = View.GONE
                binding.proceedlabel.visibility = View.GONE
                isLoadData(false)
                Constant.isParentChoose = true
            }

            binding.lblTeacher -> {
                binding.lblParent.background = null
                binding.lblParent.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))

                val showButton = userDetails?.staff_role != Constant.isStaffRole
                binding.btnGo.visibility = if (showButton) View.VISIBLE else View.GONE

                if (userDetails!!.staff_role == Constant.isStaffRole) {
                    binding.btnGo.visibility = View.GONE
                    binding.proceedlabel.visibility = View.GONE
                } else {
                    binding.btnGo.visibility = View.VISIBLE
                    binding.proceedlabel.visibility = View.VISIBLE
                }
                isLoadData(true)
                Constant.isParentChoose = false

            }


        }
        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.bg_blue)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))

        }

        override fun onItemClick(data: ChildDetails) {
            SharedPreference.putChildDetails(this, data)
            startActivity(Intent(this, ParentDashboard::class.java))
        }

        override fun onItemClick(data: StaffDetails) {
            SharedPreference.putStaffDetails(this, data)
            startActivity(Intent(this, SchoolDashboard::class.java))
        }
    }
