package com.vs.schoolmessenger.School.Communication

import android.graphics.Paint
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.CommunicationSchoolBinding

class CommunicationSchool : BaseActivity<CommunicationSchoolBinding>(), View.OnClickListener {

    override fun getViewBinding(): CommunicationSchoolBinding {
        return CommunicationSchoolBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        binding.lblHistoryList.paintFlags =
            binding.lblHistoryList.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.lblBackToVoiceMessage.paintFlags =
            binding.lblBackToVoiceMessage.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.rlaVoiceMessage.setOnClickListener(this)
        binding.rlaScheduleCall.setOnClickListener(this)
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaFromTime.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.rlaVoiceMessage -> {
                isChangeBackRoundCommunicationType(
                    binding.rlaVoiceMessage,
                    binding.imgVoiceMessage,
                    binding.lblVoiceMessage
                )
            }

            R.id.rlaScheduleCall -> {
                isChangeBackRoundCommunicationType(
                    binding.rlaScheduleCall,
                    binding.imgScheduleCall,
                    binding.lblScheduleCall
                )
            }

            R.id.rlaTextMessage -> {
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage,
                    binding.imgTextMessage,
                    binding.lblTextMessage
                )
            }

            R.id.rlaFromTime -> {
                showSpinnerTimePicker(this) { hour, minute, isAm ->
                    val amPm = if (isAm) "AM" else "PM"
                    Toast.makeText(this, "Selected Time: $hour:$minute $amPm", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun isChangeBackRoundCommunicationType(
        isTypeCommunication: RelativeLayout,
        imgTypeCommunication: ImageView,
        lblTypeCommunication: TextView
    ) {
        binding.rlaVoiceMessage.background = null
        binding.rlaScheduleCall.background = null
        binding.rlaTextMessage.background = null
        isTypeCommunication.background =
            ContextCompat.getDrawable(this, R.drawable.rect_sky_blue_shadow)

        binding.lblVoiceMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.lblScheduleCall.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.lblTextMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        lblTypeCommunication.setTextColor(ContextCompat.getColor(this, R.color.white))

        binding.imgVoiceMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.mic_icon_black
            )
        )
        binding.imgScheduleCall.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.call_schedule_icon_black
            )
        )
        binding.imgTextMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.text_icon_black
            )
        )

        when (imgTypeCommunication) {
            binding.imgVoiceMessage -> {
                binding.imgVoiceMessage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.mic_icon
                    )
                )
            }

            binding.imgScheduleCall -> {
                binding.imgScheduleCall.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.call_schedule_icon
                    )
                )
            }

            binding.imgTextMessage -> {
                binding.imgTextMessage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.text_icon
                    )
                )
            }
        }
    }
}