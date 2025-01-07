package com.vs.schoolmessenger.School.DailyCollection

import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.DailyCollectionBinding

class DailyCollection : BaseActivity<DailyCollectionBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): DailyCollectionBinding {
        return DailyCollectionBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()


    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }
}