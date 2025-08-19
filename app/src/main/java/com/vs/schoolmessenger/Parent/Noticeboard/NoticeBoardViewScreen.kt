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

//        try {
//            filePath = intent.getStringExtra(Constant.SelectedDocumentPath) ?: ""
//            fileType = intent.getStringExtra(Constant.SelectedDocumentType) ?: ""
//            position = intent.getIntExtra(Constant.position, 0)
//
//            if (filePath.isEmpty() || fileType.isEmpty()) {
//                binding.documentTextView.text = getString(R.string.Unable_to_open_file)
//                binding.documentWebView.visibility = View.GONE
//                binding.documentTextView.visibility = View.VISIBLE
//                return
//            }
//
//            when (fileType.uppercase()) {
//                Constant.PDF, Constant.DOC, Constant.DOCX, Constant.PPT, Constant.PPTX -> {
//                    binding.documentWebView.visibility = View.VISIBLE
//                    binding.documentTextView.visibility = View.GONE
//                    openDocumentInWebView(filePath)
//                }
//
//                Constant.TXT -> {
//                    binding.documentWebView.visibility = View.GONE
//                    binding.documentTextView.visibility = View.VISIBLE
//                    openTextFile(filePath)
//                }
//
//                else -> {
//                    binding.documentTextView.text = getString(R.string.Unsupported_file_type) + fileType
//                    binding.documentTextView.visibility = View.VISIBLE
//                    binding.documentWebView.visibility = View.GONE
//                }
//            }
//
//            val dataJson = intent.getStringExtra(Constant.data)
//            if (!dataJson.isNullOrEmpty()) {
//                val gson = Gson()
//                val type = object : TypeToken<ArrayList<FilePath>>() {}.type
//                fileList = gson.fromJson(dataJson, type)
//                adapter = NoticeViewerPagerAdapter(fileList, this)
//                binding.viewPager.adapter = adapter
//                binding.viewPager.setCurrentItem(position, false)
//            } else {
//                binding.viewPager.visibility = View.GONE
//            }
//
//        } catch (e: Exception) {
//            binding.documentTextView.text = getString(R.string.Unable_to_open_file)
//            binding.documentTextView.visibility = View.VISIBLE
//            binding.documentWebView.visibility = View.GONE
//        }
//    }
//
//    private fun openDocumentInWebView(path: String) {
//        val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$path"
//        binding.documentWebView.settings.javaScriptEnabled = true
//        binding.documentWebView.settings.setSupportZoom(true)
//        binding.documentWebView.settings.allowFileAccess = true
//        binding.documentWebView.settings.domStorageEnabled = true
//        binding.documentWebView.settings.loadWithOverviewMode = true
//        binding.documentWebView.settings.useWideViewPort = true
//        binding.documentWebView.webViewClient = WebViewClient()
//        binding.documentWebView.loadUrl(googleDocsUrl)
//    }
//
//    private fun openTextFile(path: String) {
//        try {
//            val file = File(path)
//            val content = file.readText()
//            binding.documentTextView.text = content
//        } catch (e: Exception) {
//            binding.documentTextView.text = getString(R.string.Unable_to_open_file)
//        }
    }
}



