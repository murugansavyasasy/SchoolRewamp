package com.vs.schoolmessenger.Parent.Attachment

import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponCredentials.AppCredentials
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Attachment.AttachmentReportAdapter
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentDataReport
import com.vs.schoolmessenger.School.Attachment.OnAttachmentReportClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentAttachmentBinding

class Attachment : BaseActivity<ParentAttachmentBinding>(), View.OnClickListener,
    OnAttachmentReportClickListener {

    override fun getViewBinding(): ParentAttachmentBinding {
        return ParentAttachmentBinding.inflate(layoutInflater)
    }

    var mAttachmentReportAdapter: AttachmentAdapter? = null
    private var isAccessToken: String? = null
    private var appViewModel: App? = null


    private var msg_id: Int = -1

    private var fromNotification: Boolean = false


    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        msg_id = intent.getIntExtra(Constant.msg_id, -1)

        fromNotification = intent.getBooleanExtra("fromNotification", false)


        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.imgFilter.setOnClickListener(this)
        binding.lblArchiveMsg.setOnClickListener(this)
        binding.lblHeaderTitle.text=Constant.isParentMenuName
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener{
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearchMenu1.setText("")
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu1.windowToken, 0)
            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtSearchMenu1.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.txtSearchMenu1, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.lblStudentSection.text =
            childDetails?.standard_name + " - " + childDetails?.section_name
        binding.linearlayout1.visibility = View.VISIBLE

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        binding.txtSearchMenu1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mAttachmentReportAdapter?.filter?.filter(s)
                if (s!!.isNotEmpty()) {
                    if (binding.isArchiveErrorMsg.visibility == View.VISIBLE) {
                        binding.isArchiveErrorMsg.visibility = View.GONE
                    }
                } else {
                    if (binding.isArchiveErrorMsg.visibility == View.GONE) {
                        binding.isArchiveErrorMsg.visibility = View.VISIBLE
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.txtSearchMenu1.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.txtSearchMenu1.text.toString()
                mAttachmentReportAdapter?.filter?.filter(query)

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu1.windowToken, 0)
                binding.txtSearchMenu1.clearFocus()
                true
            } else false
        }

        appViewModel?.isAttachmentResponseArchive?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    if(response.data.isNotEmpty()){
                        mAttachmentReportAdapter!!.AppendData(response.data)
                        binding.txtSearchMenu1.text.clear()
                        binding.isArchiveErrorMsg.visibility=View.GONE
                        binding.recycleracademic.visibility = View.VISIBLE
                        binding.nomessage.visibility = View.GONE
                        binding.txtNoData.visibility = View.GONE

                        if(mAttachmentReportAdapter!!.getCurrentListSize()>0){
//                            binding.rytSearch1.visibility = View.GONE
                            binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                            binding.txtSearchMenu1.text.clear()
                        }
                        else{
                            binding.rytSearch1.visibility = View.GONE
                            binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                            binding.txtSearchMenu1.text.clear()
                        }

                    }
                    else{
                        binding.isArchiveErrorMsg.visibility=View.VISIBLE
                        binding.isArchiveErrorMsg.text=response.message
                        if(mAttachmentReportAdapter!!.getCurrentListSize()==0){
                            binding.txtNoData.visibility=View.GONE
                            binding.rytSearch1.visibility = View.GONE
                            binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                            binding.txtSearchMenu1.text.clear()
                        }else{
                            // Set top margin to 15dp dynamically
                            val layoutParams = binding.isArchiveErrorMsg.layoutParams as ViewGroup.MarginLayoutParams
                            val topMarginInDp = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                15f,
                                resources.displayMetrics
                            ).toInt()
                            layoutParams.topMargin = topMarginInDp
                            binding.isArchiveErrorMsg.layoutParams = layoutParams
                            binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                        }
                    }
                }
                else {
                    binding.isArchiveErrorMsg.visibility=View.VISIBLE
                    binding.isArchiveErrorMsg.text=response.message
                    if(mAttachmentReportAdapter!!.getCurrentListSize()==0){
                        binding.txtNoData.visibility=View.GONE
                        binding.rytSearch1.visibility = View.GONE
                        binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                        binding.txtSearchMenu1.text.clear()
                    }else{
                        // Set top margin to 15dp dynamically
                        val layoutParams = binding.isArchiveErrorMsg.layoutParams as ViewGroup.MarginLayoutParams
                        val topMarginInDp = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            15f,
                            resources.displayMetrics
                        ).toInt()
                        layoutParams.topMargin = topMarginInDp
                        binding.isArchiveErrorMsg.layoutParams = layoutParams
                        binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                    }
                }
            } else {
                binding.isArchiveErrorMsg.visibility=View.VISIBLE
                binding.isArchiveErrorMsg.text=getString(R.string.something_went_wrong_please_try_again_later)
                if(mAttachmentReportAdapter!!.getCurrentListSize()==0){
                    binding.txtNoData.visibility=View.GONE
                    binding.rytSearch1.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                    binding.txtSearchMenu1.text.clear()
                }else{
                    binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                }
            }
        }

        appViewModel?.isAttachmentResponse?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val mobileNumber = SharedPreference.getMobileNumber(this)

                val jsonObject = JsonObject().apply {
                    addProperty(APIKeyNames.mobile_number, mobileNumber)
                    addProperty(APIKeyNames.activity, Constant.add_points_view_attachments)
                    addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                    addProperty(APIKeyNames.menu_id,Constant.SELECTED_SCHOOL_MENU )
                }
                appViewModel?.isAddRewardPoints(isAccessToken ?: "", jsonObject)

                binding.txtNoData.visibility = View.GONE
                binding.nomessage.visibility = View.GONE
                binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
                binding.recycleracademic.visibility = View.VISIBLE
                isLoadData(response.data)
                Log.d("Message Id Value Indication", msg_id.toString())
                scrollToMessageId(msg_id)
            } else {
                binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                showEmptyState(response?.message ?: getString(R.string.no_data_found))
            }
        }
        isGetAttachment()
    }

    fun isLoadData(data: List<AttachmentDataReport>) {
        mAttachmentReportAdapter = AttachmentAdapter(
            data,
            this,
            this,
            Constant.isShimmerViewDisable,
            binding.nomessage,
            binding.txtNoData
        )
        binding.recycleracademic.layoutManager = LinearLayoutManager(this)
        binding.recycleracademic.isNestedScrollingEnabled = false
        binding.recycleracademic.adapter = mAttachmentReportAdapter
    }


    private fun isGetAttachment() {

        mAttachmentReportAdapter =
            AttachmentAdapter(
                emptyList(),
                this,
                this,
                Constant.isShimmerView
            )
        binding.recycleracademic.layoutManager = LinearLayoutManager(this)
        binding.recycleracademic.isNestedScrollingEnabled = false
        binding.recycleracademic.adapter = mAttachmentReportAdapter

        appViewModel?.getAttachment(isAccessToken.orEmpty(), this)
    }

    private fun isGetAttachmentArchive() {
        appViewModel?.getAttachmentArchive(isAccessToken.orEmpty(), this)
    }

    private fun showEmptyState(message: String) {
        binding.recycleracademic.visibility = View.GONE
        binding.nomessage.visibility = View.VISIBLE
//        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
    }


    private fun scrollToMessageId(msg_id: Int) {
        if (msg_id == -1) return

        val dataList = mAttachmentReportAdapter?.getCurrentList()
        if (!dataList.isNullOrEmpty()) {
            val index = dataList.indexOfFirst { it.id.toIntOrNull() == msg_id }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index")
                binding.recycleracademic.post {
                    binding.recycleracademic.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.recycleracademic, index)
                }
            } else {
                Log.d("ScrollDebug", "No index found for msg_id $msg_id")
            }
        }
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


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.lblArchiveMsg->{
                binding.txtSearchMenu1.text.clear()
                isGetAttachmentArchive()
                binding.lblArchiveMsg.visibility=View.GONE
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onItemClick(
        isData: List<AttachmentDataReport>,
        view: View,
        isPosition: Int
    ) {
    }

    override fun onReadStatusClick(isData: List<AttachmentDataReport>, isPosition: Int) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, Constant.ATTACHMENT)
            addProperty(APIKeyNames.detail_id, isData[isPosition].id)
        }

        if (isData[isPosition].is_archive){
            appViewModel?.isUpdateStatusArchive(isAccessToken!!, jsonObject, this)
        }
        else{
            appViewModel?.isUpdateStatusCommunication(isAccessToken!!, jsonObject, this)

        }
    }

    override fun onFilterEmpty(showNoData: Boolean) {
        if (showNoData) {
            Log.d("NoDta","No data")
            binding.recycleracademic.visibility = View.GONE
            binding.nomessage.visibility=View.VISIBLE
            binding.txtNoData.visibility=View.VISIBLE
        } else {
            Log.d("NoDta","data")
            binding.recycleracademic.visibility = View.VISIBLE
            binding.nomessage.visibility=View.GONE
            binding.txtNoData.visibility=View.GONE
        }
    }
}