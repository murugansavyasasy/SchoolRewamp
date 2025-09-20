package com.vs.schoolmessenger.Parent.LSRW

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
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
        isToolBarPrimaryTheme1(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.lsrw)
        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
        binding.toolbarLayout.imgBack.setOnClickListener(this)
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

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        appViewModel.islsrwSkilllist?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcyrecyclerview.visibility = View.VISIBLE
                binding.rlNoDataContainer.visibility = View.GONE
                allItems = response.data
                adapter.updateList(allItems)
            } else {
                binding.rcyrecyclerview.visibility = View.GONE
                binding.toolbarLayout.rytSearch.visibility = View.GONE
                binding.rlNoDataContainer.visibility = View.VISIBLE
                binding.noDataFound.text = getString(R.string.no_data_found)
            }
        }

    }

    private fun fetchLsrwSkillReportData() {
        binding.rcyrecyclerview.visibility = View.VISIBLE
        binding.rcyrecyclerview.isNestedScrollingEnabled = false
        appViewModel.islsrwSkilllist(isAccessToken ?: "")
    }

    private fun filterList(query: String) {
        if (query.isEmpty()) {
            adapter.updateList(allItems)
            binding.rlNoDataContainer.visibility = if (allItems.isEmpty()) View.VISIBLE else View.GONE
            return
        }

        val filteredList = allItems.filter { item ->
            item.subject?.contains(
                query,
                ignoreCase = true
            ) == true || item.activity_type?.contains(
                query,
                ignoreCase = true
            ) == true || item.title?.contains(
                query,
                ignoreCase = true
            ) == true || item.description?.contains(
                query,
                ignoreCase = true
            ) == true || item.sent_by?.contains(query, ignoreCase = true) == true
        }

        if (filteredList.isNotEmpty()) {
            adapter.updateList(filteredList)
            binding.rcyrecyclerview.visibility = View.VISIBLE
            binding.rlRecyclerContainer.visibility = View.VISIBLE
            binding.rlNoDataContainer.visibility = View.GONE
        } else {
            adapter.updateList(emptyList())
            binding.rcyrecyclerview.visibility = View.GONE
            binding.rlRecyclerContainer.visibility = View.GONE
            binding.rlNoDataContainer.visibility = View.VISIBLE
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> onBackPressed()


        }


    }
}