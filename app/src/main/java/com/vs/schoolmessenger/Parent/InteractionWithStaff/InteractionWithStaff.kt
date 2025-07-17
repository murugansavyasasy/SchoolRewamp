package com.vs.schoolmessenger.Parent.InteractionWithStaff

import android.content.Intent
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter.InteractionWithStaffAdapter
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Listener.InteractionWithStaffListener
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.Staff
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.StaffDataSending
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesReportAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeData
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary.LessonPlanViewDetails
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.IntectionWithStaffBinding

class InteractionWithStaff : BaseActivity<IntectionWithStaffBinding>(), View.OnClickListener,
    InteractionWithStaffListener {

    private var isChildDetails: ChildDetails? = null

    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private lateinit var interactionWithStaffAdapter: InteractionWithStaffAdapter

    override fun getViewBinding(): IntectionWithStaffBinding {
        return IntectionWithStaffBinding.inflate(layoutInflater)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name

        isAccessToken = isChildDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        fetchstaffdata()

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::interactionWithStaffAdapter.isInitialized) {
                    interactionWithStaffAdapter.filter.filter(s)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        appViewModel?.getdetailsforchat?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI("Something went wrong. Please try again.")
                return@observe
            }
            if (response.status) {
                isLoadStaffData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }
    }


    private fun fetchstaffdata() {
        appViewModel?.getdetailsforchat(
            isAccessToken ?: "",
            this
        )
    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcystaffdata.visibility = View.GONE
    }


    private fun isLoadStaffData(data: List<Staff>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI("No staff data available")
            return
        }

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcystaffdata.visibility = View.VISIBLE
        binding.rcystaffdata.layoutManager = LinearLayoutManager(this)

        interactionWithStaffAdapter = InteractionWithStaffAdapter(data, this, this, false)
        binding.rcystaffdata.adapter = interactionWithStaffAdapter
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_notices_found)
            binding.rcystaffdata.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcystaffdata.visibility = View.VISIBLE
        }
    }

    override fun onClickItem(data: Staff) {
        val intent = Intent(this@InteractionWithStaff, InteractionwithStaffChatScreen::class.java)
        val saveStaffData = StaffDataSending(
            name = data.name,
            subject_id = data.subject_id!!,
            unread_count = data.unread_count!!,
            subject_name = data.subject_name,
            is_class_teacher = data.is_class_teacher,
            id = data.id,
            is_assigned = data.is_assigned
        )
        Constant.StaffDataSending = saveStaffData
        startActivity(intent)
    }
}