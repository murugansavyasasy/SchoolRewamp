package com.vs.schoolmessenger.School.LSRW

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwAdapter
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask
import com.vs.schoolmessenger.databinding.ActivityTasklistBinding

class ActiveTaskList : AppCompatActivity() {

    private lateinit var binding: ActivityTasklistBinding
    private lateinit var adapter: LsrwAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}
