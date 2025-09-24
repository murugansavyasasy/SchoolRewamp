package com.vs.schoolmessenger.Parent.Assignment

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AssignmentParentBinding

class Assignment : BaseActivity<AssignmentParentBinding>(), AssignmentClickListener,
    View.OnClickListener {

    override fun getViewBinding(): AssignmentParentBinding {
        return AssignmentParentBinding.inflate(layoutInflater)
    }

    var isAssignmentAdapter: AssignmentParentAdapter? = null
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isAssignmentReportData: List<ParentAssignmentData>? = null

    lateinit var mAdapter: AssignmentAdapter
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        val isChildDetails = SharedPreference.getChildDetails(this)

        isAccessToken = isChildDetails?.access_token

        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.toolbarLayout.rytSearch.visibility == View.VISIBLE) {
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.toolbarLayout.txtVideoMenu.setText("")
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.toolbarLayout.txtVideoMenu.windowToken, 0)
            } else {
                binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                binding.toolbarLayout.txtVideoMenu.setText("")
                binding.toolbarLayout.txtVideoMenu.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.toolbarLayout.txtVideoMenu, InputMethodManager.SHOW_IMPLICIT)
            }
        }



        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name
        binding.lblHeaderTitle.text = Constant.isParentMenuName
        Log.d("isParentMenuName",Constant.isParentMenuName)

        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.rcyAssignment.layoutManager = LinearLayoutManager(this)

        appViewModel?.isAssignmentlist?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                isAssignmentReportData = response.data
                loadAssignmentReportData()
                binding.rcyAssignment.visibility = View.VISIBLE
                binding.lytList.visibility = View.GONE
            } else {
                binding.rcyAssignment.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message
            }
        }

        fetchAssignmentReportData()


        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isAssignmentAdapter?.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun fetchAssignmentReportData() {
        binding.rcyAssignment.visibility = View.VISIBLE
        isAssignmentAdapter =
            AssignmentParentAdapter(mutableListOf(), this, this, Constant.isShimmerViewDisable)
        binding.rcyAssignment.adapter = isAssignmentAdapter

        appViewModel?.isAssignmentlist(isAccessToken!!)
    }

    private fun loadAssignmentReportData() {
        binding.rcyAssignment.visibility = View.VISIBLE
        isAssignmentAdapter = AssignmentParentAdapter(
            isAssignmentReportData!!.toMutableList(), this, this, Constant.isShimmerViewDisable
        )
        binding.rcyAssignment.adapter = isAssignmentAdapter
    }



    override fun onSubmittedClick(data: AssignmentData) {

    }

    override fun onEditAndDeleteClick(
        data: AssignmentData,
        anchorView: View,
        adapterPosition: Int
    ) {
    }

    override fun onNotSubmittedClick(data: AssignmentData) {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            
        }
    }
}