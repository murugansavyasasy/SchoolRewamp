package com.vs.schoolmessenger.Parent.LSRW

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.LSRW.Model.SkillData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LsrwBinding


class LSRW : BaseActivity<LsrwBinding>(), View.OnClickListener {

    private lateinit var adapter: LSRWAdapter
    private lateinit var appViewModel: App
    private var isAccessToken: String? = null
    private var allItems: List<SkillData> = emptyList()

    override fun getViewBinding(): LsrwBinding {
        return LsrwBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        binding.toolbarLayout.lblParentToolBar.text = "LSRW"
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        binding.toolbarLayout.imgBack.setOnClickListener(this)

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.lblStudentName.text = childDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            "${childDetails?.standard_name ?: ""} - ${childDetails?.section_name ?: ""}"


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()


        binding.rcyrecyclerview.layoutManager = LinearLayoutManager(this)
        adapter = LSRWAdapter(emptyList(), this)
        binding.rcyrecyclerview.adapter = adapter

        fetchLsrwSkillReportData()


        appViewModel.islsrwSkilllist?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyrecyclerview.visibility = View.VISIBLE
                binding.noDataFound.visibility = View.GONE
                allItems = response.data
                adapter.updateList(allItems)
            } else {
                binding.rcyrecyclerview.visibility = View.GONE
                binding.noDataFound.visibility = View.VISIBLE
            }
        }
    }

    private fun fetchLsrwSkillReportData() {
        binding.rcyrecyclerview.visibility = View.VISIBLE
        binding.rcyrecyclerview.isNestedScrollingEnabled = false
        appViewModel.islsrwSkilllist(isAccessToken ?: "")
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}
