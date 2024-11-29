package com.vs.schoolmessenger.School.AbsenteesMarking

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AbsenteesStudentMarkingBinding

class AbsenteesStudentMark : BaseActivity<AbsenteesStudentMarkingBinding>(), AbsenteesClickListener,
    View.OnClickListener {

    lateinit var mAdapter: AbsenteesMarkAdapter
    private lateinit var studentsList: List<StudentData>

    override fun getViewBinding(): AbsenteesStudentMarkingBinding {
        return AbsenteesStudentMarkingBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()


        studentsList = listOf(
            StudentData(
                "Murugan", "76979871",
                "Present"
            ),
            StudentData(
                "Sathish", "22439234",
                "Present"
            ),
            StudentData(
                "Saran Raj", "259411563",
                "Present"

            ),
            StudentData(
                "Chanthru", "216098214",
                "Present"

            ),
            StudentData(
                "Ramesh", "90509568",
                "Present"
            ),

            StudentData(
                "Lakshmanan Narayanan", "90509568",
                "Present"
            ),
            StudentData(
                "Gunal", "90509568",
                "Present"
            ),
            StudentData(
                "Lakshmanan", "90509568",
                "Present"
            ),
            StudentData(
                "Narayanan", "90509568",
                "Present"
            )
        )

        mAdapter = AbsenteesMarkAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.recycleStudents.layoutManager = LinearLayoutManager(this)
        binding.recycleStudents.adapter = mAdapter

        // Simulate loading data with a delay (e.g., fetch from server or database)
        binding.recycleStudents.postDelayed({
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                AbsenteesMarkAdapter(studentsList, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.recycleStudents.adapter = mAdapter
        }, 500) // Simulate 2 seconds loading time


    }

    override fun onClick(v: View?) {

    }

    override fun onItemClick(data: StudentData) {
        TODO("Not yet implemented")
    }
}