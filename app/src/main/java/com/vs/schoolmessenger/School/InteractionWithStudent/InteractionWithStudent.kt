package com.vs.schoolmessenger.School.InteractionWithStudent

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.Staff
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.QuestionDataSending
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.IntrectionWithStudentBinding

class InteractionWithStudent : BaseActivity<IntrectionWithStudentBinding>(), View.OnClickListener,
    InteractionWithStudentListener {

    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private lateinit var mAdapter: InteractionWithStudentAdapter


    override fun getViewBinding(): IntrectionWithStudentBinding {
        return IntrectionWithStudentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.blocklistLabel.setOnClickListener(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val staffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = staffDetails?.access_token

        binding.toolbarLayout.lblParentToolBar.text = staffDetails!!.name
        binding.toolbarLayout.lblSchoolName.text = staffDetails!!.school_name

        binding.toolbarLayout.blocktoolbar.visibility = View.VISIBLE

        binding.toolbarLayout.blocktoolbar.setOnClickListener {
            val intent = Intent(this, BlockListStudentActivity::class.java)
            startActivity(intent)
        }

        fetchStudentData()
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytsearch1.isVisible) {
                binding.rytsearch1.visibility = View.GONE
                binding.txtVideoMenu1.text.clear()
            } else {
                binding.rytsearch1.visibility = View.VISIBLE
                binding.txtVideoMenu1.text.clear()

            }
        }
        appViewModel?.getstudentdetailsforchat?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                return@observe
            }
            if (response.status) {
                isLoadStaffData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                binding.rytsearch1.visibility = View.GONE

            }
        }

        binding.txtVideoMenu1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


    }


    private fun fetchStudentData() {
        appViewModel?.getstudentdetailsforchat(
            isAccessToken ?: "", this
        )
    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcystudentdata.visibility = View.GONE
    }

    private fun isLoadStaffData(data: List<StudentChatData>?) {
        if (data.isNullOrEmpty()) {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            binding.rytsearch1.visibility = View.GONE
            showErrorUI(getString(R.string.no_staff_data_available))
            return
        }
        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        binding.rytsearch1.visibility = View.GONE
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcystudentdata.visibility = View.VISIBLE
        binding.rcystudentdata.layoutManager = LinearLayoutManager(this)

        mAdapter = InteractionWithStudentAdapter(data, this, this, false)
        binding.rcystudentdata.adapter = mAdapter
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = (getString(R.string.no_matching_data_found))
            binding.rcystudentdata.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcystudentdata.visibility = View.VISIBLE
        }
    }

    override fun onClickItem(data: StudentChatData) {
        val intent =
            Intent(this@InteractionWithStudent, InteractionWithStudentChatScreen::class.java)

        val saveStaffQuestionData = QuestionDataSending(
            id = data.id,
            name = data.name,
            section_id = data.section_id!!,
            section_name = data.section_name!!,
            subject_id = data.subject_id!!,
            subject_name = data.subject_name!!,
            is_class_teacher = data.is_class_teacher!!,
        )
        Constant.QuestionDataSending = saveStaffQuestionData
        startActivity(intent)
    }

    override fun onReadStatusClick(
        data: StudentChatData,
        isPosition: Int
    ) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, "STAFFCHAT")
            addProperty(APIKeyNames.detail_id, data.section_id)
        }
        appViewModel?.isUpdateStatusCommunication(isAccessToken!!, jsonObject, this)
    }

    override fun onBlockedSearchResultEmpty(isEmpty: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        fetchStudentData()
    }

}