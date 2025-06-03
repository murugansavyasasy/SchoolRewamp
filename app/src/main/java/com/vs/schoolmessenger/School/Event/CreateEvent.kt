package com.vs.schoolmessenger.School.Event
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.CreateEventBinding

class CreateEvent : BaseActivity<CreateEventBinding>(),
    View.OnClickListener, EventClickListener {

    override fun getViewBinding(): CreateEventBinding {
        return CreateEventBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: EventHistoryAdapter
    private var isStaffDetails: StaffDetails? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.toolbarLayout.imgBack.setOnClickListener(this)




        Constant.editTextCounter(this,binding.txtDesc,500,binding.lbTextCount)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }






    override fun onItemClick(data: EventHistoryData) {

    }
}