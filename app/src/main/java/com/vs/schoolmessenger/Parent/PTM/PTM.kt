package com.vs.schoolmessenger.Parent.PTM

import android.graphics.Color
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.PtmBinding

class PTM : BaseActivity<PtmBinding>(), View.OnClickListener  {

    override fun getViewBinding(): PtmBinding {
        return PtmBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.smtext.setOnClickListener(this)
        binding.mhtext.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            R.id.smtext -> {
                binding.smtext.setBackgroundResource(R.color.emerald)
                binding.mhcardview.setBackgroundResource(0)
                binding.smtext.setTextColor(Color.BLACK)
                binding.mhtext.setTextColor(Color.GRAY)
            }

            R.id.mhtext -> {
                binding.mhtext.setBackgroundResource(R.color.emerald)
                binding.smcardview.setBackgroundResource(0)
                binding.mhtext.setTextColor(Color.BLACK)
                binding.smtext.setTextColor(Color.GRAY)
            }
        }
    }

}