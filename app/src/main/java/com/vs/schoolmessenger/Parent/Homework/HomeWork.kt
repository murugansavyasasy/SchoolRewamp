package com.vs.schoolmessenger.Parent.Homework

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeWorkAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HomeWorkParentBinding
import java.util.Locale

class HomeWork : BaseActivity<HomeWorkParentBinding>(), View.OnClickListener,
    HomeWorkDateClickListener, HomeWorkItemClick {

    private var isAccessToken: String? = null
    private var isStudentName: String? = null
    private var isStudentSectionName: String? = null
    private var isStudentStandardName: String? = null
    private var appViewModel: App? = null
    override fun getViewBinding(): HomeWorkParentBinding {
        return HomeWorkParentBinding.inflate(layoutInflater)
    }
    private var fullHomeworkList = listOf<GetDateWiseHomeworkData>()
    private var filteredHomeworkList = listOf<GetDateWiseHomeworkData>()

    var mAdapter: HomeWorkAdapter? = null

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        binding.lblSeeMore.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.HomeWork)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        isStudentName = isChildDetails?.name
        isStudentStandardName = isChildDetails?.standard_name
        isStudentSectionName = isChildDetails?.section_name.toString()
        Log.d("SectionID", isStudentSectionName.toString())

        isGetHomeWorkList()
        binding.toolbarLayout.lblStudentName.text = isStudentName
        binding.toolbarLayout.lblStudentSection.text =
            isStudentStandardName + " " + isStudentSectionName

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        appViewModel?.isHomeWorkDetailsList?.observe(this) { response ->
            if (response!!.status) {
                binding.rytNORecordFound.visibility = View.GONE
                binding.rcyHomework.visibility = View.VISIBLE
                isloadhomeworkData(response.data)
                fullHomeworkList = response.data
                filteredHomeworkList = fullHomeworkList
            } else {
                binding.rytNORecordFound.visibility = View.VISIBLE
                binding.rcyHomework.visibility = View.GONE
                binding.lblNoRecordFound.text = response.message
            }
        }

        appViewModel?.isHomeWorkDetailsListArchive?.observe(this) { response ->
            if (response!!.status) {
                binding.rytNORecordFound.visibility = View.GONE
                binding.rcyHomework.visibility = View.VISIBLE
                isloadhomeworkData(response.data)
            } else {
                binding.rytNORecordFound.visibility = View.VISIBLE
                binding.rcyHomework.visibility = View.GONE
                binding.lblNoRecordFound.text = response.message
            }
        }

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase(Locale.ROOT)
                filterHomework(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun filterHomework(query: String) {
        filteredHomeworkList = if (query.isEmpty()) {
            fullHomeworkList
        } else {
            fullHomeworkList.mapNotNull { dateWiseData ->
                val filteredDetails = dateWiseData.homework.filter {
                    it.title.lowercase(Locale.ROOT).contains(query) ||
                            it.description.lowercase(Locale.ROOT).contains(query) ||
                            it.subject_name.lowercase(Locale.ROOT).contains(query)
                }
                if (filteredDetails.isNotEmpty()) {
                    dateWiseData.copy(homework = filteredDetails)
                } else {
                    null
                }
            }
        }


        mAdapter = HomeWorkAdapter(filteredHomeworkList, this, this, false)
        binding.rcyHomework.adapter = mAdapter

        if (filteredHomeworkList.isEmpty()) {
            binding.rytNORecordFound.visibility = View.VISIBLE
            binding.rcyHomework.visibility = View.GONE
            binding.lblNoRecordFound.text = "No matching homework found."
        } else {
            binding.rytNORecordFound.visibility = View.GONE
            binding.rcyHomework.visibility = View.VISIBLE
        }
    }


    private fun isloadhomeworkData(newData: List<GetDateWiseHomeworkData>?) {
        mAdapter = HomeWorkAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyHomework.adapter = mAdapter
    }

    fun isGetHomeWorkList() {
        mAdapter = HomeWorkAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHomework.layoutManager = LinearLayoutManager(this)
        binding.rcyHomework.isNestedScrollingEnabled = false
        binding.rcyHomework.adapter = mAdapter
        appViewModel!!.isHomeWorkDetails(
            isAccessToken!!, this
        )
    }

    fun isGetHomeWorkListArchive() {
        mAdapter = HomeWorkAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHomework.layoutManager = LinearLayoutManager(this)
        binding.rcyHomework.isNestedScrollingEnabled = false
        binding.rcyHomework.adapter = mAdapter
        appViewModel!!.isHomeworkListArchive(
            isAccessToken!!, this
        )
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lblSeeMore -> {
                isGetHomeWorkListArchive()
            }
        }
    }

    override fun onItemClick(data: HomeWorkDateData, holder: HomeWorkAdapter.DataViewHolder) {
    }

    override fun onItemTextClick(data: HomeWorkList) {
    }

    override fun onItemImageClick(data: HomeWorkList) {
    }

    override fun onItemPDFClick(data: HomeWorkList) {
    }

    override fun onItemVoiceClick(data: HomeWorkList) {
    }

    override fun onItemVideoClick(data: HomeWorkList) {
    }
}