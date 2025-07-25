package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ChildHomeworkActivityBinding

class ChildHomeWork : BaseActivity<ChildHomeworkActivityBinding>(), View.OnClickListener {

    override fun getViewBinding(): ChildHomeworkActivityBinding {
        return ChildHomeworkActivityBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlue()
        binding.imgBack.setOnClickListener(this)
        val data = intent.getParcelableExtra<GetHomeworkDetails>("isHomeWorkData")

        binding.lbltitle.text = data!!.title
        binding.lblDescription.text = data.description
        binding.lblSubjectName.text = data.subject_name
        for (i in data.file_path.indices) {
            Log.d("isComingFilePath", data.file_path.get(i).url)
        }
        val adapter = HomeWorkChildAdapter(this, data.file_path, data.subject_name)
        binding.rcChildHW.layoutManager =
            GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        binding.rcChildHW.adapter = adapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}