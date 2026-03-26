package com.vs.schoolmessenger.Parent.Hostel

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Hostel.Adapter.OutpassRequestList.OutpassRequestList
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.OutpassRequestData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentHostelOutpassRequestListBinding

class ParentOutpassrequestList : BaseActivity<ParentHostelOutpassRequestListBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): ParentHostelOutpassRequestListBinding {
        return ParentHostelOutpassRequestListBinding.inflate(layoutInflater)
    }
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    lateinit var nAdapter: OutpassRequestList
    private var currentFilteredList: List<OutpassRequestData> = listOf()




    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.lblInitialName.visibility = View.VISIBLE
        binding.toolbarLayout.imgCall.visibility = View.VISIBLE
        binding.toolbarLayout.lblClassAndRoomDetails.visibility = View.VISIBLE
        binding.toolbarLayout.lblHostelName.visibility = View.VISIBLE
        binding.toolbarLayout.lblName.visibility = View.VISIBLE

        binding.toolbarLayout.lblToday.visibility = View.GONE
        binding.toolbarLayout.lblDate.visibility = View.GONE

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.imgSearchIcon.setOnClickListener {
            if (binding.rytSearch.isVisible) {
                binding.rytSearch.visibility = View.GONE
                binding.txtSearch.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch.windowToken, 0)
            } else {
                binding.rytSearch.visibility = View.VISIBLE
                binding.txtSearch.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch.windowToken, 0)

            }
        }

        binding.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextSSS", s.toString())
                filter(s.toString())

            }
        })

        val list = intent.getSerializableExtra("OUTPASS_LIST") as? ArrayList<OutpassRequestData> ?: arrayListOf()
        currentFilteredList=list
        isLoadOutpassRequest(list)



    }

    private fun filter(text: String) {
        val filteredList = if (text.isBlank()) {
            currentFilteredList
        } else {
            val searchWords = text.trim().lowercase().split("\\s+".toRegex())
            currentFilteredList.filter { outpass ->
                val fieldsToSearch = listOf(
                    outpass.reason.lowercase(),
                    outpass.request_time.lowercase(),
                    outpass.fromdate_todate.lowercase(),
                    outpass.status.lowercase(),
                )
                // Check if ALL search words are found in ANY of the fields(feildTosearch List ie name,email...etc)
                searchWords.all { word ->
                    fieldsToSearch.any { field ->
                        field.contains(word)
                    }
                }
            }
        }

        if (filteredList.isNotEmpty()) {
            ShowData()
            nAdapter.updateData(filteredList)
        } else {
            ErrorMessage(Constant.NO_DATA_FOUND)
        }
    }

    fun ErrorMessage(ErrorMessage: String) {
        binding.rcHostelOutpassRequest.visibility = View.GONE
        binding.lblErrorMessage.visibility = View.VISIBLE
        binding.imgNoDataFound.visibility = View.VISIBLE
        binding.lblErrorMessage.text = ErrorMessage
    }

    fun ShowData() {
        binding.rcHostelOutpassRequest.visibility = View.VISIBLE
        binding.lblErrorMessage.visibility = View.GONE
        binding.imgNoDataFound.visibility = View.GONE
    }



    private fun isLoadOutpassRequest(newData: List<OutpassRequestData>) {
        currentFilteredList=newData
        binding.rcHostelOutpassRequest.visibility = View.VISIBLE
        nAdapter = OutpassRequestList(
            newData,this, Constant.isShimmerViewDisable
        )
        binding.rcHostelOutpassRequest.adapter = nAdapter
    }



    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }
}