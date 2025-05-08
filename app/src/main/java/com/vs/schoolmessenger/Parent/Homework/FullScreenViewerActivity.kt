package com.vs.schoolmessenger.Parent.Homework

import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ViewerPagerAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding
import java.io.File


class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>() {


    private lateinit var adapter: ViewerPagerAdapter
    private lateinit var fileList: ArrayList<GetFilePathDetails>
    private var position: Int = 0

    private lateinit var filePath: String
    private lateinit var fileType: String
    private lateinit var subjectName: String

    override fun getViewBinding(): HomeworkViewImageDocumentBinding {
        return HomeworkViewImageDocumentBinding.inflate(layoutInflater)
    }


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()


        subjectName= intent.getStringExtra(Constant.subjectName) ?: ""
        Log.d("knowing subjectName", subjectName)
        filePath = intent.getStringExtra(Constant.SelectedDocumentPath) ?: ""
        Log.d("knowing FilePath", filePath)
        fileType = intent.getStringExtra(Constant.SelectedDocumentType) ?: ""
        Log.d("knowing FileType", fileType)


                when (fileType.uppercase()) {
            Constant.PDF, Constant.DOC, Constant.DOCX, Constant.PPT, Constant.PPTX -> {
        binding.documentWebView.visibility = View.VISIBLE
                binding.documentTextView.visibility = View.GONE
        openDocumentInWebView(filePath)
            }

            Constant.TXT -> {
                binding.documentWebView.visibility = View.GONE
                binding.documentTextView.visibility = View.VISIBLE
                openTextFile(filePath)
            }

            else -> {
                binding.documentTextView.text = getString(R.string.Unsupported_file_type) +fileType
                binding.documentTextView.visibility = View.VISIBLE
                binding.documentWebView.visibility = View.GONE
            }
        }
        val dataJson = intent.getStringExtra(Constant.data)
        position = intent.getIntExtra(Constant.position, 0)

        if (!dataJson.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<GetFilePathDetails>>() {}.type
            Log.d("JsonType", type.toString())
            fileList = gson.fromJson(dataJson, type)
            Log.d("JsonType", fileList.toString())
            adapter = ViewerPagerAdapter(fileList,subjectName, this)
            binding.viewPager.adapter = adapter
            binding.viewPager.setCurrentItem(position, false)
        } else {
            findViewById<ViewPager2>(R.id.viewPager)?.visibility = View.GONE


        }





        //3
//        val dataJson = intent.getStringExtra("dataList")
//        position = intent.getIntExtra("position", 0)
//
//            val gson = Gson()
//            val type = object : TypeToken<ArrayList<GetFilePathDetails>>() {}.type
//            fileList = gson.fromJson(dataJson, type)
//
//            viewPager = findViewById(R.id.viewPager)
//            adapter = ViewerPagerAdapter(fileList, this)
//            viewPager.adapter = adapter
//            viewPager.setCurrentItem(position, false)


    }

    private fun openDocumentInWebView(path: String) {
        Log.d("knowing FilePath", path)

        val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$path"

        binding.documentWebView.settings.javaScriptEnabled = true
        binding.documentWebView.settings.setSupportZoom(true)
        binding.documentWebView.webViewClient = WebViewClient()
        binding.documentWebView.settings.allowFileAccess = true
        binding.documentWebView.settings.domStorageEnabled = true
        binding.documentWebView.settings.loadWithOverviewMode = true
        binding.documentWebView.settings.useWideViewPort = true
        binding.documentWebView.getSettings().allowFileAccess = true;
        binding.documentWebView.loadUrl(googleDocsUrl);
        Log.d("After Loading FilePath", googleDocsUrl)

        // Encode the URL
//        val encodedUrl = Uri.encode(path)
//        val googleViewerUrl = "https://docs.google.com/gview?embedded=true&url=$encodedUrl"
//
//        webView.settings.javaScriptEnabled = true
//        webView.loadUrl(googleViewerUrl)
    }

    private fun openTextFile(path: String) {
        try {
            val file = File(path)
            val content = file.readText()
            binding.documentTextView.text = content
        } catch (e: Exception) {
            binding.documentTextView.text = getString(R.string.Unable_to_open_file)
        }


    }
}



