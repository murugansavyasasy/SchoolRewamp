package com.vs.schoolmessenger.Parent.Homework

import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.FileViewerAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding

class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>() {

    private lateinit var adapter: FileViewerAdapter

    override fun getViewBinding(): HomeworkViewImageDocumentBinding {
        return HomeworkViewImageDocumentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        val subjectName = intent.getStringExtra(Constant.subjectName) ?: ""
        binding.lblSubject.text = subjectName

        adapter = FileViewerAdapter(this,  Constant.commonFileList)
        binding.rcyFile.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyFile.adapter = adapter
    }
}
