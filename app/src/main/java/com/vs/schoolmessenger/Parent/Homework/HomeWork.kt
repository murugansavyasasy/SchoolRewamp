package com.vs.schoolmessenger.Parent.Homework

import android.text.Editable
import android.text.TextWatcher
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

class HomeWork : BaseActivity<HomeWorkParentBinding>(), View.OnClickListener,
    HomeWorkDateClickListener {
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var mAdapter: HomeWorkAdapter? = null
    private var fullHomeworkList = mutableListOf<GetDateWiseHomeworkData>()
    private var filteredHomeworkList = listOf<GetDateWiseHomeworkData>()
    private var hasFetchedMore = false

    override fun getViewBinding(): HomeWorkParentBinding {
        return HomeWorkParentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.HomeWork)
        binding.toolbarLayout.lblStudentName.text = childDetails?.name
        binding.toolbarLayout.lblStudentSection.text =
            "${childDetails?.standard_name} ${childDetails?.section_name}"

        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.lblSeeMore.setOnClickListener(this)

        binding.rcyHomework.layoutManager = LinearLayoutManager(this)
        binding.rcyHomework.isNestedScrollingEnabled = false

        observeHomeworkResponse()
        showInitialShimmer()
        fetchInitialData()

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterHomework(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeHomeworkResponse() {
        appViewModel?.isHomeWorkDetailsList?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rytNORecordFound.visibility = View.GONE
                binding.rcyHomework.visibility = View.VISIBLE
                appendData(response.data)
            } else {
                showEmptyState(response?.message ?: "No data found")
            }
        }

        appViewModel?.isHomeWorkDetailsListArchive?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                appendData(response.data)
            } else {
                showEmptyState(response?.message ?: "No more data")
            }
        }
    }

    private fun showInitialShimmer() {
        mAdapter = HomeWorkAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHomework.adapter = mAdapter
    }

    private fun showEmptyState(message: String) {
        binding.rcyHomework.visibility = View.GONE
        binding.rytNORecordFound.visibility = View.VISIBLE
        binding.lblNoRecordFound.text = message
    }

    private fun fetchInitialData() {
        appViewModel?.isHomeWorkDetails(isAccessToken.orEmpty(), this)
    }

    private fun fetchMoreData() {
        appViewModel?.isHomeworkListArchive(isAccessToken.orEmpty(), this)
    }

    private fun appendData(newData: List<GetDateWiseHomeworkData>) {
        val newItems = newData.filter { newItem ->
            fullHomeworkList.none { existingItem -> existingItem.date == newItem.date }
        }

        fullHomeworkList.addAll(newItems)
        filteredHomeworkList = fullHomeworkList

        mAdapter = HomeWorkAdapter(filteredHomeworkList, this, this, Constant.isShimmerViewDisable)
        binding.rcyHomework.adapter = mAdapter
    }

    private fun filterHomework(query: String) {
        filteredHomeworkList = if (query.isEmpty()) {
            fullHomeworkList
        } else {
            fullHomeworkList.mapNotNull { dateWise ->
                val filtered = dateWise.homework.filter {
                    it.title.contains(query, true) || it.description.contains(
                        query,
                        true
                    ) || it.subject_name.contains(query, true)
                }
                if (filtered.isNotEmpty()) {
                    dateWise.copy(homework = filtered)
                } else null
            }
        }

        if (filteredHomeworkList.isEmpty()) {
            showEmptyState("No matching homework found.")
        } else {
            binding.rytNORecordFound.visibility = View.GONE
            binding.rcyHomework.visibility = View.VISIBLE
            mAdapter =
                HomeWorkAdapter(filteredHomeworkList, this, this, Constant.isShimmerViewDisable)
            binding.rcyHomework.adapter = mAdapter
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.lblSeeMore -> {
                if (!hasFetchedMore) {
                    hasFetchedMore = true
                    binding.lblSeeMore.visibility = View.GONE
                    fetchMoreData()
                }
            }
        }
    }

    override fun onItemClick(data: HomeWorkDateData, holder: HomeWorkAdapter.DataViewHolder) {}
}
