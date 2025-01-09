package com.vs.schoolmessenger.Parent.Communication

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.CommunicationBinding

class Communication : BaseActivity<CommunicationBinding>(), View.OnClickListener,
    VoiceClickListener, TextClickListener {

    override fun getViewBinding(): CommunicationBinding {
        return CommunicationBinding.inflate(layoutInflater)
    }

    private lateinit var isVoiceData: ArrayList<VoiceData>
    var mAdapter: VoiceAdapter? = null
    private lateinit var isTextData: ArrayList<TextData>
    var mTextAdapter: TextAdapter? = null
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaVoiceMessage.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Communication)
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"
        loadVoiceData()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rlaTextMessage -> {

                loadTextData()
                mAdapter!!.updateData()
                mAdapter!!.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage,
                    binding.imgTextMessage,
                    binding.lblTextMessage
                )
            }

            R.id.rlaVoiceMessage -> {
                loadVoiceData()
                mTextAdapter!!.updateData()
                mTextAdapter!!.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(
                    binding.rlaVoiceMessage,
                    binding.imgVoiceMessage,
                    binding.lblVoiceMessage
                )
            }
        }
    }

    private fun isChangeBackRoundCommunicationType(
        isTypeCommunication: RelativeLayout,
        imgTypeCommunication: ImageView,
        lblTypeCommunication: TextView
    ) {
        // Reset backgrounds and colors
        binding.rlaVoiceMessage.background = null
        binding.rlaTextMessage.background = null
        isTypeCommunication.background =
            ContextCompat.getDrawable(this, R.drawable.bg_gradient_redious_parent)

        binding.lblVoiceMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.lblTextMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        lblTypeCommunication.setTextColor(ContextCompat.getColor(this, R.color.white))

        binding.imgVoiceMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.mic_icon_black
            )
        )


        binding.imgTextMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.text_icon_black
            )
        )

        // Update icons based on selection
        when (imgTypeCommunication) {
            binding.imgVoiceMessage -> {
                binding.imgVoiceMessage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.mic_icon
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


    private fun loadVoiceData() {

        isVoiceData = arrayListOf(
            VoiceData(
                "Annual Day celebrations",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            ),
            VoiceData(
                "Parent Meeting",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
            ),
            VoiceData(
                "Normal Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
            ),
            VoiceData(
                "Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_700KB.mp3"
            ),
            VoiceData(
                "Monday",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_1MG.mp3"
            ),
            VoiceData(
                "Nothing",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www2.cs.uic.edu/~i101/SoundFiles/BabyElephantWalk60.wav"
            ),
            VoiceData(
                "Value Education",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
            ),
            VoiceData(
                "Environmental Science",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://filesamples.com/samples/audio/mp3/sample4.mp3"
            ),
            VoiceData(
                "Okay okay",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "https://ia800304.us.archive.org/8/items/testmp3testfile/mpthreetest.mp3"
            )
        )

        mAdapter = VoiceAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rlaCommunicationData.layoutManager = LinearLayoutManager(this)
        binding.rlaCommunicationData.isNestedScrollingEnabled = false;
        binding.rlaCommunicationData.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                VoiceAdapter(isVoiceData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rlaCommunicationData.adapter = mAdapter
        }
    }


    private fun loadTextData() {

        isTextData = arrayListOf(
            TextData(
                "Annual Day celebrations",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextData(
                "Parent Meeting",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextData(
                "Normal Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextData(
                "Day",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextData(
                "Monday",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextData(
                "Nothing",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextData(
                "Value Education",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextData(
                "Environmental Science",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            ),
            TextData(
                "Okay okay",
                "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "Apr 1, 2021"
            )
        )

        mTextAdapter = TextAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.rlaCommunicationData.layoutManager = LinearLayoutManager(this)
        binding.rlaCommunicationData.isNestedScrollingEnabled = false;
        binding.rlaCommunicationData.adapter = mTextAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mTextAdapter =
                TextAdapter(isTextData, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rlaCommunicationData.adapter = mTextAdapter
        }
    }

    override fun onItemClick(data: VoiceData, holder: VoiceAdapter.DataViewHolder) {

    }

    override fun onItemClick(data: TextData, holder: TextAdapter.DataViewHolder) {

    }
}
