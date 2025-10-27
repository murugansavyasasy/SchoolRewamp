//package com.vs.schoolmessenger.School.InteractionWithStudent
//
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.view.View
//import androidx.core.view.isVisible
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Repository.App
//import com.vs.schoolmessenger.School.InteractionWithStudent.Model.BlockedStudent
//import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData
//import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentListener
//import com.vs.schoolmessenger.Utils.SharedPreference
//import com.vs.schoolmessenger.databinding.BlockListstudentBinding
//
//class BlockListStudentActivity : BaseActivity<BlockListstudentBinding>(), View.OnClickListener,
//    InteractionWithStudentListener {
//
//    private var isStaffDetails: StaffDetails? = null
//    private var isAccessToken: String? = null
//    private var appViewModel: App? = null
//
//    private lateinit var mAdapter: BlockListStudentAdapter
//
//
//
//    override fun getViewBinding(): BlockListstudentBinding {
//        return BlockListstudentBinding.inflate(layoutInflater)
//    }
//
//    override fun setupViews() {
//        super.setupViews()
//        isToolBarPrimaryParent(
//            mainViewId = R.id.main,
//            statusBarBgView = binding.statusBarBackground
//        )
//
//        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
//        isStaffDetails = SharedPreference.getStaffDetails(this)
//        appViewModel = ViewModelProvider(this).get(App::class.java)
//        appViewModel?.init()
//        val staffDetails = SharedPreference.getStaffDetails(this)
//        isAccessToken = staffDetails?.access_token
//
//        binding.toolbarLayout.lblParentToolBar.text = staffDetails!!.name
//        binding.toolbarLayout.lblSchoolName.text = staffDetails!!.school_name
//
//        fetchStudentData()
//        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
//            if (binding.rytsearch1.isVisible) {
//                binding.rytsearch1.visibility = View.GONE
//                binding.txtVideoMenu1.text.clear()
//            } else {
//                binding.rytsearch1.visibility = View.VISIBLE
//                binding.txtVideoMenu1.text.clear()
//
//            }
//        }
//        appViewModel?.isblockstudentlist?.observe(this) { response ->
//            Log.d("response++", response.toString())
//            if (response == null) {
//                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
//                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//                return@observe
//            }
//            if (response.status) {
//                isLoadStaffData(response.data)
//            } else {
//                showErrorUI(response.message ?: "No data available")
//                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//                binding.rytsearch1.visibility = View.GONE
//
//            }
//        }
//
//        binding.txtVideoMenu1.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (::mAdapter.isInitialized) {
//                    mAdapter.filter.filter(s)
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//        })
//
//
//    }
//
//
//    private fun fetchStudentData() {
//        appViewModel?.isblockstudentlist(
//            isAccessToken ?: ""
//        )
//    }
//
//    private fun showErrorUI(message: String) {
//        binding.nomessage.visibility = View.VISIBLE
//        binding.txtNoData.text = message
//        binding.txtNoData.visibility = View.VISIBLE
//        binding.rcystudentdata.visibility = View.GONE
//    }
//
//    private fun isLoadStaffData(data: List<BlockedStudent>?) {
//        if (data.isNullOrEmpty()) {
//            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//            binding.rytsearch1.visibility = View.GONE
//            showErrorUI(getString(R.string.no_staff_data_available))
//            return
//        }
//        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
//        binding.rytsearch1.visibility = View.GONE
//        binding.nomessage.visibility = View.GONE
//        binding.txtNoData.visibility = View.GONE
//        binding.rcystudentdata.visibility = View.VISIBLE
//        binding.rcystudentdata.layoutManager = LinearLayoutManager(this)
//
//        mAdapter = BlockListStudentAdapter(data, this, this, false)
//        binding.rcystudentdata.adapter = mAdapter
//    }
//
//    override fun onClick(p0: View?) {
//        when (p0?.id) {
//
//        }
//    }
//
//    override fun onSearchResultEmpty(isEmpty: Boolean) {
//
//    }
//
//    override fun onClickItem(data: StudentChatData) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onReadStatusClick(
//        data: StudentChatData,
//        isPosition: Int
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onBlockedSearchResultEmpty(isEmpty: Boolean) {
//        if (isEmpty) {
//            binding.nomessage.visibility = View.VISIBLE
//            binding.txtNoData.visibility = View.VISIBLE
//            binding.txtNoData.text = (getString(R.string.no_matching_data_found))
//            binding.rcystudentdata.visibility = View.GONE
//        } else {
//            binding.nomessage.visibility = View.GONE
//            binding.txtNoData.visibility = View.GONE
//            binding.rcystudentdata.visibility = View.VISIBLE
//        }
//    }
//
//
//    override fun onResume() {
//        super.onResume()
//        fetchStudentData()
//    }
//
//}