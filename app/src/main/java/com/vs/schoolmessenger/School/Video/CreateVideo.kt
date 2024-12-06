package com.vs.schoolmessenger.School.Video
import android.view.View
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.CreateVideoBinding

class CreateVideo  : BaseActivity<CreateVideoBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): CreateVideoBinding {
        return CreateVideoBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)

//        // Example waveform data (you can generate or fetch actual audio waveform data)
//        val waveformData = IntArray(100) { (5..20).random() }
//        binding.waveformSeekBar.setSampleFrom(waveformData)
//        binding.waveformSeekBar.progress = 50F // Sets progress to 50%

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

}