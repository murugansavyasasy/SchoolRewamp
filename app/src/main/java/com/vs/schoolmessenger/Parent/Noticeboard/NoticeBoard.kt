package com.vs.schoolmessenger.Parent.Noticeboard

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.WebView
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.NoticeBoardAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.NoticeBoardBinding

class NoticeBoard : BaseActivity<NoticeBoardBinding>(), View.OnClickListener,
    NoticeBoardClickListener {

    override fun getViewBinding(): NoticeBoardBinding {
        return NoticeBoardBinding.inflate(layoutInflater)
    }

    private lateinit var isNoticeBoardData: List<Notice>
    lateinit var mAdapter: NoticeBoardAdapter
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    override fun setupViews() {
        super.setupViews()

        setUpGradientParent()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
//        loadData()
    }

    override fun onClick(p0: View?) {

    }


    private fun loadData() {

//        isNoticeBoardData = listOf(
//
//            Notice(
//                "Annual Day celebrartions",
//                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
//                "15 Nov 2024",
//                "isPDF", "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
//                "",
//                "",
//                "",
//                ""
//            ),
//            Notice(
//                "Annual Day celebrartions",
//                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
//                "15 Nov 2024",
//                "isImage", "",
//                "",
//                "",
//                "",
//                ""
//            ),
//
//            Notice(
//                "Annual Day celebrartions",
//                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
//                "15 Nov 2024",
//                "isImage",
//                "",
//                "",
//                "",
//                "",
//                ""
//            )
//        )
//
//        mAdapter = NoticeBoardAdapter(null, this, this, Constant.isShimmerViewShow)
//        binding.rcyNoticeBoard.layoutManager = LinearLayoutManager(this)
//        binding.rcyNoticeBoard.adapter = mAdapter
//
//        Constant.executeAfterDelay {
//            // Once data is loaded, stop shimmer and pass the actual data
//            mAdapter =
//                NoticeBoardAdapter(isNoticeBoardData, this, this, Constant.isShimmerViewDisable)
//            // Set GridLayoutManager (2 columns in this case)
//            binding.rcyNoticeBoard.adapter = mAdapter
//        }

    }

//    override fun onItemImageClick(data: Notice) {
//    }
//
//    override fun onItemPDFClick(data: Notice) {
//        val intent = Intent(this, WebView::class.java)
//        intent.putExtra("isTitle", data.title)
////        intent.putExtra("isWebLink", data.file_path)
//        startActivity(intent)
//    }

}