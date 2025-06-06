package com.vs.schoolmessenger.Parent.Homework

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.FileViewerAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding

class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>(),
    View.OnClickListener {

    private lateinit var adapter: FileViewerAdapter

    override fun getViewBinding(): HomeworkViewImageDocumentBinding {
        return HomeworkViewImageDocumentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        val subjectName = intent.getStringExtra(Constant.subjectName) ?: ""
        binding.lblSubject.text = subjectName
        binding.imgBack.setOnClickListener(this)
        adapter = FileViewerAdapter(this,  Constant.commonFileList)
        binding.rcyFile.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcyFile.adapter = adapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}