//
//
//package com.vs.schoolmessenger.Parent.Homework
//
//import android.net.Uri
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.RecyclerView
//import androidx.viewpager2.widget.ViewPager2
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ViewerPagerAdapter
//import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Repository.App
//import com.vs.schoolmessenger.databinding.HomeWorkParentBinding
//import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding
//import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentItemBinding
//
//
//class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>() {
//
//
//    //    private lateinit var viewPager: ViewPager2
//    private lateinit var adapter: ViewerPagerAdapter
//    private lateinit var fileList: ArrayList<GetFilePathDetails>
//    private var position: Int = 0
//
//    //    private lateinit var webView: WebView
////    private lateinit var textView: TextView
//    private lateinit var filePath: String
//    private lateinit var fileType: String
//    override fun getViewBinding(): HomeworkViewImageDocumentBinding {
//        return HomeworkViewImageDocumentBinding.inflate(layoutInflater)
//    }
//
//
//    override fun setupViews() {
//        super.setupViews()
//        setUpGradientParent()
//
//
//
//        filePath = intent.getStringExtra("SelectedDocumentPath") ?: ""
//        Log.d("knowing FilePath", filePath)
//        fileType = intent.getStringExtra("SelectedDocumentType") ?: ""
//        Log.d("knowing FileType", fileType)
//
//
//
//
//
//        binding.documentWebView.visibility = View.VISIBLE
//        openDocumentInWebView(filePath)
//
//        val dataJson = intent.getStringExtra("data")
//        position = intent.getIntExtra("position", 0)
//
//        if (!dataJson.isNullOrEmpty()) {
//            val gson = Gson()
//            val type = object : TypeToken<ArrayList<GetFilePathDetails>>() {}.type
//            fileList = gson.fromJson(dataJson, type)
//
////            binding.viewPager = findViewById(R.id.viewPager)
//            adapter = ViewerPagerAdapter(fileList, this)
//            binding.viewPager.adapter = adapter
//            binding.viewPager.setCurrentItem(position, false)
//        } else {
//            // Hide or disable the ViewPager since this is a non-image document
//            findViewById<ViewPager2>(R.id.viewPager)?.visibility = View.GONE
//
//
//        }
//
//
//        //3
////        val dataJson = intent.getStringExtra("dataList")
////        position = intent.getIntExtra("position", 0)
////
////            val gson = Gson()
////            val type = object : TypeToken<ArrayList<GetFilePathDetails>>() {}.type
////            fileList = gson.fromJson(dataJson, type)
////
////            viewPager = findViewById(R.id.viewPager)
////            adapter = ViewerPagerAdapter(fileList, this)
////            viewPager.adapter = adapter
////            viewPager.setCurrentItem(position, false)
//
//
//    }
//
//    private fun openDocumentInWebView(path: String) {
//        Log.d("knowing FilePath", path)
//
//        val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$path"
//
//        binding.documentWebView.settings.javaScriptEnabled = true
//        binding.documentWebView.settings.setSupportZoom(true)
//        binding.documentWebView.webViewClient = WebViewClient()
//        binding.documentWebView.settings.allowFileAccess = true
//        binding.documentWebView.settings.domStorageEnabled = true
//        binding.documentWebView.settings.loadWithOverviewMode = true
//        binding.documentWebView.settings.useWideViewPort = true
//        binding.documentWebView.getSettings().allowFileAccess = true;
//        binding.documentWebView.loadUrl(googleDocsUrl);
//        Log.d("After Loading FilePath", googleDocsUrl)
//
//        // Encode the URL
////        val encodedUrl = Uri.encode(path)
////        val googleViewerUrl = "https://docs.google.com/gview?embedded=true&url=$encodedUrl"
////
////        webView.settings.javaScriptEnabled = true
////        webView.loadUrl(googleViewerUrl)
//    }
//
//    private fun openTextFile(path: String) {
////        try {
////            val file = File(path)
////            val content = file.readText()
////            textView.text = content
////        } catch (e: Exception) {
////            textView.text = "Unable to open file"
////        }
//
//
//    }
//}


