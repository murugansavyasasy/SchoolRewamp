package com.vs.schoolmessenger.School.LSRW

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwCompletedAdapter
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ActivityTasklistBinding
import com.vs.schoolmessenger.databinding.CompletedTasklistBinding

class CompletedTaskList : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: CompletedTasklistBinding
    private lateinit var adapter: LsrwCompletedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CompletedTasklistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.Completed_Task)
        val taskList =
            intent.getParcelableArrayListExtra<LsrwTask>(Constant.COMPLETED_TASK_LIST) ?: arrayListOf()

        binding.rcycompletedtaskrcy.layoutManager = LinearLayoutManager(this)
        adapter = LsrwCompletedAdapter(
            itemList = taskList,
            context = this,
            noDataImage = binding.noDataImage,
            noDataText = binding.noDataFound
        )
        binding.rcycompletedtaskrcy.adapter = adapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()

        }
    }
}
