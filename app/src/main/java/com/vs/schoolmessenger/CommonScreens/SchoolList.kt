package com.vs.schoolmessenger.CommonScreens

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.SchoolListActivityBinding

class SchoolList : BaseActivity<SchoolListActivityBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): SchoolListActivityBinding {
        return SchoolListActivityBinding.inflate(layoutInflater)
    }

    private lateinit var mAdapter: SchoolListAdapter
    private lateinit var schoolsList: List<SchoolsData>

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()


        schoolsList = listOf(
            SchoolsData(
                "Savyasasy software solutions pvt ltd.,", "Chennai, Nungampakkam - 60043",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391795845263.png","22323","1313"
            ),
            SchoolsData(
                "Savyasasy software solutions pvt ltd.,", "Chennai, Nungampakkam - 60043",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391795387749.png","22323","1313"
            ),
            SchoolsData(
                "Savyasasy software solutions pvt ltd.,", "Chennai, Nungampakkam - 60043",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391797604035.png","22323","1313"
            ),
            SchoolsData(
                "Savyasasy software solutions pvt ltd.,", "Chennai, Nungampakkam - 60043",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391799793266.png","22323","1313"
            ),
            SchoolsData(
                "Savyasasy software solutions pvt ltd.,", "Chennai, Nungampakkam - 60043",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391801142838.png","22323","1313"
            ),
            SchoolsData(
                "Savyasasy software solutions pvt ltd.,", "Chennai, Nungampakkam - 60043",
                "https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391795845263.png","22323","1313"
            )
        )

        mAdapter = SchoolListAdapter(null, this, Constant.isShimmerViewShow)
        binding.recycleSchools.layoutManager = LinearLayoutManager(this)
        binding.recycleSchools.adapter = mAdapter
        Constant.executeAfterDelay {
            mAdapter = SchoolListAdapter(schoolsList, this, Constant.isShimmerViewDisable)
            binding.recycleSchools.adapter = mAdapter
        }
    }
    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }

}