//working code
//package com.vs.schoolmessenger.Parent.Homework
//
//import android.net.Uri
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.RecyclerView
//import androidx.viewpager2.widget.ViewPager2
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ViewerPagerAdapter
//import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Repository.App
//import com.vs.schoolmessenger.databinding.HomeWorkParentBinding
//import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentBinding
//import com.vs.schoolmessenger.databinding.HomeworkViewImageDocumentItemBinding
//
//
//class FullScreenViewerActivity : BaseActivity<HomeworkViewImageDocumentBinding>() {
//
//
//    //    private lateinit var viewPager: ViewPager2
//    private lateinit var adapter: ViewerPagerAdapter
//    private lateinit var fileList: ArrayList<GetFilePathDetails>
//    private var position: Int = 0
//
//    //    private lateinit var webView: WebView
////    private lateinit var textView: TextView
//    private lateinit var filePath: String
//    private lateinit var fileType: String
//    override fun getViewBinding(): HomeworkViewImageDocumentBinding {
//        return HomeworkViewImageDocumentBinding.inflate(layoutInflater)
//    }
//
//
//    override fun setupViews() {
//        super.setupViews()
//        setUpGradientParent()
////        appViewModel = ViewModelProvider(this).get(App::class.java)
////        appViewModel?.init()
//
//
//        filePath = intent.getStringExtra("SelectedDocumentPath") ?: ""
//        Log.d("knowing FilePath", filePath)
//        fileType = intent.getStringExtra("SelectedDocumentType") ?: ""
//        Log.d("knowing FileType", fileType)
//
//
////        textView = findViewById(R.id.documentTextView)
//
////        when (fileType.uppercase()) {
////            "PDF", "DOC", "DOCX", "PPT", "PPTX" -> {
//        binding.documentWebView.visibility = View.VISIBLE
////                textView.visibility = View.GONE
//        openDocumentInWebView(filePath)
////            }
////
////            "TXT" -> {
////                webView.visibility = View.GONE
////                textView.visibility = View.VISIBLE
////                openTextFile(filePath)
////            }
//
////            else -> {
////                textView.text = "Unsupported file type: $fileType"
////                textView.visibility = View.VISIBLE
////                webView.visibility = View.GONE
////            }
////        }
//        val dataJson = intent.getStringExtra("dataList")
//        position = intent.getIntExtra("position", 0)
//
//        if (!dataJson.isNullOrEmpty()) {
//            val gson = Gson()
//            val type = object : TypeToken<ArrayList<GetFilePathDetails>>() {}.type
//            fileList = gson.fromJson(dataJson, type)
//
////            viewPager = findViewById(R.id.viewPager)
////            adapter = ViewerPagerAdapter(fileList, this)
////            viewPager.adapter = adapter
////            viewPager.setCurrentItem(position, false)
//        } else {
//            // Hide or disable the ViewPager since this is a non-image document
////            findViewById<ViewPager2>(R.id.viewPager)?.visibility = View.GONE
//
//
//        }
//
//
//        //3
////        val dataJson = intent.getStringExtra("dataList")
////        position = intent.getIntExtra("position", 0)
////
////            val gson = Gson()
////            val type = object : TypeToken<ArrayList<GetFilePathDetails>>() {}.type
////            fileList = gson.fromJson(dataJson, type)
////
////            viewPager = findViewById(R.id.viewPager)
////            adapter = ViewerPagerAdapter(fileList, this)
////            viewPager.adapter = adapter
////            viewPager.setCurrentItem(position, false)
//
//
//    }
//
//    private fun openDocumentInWebView(path: String) {
//        Log.d("knowing FilePath", path)
//
//        val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$path"
//
//        binding.documentWebView.settings.javaScriptEnabled = true
//        binding.documentWebView.settings.setSupportZoom(true)
//        binding.documentWebView.webViewClient = WebViewClient()
//        binding.documentWebView.settings.allowFileAccess = true
//        binding.documentWebView.settings.domStorageEnabled = true
//        binding.documentWebView.settings.loadWithOverviewMode = true
//        binding.documentWebView.settings.useWideViewPort = true
//        binding.documentWebView.getSettings().allowFileAccess = true;
//        binding.documentWebView.loadUrl(googleDocsUrl);
//        Log.d("After Loading FilePath", googleDocsUrl)
//
//        // Encode the URL
////        val encodedUrl = Uri.encode(path)
////        val googleViewerUrl = "https://docs.google.com/gview?embedded=true&url=$encodedUrl"
////
////        webView.settings.javaScriptEnabled = true
////        webView.loadUrl(googleViewerUrl)
//    }
//
//    private fun openTextFile(path: String) {
////        try {
////            val file = File(path)
////            val content = file.readText()
////            textView.text = content
////        } catch (e: Exception) {
////            textView.text = "Unable to open file"
////        }
//
//
//    }
//}
//
//
