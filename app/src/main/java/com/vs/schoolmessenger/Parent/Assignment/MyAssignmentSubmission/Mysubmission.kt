package com.vs.schoolmessenger.Parent.Assignment.MyAssignmentSubmission

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Assignment.Assignment
import com.vs.schoolmessenger.Parent.Assignment.AssignmentAdapter
import com.vs.schoolmessenger.Parent.Assignment.AssignmentClickListener
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentData
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.SubmittedAssignment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.School.NoticeBoard.CreateNoticeBoard
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MysubmissionAssignmentBinding

class Mysubmission : BaseActivity<MysubmissionAssignmentBinding>(), AssignmentClickListener,
    View.OnClickListener {

    override fun getViewBinding(): MysubmissionAssignmentBinding {
        return MysubmissionAssignmentBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private var assignmentId: String? = null
    private var titleName: String? = null
    private var subjectName: String? = null

    var isMySubmissionId = ""

    var isMySubmissionPosition = 0

    lateinit var mAdapter: MySubmissionAdapter
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

        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Assignment)
        binding.toolbarLayout.rytSearch.visibility = View.GONE

        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name

        binding.lblHeaderTitle.setText("My Submission")

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        assignmentId = intent.getStringExtra(Constant.assignment_id)
        titleName = intent.getStringExtra(Constant.title_)
        subjectName = intent.getStringExtra(Constant.subject)


        binding.rcyAssignment.layoutManager = LinearLayoutManager(this)

        appViewModel?.getassignmentmysubmission?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyAssignment.visibility = View.VISIBLE
                mAdapter.updateList(response.data)
            } else {
                showEmptyState(response?.message ?: getString(R.string.no_data_found))
            }
        }


        appViewModel!!.ismysubmissiondelete?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@Mysubmission)
                    mAdapter!!.removeItemAt(isMySubmissionPosition)
                    val intent = Intent(this, Assignment::class.java)
                    startActivity(intent)

                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }

        fetchAssignmentReportData()
    }

    private fun fetchAssignmentReportData() {
        binding.rcyAssignment.visibility = View.VISIBLE
        mAdapter = MySubmissionAdapter(
            mutableListOf(),
            this,
            this,
            Constant.isShimmerViewDisable,
            titleName,
            subjectName
        )

        binding.rcyAssignment.adapter = mAdapter

        if (!assignmentId.isNullOrEmpty() && !isAccessToken.isNullOrEmpty()) {
            appViewModel?.isGetAssignmentSubList(isAccessToken!!, assignmentId!!)
        } else {
            Log.d("Assignment Id", "Issue in API Call")
        }

    }


    private fun showEmptyState(message: String) {
        binding.rcyAssignment.visibility = View.GONE
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
    }


    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onSubmittedClick(data: AssignmentData) {
        TODO("Not yet implemented")
    }

    override fun onEditAndDeleteClick(
        data: AssignmentData,
        anchorView: View,
        adapterPosition: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onNotSubmittedClick(data: AssignmentData) {
        TODO("Not yet implemented")
    }

    override fun onItemClick(
        data: AssignmentData,
        holder: AssignmentAdapter.DataViewHolder
    ) {
        TODO("Not yet implemented")
    }

    override fun onReadStatusClick(
        isData: ParentAssignmentData,
        isPosition: Int
    ) {
        TODO("Not yet implemented")
    }


    fun showEditDeletePopup(data: SubmittedAssignment, anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_delete, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        val layoutEdit = popupView.findViewById<LinearLayout>(R.id.layout_edit)
        val layoutDelete = popupView.findViewById<LinearLayout>(R.id.layout_delete)

        layoutEdit.setOnClickListener {
            Log.d("PopupClick", "Edit clicked ✅")
            Log.d("My Submission Data String", "Assignment data: $data")
            Constant.isClickEdit = true
            val intent = Intent(this, MyAssignmentSubmit::class.java)
            intent.putExtra(Constant.mysubmission_data, data)
            startActivity(intent)
            popupWindow.dismiss()
        }


        layoutDelete.setOnClickListener {
            showSendConfirmationDialog(false)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, 0, 10)
    }


    override fun onClickListener(
        data: SubmittedAssignment,
        anchorView: View,
        adapterPosition: Int
    ) {
        isMySubmissionId = data.id
        isMySubmissionPosition = adapterPosition
        showEditDeletePopup(data, anchorView)
    }


    fun showSendConfirmationDialog(isMySubmissionUpdate: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        alertMessage.text = getString(R.string.are_you_sure_want_to_update_this_noticeboard)

        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            val jsonObject = JsonObject()
            jsonObject.addProperty(APIKeyNames.id, isMySubmissionId)
            appViewModel?.ismysubmissiondelete(isAccessToken!!, jsonObject, this)
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }


    override fun onResume() {
        super.onResume()
        fetchAssignmentReportData()
    }

}