package com.vs.schoolmessenger.School.LSRW

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwCompletedAdapter
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask
import com.vs.schoolmessenger.Utils.Constant
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.School.LSRW.Listener.lsrwskillreportlistener
import com.vs.schoolmessenger.databinding.CompletedTasklistBinding


class CompletedTaskList : BaseActivity<CompletedTasklistBinding>(), View.OnClickListener,
    lsrwskillreportlistener {

    override fun getViewBinding(): CompletedTasklistBinding {
        return CompletedTasklistBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: LsrwCompletedAdapter
    private lateinit var LsrwTaskList: List<LsrwTask>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()


        binding = CompletedTasklistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearchbox.visibility == View.VISIBLE) {
                binding.rytSearchbox.visibility = View.GONE
                binding.txtSearchBox.text.clear()
                binding.root.hideKeyboard()

            } else {
                binding.rytSearchbox.visibility = View.VISIBLE
                binding.txtSearchBox.text.clear()
            }
        }

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.Completed_Task)
        val taskList =
            intent.getParcelableArrayListExtra<LsrwTask>(Constant.COMPLETED_TASK_LIST) ?: arrayListOf()
        LsrwTaskList=taskList


       if (taskList.isNullOrEmpty()){
           binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
           ErrorMessage(getString(R.string.no_data_found))
       }
       else{
           binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
           ShowData()
           binding.rcycompletedtaskrcy.layoutManager = LinearLayoutManager(this)
           adapter = LsrwCompletedAdapter(
               itemList = taskList,
               context = this,
               this,
               noDataImage = binding.noDataImage,
               noDataText = binding.noDataFound
           )
           binding.rcycompletedtaskrcy.adapter = adapter
       }

        binding.txtSearchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TEXTCOMING", "Text changed to: ${s.toString()}")
                filter(s.toString())
            }
        })
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    private fun filter(text: String) {
        val filteredList = if (text.isBlank()) {
            LsrwTaskList
        } else {

            val searchWords = text.trim().lowercase().split("\\s+".toRegex())

            LsrwTaskList.filter { lsrw ->
                val fieldsToSearch = listOf(
                    lsrw.title.lowercase(),
                    lsrw.description.lowercase(),
                    lsrw.created_on.lowercase(),
                    lsrw.subject.lowercase(),
                    lsrw.submitted_average.lowercase(),
                    lsrw.activity_type.lowercase(),
                )

                searchWords.all { word ->
                    fieldsToSearch.any { field ->
                        field.contains(word)
                    }
                }
            }

        }

        if (filteredList.isNotEmpty()) {
            adapter.updateList(filteredList)
            ShowData()

        } else {
            binding.rcycompletedtaskrcy.visibility = View.GONE
            ErrorMessage(resources.getString(R.string.no_data_found))
        }
    }

    fun ErrorMessage(ErrorMessage: String) {
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = ErrorMessage
    }

    fun ShowData() {
        binding.rcycompletedtaskrcy.visibility = View.VISIBLE
        binding.lytNoDataFound.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    override fun onEditAndDeleteCompleted(
        data: LsrwTask,
        anchorView: View,
        adapterPosition: Int,
        source: String
    ) {
        TODO("Not yet implemented")
    }

}