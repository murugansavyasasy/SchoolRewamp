package com.vs.schoolmessenger.School.LSRW

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwAdapter
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwCompletedAdapter
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask
import com.vs.schoolmessenger.databinding.ActivityTasklistBinding
import com.vs.schoolmessenger.databinding.CompletedTasklistBinding

class CompletedTaskList : AppCompatActivity() {

    private lateinit var binding: CompletedTasklistBinding
    private lateinit var adapter: LsrwCompletedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CompletedTasklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskList =
            intent.getParcelableArrayListExtra<LsrwTask>("COMPLETED_TASK_LIST") ?: arrayListOf()

        binding.rcycompletedtaskrcy.layoutManager = LinearLayoutManager(this)
        adapter = LsrwCompletedAdapter(
            itemList = taskList,
            context = this,
            noDataImage = binding.noDataImage,
            noDataText = binding.noDataFound
        )
        binding.rcycompletedtaskrcy.adapter = adapter
    }
}
