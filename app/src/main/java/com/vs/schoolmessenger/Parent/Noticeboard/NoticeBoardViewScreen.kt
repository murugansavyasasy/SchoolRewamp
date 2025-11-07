package com.vs.schoolmessenger.Parent.Noticeboard

import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.NoticeViewerPagerAdapter
import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding

class NoticeBoardViewScreen : BaseActivity<HomeworkViewImageDocumentBinding>() {

    private lateinit var adapter: NoticeViewerPagerAdapter
    private lateinit var fileList: ArrayList<FilePath>
    private var position: Int = 0

    private lateinit var filePath: String
    private lateinit var fileType: String

    override fun getViewBinding(): HomeworkViewImageDocumentBinding {
        return HomeworkViewImageDocumentBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()

    }
}



