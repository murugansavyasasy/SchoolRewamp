package com.vs.schoolmessenger.Parent.LSRW

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LsrwBinding


class LSRW : BaseActivity<LsrwBinding>(), View.OnClickListener {

    private lateinit var adapter: LSRWAdapter
    private val lsrwList = mutableListOf<LSRWData>()

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    override fun getViewBinding(): LsrwBinding {
        return LsrwBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()


        // Toolbar setup
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text = isChildDetails?.standard_name+ " - " +isChildDetails?.section_name

        setupRecyclerView()
        loadHardcodedData()
    }


    private fun setupRecyclerView() {
        adapter = LSRWAdapter(lsrwList, object : LSRWClickListener {
            override fun onItemClick(
                data: LSRWData,
                holder: LSRWAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.rcLsrw.layoutManager =
            LinearLayoutManager(this)
        binding.rcLsrw.adapter = adapter
    }


    private fun loadHardcodedData() {
        lsrwList.apply {
            add(
                LSRWData(
                    "Listening Comprehension - The Environment",
                    "Listen to an audio about saving the environment and answer questions"
                )
            )
            add(
                LSRWData(
                    "Listening Comprehension - The Environment",
                    "Listen to an audio about saving the environment and answer questions"
                )
            )
            add(
                LSRWData(
                    "Listening Comprehension - The Environment",
                    "Listen to an audio about saving the environment and answer questions"
                )
            )
            add(
                LSRWData(
                    "Listening Comprehension - The Environment",
                    "Listen to an audio about saving the environment and answer questions"
                )
            )
            add(
                LSRWData(
                    "Listening Comprehension - The Environment",
                    "Listen to an audio about saving the environment and answer questions"
                )
            )
        }
        adapter.notifyDataSetChanged()
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}
