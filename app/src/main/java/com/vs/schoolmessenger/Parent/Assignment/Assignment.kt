package com.vs.schoolmessenger.Parent.Assignment

import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentData
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.SubmittedAssignment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
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

    private var msg_id: Int = -1

    private var fromNotification: Boolean = false

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


        msg_id = intent.getIntExtra(Constant.msg_id, -1)

        fromNotification = intent.getBooleanExtra("fromNotification", false)


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
                imm.showSoftInput(
                    binding.toolbarLayout.txtVideoMenu,
                    InputMethodManager.SHOW_IMPLICIT
                )
            }
        }



        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name
        binding.lblHeaderTitle.text = Constant.isParentMenuName
        Log.d("isParentMenuName", Constant.isParentMenuName)

        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        binding.rcyAssignment.layoutManager = LinearLayoutManager(this)

        appViewModel?.isAssignmentlist?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                isAssignmentReportData = response.data
                loadAssignmentReportData()
                if (response.data.isNotEmpty()) {
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                } else {
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                }
                binding.rcyAssignment.visibility = View.VISIBLE
                binding.lytList.visibility = View.GONE
                Log.d("Message Id Value Indication", msg_id.toString())
                scrollToMessageId(msg_id)

            } else {
                binding.rcyAssignment.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.nomessage.visibility = View.VISIBLE
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                binding.txtNoData.text = response?.message
            }
        }

        fetchAssignmentReportData()


        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isAssignmentAdapter?.filter(s.toString())
                if (isAssignmentAdapter?.itemCount == 0) {
                    binding.rcyAssignment.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.text = getString(R.string.no_list_found)
                } else {
                    binding.rcyAssignment.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                }
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

    private fun scrollToMessageId(msg_id: Int) {
        if (msg_id == -1) return

        isAssignmentReportData?.let { list ->
            val index = list.indexOfFirst { it.id.toIntOrNull() == msg_id }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in completed")
                binding.rcyAssignment.post {
                    binding.rcyAssignment.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rcyAssignment, index)
                }
                return
            }
        }
        Log.d("ScrollDebug", "No index found for msg_id $msg_id")
    }


    private fun highlightItemTemporarily(recyclerView: RecyclerView, position: Int) {
        recyclerView.post {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.itemView?.setBackgroundColor(Color.parseColor("#FFE082"))
            recyclerView.postDelayed({
                viewHolder?.itemView?.setBackgroundColor(Color.TRANSPARENT)
            }, 2000)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
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

    override fun onItemClick(
        data: AssignmentData,
        holder: AssignmentAdapter.DataViewHolder
    ) {

    }

    override fun onReadStatusClick(
        isData: ParentAssignmentData,
        isPosition: Int
    ) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, "ASSIGNMENT")
            addProperty(APIKeyNames.detail_id, isData.id)
        }
        appViewModel?.isUpdateStatusCommunication(isAccessToken!!, jsonObject, this)
    }

    override fun onClickListener(
        data: SubmittedAssignment,
        anchorView: View,
        adapterPosition: Int
    ) {
        TODO("Not yet implemented")
    }


    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}