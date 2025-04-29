package com.vs.schoolmessenger.Parent.Homework

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.HomeWorkParentBinding

class HomeWork : BaseActivity<HomeWorkParentBinding>(), View.OnClickListener,
    HomeWorkDateClickListener, HomeWorkItemClick {

    private var isAccessToken: String? = null
    private var isStudentName: String? = null
    private var isStudentSectionName: String? = null
    private var isStudentStandardName: String? = null
    private var appViewModel: App? = null
    override fun getViewBinding(): HomeWorkParentBinding {
        return HomeWorkParentBinding.inflate(layoutInflater)
    }
    var mAdapter: HomeWorkAdapter? = null


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.HomeWork)
        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        isStudentName = isChildDetails?.name
        isStudentStandardName = isChildDetails?.standard_name
        isStudentSectionName = isChildDetails?.section_name.toString()
        Log.d("SectionID", isStudentSectionName.toString())

        isGetHomeWorkList()
        binding.toolbarLayout.lblStudentName.text = isStudentName
        binding.toolbarLayout.lblStudentSection.text =
            isStudentStandardName + " " + isStudentSectionName


        appViewModel?.isHomeWorkDetails?.observe(this) { response ->
            if (response?.status == true) {
                appendData(response.data)
                Log.d("GetHomeWorkDetails", response.data.toString())
            }
        }
    }

    private fun appendData(newData: ArrayList<GetDateWiseHomeworkData>?) {

        Log.d("GetHomeworkDataWise", newData.toString())

        mAdapter =
            HomeWorkAdapter(newData, this, this, Constant.isShimmerViewDisable)
        binding.rcyHomework.adapter = mAdapter

    }


    fun isGetHomeWorkList() {

        mAdapter = HomeWorkAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rcyHomework.layoutManager = LinearLayoutManager(this)
        binding.rcyHomework.isNestedScrollingEnabled = false
        binding.rcyHomework.adapter = mAdapter
        appViewModel!!.isHomeWorkDetails(
            isAccessToken!!, this
        )
    }


    override fun onClick(p0: View?) {

    }

    override fun onItemClick(data: HomeWorkDateData, holder: HomeWorkAdapter.DataViewHolder) {
    }

    override fun onItemTextClick(data: HomeWorkList) {
    }

    override fun onItemImageClick(data: HomeWorkList) {
    }

    override fun onItemPDFClick(data: HomeWorkList) {
    }

    override fun onItemVoiceClick(data: HomeWorkList) {
    }

    override fun onItemVideoClick(data: HomeWorkList) {
    }
}

//package com.vs.schoolmessenger.Parent.Homework
//
//import android.view.View
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.vs.schoolmessenger.Auth.Base.BaseActivity
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.Homework.HomeWorkReportAdapter
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.databinding.HomeWorkParentBinding
//
//class HomeWork : BaseActivity<HomeWorkParentBinding>(), View.OnClickListener,HomeWorkDateClickListener,HomeWorkItemClick {
//
//    override fun getViewBinding(): HomeWorkParentBinding {
//        return HomeWorkParentBinding.inflate(layoutInflater)
//    }
//
//    private lateinit var isHomeWork: ArrayList<HomeWorkDateData>
//    var mAdapter: HomeWorkAdapter? = null
//
//
//    override fun setupViews() {
//        super.setupViews()
//        setUpGradientParent()
//        binding.toolbarLayout.imgBack.setOnClickListener(this)
//        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.HomeWork)
//        binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
//        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
//        binding.toolbarLayout.lblStudentSection.text = "XII - B"
//        loadVoiceData()
//
//    }
//
//
//    private fun loadVoiceData() {
//
//        isHomeWork = arrayListOf(
//            HomeWorkDateData(
//                "Sun, April 1, 2025"
//            ),
//            HomeWorkDateData(
//                "Sun, April 1, 2025"
//            ),
//            HomeWorkDateData(
//                "Sun, April 1, 2025"
//            ),
//            HomeWorkDateData(
//                "Sun, April 1, 2025"
//            ),
//            HomeWorkDateData(
//                "Sun, April 1, 2025"
//            ),
//            HomeWorkDateData(
//                "Sun, April 1, 2025"
//            ),
//            HomeWorkDateData(
//                "Sun, April 1, 2025"
//            ),
//            HomeWorkDateData(
//                "Sun, April 1, 2025"
//            ),
//            HomeWorkDateData(
//                "Sun, April 1, 2025"
//            )
//        )
//
//        mAdapter = HomeWorkAdapter(null, this, this, Constant.isShimmerViewShow)
//        binding.rcyHomework.layoutManager = LinearLayoutManager(this)
//        binding.rcyHomework.isNestedScrollingEnabled = false;
//        binding.rcyHomework.adapter = mAdapter
//
//        Constant.executeAfterDelay {
//            // Once data is loaded, stop shimmer and pass the actual data
//            mAdapter =
//                HomeWorkAdapter(isHomeWork, this, this, Constant.isShimmerViewDisable)
//            // Set GridLayoutManager (2 columns in this case)
//            binding.rcyHomework.adapter = mAdapter
//        }
//    }
//
//
//    override fun onClick(p0: View?) {
//
//    }
//
//    override fun onItemClick(data: HomeWorkDateData, holder: HomeWorkAdapter.DataViewHolder) {
//    }
//
//    override fun onItemTextClick(data: HomeWorkList) {
//    }
//
//    override fun onItemImageClick(data: HomeWorkList) {
//    }
//
//    override fun onItemPDFClick(data: HomeWorkList) {
//    }
//
//    override fun onItemVoiceClick(data: HomeWorkList) {
//    }
//
//    override fun onItemVideoClick(data: HomeWorkList) {
//    }
//}