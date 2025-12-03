package com.vs.schoolmessenger.Dashboard.Combination

import android.annotation.SuppressLint
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SchoolClickListener
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isSchoolDashBoardData
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.RoleSelecionBinding

class PrioritySelection : BaseActivity<RoleSelecionBinding>(), View.OnClickListener,
    PriorityClickListener, SchoolClickListener {

    private lateinit var isStudentDetailAdapter: StudentDetailAdapter
    private lateinit var isStaffDetailAdapter: StaffDetailAdapter
    override fun getViewBinding(): RoleSelecionBinding {
        return RoleSelecionBinding.inflate(layoutInflater)
    }
    var authViewModel: Auth? = null


    private var userDetails: UserDetails? = null


    override fun setupViews() {
        super.setupViews()
        isPrioritySelection(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.lblParent.setOnClickListener(this)
        binding.lblTeacher.setOnClickListener(this)
        binding.btnGo.setOnClickListener(this)
        binding.lytLogout.setOnClickListener(this)

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()


        userDetails = SharedPreference.getUserDetails(this@PrioritySelection)
        Constant.checkBiometricSupport(this)

        val isStaff = userDetails?.is_staff == true
        val isParent = userDetails?.is_parent == true
        val staffRole = userDetails?.staff_role.orEmpty()
        val roleName = userDetails?.role_name.orEmpty()

        when {
            isStaff && isParent -> {
                binding.lblTeacher.visibility = View.VISIBLE
                binding.lblParent.visibility = View.VISIBLE
                binding.lblLoginTeacherOrParent.visibility = View.VISIBLE
                if (staffRole == Constant.isPrincipalRole) {
                    binding.lblLoginTeacherOrParent.text =
                        resources.getString(R.string.Login_Management)
                } else {
                    binding.lblLoginTeacherOrParent.text =
                        getString(R.string.Login_As) + " " + userDetails!!.role_name
                }
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
                Toast.makeText(this, resources.getString(R.string.Invalid_role), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        if (staffRole == Constant.isStaffRole || staffRole.isEmpty()) {
            binding.btnGo.visibility = View.GONE
            binding.proceedlabel.visibility = View.GONE
        } else {
            binding.btnGo.visibility = View.VISIBLE
            binding.proceedlabel.visibility = View.VISIBLE
        }

        if (isStaff) {
            binding.lblTeacher.text = roleName
        }

        if (isParent) {
            binding.lblParent.text = resources.getString(R.string.Student_Parent)
        }

        binding.btnGo.setOnClickListener {
            isSchoolDashBoardData = null
            val staffDetails = userDetails!!.staff_details.get(0)
            if (staffDetails != null) {
                SharedPreference.putStaffDetails(this, staffDetails)
                val intent = Intent(this, SchoolDashboard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.Staff_details_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun isLoadData(isStaff: Boolean) {
        if (isStaff) {
            val staffDetails = userDetails!!.staff_details
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
                Toast.makeText(
                    this,
                    resources.getString(R.string.No_staff_data_ound),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val childDetails = userDetails!!.child_details
            if (!childDetails.isNullOrEmpty()) {
                isStudentDetailAdapter = StudentDetailAdapter(childDetails, this, this)
                binding.recyclerViews.layoutManager = LinearLayoutManager(this)
                binding.recyclerViews.adapter = isStudentDetailAdapter
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.No_student_data_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lblTeacher -> isBackRoundChange(binding.lblTeacher)
            R.id.lblParent -> isBackRoundChange(binding.lblParent)
            R.id.lytLogout -> isShowLogoutPopup()
        }
    }

    private fun isShowLogoutPopup() {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.logout_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )

        dimBehind(popupWindow)
        val btnCancel: TextView = popupView.findViewById(R.id.btnCancel)
        val rlaLogout: RelativeLayout = popupView.findViewById(R.id.rlaLogout)
        btnCancel.setOnClickListener {
            clearDim()
            popupWindow.dismiss()
        }

        rlaLogout.setOnClickListener {
            clearDim()
            popupWindow.dismiss()
            isLogout(
                activity = this,
                viewModel =authViewModel,
                secure_id = Constant.getAndroidSecureId(this) ,
                device_type = Constant.isDeviceType,
                mobile_number = SharedPreference.getMobileNumber(this).toString()
            ) { isSuccess, message ->

                if (isSuccess) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    SharedPreference.putLogout(this, true)
                    SharedPreference.setLoggedIn(this, false)
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Constant.showErrorAlert(this,getString(R.string.Oops),message)
                }
            }
        }

        val rootView = this.window.decorView.rootView
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)

        popupWindow.setOnDismissListener {
            clearDim()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun isBackRoundChange(isClickingId: TextView) {
        when (isClickingId) {
            binding.lblParent -> {
                binding.lblLoginTeacherOrParent.text =
                    resources.getString(R.string.Login_Student_Parent)
                binding.lblTeacher.background = null
                binding.lblTeacher.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
                binding.btnGo.visibility = View.GONE
                binding.proceedlabel.visibility = View.GONE
                isLoadData(false)
                Constant.isParentChoose = true
            }

            binding.lblTeacher -> {

                if (userDetails!!.staff_role == Constant.isPrincipalRole) {
                    binding.lblLoginTeacherOrParent.text =
                        resources.getString(R.string.Login_Management)
                } else {
                    binding.lblLoginTeacherOrParent.text =
                        getString(R.string.Login_As) + " " + userDetails!!.role_name
                }
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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

    }

    override fun onItemClick(data: StaffDetails) {
        SharedPreference.putStaffDetails(this, data)
        startActivity(Intent(this, SchoolDashboard::class.java))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
}
