package com.vs.schoolmessenger.School.LSRW

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwAdapter
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ActivityTasklistBinding

class ActiveTaskList : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityTasklistBinding
    private lateinit var adapter: LsrwAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTasklistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarLayout.lblParentToolBar.text = "Active Task"
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        val taskList = intent.getParcelableArrayListExtra<LsrwTask>("TASK_LIST") ?: arrayListOf()

        binding.rcyactivetaskrcy.layoutManager = LinearLayoutManager(this)
        adapter = LsrwAdapter(
            itemList = taskList,
            context = this,
            noDataImage = binding.noDataImage,
            noDataText = binding.noDataFound
        )
        binding.rcyactivetaskrcy.adapter = adapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()

        }
    }
}
