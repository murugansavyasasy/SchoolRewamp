package com.vs.schoolmessenger.Parent.Homework

import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.FileViewerAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding

class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>() {

    private lateinit var adapter: FileViewerAdapter
    private lateinit var fileList: ArrayList<GetFilePathDetails>
    private lateinit var subjectName: String

    override fun getViewBinding(): HomeworkViewImageDocumentBinding {
        return HomeworkViewImageDocumentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        fileList = intent.getParcelableArrayListExtra<GetFilePathDetails>(Constant.position)
            ?: arrayListOf()
        subjectName = intent.getStringExtra("SubjectName") ?: ""
        binding.lblSubject.text = subjectName
        adapter = FileViewerAdapter(this, fileList)
        binding.rcyFile.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyFile.adapter = adapter
    }
}
