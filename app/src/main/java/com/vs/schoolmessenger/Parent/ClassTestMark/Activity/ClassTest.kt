package com.vs.schoolmessenger.Parent.ClassTestMark.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.ClassTestMark.Adapter.ClassTestAdapter
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.ClassTestData
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.ClassTestResponse
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ClasstestViewmarkBinding

class ClassTest : BaseActivity<ClasstestViewmarkBinding>() {

    override fun getViewBinding(): ClasstestViewmarkBinding {
        return ClasstestViewmarkBinding.inflate(layoutInflater)
    }

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private lateinit var classTestAdapter: ClassTestAdapter
    private var classTestList: List<ClassTestData> = emptyList()

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token
        binding.toolbarLayout.lblStudentName.visibility=View.VISIBLE
        binding.toolbarLayout.lblStudentSection.visibility=View.VISIBLE
        binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
        binding.toolbarLayout.lblStudentName.text = childDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            childDetails?.standard_name + " - " + childDetails?.section_name

        setupRecyclerView()
        isGetClassTestMark()

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

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

        appViewModel?.isClassTestResponsel?.observe(this) { response ->
            binding.progressBar.visibility = View.GONE

            if (response != null && response.status) {
                if (response.data.isNotEmpty()) {
                    binding.rvClassTests.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE

                    classTestList = response.data
                    classTestAdapter.updateList(classTestList)
                } else {
                    binding.rvClassTests.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.text = response.message
                }
            } else {
                binding.rvClassTests.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message
            }
        }


        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                classTestAdapter.filter(s.toString())
                if (classTestAdapter.itemCount == 0) {
                    binding.rvClassTests.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                    binding.txtNoData.text = getString(R.string.no_list_found)
                } else {
                    binding.rvClassTests.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        classTestAdapter = ClassTestAdapter(emptyList()) { selectedExam ->
            val intent = Intent(this, MarksReportActivity::class.java).apply {
                putExtra("EXAM_ID", selectedExam.classTestId)
                putExtra("EXAM_NAME", selectedExam.examName)
            }
            startActivity(intent)
        }

        binding.rvClassTests.apply {
            layoutManager = LinearLayoutManager(this@ClassTest)
            adapter = classTestAdapter
        }
    }

    private fun isGetClassTestMark() {
        binding.progressBar.visibility = View.VISIBLE
        appViewModel!!.isClassTestStudent(
            isAccessToken!!,
            this
        )
    }
}