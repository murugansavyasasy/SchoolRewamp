package com.vs.schoolmessenger.School.ExamSchedule

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.NoticeBoard.NoticeData
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ExamSubjectListBinding

class ExamSubjectList : BaseActivity<ExamSubjectListBinding>(),
    View.OnClickListener,ExamSubjectListClickListener {

    private lateinit var isExamSubjectNameData: List<ExamSubjectNameData>
    lateinit var mAdapter: ExamSubjectListAdapter


    override fun getViewBinding(): ExamSubjectListBinding {
        return ExamSubjectListBinding.inflate(layoutInflater)
    }


    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        loadData()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
    private fun loadData() {
        isExamSubjectNameData = listOf(
            ExamSubjectNameData(
                "Tamil"
            ),
            ExamSubjectNameData(
                "Science"
            ),
            ExamSubjectNameData(
                "Mathematics"
            ),
            ExamSubjectNameData(
                "Geography"
            ),
            ExamSubjectNameData(
                "Physics"
            ),
            ExamSubjectNameData(
                "Chemistry"
            ),
            ExamSubjectNameData(
                "Biology"
            ),
            ExamSubjectNameData(
                "Computer Science"
            ),
            ExamSubjectNameData(
                "Civics"
            ),
            ExamSubjectNameData(
                "Environmental Science"
            ),
            ExamSubjectNameData(
                "Physical Education"
            ),
            ExamSubjectNameData(
                "Business Studies"
            ),
            ExamSubjectNameData(
                "Accounting"
            ),
            ExamSubjectNameData(
                "Sociology"
            )
        )
    }

    override fun onItemClick(data: ExamSubjectNameData) {

    }
}